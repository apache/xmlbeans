/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.store;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 6, 2003
 *
 * Help class to decouple from xbean_xpath.jar and jaxen.jar (version v1.1 beta2)
 */
public final class JaxenXBeansDelegate
{
    private JaxenXBeansDelegate()
    {}

    static SelectPathInterface createInstance(String xpath)
    {
        if (_constructor==null)
            return null;

        try
        {
            return (JaxenXBeansDelegate.SelectPathInterface)_constructor.newInstance(new Object[] {xpath});
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    // Loose coupling functionality with xqrl.jar

    private static Constructor _constructor;

    static
    {
        boolean hasTheJars = false;
        Class jaxenXPathImpl = null;
        try
        {
            // from jaxen.jar
            Class.forName( "org.jaxen.BaseXPath" );
            // from xbean_xpath.jar
            jaxenXPathImpl = Class.forName( "org.apache.xmlbeans.impl.xpath.jaxen.XBeansXPathAdv" );

            hasTheJars = true;
        }
        catch ( ClassNotFoundException e )
        {
            hasTheJars = false;
        }
        catch ( NoClassDefFoundError e )
        {
            hasTheJars = false;
        }

        if (hasTheJars)
        {
            try
            {
                _constructor =
                    jaxenXPathImpl.getConstructor( new Class[] { String.class } );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    public static interface SelectPathInterface
    {
        public List selectPath(Object node);
    }
}
