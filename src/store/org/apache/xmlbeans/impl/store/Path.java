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

import java.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import org.apache.xmlbeans.impl.common.XPath;
import org.apache.xmlbeans.impl.common.XPath.XPathCompileException;
import org.apache.xmlbeans.impl.common.XPath.ExecutionContext;

import org.apache.xmlbeans.*;
import org.w3c.dom.Node;


// TODO - This class handled query *and* path ... rename it?

public abstract class Path
{
    Path(String key)
    {
        _pathKey = key;
    }

    public static String _useXqrlForXpath = "use xqrl for xpath";
    public static String _useXbeanForXpath = "use xbean for xpath";

    interface PathEngine
    {
        void release();

        boolean next(Cur c);
    }

    abstract PathEngine execute(Cur c);

    //
    //
    //

    static String getCurrentNodeVar(XmlOptions options)
    {
        String currentNodeVar = "this";

        options = XmlOptions.maskNull(options);

        if (options.hasOption(XmlOptions.XQUERY_CURRENT_NODE_VAR)) {
            currentNodeVar = (String) options.get(XmlOptions.XQUERY_CURRENT_NODE_VAR);

            if (currentNodeVar.startsWith("$")) {
                throw new IllegalArgumentException("Omit the '$' prefix for the current node variable");
            }
        }

        return currentNodeVar;
    }

    private static final int USE_XQRL = 0x02;
    private static final int USE_XBEAN =0x01;
    private static final int USE_SAXON =0x04;

    public static Path getCompiledPath(String pathExpr, XmlOptions options)
    {
        options = XmlOptions.maskNull(options);

        int force =
                options.hasOption(_useXqrlForXpath)
                ? USE_XQRL
                : options.hasOption(_useXbeanForXpath)
                ? USE_XBEAN
                : USE_XBEAN|USE_XQRL|USE_SAXON; //set all bits

        return getCompiledPath(pathExpr, force, getCurrentNodeVar(options));
    }

    static synchronized Path getCompiledPath(String pathExpr, int force,
        String currentVar)
    {
        Path path = null;
        Map namespaces = (force & USE_SAXON) != 0 ? new HashMap() : null;

        if ((force & USE_XBEAN) != 0)
            path = (Path) _xbeanPathCache.get(pathExpr);
        if (path == null && (force & USE_XBEAN) != 0)
            path = (Path) _xqrlPathCache.get(pathExpr);

        if (path != null)
            return path;

        if ((force & USE_XBEAN) != 0)
            path = getCompiledPathXbean(pathExpr, currentVar, namespaces);
        if (path == null && (force & USE_XQRL) != 0 &&
            !SaxonXBeansDelegate.bInstantiated)
            path = getCompiledPathXqrl(pathExpr, currentVar);
        if (path == null && (force & USE_SAXON) != 0)
            path = getCompiledPathSaxon(pathExpr, currentVar,
                namespaces);
        if (path == null)
        {
            StringBuffer errMessage = new StringBuffer();
            if ((force & USE_XBEAN) != 0)
                errMessage.append(" Trying XBeans path engine...");
            if ((force & USE_XQRL) != 0 && !SaxonXBeansDelegate.bInstantiated)
                errMessage.append(" Trying XQRL...");
            if ((force & USE_SAXON) != 0)
                errMessage.append(" Trying Saxon...");
            throw new RuntimeException(errMessage.toString()+" FAILED on "+pathExpr);
        }
        return path;
    }

    static private synchronized Path getCompiledPathXqrl(String pathExpr,
        String currentVar)
    {
        Path path = createXqrlCompiledPath(pathExpr, currentVar);
        if (path != null)
            _xqrlPathCache.put(path._pathKey, path);
        return path;
    }

    static private synchronized Path getCompiledPathXbean(String pathExpr,
        String currentVar, Map namespaces)
    {
        Path path = XbeanPath.create(pathExpr, currentVar, namespaces);
        if (path != null)
            _xbeanPathCache.put(path._pathKey, path);
        return path;
    }

    static private synchronized Path getCompiledPathSaxon(String pathExpr, String currentVar, Map namespaces)
    {
        Path path = null;
        if ( namespaces == null )  namespaces = new HashMap();
        try{
            XPath.compileXPath(pathExpr, currentVar, namespaces);
        }catch (XPath.XPathCompileException e){
            //do nothing, this function is only called to populate the namespaces map
        }
        int offset =
            namespaces.get(XPath._NS_BOUNDARY) == null ?
            0 :
            ((Integer) namespaces.get(XPath._NS_BOUNDARY)).intValue();
        namespaces.remove(XPath._NS_BOUNDARY);
        path = SaxonPathImpl.create(pathExpr.substring(offset),
            currentVar,
            namespaces);
        return path;
    }


    public static synchronized String compilePath(String pathExpr, XmlOptions options)
    {
        return getCompiledPath(pathExpr, options)._pathKey;
    }

    //
    // Xbean store specific implementation of compiled path
    //

    private static final class XbeanPath extends Path
    {
        static Path create(String pathExpr, String currentVar, Map namespaces)
        {
            try
            {
                return new XbeanPath(pathExpr, currentVar,
                                XPath.compileXPath(pathExpr, currentVar, namespaces));
            }
            catch (XPathCompileException e) {
                return null;
            }
        }

        private XbeanPath(String pathExpr, String currentVar, XPath xpath)
        {
            super(pathExpr);

            _currentVar = currentVar;
            _compiledPath = xpath;
        }

        PathEngine execute(Cur c)
        {
            // The builtin XPath engine works only on containers.  Delegate to
            // xqrl otherwise.  Also, if the path had a //. at the end, the
            // simple xpath engine can't do the generate case, it only handles
            // attrs and elements.

            if (!c.isContainer() || _compiledPath.sawDeepDot())
            {
                int force = USE_SAXON | USE_XQRL;
                return getCompiledPath(_pathKey, force, _currentVar).execute(c);
            }
            return new XbeanPathEngine(_compiledPath, c);
        }

        private final String _currentVar;
        private final XPath _compiledPath;
        //return a map of namespaces for Saxon, if it's ever invoked
        public Map namespaces;
    }

    private static Path createXqrlCompiledPath(String pathExpr, String currentVar)
    {
        if (_xqrlCompilePath == null) {
            try {
                Class xqrlImpl = Class.forName("org.apache.xmlbeans.impl.store.XqrlImpl");

                _xqrlCompilePath =
                        xqrlImpl.getDeclaredMethod("compilePath",
                                new Class[]{String.class, String.class, Boolean.class});
            }
            catch (ClassNotFoundException e) {
                return null;
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        Object[] args = new Object[]{pathExpr, currentVar, new Boolean(true)};

        try {
            return (Path) _xqrlCompilePath.invoke(null, args);
        }
        catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            throw new RuntimeException(t.getMessage(), t);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static final class XbeanPathEngine extends ExecutionContext implements PathEngine
    {
        XbeanPathEngine(XPath xpath, Cur c)
        {
            assert c.isContainer();

            _version = c._locale.version();
            _cur = c.weakCur(this);

            _cur.push();

            init(xpath);

            int ret = start();

            if ((ret & HIT) != 0)
                c.addToSelection();

            doAttrs(ret, c);

            if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement(_cur))
                release();
        }

        private void advance(Cur c)
        {
            assert _cur != null;

            if (_cur.isFinish()) {
                if (_cur.isAtEndOfLastPush())
                    release();
                else {
                    end();
                    _cur.next();
                }
            }
            else if (_cur.isElem()) {
                int ret = element(_cur.getName());

                if ((ret & HIT) != 0)
                    c.addToSelection(_cur);

                doAttrs(ret, c);

                if ((ret & DESCEND) == 0 || !Locale.toFirstChildElement(_cur)) {
                    end();
                    _cur.skip();
                }
            }
            else
                _cur.next();
        }

        private void doAttrs(int ret, Cur c)
        {
            assert _cur.isContainer();

            if ((ret & ATTRS) != 0) {
                if (_cur.toFirstAttr()) {
                    do {
                        if (attr(_cur.getName()))
                            c.addToSelection(_cur);
                    }
                    while (_cur.toNextAttr());

                    _cur.toParent();
                }
            }
        }

        public boolean next(Cur c)
        {
            if (_cur != null && _version != _cur._locale.version())
                throw new ConcurrentModificationException("Document changed during select");

            int startCount = c.selectionCount();

            while (_cur != null) {
                advance(c);

                if (startCount != c.selectionCount())
                    return true;
            }

            return false;
        }

        public void release()
        {
            if (_cur != null) {
                _cur.release();
                _cur = null;
            }
        }

        private final long _version;
        private Cur _cur;
    }

    private static final class SaxonPathImpl extends Path
    {

        private SaxonXBeansDelegate.SelectPathInterface _xpathImpl;


        static Path create(String pathExpr, String currentNodeVar, Map namespaceMap)
        {
            assert !currentNodeVar.startsWith("$"); // cezar review with ericvas

            SaxonXBeansDelegate.SelectPathInterface impl =
                    SaxonXBeansDelegate.createInstance(pathExpr, namespaceMap);
            if (impl == null)
                return null;

            return new SaxonPathImpl(impl, pathExpr);
        }


        private SaxonPathImpl(SaxonXBeansDelegate.SelectPathInterface xpathImpl,
                              String pathExpr)
        {
            super(pathExpr);
            _xpathImpl = xpathImpl;
        }

        protected PathEngine execute(Cur c)
        {
            return new SaxonPathEngine(_xpathImpl, c);
        }

        private static class SaxonPathEngine
                extends XPath.ExecutionContext
                implements PathEngine
        {

            SaxonPathEngine(SaxonXBeansDelegate.SelectPathInterface xpathImpl,
                            Cur c)
            {
                _saxonXpathImpl = xpathImpl;
                _version = c._locale.version();
                _cur = c.weakCur(this);
            }

            public boolean next(Cur c)
            {
                if (!_firstCall)
                    return false;

                _firstCall = false;

                if (_cur != null && _version != _cur._locale.version())
                    throw new ConcurrentModificationException("Document changed during select");

                List resultsList;
                Object context_node;

                context_node = _cur.getDom();
                resultsList = _saxonXpathImpl.selectPath(context_node);

                int i;
                for (i = 0; i < resultsList.size(); i++) {
                    //simple type function results
                    Object node = resultsList.get(i);
                    Cur pos = null;
                    if (!(node instanceof Node)) {
                        String value;

                        value = resultsList.get(i).toString();

                        //we cannot leave the cursor's locale, as
                        //everything is done in the selections of this cursor

                        Locale l = c._locale;
                        try {
                            pos = l.load("<xml-fragment>" +
                                    value +
                                    "</xml-fragment>").tempCur();
                            SchemaType type = getType(node);
                            Locale.autoTypeDocument(pos, type, null);
                            //move the cur to the actual text
                            pos.next();
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else {
                        assert (node instanceof DomImpl.Dom):
                                "New object created in XPATH!";
                        pos = ((DomImpl.Dom) node).tempCur();

                    }
                    c.addToSelection(pos);
                    pos.release();
                }
                release();
                _saxonXpathImpl = null;
                return true;
            }

            private SchemaType getType(Object node)
            {
                SchemaType type;
                if (node instanceof Integer)
                    type = XmlInteger.type;
                else if (node instanceof Double)
                    type = XmlDouble.type;
                else if (node instanceof Long)
                    type = XmlLong.type;
                else if (node instanceof Float)
                    type = XmlFloat.type;
                else if (node instanceof BigDecimal)
                    type = XmlDecimal.type;
                else if (node instanceof Boolean)
                    type = XmlBoolean.type;
                else if (node instanceof String)
                    type = XmlString.type;
                else if (node instanceof Date)
                    type = XmlDate.type;
                else
                    type = XmlAnySimpleType.type;
                return type;
            }

            public void release()
            {
                if (_cur != null) {
                    _cur.release();
                    _cur = null;
                }
            }

            private Cur _cur;
            private SaxonXBeansDelegate.SelectPathInterface _saxonXpathImpl;
            private boolean _firstCall = true;
            private long _version;
        }

    }


    protected final String _pathKey;

    private static Map _xbeanPathCache = new HashMap();
    private static Map _xqrlPathCache = new HashMap();

    private static Method _xqrlCompilePath;
}