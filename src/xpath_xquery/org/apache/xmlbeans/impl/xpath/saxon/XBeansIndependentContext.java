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

import net.sf.saxon.trans.IndependentContext;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;

/**
 * Date: Jan 10, 2005
 * Time: 10:46:59 AM
 * <p/>
 * This class is used to circumvent a Saxon limitation,
 * namely, the lack of a method to set the default element NS
 */
public class XBeansIndependentContext extends IndependentContext
{
    public XBeansIndependentContext(Configuration c)
    {
        super(c);
    }

    public XBeansIndependentContext()
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
