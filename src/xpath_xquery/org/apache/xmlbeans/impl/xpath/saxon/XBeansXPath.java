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

package org.apache.xmlbeans.impl.xpath.saxon;

import org.apache.xmlbeans.impl.store.SaxonXBeansDelegate;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.Variable;

import javax.xml.transform.dom.DOMSource;


public class XBeansXPath
        implements SaxonXBeansDelegate.SelectPathInterface {

    /**
     * Construct given an XPath expression string.
     * @param xpathExpr The XPath expression.
     * @param namespaceMap a map of prefix/uri bindings for NS support
     * @param defaultNS the uri for the default element NS, if any
     */
    public XBeansXPath(String xpathExpr, Map namespaceMap, String defaultNS)
    {
        _queryExp = xpathExpr;
        this.defaultNS = defaultNS;
        this.namespaceMap = namespaceMap.entrySet().toArray();
    }

    /**
     * Select all nodes that are selectable by this XPath
     * expression. If multiple nodes match, multiple nodes
     * will be returned.
     * <p/>
     * <p/>
     * <b>NOTE:</b> In most cases, nodes will be returned
     * in document-order, as defined by the XML Canonicalization
     * specification.  The exception occurs when using XPath
     * expressions involving the <code>union</code> operator
     * (denoted with the pipe '|' character).
     * </p>
     * <p/>
     * <p/>
     * <b>NOTE:</b> Param node must be a Dom node which will be used during the xpath
     * execution and iteration through the results. A call of node.dispose() must be done
     * after reading all results.
     * </p>
     *
     * @param node The node, nodeset or Context object for evaluation.
     * This value can be null.
     * @return The <code>a list</code> of all items selected
     *         by this XPath expression.
     */
    public List selectNodes(Object node)
    {
        try
        {
            DOMSource rootNode = new DOMSource((Node) node);
            XPathEvaluator xpe = new XPathEvaluator();
            XBeansIndependentContext sc = new XBeansIndependentContext();

            // Declare ns bindings
            if (defaultNS != null)
                sc.setDefaultElementNamespace(defaultNS);

            for (int i = 0; i < namespaceMap.length; i++)
            {
                Map.Entry entry = (Map.Entry) namespaceMap[i];
                sc.declareNamespace((String) entry.getKey(),
                        (String) entry.getValue());
            }

            xpe.setStaticContext(sc);

            Variable thisVar = sc.declareVariable("this");
            thisVar.setValue(rootNode);

            XPathExpression exp = xpe.createExpression(_queryExp);
            return exp.evaluate(rootNode);
        }
        catch (TransformerException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List selectPath(Object node)
    {
        return selectNodes(node);
    }

    private Object[] namespaceMap;
    private String _queryExp;
    private String defaultNS;
}
