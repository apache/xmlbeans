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

package org.apache.xmlbeans.impl.xpath.jaxen;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.store.JaxenXBeansDelegate;

import java.util.List;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Oct 10, 2003
 */
public class XBeansXPathAdv
    extends BaseXPath
    implements JaxenXBeansDelegate.SelectPathInterface
{
    /** Construct given an XPath expression string.
     *
     *  @param xpathExpr The XPath expression.
     *
     *  @throws org.jaxen.JaxenException if there is a syntax error while
     *          parsing the expression.
     */
    public XBeansXPathAdv(String xpathExpr) throws JaxenException
    {
        super( xpathExpr, XBeansNavigator.getInstance() );
    }

    /** Select all nodes that are selectable by this XPath
     *  expression. If multiple nodes match, multiple nodes
     *  will be returned.
     *
     *  <p>
     *  <b>NOTE:</b> In most cases, nodes will be returned
     *  in document-order, as defined by the XML Canonicalization
     *  specification.  The exception occurs when using XPath
     *  expressions involving the <code>union</code> operator
     *  (denoted with the pipe '|' character).
     *  </p>
     *
     *  <p>
     *  <b>NOTE:</b> Param node must be an XmlCursor, which will be used during the xpath
     *  execution and iteration through the results. A call of node.dispose() must be done
     *  after reading all results.
     *  </p>
     *
     *  @param node The node, nodeset or Context object for evaluation. This value can be null.
     *
     *  @return The <code>node-set</code> of all items selected
     *          by this XPath expression.
     *
     *  @see #selectSingleNode
     */
    public List selectNodes(Object node) throws JaxenException
    {
        XmlCursor xc;
        xc = ((XmlCursor)node);

        ((XBeansNavigator)getNavigator()).setCursor(xc);
        return super.selectNodes( XBeansNavigator.getBookmarkInThisPlace(xc) );
    }

    public List selectPath(Object node)
    {
        try
        {
            return selectNodes(node);
        }
        catch (JaxenException e)
        {
            throw new RuntimeException(e);
        }
    }
}
