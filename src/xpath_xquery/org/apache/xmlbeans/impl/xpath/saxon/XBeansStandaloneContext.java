package org.apache.xmlbeans.impl.xpath.saxon;

import net.sf.saxon.xpath.StandaloneContext;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.om.NamePool;

/**
 * Date: Jan 10, 2005
 * Time: 10:46:59 AM
 * <p/>
 * This class is used to circumvent a Saxon limitation,
 * namely, the lack of a method to set the default element NS
 */
public class XBeansStandaloneContext extends StandaloneContext
{
    public XBeansStandaloneContext(Configuration c)
    {
        super(c);
    }

    public XBeansStandaloneContext()
    {
        super();
    }

    public void setDefaultElementNamespace(String uri)
    {
        defaultUri = true;
        defaultNSCode = this.getNamePool().allocateCodeForURI(uri);
    }

    public short getDefaultElementNamespace()
    {
        short result = super.getDefaultElementNamespace();
        if (result == NamespaceConstant.NULL_CODE
                && defaultUri)
            return (short) defaultNSCode;

        else
            return result;
    }

    private int defaultNSCode;
    private boolean defaultUri;
}
