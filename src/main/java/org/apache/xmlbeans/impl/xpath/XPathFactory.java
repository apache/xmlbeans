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

package org.apache.xmlbeans.impl.xpath;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.store.Cur;
import org.apache.xmlbeans.impl.xpath.saxon.SaxonXPath;
import org.apache.xmlbeans.impl.xpath.saxon.SaxonXQuery;
import org.apache.xmlbeans.impl.xpath.xmlbeans.XmlbeansXPath;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class XPathFactory {
    private static final int USE_XMLBEANS = 0x01;
    private static final int USE_SAXON = 0x04;

    private static final Map<String, WeakReference<Path>> _xmlbeansPathCache = new WeakHashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
        return getCompiledPath(pathExpr, options, getCurrentNodeVar(options));
    }

    public static Path getCompiledPath(String pathExpr, XmlOptions options, String currentVar) {
        int force =
            options.isXPathUseSaxon() ? USE_SAXON
                : options.isXPathUseXmlBeans() ? USE_XMLBEANS
                : USE_XMLBEANS | USE_SAXON;

        Path path = null;
        WeakReference<Path> pathWeakRef = null;
        Map<String, String> namespaces = (force & USE_SAXON) != 0 ? new HashMap<>() : null;
        lock.readLock().lock();
        try {
            if ((force & USE_XMLBEANS) != 0) {
                pathWeakRef = _xmlbeansPathCache.get(pathExpr);
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
            if ((force & USE_XMLBEANS) != 0) {
                pathWeakRef = _xmlbeansPathCache.get(pathExpr);
                if (pathWeakRef != null) {
                    path = pathWeakRef.get();
                }
                if (path == null) {
                    path = getCompiledPathXmlBeans(pathExpr, currentVar, namespaces);
                }
            }
            if (path == null && (force & USE_SAXON) != 0) {
                path = getCompiledPathSaxon(pathExpr, currentVar, namespaces);
            }
            if (path == null) {
                StringBuilder errMessage = new StringBuilder();
                if ((force & USE_XMLBEANS) != 0) {
                    errMessage.append(" Trying XmlBeans path engine...");
                }
                if ((force & USE_SAXON) != 0) {
                    errMessage.append(" Trying Saxon path engine...");
                }

                throw new RuntimeException(errMessage.toString() + " FAILED on " + pathExpr);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return path;
    }

    private static Path getCompiledPathXmlBeans(String pathExpr, String currentVar, Map<String, String> namespaces) {
        try {
            Path path = new XmlbeansXPath(pathExpr, currentVar,
                XPath.compileXPath(pathExpr, currentVar, namespaces));
            _xmlbeansPathCache.put(pathExpr, new WeakReference<>(path));
            return path;
        } catch (XPath.XPathCompileException ignored) {
            return null;
        }
    }

    public static Path getCompiledPathSaxon(String pathExpr, String currentVar, Map<String, String> namespaces) {
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

        return new SaxonXPath(pathExpr.substring(offset), currentVar, namespaces);
    }


    public static String compilePath(String pathExpr, XmlOptions options) {
        getCompiledPath(pathExpr, options);
        return pathExpr;
    }

    //
    // Xqrl store specific implementation of compiled path/query
    //

    public static XmlObject[] objectExecQuery(Cur c, String queryExpr, XmlOptions options) {
        return getCompiledQuery(queryExpr, options).objectExecute(c, options);
    }

    public static XmlCursor cursorExecQuery(Cur c, String queryExpr, XmlOptions options) {
        return getCompiledQuery(queryExpr, options).cursorExecute(c, options);
    }

    public static synchronized XQuery getCompiledQuery(String queryExpr, XmlOptions options) {
        return getCompiledQuery(queryExpr, XPathFactory.getCurrentNodeVar(options), options);
    }

    static synchronized XQuery getCompiledQuery(String queryExpr, String currentVar, XmlOptions options) {
        assert queryExpr != null;
        options = XmlOptions.maskNull(options);

        //Parse the query via XmlBeans: need to figure out end of prolog
        //in order to bind $this...not good but...
        Map<String, String> boundary = new HashMap<>();
        int boundaryVal;
        try {
            XPath.compileXPath(queryExpr, currentVar, boundary);
        } catch (XPath.XPathCompileException e) {
            //don't care if it fails, just care about boundary
        } finally {
            boundaryVal = Integer.parseInt(boundary.getOrDefault(XPath._NS_BOUNDARY, "0"));
        }

        return new SaxonXQuery(queryExpr, currentVar, boundaryVal, options);
    }

    public static synchronized String compileQuery(String queryExpr, XmlOptions options) {
        getCompiledQuery(queryExpr, options);
        return queryExpr;
    }
}
