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
package myPackage;

import org.apache.xmlbeans.XmlObject;

/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Apr 25, 2004
 */

public class BarHandler
{
    public static byte[] bar(XmlObject xo, String s) throws Bar.MyException
    {
        String msg = "{in BarHandler.handleBar(s: " + s + ")}";
        
        if (s==null)
            throw new Bar.MyException();

        return msg.getBytes();
    }
}
