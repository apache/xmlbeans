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

package drtcases;

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlSaxHandler;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlInt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.Comparable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.TreeSet;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

   
public class TypedSettersTests extends TestCase
{
    public TypedSettersTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(TypedSettersTests.class); }

    private static final String schemaNs ="xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    private static final String instanceNs = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";

    private static final String fmt ( String s )
    {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0 ; i < s.length() ; i++ )
        {
            char ch = s.charAt( i );

            if (ch != '$')
            {
                sb.append( ch );
                continue;
            }
            
            ch = s.charAt( ++i );

            String id = "";

            while ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
            {
                id = id + ch;
                ch = s.charAt( ++i );
            }

            String arg = "";

            if (ch == '(')
            {
                ch = s.charAt( ++i );

                while ( ch != ')' )
                {
                    arg += ch;
                    ch = s.charAt( ++i );
                }
            }
            else
                i--;

            if (id.equals( "schema" ))
                sb.append( schemaNs );
            else if (id.equals( "xsi" ))
                sb.append( instanceNs );
            else if (id.equals( "type" ))
            {
                Assert.assertTrue( arg.length() > 0 );
                sb.append( "xsi:type=\"" + arg + "\"" );
            }
            else
                Assert.assertTrue( false );
        }

        return sb.toString();
    }

    private static final String nses = schemaNs + " " + instanceNs;
    
    public void testJavaNoTypeSingletonElement ( )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( "<xyzzy/>" );
        XmlObject x2 = XmlObject.Factory.parse( "<bubba>moo</bubba>" );
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c2.toNextToken();

        c.getObject().set( c2.getObject() );

        Assert.assertTrue( x.xmlText().equals( "<xyzzy>moo</xyzzy>" ) );
    }
    
    public void testJavaNoTypeSingletonAttribute ( )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( "<xyzzy a=''/>" );
        XmlObject x2 = XmlObject.Factory.parse( "<bubba b='moo'/>" );
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c.toNextToken();
        c2.toNextToken();
        c2.toNextToken();

        c.getObject().set( c2.getObject() );

        Assert.assertTrue( x.xmlText().equals( "<xyzzy a=\"moo\"/>" ) );
    }
    
    public void testJavaNoTypeSingletonElementWithXsiType ( )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( "<xyzzy/>", new XmlOptions().setDocumentType( XmlObject.type ) );
        XmlObject x2 = XmlObject.Factory.parse( fmt( "<bubba $type(xs:int) $xsi $schema>69</bubba>" ) );
        XmlCursor c = x.newCursor();
        XmlCursor c2 = x2.newCursor();

        c.toNextToken();
        c2.toNextToken();

        XmlObject xyzzy = c.getObject();
        XmlObject bubba = c2.getObject();

        Assert.assertTrue( bubba.schemaType() == XmlInt.type );

//        xyzzy.set( bubba );

//        Assert.assertTrue( x.xmlText().equals( fmt( "<xyzzy $type(xs:int) $xsi $schema>moo</xyzzy>" ) ) );
    }
    
}
