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

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMNodeWrapper;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.sxpath.*;
import net.sf.saxon.tree.wrapper.VirtualNode;
import org.apache.xmlbeans.impl.store.PathDelegate;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class XBeansXPath
    implements PathDelegate.SelectPathInterface {
    private final Map<String, String> namespaceMap = new HashMap<String, String>();
    private String path;
    private String contextVar;
    private String defaultNS;

    /**
     * Construct given an XPath expression string.
     *
     * @param path         The XPath expression
     * @param contextVar   The name of the context variable
     * @param namespaceMap a map of prefix/uri bindings for NS support
     * @param defaultNS    the uri for the default element NS, if any
     */
    public XBeansXPath(String path, String contextVar,
                       Map<String, String> namespaceMap, String defaultNS) {
        this.path = path;
        this.contextVar = contextVar;
        this.defaultNS = defaultNS;
        this.namespaceMap.putAll(namespaceMap);
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
     * <b>NOTE:</b> Param node must be a DOM node which will be used
     * during the xpath execution and iteration through the results.
     * A call of node.dispose() must be done after reading all results.
     * </p>
     *
     * @param node The node, nodeset or Context object for evaluation.
     *             This value can be null.
     * @return The <code>List</code> of all items selected
     * by this XPath expression.
     */
    public List selectNodes(Object node) {
        try {
            Node contextNode = (Node) node;
            Configuration config = new Configuration();
            IndependentContext sc = new IndependentContext(config);
            // Declare ns bindings
            // also see https://saxonica.plan.io/issues/2130
            // (XPath referencing attribute with namespace fails when using DOM)
            if (defaultNS != null) {
                sc.setDefaultElementNamespace(defaultNS);
            }

            namespaceMap.forEach(sc::declareNamespace);

            NodeInfo contextItem = config.unravel(new DOMSource(contextNode));

            XPathEvaluator xpe = new XPathEvaluator(config);
            xpe.setStaticContext(sc);
            XPathVariable thisVar = sc.declareVariable("", contextVar);
            XPathExpression xpath = xpe.createExpression(path);
            XPathDynamicContext dc = xpath.createDynamicContext(null);
            dc.setContextItem(contextItem);
            dc.setVariable(thisVar, contextItem);

            List<Item> saxonNodes = xpath.evaluate(dc);
            List<Object> retNodes = new ArrayList<>(saxonNodes.size());
            for (Item o : saxonNodes) {
                if (o instanceof DOMNodeWrapper) {
                    Node n = getUnderlyingNode((DOMNodeWrapper) o);
                    retNodes.add(n);
                } else if (o instanceof NodeInfo) {
                    retNodes.add(o.getStringValue());
                } else {
                    retNodes.add(SequenceTool.convertToJava(o));
                }
            }
            return retNodes;
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public List selectPath(Object node) {
        return selectNodes(node);
    }

    /**
     * According to the Saxon javadoc:
     * <code>getUnderlyingNode</code> in <code>NodeWrapper</code> implements
     * the method specified in the interface <code>VirtualNode</code>, and
     * the specification of the latter says that it may return another
     * <code>VirtualNode</code>, and you may have to drill down through
     * several layers of wrapping.
     * To be safe, this method is provided to drill down through multiple
     * layers of wrapping.
     *
     * @param v The <code>VirtualNode</code>
     * @return The underlying node
     */
    private static Node getUnderlyingNode(VirtualNode v) {
        Object o = v;
        while (o instanceof VirtualNode) {
            o = ((VirtualNode) o).getUnderlyingNode();
        }
        return (Node) o;
    }
}
