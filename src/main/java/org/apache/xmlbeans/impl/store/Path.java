/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.DefaultClassLoaderResourceLoader;
import org.apache.xmlbeans.impl.common.XPath;
import org.apache.xmlbeans.impl.common.XPath.XPathCompileException;
import org.apache.xmlbeans.impl.common.XPathExecutionContext;
import org.apache.xmlbeans.impl.xpath.saxon.XBeansXPath;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


// TODO - This class handled query *and* path ... rename it?

public abstract class Path {
    public static final String PATH_DELEGATE_INTERFACE = "PATH_DELEGATE_INTERFACE";
    public static String _useDelegateForXpath = "use delegate for xpath";
    public static String _useXbeanForXpath = "use xbean for xpath";

    private static final int USE_XBEAN = 0x01;
    private static final int USE_DELEGATE = 0x04;

    private static final Map<String, WeakReference<Path>> _xbeanPathCache = new WeakHashMap<>();


    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected final String _pathKey;

    Path(String key) {
        _pathKey = key;
    }


    interface PathEngine {
        void release();

        boolean next(Cur c);
    }

    abstract PathEngine execute(Cur c, XmlOptions options);

    //
    //
    //

    static String getCurrentNodeVar(XmlOptions options) {
        String currentNodeVar = "this";

        options = XmlOptions.maskNull(options);

        String cnv = options.getXqueryCurrentNodeVar();
        if (cnv != null) {
            currentNodeVar = cnv;

            if (currentNodeVar.startsWith("$")) {
                throw new IllegalArgumentException("Omit the '$' prefix for the current node variable");
            }
        }

        return currentNodeVar;
    }

    public static Path getCompiledPath(String pathExpr, XmlOptions options) {
        options = XmlOptions.maskNull(options);

        int force =
            options.isXPathUseDelegate() ? USE_DELEGATE
                : options.isXPathUseXmlBeans() ? USE_XBEAN
                : USE_XBEAN | USE_DELEGATE;
        String delIntfName = XBeansXPath.class.getName();

        return getCompiledPath(pathExpr, force, getCurrentNodeVar(options), delIntfName);
    }

    static Path getCompiledPath(String pathExpr, int force,
                                String currentVar, String delIntfName) {
        Path path = null;
        WeakReference<Path> pathWeakRef = null;
        Map<String, String> namespaces = (force & USE_DELEGATE) != 0 ? new HashMap<>() : null;
        lock.readLock().lock();
        try {
            if ((force & USE_XBEAN) != 0) {
                pathWeakRef = _xbeanPathCache.get(pathExpr);
            }

            if (pathWeakRef != null) {
                path = pathWeakRef.get();
            }
            if (path != null) {
                return path;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if ((force & USE_XBEAN) != 0) {
                pathWeakRef = _xbeanPathCache.get(pathExpr);
                if (pathWeakRef != null) {
                    path = pathWeakRef.get();
                }
                if (path == null) {
                    path = getCompiledPathXbean(pathExpr, currentVar, namespaces);
                }
            }
            if (path == null && (force & USE_DELEGATE) != 0) {
                path = getCompiledPathDelegate(pathExpr, currentVar, namespaces, delIntfName);
            }
            if (path == null) {
                StringBuilder errMessage = new StringBuilder();
                if ((force & USE_XBEAN) != 0) {
                    errMessage.append(" Trying XBeans path engine...");
                }
                if ((force & USE_DELEGATE) != 0) {
                    errMessage.append(" Trying delegated path engine...");
                }

                throw new RuntimeException(errMessage.toString() + " FAILED on " + pathExpr);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return path;
    }

    static private Path getCompiledPathXbean(String pathExpr,
                                             String currentVar, Map<String, String> namespaces) {
        Path path = XbeanPath.create(pathExpr, currentVar, namespaces);
        if (path != null) {
            _xbeanPathCache.put(path._pathKey, new WeakReference<>(path));
        }

        return path;
    }

    static private Path getCompiledPathDelegate(String pathExpr, String currentVar, Map<String, String> namespaces, String delIntfName) {
        if (namespaces == null) {
            namespaces = new HashMap<>();
        }

        try {
            XPath.compileXPath(pathExpr, currentVar, namespaces);
        } catch (XPath.XPathCompileException e) {
            //do nothing, this function is only called to populate the namespaces map
        }


        int offset = Integer.parseInt(namespaces.getOrDefault(XPath._NS_BOUNDARY, "0"));
        namespaces.remove(XPath._NS_BOUNDARY);

        return DelegatePathImpl.create(delIntfName,
            pathExpr.substring(offset),
            currentVar,
            namespaces);
    }


    public static String compilePath(String pathExpr, XmlOptions options) {
        return getCompiledPath(pathExpr, options)._pathKey;
    }

    //
    // Xbean store specific implementation of compiled path
    //

    private static final class XbeanPath extends Path {
        static Path create(String pathExpr, String currentVar, Map<String, String> namespaces) {
            try {
                return new XbeanPath(pathExpr, currentVar,
                    XPath.compileXPath(pathExpr, currentVar, namespaces));
            } catch (XPathCompileException e) {
                return null;
            }
        }

        private XbeanPath(String pathExpr, String currentVar, XPath xpath) {
            super(pathExpr);

            _currentVar = currentVar;
            _compiledPath = xpath;
        }

        PathEngine execute(Cur c, XmlOptions options) {
            options = XmlOptions.maskNull(options);
            String delIntfName = XBeansXPath.class.getName();

            // The builtin XPath engine works only on containers.  Delegate to
            // xqrl otherwise.  Also, if the path had a //. at the end, the
            // simple xpath engine can't do the generate case, it only handles
            // attrs and elements.

            if (!c.isContainer() || _compiledPath.sawDeepDot()) {
                int force = USE_DELEGATE;
                return getCompiledPath(_pathKey, force, _currentVar, delIntfName).execute(c, options);
            }
            return new XbeanPathEngine(_compiledPath, c);
        }

        private final String _currentVar;
        private final XPath _compiledPath;
        public Map<String, String> namespaces;
    }

    private static final class XbeanPathEngine
        extends XPathExecutionContext
        implements PathEngine {
        XbeanPathEngine(XPath xpath, Cur c) {
            assert c.isContainer();

            _version = c._locale.version();
            _cur = c.weakCur(this);

            _cur.push();

            init(xpath);

            int ret = start();

            if ((ret & HIT) != 0) {
                c.addToSelection();
            }

            doAttrs(ret, c);

            if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement(_cur)) {
                release();
            }
        }

        private void advance(Cur c) {
            assert _cur != null;

            if (_cur.isFinish()) {
                if (_cur.isAtEndOfLastPush()) {
                    release();
                } else {
                    end();
                    _cur.next();
                }
            } else if (_cur.isElem()) {
                int ret = element(_cur.getName());

                if ((ret & HIT) != 0) {
                    c.addToSelection(_cur);
                }

                doAttrs(ret, c);

                if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement(_cur)) {
                    end();
                    _cur.skip();
                }
            } else {
                do {
                    _cur.next();
                }
                while (!_cur.isContainerOrFinish());
            }
        }

        private void doAttrs(int ret, Cur c) {
            assert _cur.isContainer();

            if ((ret & ATTRS) != 0) {
                if (_cur.toFirstAttr()) {
                    do {
                        if (attr(_cur.getName())) {
                            c.addToSelection(_cur);
                        }
                    }
                    while (_cur.toNextAttr());

                    _cur.toParent();
                }
            }
        }

        public boolean next(Cur c) {
            if (_cur != null && _version != _cur._locale.version()) {
                throw new ConcurrentModificationException("Document changed during select");
            }

            int startCount = c.selectionCount();

            while (_cur != null) {
                advance(c);

                if (startCount != c.selectionCount()) {
                    return true;
                }
            }

            return false;
        }

        public void release() {
            if (_cur != null) {
                _cur.release();
                _cur = null;
            }
        }

        private final long _version;
        private Cur _cur;
    }

    private static final class DelegatePathImpl
        extends Path {
        private PathDelegate.SelectPathInterface _xpathImpl;

        static Path create(String implClassName, String pathExpr, String currentNodeVar, Map namespaceMap) {
            assert !currentNodeVar.startsWith("$"); // cezar review with ericvas

            PathDelegate.SelectPathInterface impl =
                PathDelegate.createInstance(implClassName, pathExpr, currentNodeVar, namespaceMap);
            if (impl == null) {
                return null;
            }

            return new DelegatePathImpl(impl, pathExpr);
        }


        private DelegatePathImpl(PathDelegate.SelectPathInterface xpathImpl,
                                 String pathExpr) {
            super(pathExpr);
            _xpathImpl = xpathImpl;
        }

        protected PathEngine execute(Cur c, XmlOptions options) {
            return new DelegatePathEngine(_xpathImpl, c);
        }

        private static class DelegatePathEngine
            extends XPathExecutionContext
            implements PathEngine {
            // full datetime format: yyyy-MM-dd'T'HH:mm:ss'Z'
            private final DateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            DelegatePathEngine(PathDelegate.SelectPathInterface xpathImpl,
                               Cur c) {
                _engine = xpathImpl;
                _version = c._locale.version();
                _cur = c.weakCur(this);
            }

            public boolean next(Cur c) {
                if (!_firstCall) {
                    return false;
                }

                _firstCall = false;

                if (_cur != null && _version != _cur._locale.version()) {
                    throw new ConcurrentModificationException("Document changed during select");
                }

                List resultsList;

                Object context_node = _cur.getDom();
                resultsList = _engine.selectPath(context_node);

                int i;
                for (i = 0; i < resultsList.size(); i++) {
                    //simple type function results
                    Object node = resultsList.get(i);
                    Cur pos = null;
                    if (!(node instanceof Node)) {
                        Object obj = resultsList.get(i);
                        String value;
                        if (obj instanceof Date) {
                            value = xmlDateFormat.format((Date) obj);
                        } else if (obj instanceof BigDecimal) {
                            value = ((BigDecimal) obj).toPlainString();
                        } else {
                            value = obj.toString();
                        }

                        //we cannot leave the cursor's locale, as
                        //everything is done in the selections of this cursor

                        Locale l = c._locale;
                        try {
                            pos = l.load("<xml-fragment/>").tempCur();
                            pos.setValue(value);
                            SchemaType type = getType(node);
                            Locale.autoTypeDocument(pos, type, null);
                            //move the cur to the actual text
                            pos.next();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        assert (node instanceof DomImpl.Dom) :
                            "New object created in XPATH!";
                        pos = ((DomImpl.Dom) node).tempCur();

                    }
                    c.addToSelection(pos);
                    pos.release();
                }
                release();
                _engine = null;
                return true;
            }

            private SchemaType getType(Object node) {
                SchemaType type;
                if (node instanceof Integer) {
                    type = XmlInteger.type;
                } else if (node instanceof Double) {
                    type = XmlDouble.type;
                } else if (node instanceof Long) {
                    type = XmlLong.type;
                } else if (node instanceof Float) {
                    type = XmlFloat.type;
                } else if (node instanceof BigDecimal) {
                    type = XmlDecimal.type;
                } else if (node instanceof Boolean) {
                    type = XmlBoolean.type;
                } else if (node instanceof String) {
                    type = XmlString.type;
                } else if (node instanceof Date) {
                    type = XmlDate.type;
                } else {
                    type = XmlAnySimpleType.type;
                }
                return type;
            }

            public void release() {
                if (_cur != null) {
                    _cur.release();
                    _cur = null;
                }
            }

            private Cur _cur;
            private PathDelegate.SelectPathInterface _engine;
            private boolean _firstCall = true;
            private long _version;
        }
    }
}
