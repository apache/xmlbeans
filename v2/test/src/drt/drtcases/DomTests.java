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

import org.apache.xmlbeans.impl.store.Root;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class DomTests extends TestCase
{
    public DomTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(DomTests.class); }

    static String[] _args;
    static String _test;

    public static File getCaseFile(String theCase)
    {
        return TestEnv.xbeanCase("store/" + theCase);
    }

    static XmlCursor loadCase(String theCase) throws Exception
    {
        return XmlObject.Factory.parse(getCaseFile(theCase), null).newCursor();
    }

    public void doTestDomImport ( String xml )
        throws Exception
    {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder parser = f.newDocumentBuilder();

        Document doc = parser.parse( new InputSource( new StringReader( xml ) ) );

        XmlObject x = XmlObject.Factory.parse( doc );

        Assert.assertTrue( x.xmlText().equals( xml ) );
    }
    
    public void doTestDomExport ( String xml )
        throws Exception
    {
        XmlObject x = XmlObject.Factory.parse( XmlObject.Factory.parse( xml ).newDomNode() );
        Assert.assertTrue( x.xmlText().equals( xml ) );
    }
    
    public void doTest ( String xml )
        throws Exception
    {
        doTestDomImport( xml );
        doTestDomExport( xml );
    }

//    public void testDom2 ( )
//        throws Exception
//    {
//        DOMParser parser = new DOMParser();
//        
//        parser.setFeature( "http://xml.org/sax/features/namespaces", true );
//        
//        parser.parse( new InputSource( new StringReader( "<foo a='x\n\ny'></foo>" ) ) );
//
//        XmlObject x = XmlLoader.Factory.parse( parser.getDocument() );
//
//        System.out.println( x.xmlText() );
//    }
    
    public void testDom ( )
        throws Exception
    {
        doTest( "<foo xmlns=\"x\"/>" );
        doTest( "<foo xmlns=\"x\" xmlns:e=\"v\"/>" );
        doTest( "<foo>a<?X?>b</foo>" );
        doTest( "<foo>a<!--X-->b</foo>" );
        doTest( "<!--X--><foo/>" );
        doTest( "<foo/>" );
        doTest( "<foo x=\"y\"/>" );
        doTest( "<foo><a/><b>moo</b></foo>" );
        
        String xx =
            "<!--gg--><?a b?><foo>sdsd<a/>sdsd<b>moo</b>sd<!--asas-->sd</foo><!--hh-->";
        
        doTest( xx );

        String xml =
            "<xml-fragment>" +
            "foo" +
            "</xml-fragment>";

        doTestDomExport( xml );
        
        try
        {
            xml =
                "<xml-fragment " +
                "  foo='bar'>" +
                "</xml-fragment>";
                
            doTestDomExport( xml );
            
            Assert.assertTrue( false );
        }
        catch ( Exception e )
        {
        }

        XmlObject x = XmlObject.Factory.parse( xx );

        XmlCursor c = x.newCursor();

        for ( ; ; )
        {
            Node n = c.newDomNode();
            XmlObject.Factory.parse( n );

            if (c.toNextToken().isNone())
                break;
        }
    }
}
