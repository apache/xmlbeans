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
