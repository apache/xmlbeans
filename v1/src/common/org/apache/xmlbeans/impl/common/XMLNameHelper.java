/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans.impl.common;

import weblogic.xml.stream.XMLName;
import javax.xml.namespace.QName;

public class XMLNameHelper
{
    public static QName getQName(XMLName xmlName)
    {
        if (xmlName == null)
            return null;
        
        return QNameHelper.forLNS( xmlName.getLocalName(), xmlName.getNamespaceUri() );
    }
    
    public static XMLName forLNS(String localname, String uri)
    {
        if (uri == null)
            uri = "";
        return new XmlNameImpl(uri, localname);
    }

    public static XMLName forLN(String localname)
    {
        return new XmlNameImpl("", localname);
    }

    public static XMLName forPretty(String pretty, int offset)
    {
        int at = pretty.indexOf('@', offset);
        if (at < 0)
            return new XmlNameImpl("", pretty.substring(offset));
        return new XmlNameImpl(pretty.substring(at + 1), pretty.substring(offset, at));
    }

    public static String pretty(XMLName name)
    {
        if (name == null)
            return "null";

        if (name.getNamespaceUri() == null || name.getNamespaceUri().length() == 0)
            return name.getLocalName();
        
        return name.getLocalName() + "@" + name.getNamespaceUri();
    }

    private static final char[] hexdigits = new char[]
        {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private static boolean isSafe(int c)
    {
        if (c >= 'a' && c <= 'z')
            return true;
        if (c >= 'A' && c <= 'Z')
            return true;
        if (c >= '0' && c <= '9')
            return true;
        return false;
    }

    public static String hexsafe(String s)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);
            if (isSafe(ch))
            {
                result.append(ch);
            }
            else
            {
                byte[] utf8 = s.substring(i, i + 1).getBytes();
                for (int j = 0; j < utf8.length; j++)
                {
                    result.append('_');
                    result.append(hexdigits[(utf8[j] >> 4) & 0xF]);
                    result.append(hexdigits[utf8[j] & 0xF]);
                }
            }
        }
        return result.toString();
    }

    public static String hexsafedir(XMLName name)
    {
        if (name.getNamespaceUri() == null || name.getNamespaceUri().length() == 0)
            return "_nons/" + hexsafe(name.getLocalName());
        return hexsafe(name.getNamespaceUri()) + "/" + hexsafe(name.getLocalName());
    }
}
