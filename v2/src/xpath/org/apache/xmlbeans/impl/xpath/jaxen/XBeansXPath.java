package org.apache.xmlbeans.impl.xpath.jaxen;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.apache.xmlbeans.impl.xpath.jaxen.XBeansNavigator;

/**
 * Author: Cezar Andrei (cezar.andrei@bea.com)
 * Date: Oct 10, 2003
 */
public class XBeansXPath extends BaseXPath
{
    /** Construct given an XPath expression string.
     *
     *  @param xpathExpr The XPath expression.
     *
     *  @throws org.jaxen.JaxenException if there is a syntax error while
     *          parsing the expression.
     */
    public XBeansXPath(String xpathExpr) throws JaxenException
    {
        super( xpathExpr, XBeansNavigator.getInstance() );
    }
}
