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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.apache.xmlbeans.impl.store.Root;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3.x2001.xmlSchema.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import weblogic.xml.stream.XMLEvent;
import weblogic.xml.stream.XMLInputStream;
import weblogic.xml.stream.XMLName;
import com.bea.x2002.x09.xbean.config.ConfigDocument;
import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.impl.newstore.pub.Public;
import org.apache.xmlbeans.impl.newstore.pub.store.Locale;
import org.apache.xmlbeans.impl.newstore.pub.store.Cur;
import org.apache.xmlbeans.impl.newstore.pub.store.Backend;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Text;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.NamedNodeMap;

import javax.xml.stream.XMLStreamReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xmlbeans.impl.newstore.CharUtil;
import org.apache.xmlbeans.impl.newstore.CharUtil.CharJoin;

public class EricTest
{
    public static void main ( String[] args ) throws Exception
    {
        Document doc = Public.parse( "<a><b><c/></b><d/><b/></a>", Public.memoryBackend() );

        NodeList elements = doc.getDocumentElement().getElementsByTagName( "*" );

        for ( int i = 0 ; i < elements.getLength() ; i++ )
        {
            org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( elements.item( i ) );
        }


        
        
//        charTest();
        domTest();

//        domTest( Public.getImplementation( Public.memoryBackend() ) );
//        domTest( Public.getImplementation( 0 ) );

//        domTest2( Public.getImplementation( 1 ) );
//        domTest2( Public.getImplementation( 0 ) );

//        parseTest( 1 );
//        parseTest( 0 );
        
        cursorTest( Public.memoryBackend() );
//        cursorTest( 0 );

//        runDrt( Public.getImplementation( 1 ) );
//        runDrt( Public.getImplementation( 0 ) );
    }

    private static void charTest ( )
    {
        Object o = new CharJoin( "abcdef", 2, 2, "123456", 1, 3 );
        CharUtil.getChars( new char [ 100 ], 0, o, 1, 3 );
    }

    private static void cursorTest ( Backend be ) throws Exception
    {
        Document doc =
            Public.parse(
                "<?bonk honk?><a p='q' xmlns:foo='bar'>12&amp;34567<b/><!--moo--></a>", be );

        XmlCursor c = Public.getCursor( doc );

        c.newCursor();

        c.toFirstChild();
        c.toFirstChild();
        c.toFirstChild();
        c.toParent();

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( c );
    }
    
    private static void parseTest ( Backend be ) throws Exception
    {
//        Xcur x = m.load( "<foo a='b'>X<b/>Y<!--hi--></foo>" );
//        Xcur x = m.load( "<ns:foo xmlns:ns='NAMESPACE'>X<b/>Y</ns:foo>" );
        Document doc =
            Public.parse(
                "<?bonk honk?><a p='q' xmlns:foo='bar'>12&amp;34567<b/><!--moo--></a>", be );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        Text t = doc.createTextNode( "Mooo" );

        doc.getDocumentElement().insertBefore( t, doc.getDocumentElement().getFirstChild() );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        doc.getDocumentElement().removeChild( t );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );
        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( t );

        XMLStreamReader xs = Public.getStream( doc );

        for ( ; ; )
        {
            switch ( xs.getEventType() )
            {
                case XMLStreamReader.START_ELEMENT :
                    System.out.println( "START_ELEMENT" );
                    System.out.println( "  name: " + xs.getName() );
                    System.out.println( "  num attrs=" + xs.getAttributeCount() );
                    System.out.println( "  num xmlns=" + xs.getNamespaceCount() );
                    System.out.println( "  name lookup @p=" + xs.getAttributeValue( "", "p" ) );
                    for ( int a = 0 ; a < xs.getAttributeCount() ; a++ )
                        System.out.println( "  @" + xs.getAttributeName( a ) + "=" + xs.getAttributeValue( a ) );
                    for ( int a = 0 ; a < xs.getNamespaceCount() ; a++ )
                        System.out.println( "  #" + xs.getNamespacePrefix( a ) + "=" + xs.getNamespaceURI( a ) );
                    break;

                case XMLStreamReader.ATTRIBUTE :
                    System.out.println( "ATTRIBUTE" );
                    System.out.println( "  num attrs=" + xs.getAttributeCount() );
                    break;

                case XMLStreamReader.NAMESPACE :
                    System.out.println( "NAMESPACE" );
                    break;

                case XMLStreamReader.END_ELEMENT :
                    System.out.println( "END_ELEMENT" );
                    break;

                case XMLStreamReader.CHARACTERS :
                    System.out.println( "CHARACTERS" );
                    System.out.println( "  text: " + xs.getText() );
                    System.out.println( "  chars: " + new String( xs.getTextCharacters(), xs.getTextStart(), xs.getTextLength() ) );
                    int sourceStart = 0;
                    char[] target = new char [ 2 ];
                    for ( ; ; )
                    {
                        int n = xs.getTextCharacters( sourceStart, target, 0, target.length );
                        System.out.println( "  frag: " + sourceStart + "=" + new String( target, 0, n ) );
                        if (n < target.length)
                            break;
                        sourceStart += n;
                    }

                    break;

                case XMLStreamReader.CDATA :
                    System.out.println( "CDATA" );
                    break;

                case XMLStreamReader.COMMENT :
                    System.out.println( "COMMENT" );
                    break;

                case XMLStreamReader.SPACE :
                    System.out.println( "SPACE" );
                    break;

                case XMLStreamReader.START_DOCUMENT :
                    System.out.println( "  num attrs=" + xs.getAttributeCount() );
                    System.out.println( "START_DOCUMENT" );
                    break;

                case XMLStreamReader.END_DOCUMENT :
                    System.out.println( "END_DOCUMENT" );
                    break;

                case XMLStreamReader.PROCESSING_INSTRUCTION :
                    System.out.println( "PROCESSING_INSTRUCTION" );
                    System.out.println( "  target=" + xs.getPITarget() );
                    System.out.println( "  data=" + xs.getPIData() );
                    break;

                case XMLStreamReader.ENTITY_REFERENCE :
                    System.out.println( "ENTITY_REFERENCE" );
                    break;

                case XMLStreamReader.DTD :
                    System.out.println( "DTD" );
                    break;

                default :
                    System.out.println( "!!! Unknown Event type !!!" );
                    break;

            }

            if (!xs.hasNext())
                break;

            xs.next();
        }
    }

    private static void domTest2 ( DOMImplementation impl )
    {
        Document doc = impl.createDocument( "", "foo", null );

        Element docElem = doc.getDocumentElement();

        docElem.appendChild( doc.createTextNode( "xxx" ) );
//        docElem.appendChild( doc.createElement( "e" ) );
//
        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        docElem.setAttributeNS( "", "name", "value" );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );
    }

    private static void domTest ( DOMImplementation impl )
    {
        Document doc = impl.createDocument( "", "Vasilik", null );

        assert doc.getImplementation() == impl;

        Element v = doc.getDocumentElement();

        NamedNodeMap attrs = v.getAttributes();

        Attr a = doc.createAttribute( "x" );

        assert a.getOwnerDocument() == doc;

        a.appendChild( doc.createTextNode( "yyyyyy" ) );

        attrs.setNamedItem( a );

        attrs.item( 0 );

        assert attrs.getNamedItem( "x" ) == a;

        a = doc.createAttribute( "y" );

        v.setAttributeNode( a );

        Element e = doc.createElement( "Eric" );
        Element k = doc.createElement( "Kenneth" );

        v.appendChild( e );
        v.insertBefore( k, e );

        e.appendChild( doc.createTextNode( "" ) );
        e.appendChild( doc.createTextNode( "ABC" ) );
        Text xyz = doc.createTextNode( "XYZ" );
        e.appendChild( xyz );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        e.insertBefore( doc.createElement( "Moo" ), xyz );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        v.removeChild( k );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( k );

        xyz.setNodeValue( "XXXYYYZZZ" );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        xyz.splitText( 3 );

        org.apache.xmlbeans.impl.newstore.pub.store.Cur.dump( doc );

        System.out.println( e.getFirstChild().getNodeValue() );
    }

    private static void runDrt ( DOMImplementation impl )
    {
    }


    private static void domTest ( ) throws Exception
    {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        Document theirDoc = fac.newDocumentBuilder().parse( new File( "c:\\test\\test.xml" ) );

        Element e = theirDoc.getDocumentElement();

        System.out.println( e.getAttributes().item( 0 ).getLocalName() );
        System.out.println( e.getAttributes().item( 0 ).getNamespaceURI() );
        System.out.println( e.getAttributes().item( 0 ).getNodeName() );
        System.out.println( e.getAttributes().item( 0 ).getPrefix() );
        System.out.println( e.getAttributes().item( 0 ).getNodeValue() );
        System.out.println();

        System.out.println( e.getAttributes().item( 1 ).getLocalName() );
        System.out.println( e.getAttributes().item( 1 ).getNamespaceURI() );
        System.out.println( e.getAttributes().item( 1 ).getNodeName() );
        System.out.println( e.getAttributes().item( 1 ).getPrefix() );
        System.out.println( e.getAttributes().item( 1 ).getNodeValue() );
        System.out.println();

        System.out.println( e.getAttributes().item( 2 ).getLocalName() );
        System.out.println( e.getAttributes().item( 2 ).getNamespaceURI() );
        System.out.println( e.getAttributes().item( 2 ).getNodeName() );
        System.out.println( e.getAttributes().item( 2 ).getPrefix() );
        System.out.println( e.getAttributes().item( 2 ).getNodeValue() );
        System.out.println();

        System.out.println( e.getAttributes().item( 3 ).getLocalName() );
        System.out.println( e.getAttributes().item( 3 ).getNamespaceURI() );
        System.out.println( e.getAttributes().item( 3 ).getNodeName() );
        System.out.println( e.getAttributes().item( 3 ).getPrefix() );
        System.out.println( e.getAttributes().item( 3 ).getNodeValue() );
        System.out.println();
    }
}

