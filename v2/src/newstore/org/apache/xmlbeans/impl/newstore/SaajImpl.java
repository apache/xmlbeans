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

package org.apache.xmlbeans.impl.newstore;

import org.apache.xmlbeans.impl.newstore.pub.store.Dom;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.newstore.pub.store.Locale;
import org.apache.xmlbeans.impl.newstore.pub.store.Cur;

import java.util.Iterator;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;

import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class SaajImpl
{
    private static class SaajData
    {
        Object _obj;
    }

    // TODO - I don't think these methods should need gateways because they are
    // call backs from the Saaj interface which is gatewayed already ....
    
    public static void saajCallback_setSaajData ( Dom d, Object o )
    {
        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { impl_saajCallback_setSaajData( d, o ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { impl_saajCallback_setSaajData( d, o ); } finally { l.exit(); } }
    }
    
    public static void impl_saajCallback_setSaajData ( Dom d, Object o )
    {
        Locale l = d.locale();

        Cur c = l.tempCur();

        c.moveToDom( d );

        SaajData sd = null;

        if (o != null)
        {
            sd = (SaajData) c.getBookmark( SaajData.class );

            if (sd == null)
                sd = new SaajData();

            sd._obj = o;
        }
        
        c.setBookmark( SaajData.class, sd );

        c.release();
    }

    public static Object saajCallback_getSaajData ( Dom d )
    {
        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return impl_saajCallback_getSaajData( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return impl_saajCallback_getSaajData( d ); } finally { l.exit(); } }
    }
    
    public static Object impl_saajCallback_getSaajData ( Dom d )
    {
        Locale l = d.locale();

        Cur c = l.tempCur();

        c.moveToDom( d );

        SaajData sd = (SaajData) c.getBookmark( SaajData.class );

        Object o = sd == null ? null : sd._obj;

        c.release();

        return o;
    }

    public static Element saajCallback_createSoapElement ( Dom d, QName name, QName parentName )
    {
        Locale l = d.locale();

        Dom e;

        if (l.noSync())         { l.enter(); try { e = impl_saajCallback_createSoapElement( d, name, parentName ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { e = impl_saajCallback_createSoapElement( d, name, parentName ); } finally { l.exit(); } }

        return (Element) e;
    }
    
    public static Dom impl_saajCallback_createSoapElement ( Dom d, QName name, QName parentName )
    {
        Cur c = d.locale().tempCur();
        
        c.createElement( name, parentName );
        
        Dom e = c.getDom();
        
        c.release();
        
        return e;
    }
        
    public static Element saajCallback_importSoapElement (
        Dom d, Element elem, boolean deep, QName parentName )
    {
        Locale l = d.locale();

        Dom e;

        if (l.noSync())         { l.enter(); try { e = impl_saajCallback_importSoapElement( d, elem, deep, parentName ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { e = impl_saajCallback_importSoapElement( d, elem, deep, parentName ); } finally { l.exit(); } }

        return (Element) e;
    }
    
    public static Dom impl_saajCallback_importSoapElement (
        Dom d, Element elem, boolean deep, QName parentName )
    {
        // TODO -- need to rewrite DomImpl.document_importNode to use an Xcur
        // to create the new tree.  Then, I can pass the parentName to the new
        // fcn and use it to create the correct root parent
        
        throw new RuntimeException( "Not impl" );
    }

    
    public static Text saajCallback_ensureSoapTextNode ( Dom d )
    {
        Locale l = d.locale();

        if (l.noSync())         { l.enter(); try { return impl_saajCallback_ensureSoapTextNode( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return impl_saajCallback_ensureSoapTextNode( d ); } finally { l.exit(); } }
    }
    
    public static Text impl_saajCallback_ensureSoapTextNode ( Dom d )
    {
//        if (!(d instanceof Text))
//        {
//            Xcur x = d.tempCur();
//
//            x.moveTo
//
//            x.release();
//        }
//        
//        return (Text) d;

        return null;
    }
    
    //
    // Soap Node
    //
    
    public static void _soapNode_detachNode ( Dom n )
    {
        Locale l = n.locale();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        if (l.noSync())         { l.enter(); try { l._saaj.soapNode_detachNode( node ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapNode_detachNode( node ); } finally { l.exit(); } }
    }
    
    public static void _soapNode_recycleNode ( Dom n )
    {
        Locale l = n.locale();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        if (l.noSync())         { l.enter(); try { l._saaj.soapNode_recycleNode( node ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapNode_recycleNode( node ); } finally { l.exit(); } }
    }
    
    public static String _soapNode_getValue ( Dom n )
    {
        Locale l = n.locale();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapNode_getValue( node ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapNode_getValue( node ); } finally { l.exit(); } }
    }

    public static void _soapNode_setValue ( Dom n, String value )
    {
        Locale l = n.locale();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        if (l.noSync())         { l.enter(); try { l._saaj.soapNode_setValue( node, value ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapNode_setValue( node, value ); } finally { l.exit(); } }
    }

    public static SOAPElement _soapNode_getParentElement ( Dom n )
    {
        Locale l = n.locale();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapNode_getParentElement( node ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapNode_getParentElement( node ); } finally { l.exit(); } }
    }

    public static void _soapNode_setParentElement ( Dom n, SOAPElement p )
    {
        Locale l = n.locale();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        if (l.noSync())         { l.enter(); try { l._saaj.soapNode_setParentElement( node, p ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapNode_setParentElement( node, p ); } finally { l.exit(); } }
    }

    //
    // Soap Element
    //

    public static void _soapElement_removeContents ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapElement_removeContents( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapElement_removeContents( se ); } finally { l.exit(); } }
    }
    
    public static String _soapElement_getEncodingStyle ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getEncodingStyle( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getEncodingStyle( se ); } finally { l.exit(); } }
    }
    
    public static void _soapElement_setEncodingStyle ( Dom d, String encodingStyle )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapElement_setEncodingStyle( se, encodingStyle ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapElement_setEncodingStyle( se, encodingStyle ); } finally { l.exit(); } }
    }
    
    public static boolean _soapElement_removeNamespaceDeclaration ( Dom d, String prefix )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_removeNamespaceDeclaration( se, prefix ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_removeNamespaceDeclaration( se, prefix ); } finally { l.exit(); } }
    }
    
    public static Iterator _soapElement_getAllAttributes ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getAllAttributes( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getAllAttributes( se ); } finally { l.exit(); } }
    }
    
    public static Iterator _soapElement_getChildElements ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getChildElements( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getChildElements( se ); } finally { l.exit(); } }
    }
    
    public static Iterator _soapElement_getNamespacePrefixes ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getNamespacePrefixes( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getNamespacePrefixes( se ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addAttribute ( Dom d, Name name, String value ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addAttribute( se, name, value ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addAttribute( se, name, value ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, SOAPElement oldChild ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addChildElement( se, oldChild ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addChildElement( se, oldChild ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, Name name ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addChildElement( se, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addChildElement( se, name ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, String localName ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addChildElement( se, localName ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addChildElement( se, localName ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, String localName, String prefix ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addChildElement( se, localName, prefix ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addChildElement( se, localName, prefix ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, String localName, String prefix, String uri ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addChildElement( se, localName, prefix, uri ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addChildElement( se, localName, prefix, uri ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addNamespaceDeclaration ( Dom d, String prefix, String uri )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addNamespaceDeclaration( se, prefix, uri ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addNamespaceDeclaration( se, prefix, uri ); } finally { l.exit(); } }
    }
    
    public static SOAPElement _soapElement_addTextNode ( Dom d, String data )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_addTextNode( se, data ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_addTextNode( se, data ); } finally { l.exit(); } }
    }
    
    public static String _soapElement_getAttributeValue ( Dom d, Name name )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getAttributeValue( se, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getAttributeValue( se, name ); } finally { l.exit(); } }
    }
    
    public static Iterator _soapElement_getChildElements ( Dom d, Name name )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getChildElements( se, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getChildElements( se, name ); } finally { l.exit(); } }
    }
    
    public static Name _soapElement_getElementName ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getElementName( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getElementName( se ); } finally { l.exit(); } }
    }
    
    public static String _soapElement_getNamespaceURI ( Dom d, String prefix )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getNamespaceURI( se, prefix ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getNamespaceURI( se, prefix ); } finally { l.exit(); } }
    }
    
    public static Iterator _soapElement_getVisibleNamespacePrefixes ( Dom d )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_getVisibleNamespacePrefixes( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_getVisibleNamespacePrefixes( se ); } finally { l.exit(); } }
    }
    
    public static boolean _soapElement_removeAttribute ( Dom d, Name name )
    {
        Locale l = d.locale();

        SOAPElement se = (SOAPElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapElement_removeAttribute( se, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapElement_removeAttribute( se, name ); } finally { l.exit(); } }
    }

    //
    // Soap Envelope
    //

    public static SOAPBody _soapEnvelope_addBody ( Dom d ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPEnvelope se = (SOAPEnvelope) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapEnvelope_addBody( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapEnvelope_addBody( se ); } finally { l.exit(); } }
    }
    
    public static SOAPBody _soapEnvelope_getBody ( Dom d ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPEnvelope se = (SOAPEnvelope) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapEnvelope_getBody( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapEnvelope_getBody( se ); } finally { l.exit(); } }
    }
    
    public static SOAPHeader _soapEnvelope_getHeader ( Dom d ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPEnvelope se = (SOAPEnvelope) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapEnvelope_getHeader( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapEnvelope_getHeader( se ); } finally { l.exit(); } }
    }
    
    public static SOAPHeader _soapEnvelope_addHeader ( Dom d ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPEnvelope se = (SOAPEnvelope) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapEnvelope_addHeader( se ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapEnvelope_addHeader( se ); } finally { l.exit(); } }
    }
    
    public static Name _soapEnvelope_createName ( Dom d, String localName )
    {
        Locale l = d.locale();

        SOAPEnvelope se = (SOAPEnvelope) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapEnvelope_createName( se, localName ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapEnvelope_createName( se, localName ); } finally { l.exit(); } }
    }
    
    public static Name _soapEnvelope_createName ( Dom d, String localName, String prefix, String namespaceURI )
    {
        Locale l = d.locale();

        SOAPEnvelope se = (SOAPEnvelope) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapEnvelope_createName( se, localName, prefix, namespaceURI ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapEnvelope_createName( se, localName, prefix, namespaceURI ); } finally { l.exit(); } }
    }

    //
    // Soap Header
    //

    public static Iterator soapHeader_examineAllHeaderElements ( Dom d )
    {
        Locale l = d.locale();

        SOAPHeader sh = (SOAPHeader) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeader_examineAllHeaderElements( sh ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeader_examineAllHeaderElements( sh ); } finally { l.exit(); } }
    }

    public static Iterator soapHeader_extractAllHeaderElements ( Dom d )
    {
        Locale l = d.locale();

        SOAPHeader sh = (SOAPHeader) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeader_extractAllHeaderElements( sh ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeader_extractAllHeaderElements( sh ); } finally { l.exit(); } }
    }

    public static Iterator soapHeader_examineHeaderElements ( Dom d, String actor )
    {
        Locale l = d.locale();

        SOAPHeader sh = (SOAPHeader) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeader_examineHeaderElements( sh, actor ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeader_examineHeaderElements( sh, actor ); } finally { l.exit(); } }
    }

    public static Iterator soapHeader_examineMustUnderstandHeaderElements ( Dom d, String mustUnderstandString )
    {
        Locale l = d.locale();

        SOAPHeader sh = (SOAPHeader) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeader_examineMustUnderstandHeaderElements( sh, mustUnderstandString ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeader_examineMustUnderstandHeaderElements( sh, mustUnderstandString ); } finally { l.exit(); } }
    }

    public static Iterator soapHeader_extractHeaderElements ( Dom d, String actor )
    {
        Locale l = d.locale();

        SOAPHeader sh = (SOAPHeader) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeader_extractHeaderElements( sh, actor ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeader_extractHeaderElements( sh, actor ); } finally { l.exit(); } }
    }

    public static SOAPHeaderElement soapHeader_addHeaderElement ( Dom d, Name name )
    {
        Locale l = d.locale();

        SOAPHeader sh = (SOAPHeader) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeader_addHeaderElement( sh, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeader_addHeaderElement( sh, name ); } finally { l.exit(); } }
    }
    
    //
    // Soap Body
    //

    public static boolean soapBody_hasFault ( Dom d )
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_hasFault( sb ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_hasFault( sb ); } finally { l.exit(); } }
    }
    
    public static SOAPFault soapBody_addFault ( Dom d ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_addFault( sb ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_addFault( sb ); } finally { l.exit(); } }
    }
    
    public static SOAPFault soapBody_getFault ( Dom d )
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_getFault( sb ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_getFault( sb ); } finally { l.exit(); } }
    }
    
    public static SOAPBodyElement soapBody_addBodyElement ( Dom d, Name name )
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_addBodyElement( sb, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_addBodyElement( sb, name ); } finally { l.exit(); } }
    }
    
    public static SOAPBodyElement soapBody_addDocument ( Dom d, Document document )
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_addDocument( sb, document ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_addDocument( sb, document ); } finally { l.exit(); } }
    }
    
    public static SOAPFault soapBody_addFault ( Dom d, Name name, String s ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_addFault( sb, name, s ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_addFault( sb, name, s ); } finally { l.exit(); } }
    }
    
    public static SOAPFault soapBody_addFault ( Dom d, Name faultCode, String faultString, java.util.Locale locale ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPBody sb = (SOAPBody) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapBody_addFault( sb, faultCode, faultString, locale ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapBody_addFault( sb, faultCode, faultString, locale ); } finally { l.exit(); } }
    }
    
    //
    // Soap Fault
    //

    public static void soapFault_setFaultString ( Dom d, String faultString )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapFault_setFaultString( sf, faultString ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapFault_setFaultString( sf, faultString ); } finally { l.exit(); } }
    }
    
    public static void soapFault_setFaultString ( Dom d, String faultString, java.util.Locale locale )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapFault_setFaultString( sf, faultString, locale ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapFault_setFaultString( sf, faultString, locale ); } finally { l.exit(); } }
    }
    
    public static void soapFault_setFaultCode ( Dom d, Name faultCodeName ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapFault_setFaultCode( sf, faultCodeName ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapFault_setFaultCode( sf, faultCodeName ); } finally { l.exit(); } }
    }
    
    public static void soapFault_setFaultActor ( Dom d, String faultActorString )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapFault_setFaultActor( sf, faultActorString ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapFault_setFaultActor( sf, faultActorString ); } finally { l.exit(); } }
    }
    
    public static String soapFault_getFaultActor ( Dom d )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_getFaultActor( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_getFaultActor( sf ); } finally { l.exit(); } }
    }
    
    public static String soapFault_getFaultCode ( Dom d )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_getFaultCode( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_getFaultCode( sf ); } finally { l.exit(); } }
    }
    
    public static void soapFault_setFaultCode ( Dom d, String faultCode ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapFault_setFaultCode( sf, faultCode ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapFault_setFaultCode( sf, faultCode ); } finally { l.exit(); } }
    }
    
    public static java.util.Locale soapFault_getFaultStringLocale ( Dom d )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_getFaultStringLocale( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_getFaultStringLocale( sf ); } finally { l.exit(); } }
    }
    
    public static Name soapFault_getFaultCodeAsName ( Dom d )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_getFaultCodeAsName( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_getFaultCodeAsName( sf ); } finally { l.exit(); } }
    }
    
    public static String soapFault_getFaultString ( Dom d )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_getFaultString( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_getFaultString( sf ); } finally { l.exit(); } }
    }
    
    public static Detail soapFault_addDetail ( Dom d ) throws SOAPException
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_addDetail( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_addDetail( sf ); } finally { l.exit(); } }
    }
    
    public static Detail soapFault_getDetail ( Dom d )
    {
        Locale l = d.locale();

        SOAPFault sf = (SOAPFault) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapFault_getDetail( sf ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapFault_getDetail( sf ); } finally { l.exit(); } }
    }
    
    //
    // Soap Header Element
    //

    public static void soapHeaderElement_setMustUnderstand ( Dom d, boolean mustUnderstand )
    {
        Locale l = d.locale();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapHeaderElement_setMustUnderstand( she, mustUnderstand ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapHeaderElement_setMustUnderstand( she, mustUnderstand ); } finally { l.exit(); } }
    }
    
    public static boolean soapHeaderElement_getMustUnderstand ( Dom d )
    {
        Locale l = d.locale();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeaderElement_getMustUnderstand( she ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeaderElement_getMustUnderstand( she ); } finally { l.exit(); } }
    }
    
    public static void soapHeaderElement_setActor ( Dom d, String actor )
    {
        Locale l = d.locale();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapHeaderElement_setActor( she, actor ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapHeaderElement_setActor( she, actor ); } finally { l.exit(); } }
    }
    
    public static String soapHeaderElement_getActor ( Dom d )
    {
        Locale l = d.locale();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapHeaderElement_getActor( she ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapHeaderElement_getActor( she ); } finally { l.exit(); } }
    }
    
    //
    // Soap Header Element
    //

    public static DetailEntry detail_addDetailEntry ( Dom d, Name name )
    {
        Locale l = d.locale();

        Detail detail = (Detail) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.detail_addDetailEntry( detail, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.detail_addDetailEntry( detail, name ); } finally { l.exit(); } }
    }
    
    public static Iterator detail_getDetailEntries ( Dom d )
    {
        Locale l = d.locale();

        Detail detail = (Detail) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.detail_getDetailEntries( detail ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.detail_getDetailEntries( detail ); } finally { l.exit(); } }
    }
    
    //
    // Soap Header Element
    //

    public static void soapPart_removeAllMimeHeaders ( Dom d )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapPart_removeAllMimeHeaders( sp ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapPart_removeAllMimeHeaders( sp ); } finally { l.exit(); } }
    }
    
    public static void soapPart_removeMimeHeader ( Dom d, String name )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapPart_removeMimeHeader( sp, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapPart_removeMimeHeader( sp, name ); } finally { l.exit(); } }
    }
    
    public static Iterator soapPart_getAllMimeHeaders ( Dom d )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapPart_getAllMimeHeaders( sp ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapPart_getAllMimeHeaders( sp ); } finally { l.exit(); } }
    }
    
    public static SOAPEnvelope soapPart_getEnvelope ( Dom d )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapPart_getEnvelope( sp ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapPart_getEnvelope( sp ); } finally { l.exit(); } }
    }
    
    public static Source soapPart_getContent ( Dom d )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapPart_getContent( sp ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapPart_getContent( sp ); } finally { l.exit(); } }
    }
    
    public static void soapPart_setContent ( Dom d, Source source )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapPart_setContent( sp, source ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapPart_setContent( sp, source ); } finally { l.exit(); } }
    }
    
    public static String[] soapPart_getMimeHeader ( Dom d, String name )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapPart_getMimeHeader( sp, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapPart_getMimeHeader( sp, name ); } finally { l.exit(); } }
    }
    
    public static void soapPart_addMimeHeader ( Dom d, String name, String value )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapPart_addMimeHeader( sp, name, value ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapPart_addMimeHeader( sp, name, value ); } finally { l.exit(); } }
    }
    
    public static void soapPart_setMimeHeader ( Dom d, String name, String value )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { l._saaj.soapPart_setMimeHeader( sp, name, value ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { l._saaj.soapPart_setMimeHeader( sp, name, value ); } finally { l.exit(); } }
    }
    
    public static Iterator soapPart_getMatchingMimeHeaders ( Dom d, String[] names )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapPart_getMatchingMimeHeaders( sp, names ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapPart_getMatchingMimeHeaders( sp, names ); } finally { l.exit(); } }
    }
    
    public static Iterator soapPart_getNonMatchingMimeHeaders ( Dom d, String[] names )
    {
        Locale l = d.locale();

        SOAPPart sp = (SOAPPart) d;

        if (l.noSync())         { l.enter(); try { return l._saaj.soapPart_getNonMatchingMimeHeaders( sp, names ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return l._saaj.soapPart_getNonMatchingMimeHeaders( sp, names ); } finally { l.exit(); } }
    }
}