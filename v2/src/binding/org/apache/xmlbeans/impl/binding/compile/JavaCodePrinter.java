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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.binding.compile;

import java.io.Writer;
import java.io.IOException;

public class JavaCodePrinter
{
    private Writer _writer;
    private int    _indent;

    private static final String LINE_SEPARATOR =
        System.getProperty("line.separator") == null
            ? "\n"
            : System.getProperty("line.separator");

    private static final String MAX_SPACES = "                                        ";
    private static final int INDENT_INCREMENT = 4;

    public JavaCodePrinter(Writer writer)
    {
        _writer = writer;
        _indent = 0;
    }

    public void indent()
    {
        _indent += INDENT_INCREMENT;
    }

    public void outdent()
    {
        _indent -= INDENT_INCREMENT;
    }

    public static String encodeString(String s)
    {
        StringBuffer sb = new StringBuffer();

        sb.append( '"' );

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (ch == '"')
            {
                sb.append( '\\' );
                sb.append( '\"' );
            }
            else if (ch == '\\')
            {
                sb.append( '\\' );
                sb.append( '\\' );
            }
            else if (ch == '\r')
            {
                sb.append( '\\' );
                sb.append( 'r' );
            }
            else if (ch == '\n')
            {
                sb.append( '\\' );
                sb.append( 'n' );
            }
            else if (ch == '\t')
            {
                sb.append( '\\' );
                sb.append( 't' );
            }
            else
                sb.append( ch );
        }

        sb.append( '"' );

        return sb.toString();
    }
    
    private static String intlSafe(String s)
    {
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) > 127)
                return buildIntlSafe(s);
        }
        return s;
    }
    
    private static final char[] hexLow = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    };
    
    private static final char[] hexHigh = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A',
        'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B',
        'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C',
        'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D',
        'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
        'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F',
    };
    
    private static String buildIntlSafe(String s)
    {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int j = 0;
        for (;;)
        {
            for (; i < s.length(); i++)
            {
                if (s.charAt(i) > 127)
                    break;
            }
            if (j < i)
                sb.append(s.substring(j, i));
            for (; i < s.length(); i++)
            {
                int ch = s.charAt(i);
                if (ch <= 127)
                    break;
                int highByte = ch >>> 8;
                int lowByte = ch & 0xFF;
                
                sb.append("\\u");
                sb.append(hexHigh[highByte]);
                sb.append(hexLow[highByte]);
                sb.append(hexHigh[lowByte]);
                sb.append(hexLow[lowByte]);
            }
            j = i;
        }
    }

    public void line(String s) throws IOException
    {
        int indent = _indent;
        
        if (indent > MAX_SPACES.length() / 2)
            indent = MAX_SPACES.length() / 4 + indent / 2;
        
        if (indent > MAX_SPACES.length())
            indent = MAX_SPACES.length();
        
        _writer.write(MAX_SPACES.substring(0, indent));
        _writer.write(intlSafe(s));
        _writer.write(LINE_SEPARATOR);
    }
    
    public void line() throws IOException
    {
        _writer.write(LINE_SEPARATOR);
    }
    
    public void javadoc(String s) throws IOException
    {
        line("/**");
        int i = 0;
        int j = 0;
        for (;;)
        {
            j = s.indexOf('\n', i);
            if (j < 0)
                break;
            line(" * " + s.substring(i, j));
            i = j + 1;
        }
        line(" * " + s.substring(i));
        line(" */");
    }
    
    public void startBlock() throws IOException
    {
        line("{");
        indent();
    }
    
    public void startBlock(String s) throws IOException
    {
        line(s + " {");
        indent();
    }
    
    public void endBlock() throws IOException
    {
        outdent();
        line("}");
    }
    
}
