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

package org.apache.xmlbeans.impl.xquery.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.xpath.StaticError;
import net.sf.saxon.xpath.XPathException;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.newstore2.SaxonXBeansDelegate;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import java.util.List;


public class XBeansXQuery
        implements SaxonXBeansDelegate.QueryInterface
{

    /**
     * Construct given an XQuery expression string.
     * @param queryExpr The XQuery expression.
     * @param contextVar The name of the context variable
     * @param boundary The offset of the end of the prolog
     */
    public XBeansXQuery(String queryExpr, String contextVar, Integer boundary)
    {
        this.config = new Configuration();
        this._stcContext = new StaticQueryContext(config);
        this._query = queryExpr;
        this._contextVar = contextVar;
        this.boundary = boundary.intValue();
        //Saxon requires external variables at the end of the prolog...
        String queryExp =
                (this.boundary == 0) ?
                "declare variable $" +
                _contextVar + " external;" + _query :
                _query.substring(0, this.boundary) +
                "declare variable $" +
                _contextVar + " external;" +
                _query.substring(this.boundary + 1);
        try {
            this._xquery = _stcContext.compileQuery(queryExp);
        }
        catch (XPathException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public List execQuery(Object node)
    {
        try {
            Node context_node = (Node) node;
            DynamicQueryContext dynamicContext =
                    new DynamicQueryContext(config);
            dynamicContext.setContextNode(_stcContext.
                    buildDocument(new DOMSource(context_node)));
            dynamicContext.setParameter(_contextVar,
                    dynamicContext.getContextNode());
            return _xquery.evaluate(dynamicContext);
        }
        catch (StaticError e) {
            throw new RuntimeException(" Error binding " + _contextVar);
        }
        catch (XPathException e) {
            throw new RuntimeException(e);
        }
    }

    private XQueryExpression _xquery;
    private String _query;
    private String _contextVar;
    private StaticQueryContext _stcContext;
    private Configuration config;
    private int boundary;
}
