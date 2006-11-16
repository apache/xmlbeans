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
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlTokenSource;
import org.apache.xmlbeans.impl.store.SaxonXBeansDelegate;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerException;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ListIterator;


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
        this.needsDomSourceWrapping = needsDOMSourceWrapping();
        this.config = new Configuration();
        config.setTreeModel(net.sf.saxon.event.Builder.STANDARD_TREE);
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
                _query.substring(this.boundary);
        try {
            this._xquery = _stcContext.compileQuery(queryExp);
        }
        catch (TransformerException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public List execQuery(Object node, Map variableBindings)
    {
        try {
            Node context_node = (Node) node;
            DynamicQueryContext dynamicContext =
                    new DynamicQueryContext(config);
            dynamicContext.setContextNode(_stcContext.
                    buildDocument(new DOMSource(context_node)));
            dynamicContext.setParameter(_contextVar,
                    dynamicContext.getContextItem());
            // Set the other variables
            if (variableBindings != null)
                for (Iterator it = variableBindings.entrySet().iterator();
                    it.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    if (entry.getValue() instanceof XmlTokenSource)
                    {
                        Object paramObject;
                        // Saxon 8.6.1 requires that the Node be wrapped
                        // into a DOMSource, while later versions require that
                        // it not be
                        if (needsDomSourceWrapping)
                            paramObject = new DOMSource(((XmlTokenSource)
                                entry.getValue()).getDomNode());
                        else
                            paramObject = ((XmlTokenSource) entry.getValue()).
                                getDomNode();
                        dynamicContext.setParameter((String) entry.getKey(),
                                paramObject);
                    }
                    else if (entry.getValue() instanceof String)
                    dynamicContext.setParameter((String) entry.getKey(),
                        entry.getValue());
                }

            // After 8.3(?) Saxon nodes no longer implement Dom.
            // The client needs saxon8-dom.jar, and the code needs
            // this NodeOverNodeInfo Dom wrapper doohickey
            List saxonNodes = _xquery.evaluate(dynamicContext);
            for(ListIterator it = saxonNodes.listIterator(); it.hasNext(); )
            {
                Object o = it.next();
                if(o instanceof NodeInfo)
                {
                    Node n = NodeOverNodeInfo.wrap((NodeInfo)o);
                    it.set(n);
                }
            }
            return saxonNodes;
        }
        catch (TransformerException e) {
            throw new RuntimeException(" Error binding " + _contextVar, e);
        }
    }

    /**
     * @return true if we are dealing with a version of Saxon 8.x where x<=6
     */
    private static boolean needsDOMSourceWrapping()
    {
        int saxonMinorVersion;
        int saxonMajorVersion;
        String versionString = net.sf.saxon.Version.getProductVersion();
        int dot1 = versionString.indexOf('.');
        if (dot1 < 0)
            return false;
        int dot2 = versionString.indexOf('.', dot1 + 1);
        if (dot2 < 0)
            return false;
        try
        {
            saxonMajorVersion = Integer.parseInt(versionString.substring(0, dot1));
            saxonMinorVersion = Integer.parseInt(versionString.substring(dot1 + 1, dot2));
            return saxonMajorVersion == 8 && saxonMinorVersion <= 6;
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }

    private XQueryExpression _xquery;
    private String _query;
    private String _contextVar;
    private StaticQueryContext _stcContext;
    private Configuration config;
    private int boundary;
    private boolean needsDomSourceWrapping;
}
