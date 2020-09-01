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
import org.apache.xmlbeans.impl.xquery.saxon.XBeansXQuery;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Query {
    public static final String QUERY_DELEGATE_INTERFACE = "QUERY_DELEGATE_INTERFACE";
    public static String _useDelegateForXQuery = "use delegate for xquery";


    abstract XmlObject[] objectExecute(Cur c, XmlOptions options);

    abstract XmlCursor cursorExecute(Cur c, XmlOptions options);

    //
    // Xqrl store specific implementation of compiled path/query
    //

    static XmlObject[] objectExecQuery(Cur c, String queryExpr, XmlOptions options) {
        return getCompiledQuery(queryExpr, options).objectExecute(c, options);
    }

    static XmlCursor cursorExecQuery(Cur c, String queryExpr, XmlOptions options) {
        return getCompiledQuery(queryExpr, options).cursorExecute(c, options);
    }

    public static synchronized Query getCompiledQuery(String queryExpr, XmlOptions options) {
        return getCompiledQuery(queryExpr, Path.getCurrentNodeVar(options), options);
    }

    static synchronized Query getCompiledQuery(String queryExpr, String currentVar, XmlOptions options) {
        assert queryExpr != null;
        options = XmlOptions.maskNull(options);
        Query query;

        //Parse the query via XBeans: need to figure out end of prolog
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


        String delIntfName = XBeansXQuery.class.getName();
        query = DelegateQueryImpl.createDelegateCompiledQuery(delIntfName, queryExpr, currentVar, boundaryVal, options);

        if (query != null) {
            //_delegateQueryCache.put(queryExpr, query);
            return query;
        }

        throw new RuntimeException("No query engine found");
    }

    public static synchronized String compileQuery(String queryExpr, XmlOptions options) {
        getCompiledQuery(queryExpr, options);
        return queryExpr;
    }




    private static final class DelegateQueryImpl extends Query {
        private DelegateQueryImpl(QueryDelegate.QueryInterface xqueryImpl) {
            _xqueryImpl = xqueryImpl;
        }

        static Query createDelegateCompiledQuery(String delIntfName,
                                                 String queryExpr,
                                                 String currentVar,
                                                 int boundary,
                                                 XmlOptions xmlOptions) {
            assert !(currentVar.startsWith(".") || currentVar.startsWith(".."));
            QueryDelegate.QueryInterface impl =
                QueryDelegate.createInstance(delIntfName, queryExpr,
                    currentVar, boundary, xmlOptions);
            if (impl == null) {
                return null;
            }

            return new DelegateQueryImpl(impl);
        }

        XmlObject[] objectExecute(Cur c, XmlOptions options) {
            return new DelegateQueryEngine(_xqueryImpl, c, options).objectExecute();
        }

        XmlCursor cursorExecute(Cur c, XmlOptions options) {
            return new DelegateQueryEngine(_xqueryImpl, c, options).cursorExecute();
        }


        private static class DelegateQueryEngine {
            public DelegateQueryEngine(QueryDelegate.QueryInterface xqImpl,
                                       Cur c, XmlOptions opt) {

                _engine = xqImpl;
                _version = c._locale.version();
                _cur = c.weakCur(this);
                _options = opt;

            }

            public XmlObject[] objectExecute() {
                if (_cur != null && _version != _cur._locale.version())
                //throw new ConcurrentModificationException
                // ("Document changed during select")
                {
                    ;
                }

                Map<String, Object> bindings = XmlOptions.maskNull(_options).getXqueryVariables();
                List resultsList = _engine.execQuery(_cur.getDom(), bindings);

                XmlObject[] result = new XmlObject[resultsList.size()];
                int i;
                for (i = 0; i < resultsList.size(); i++) {
                    //copy objects into the locale
                    Locale l = Locale.getLocale(_cur._locale._schemaTypeLoader, _options);

                    l.enter();
                    Object node = resultsList.get(i);
                    Cur res = null;
                    try {
                        //typed function results of XQuery
                        if (!(node instanceof Node)) {
                            //TODO: exact same code as Path.java
                            //make a common super-class and pull this--what to name that
                            //superclass???
                            res = l.load("<xml-fragment/>").tempCur();
                            res.setValue(node.toString());
                            SchemaType type = getType(node);
                            Locale.autoTypeDocument(res, type, null);
                            result[i] = res.getObject();
                        } else {
                            res = loadNode(l, (Node) node);
                        }
                        result[i] = res.getObject();
                    } catch (XmlException e) {
                        throw new RuntimeException(e);
                    } finally {
                        l.exit();
                    }
                    res.release();
                }
                release();
                _engine = null;
                return result;
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

            public XmlCursor cursorExecute() {
                if (_cur != null && _version != _cur._locale.version())
                //throw new ConcurrentModificationException
                // ("Document changed during select")
                {
                    ;
                }

                Map<String, Object> bindings = XmlOptions.maskNull(_options).getXqueryVariables();
                List resultsList = _engine.execQuery(_cur.getDom(), bindings);

                int i;
                _engine = null;

                Locale locale = Locale.getLocale(_cur._locale._schemaTypeLoader, _options);
                locale.enter();
                Locale.LoadContext _context = new Cur.CurLoadContext(locale, _options);
                Cursor resultCur = null;
                try {
                    for (i = 0; i < resultsList.size(); i++) {
                        loadNodeHelper(locale, (Node) resultsList.get(i), _context);
                    }
                    Cur c = _context.finish();
                    Locale.associateSourceName(c, _options);
                    Locale.autoTypeDocument(c, null, _options);
                    resultCur = new Cursor(c);
                } catch (Exception e) {
                } finally {
                    locale.exit();
                }
                release();
                return resultCur;
            }


            public void release() {
                if (_cur != null) {
                    _cur.release();
                    _cur = null;
                }
            }


            private Cur loadNode(Locale locale, Node node) {
                Locale.LoadContext context = new Cur.CurLoadContext(locale, _options);

                try {
                    loadNodeHelper(locale, node, context);
                    Cur c = context.finish();
                    Locale.associateSourceName(c, _options);
                    Locale.autoTypeDocument(c, null, _options);
                    return c;
                } catch (Exception e) {
                    throw new XmlRuntimeException(e.getMessage(), e);
                }
            }

            private void loadNodeHelper(Locale locale, Node node, Locale.LoadContext context) {
                if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    QName attName = new QName(node.getNamespaceURI(),
                        node.getLocalName(),
                        node.getPrefix());
                    context.attr(attName, node.getNodeValue());
                } else {
                    locale.loadNode(node, context);
                }

            }


            private Cur _cur;
            private QueryDelegate.QueryInterface _engine;
            private long _version;
            private XmlOptions _options;
        }

        private QueryDelegate.QueryInterface _xqueryImpl;
    }

}
