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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.SchemaType;

public class XmlWhitespace
{
    public static final int WS_UNSPECIFIED = SchemaType.WS_UNSPECIFIED;
    public static final int WS_PRESERVE = SchemaType.WS_PRESERVE;
    public static final int WS_REPLACE = SchemaType.WS_REPLACE;
    public static final int WS_COLLAPSE = SchemaType.WS_COLLAPSE;


    public static boolean isSpace(char ch)
    {
        switch (ch)
        {
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                return true;
        }
        return false;
    }

    public static boolean isAllSpace(String v)
    {
        for (int i = 0; i < v.length(); i++)
        {
            if (!isSpace(v.charAt(i)))
                return false;
        }
        return true;
    }

    public static String collapse(String v)
    {
        return collapse(v, XmlWhitespace.WS_COLLAPSE);
    }

    /**
     * The algorithm used by apply_wscanon: sometimes used in impls.
     */
    public static String collapse(String v, int wsr)
    {
        if (wsr == SchemaType.WS_PRESERVE || wsr == SchemaType.WS_UNSPECIFIED)
            return v;

        if (v.indexOf('\n') >= 0)
            v = v.replace('\n', ' ');
        if (v.indexOf('\t') >= 0)
            v = v.replace('\t', ' ');
        if (v.indexOf('\r') >= 0)
            v = v.replace('\r', ' ');

        if (wsr == SchemaType.WS_REPLACE)
            return v;

        int j = 0;
        int len = v.length();
        if (len == 0)
            return v;

        int i;

        /* a trick: examine every other character looking for pairs of spaces */
        if (v.charAt(0) != ' ')
        {
            examine: {
                for (j = 2; j < len; j += 2)
                {
                    if (v.charAt(j) == ' ')
                    {
                        if (v.charAt(j - 1) == ' ')
                            break examine;
                        if (j == len - 1)
                            break examine;
                        j++;
                        if (v.charAt(j) == ' ')
                            break examine;
                    }
                }
                if (j == len && v.charAt(j - 1) == ' ')
                    break examine;
                return v;
            }
            /* j is pointing at the first ws to be removed, or past end */
            i = j;
        }
        else
        {
            /**
             * j is pointing at the last whitespace in the initial run
             */
            while (j + 1 < v.length() && v.charAt(j + 1) == ' ')
                j += 1;
            i = 0;
        }

        char[] ch = v.toCharArray();

        shifter: for (;;)
        {
            for (;;)
            {
                /* j was ws or past end */
                j++;
                if (j >= len)
                    break shifter;
                if (v.charAt(j) != ' ')
                    break;
            }
            for (;;)
            {
                /* j was nonws */
                ch[i++] = ch[j++];
                if (j >= len)
                    break shifter;
                if (ch[j] == ' ')
                {
                    ch[i++] = ch[j++];
                    if (j >= len)
                        break shifter;
                    if (ch[j] == ' ')
                        break;
                }
            }
        }

        return new String(ch, 0, (i == 0 || ch[i - 1] != ' ') ? i : i - 1);
    }

}
