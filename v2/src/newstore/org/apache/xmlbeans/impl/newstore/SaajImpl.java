package org.apache.xmlbeans.impl.newstore;

import org.apache.xmlbeans.impl.newstore.DomImpl.Dom;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.newstore.xcur.Master;
import org.apache.xmlbeans.impl.newstore.xcur.Xcur;

import java.util.Iterator;
import java.util.Locale;

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
        Master m = d.master();

        try
        {
            if (m.noSync())         { m.enter(); impl_saajCallback_setSaajData( d, o ); }
            else synchronized ( m ) { m.enter(); impl_saajCallback_setSaajData( d, o ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void impl_saajCallback_setSaajData ( Dom d, Object o )
    {
        Master m = d.master();

        Xcur x = m.tempCur();

        x.moveToDom( d );

        SaajData sd = null;

        if (o != null)
        {
            sd = (SaajData) x.getBookmark( SaajData.class );

            if (sd == null)
                sd = new SaajData();

            sd._obj = o;
        }
        
        x.setBookmark( SaajData.class, sd );
    }

    public static Object saajCallback_getSaajData ( Dom d )
    {
        Master m = d.master();

        try
        {
            if (m.noSync())         { m.enter(); return impl_saajCallback_getSaajData( d ); }
            else synchronized ( m ) { m.enter(); return impl_saajCallback_getSaajData( d ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Object impl_saajCallback_getSaajData ( Dom d )
    {
        Master m = d.master();

        Xcur x = m.tempCur();

        x.moveToDom( d );

        SaajData sd = (SaajData) x.getBookmark( SaajData.class );

        return sd == null ? null : sd._obj;
    }

    public static Element saajCallback_createSoapElement ( Dom d, QName name, QName parentName )
    {
        Master m = d.master();

        Dom e;

        try
        {
            if (m.noSync())         { m.enter(); e = impl_saajCallback_createSoapElement( d, name, parentName ); }
            else synchronized ( m ) { m.enter(); e = impl_saajCallback_createSoapElement( d, name, parentName ); }
        }
        finally
        {
            m.exit();
        }

        return (Element) e;
    }
    
    public static Dom impl_saajCallback_createSoapElement ( Dom d, QName name, QName parentName )
    {
        Xcur x = d.master().tempCur();
        
        x.createElement( name, parentName );
        
        Dom e = x.getDom();
        
        x.release();
        
        return e;
    }
        
    public static Element saajCallback_importSoapElement (
        Dom d, Element elem, boolean deep, QName parentName )
    {
        Master m = d.master();

        Dom e;

        try
        {
            if (m.noSync())         { m.enter(); e = impl_saajCallback_importSoapElement( d, elem, deep, parentName ); }
            else synchronized ( m ) { m.enter(); e = impl_saajCallback_importSoapElement( d, elem, deep, parentName ); }
        }
        finally
        {
            m.exit();
        }

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
        Master m = d.master();

        try
        {
            if (m.noSync())         { m.enter(); return impl_saajCallback_ensureSoapTextNode( d ); }
            else synchronized ( m ) { m.enter(); return impl_saajCallback_ensureSoapTextNode( d ); }
        }
        finally
        {
            m.exit();
        }
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
        Master m = n.master();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapNode_detachNode( node ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapNode_detachNode( node ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void _soapNode_recycleNode ( Dom n )
    {
        Master m = n.master();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapNode_recycleNode( node ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapNode_recycleNode( node ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String _soapNode_getValue ( Dom n )
    {
        Master m = n.master();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapNode_getValue( node ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapNode_getValue( node ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static void _soapNode_setValue ( Dom n, String value )
    {
        Master m = n.master();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapNode_setValue( node, value ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapNode_setValue( node, value ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static SOAPElement _soapNode_getParentElement ( Dom n )
    {
        Master m = n.master();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapNode_getParentElement( node ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapNode_getParentElement( node ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static void _soapNode_setParentElement ( Dom n, SOAPElement p )
    {
        Master m = n.master();

        javax.xml.soap.Node node = (javax.xml.soap.Node) n;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapNode_setParentElement( node, p ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapNode_setParentElement( node, p ); }
        }
        finally
        {
            m.exit();
        }
    }

    //
    // Soap Element
    //

    public static void _soapElement_removeContents ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapElement_removeContents( se ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapElement_removeContents( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String _soapElement_getEncodingStyle ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getEncodingStyle( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getEncodingStyle( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void _soapElement_setEncodingStyle ( Dom d, String encodingStyle )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapElement_setEncodingStyle( se, encodingStyle ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapElement_setEncodingStyle( se, encodingStyle ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static boolean _soapElement_removeNamespaceDeclaration ( Dom d, String prefix )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_removeNamespaceDeclaration( se, prefix ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_removeNamespaceDeclaration( se, prefix ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator _soapElement_getAllAttributes ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getAllAttributes( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getAllAttributes( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator _soapElement_getChildElements ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getChildElements( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getChildElements( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator _soapElement_getNamespacePrefixes ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getNamespacePrefixes( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getNamespacePrefixes( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addAttribute ( Dom d, Name name, String value ) throws SOAPException
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addAttribute( se, name, value ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addAttribute( se, name, value ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, SOAPElement oldChild ) throws SOAPException
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addChildElement( se, oldChild ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addChildElement( se, oldChild ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, Name name ) throws SOAPException
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addChildElement( se, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addChildElement( se, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, String localName ) throws SOAPException
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addChildElement( se, localName ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addChildElement( se, localName ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, String localName, String prefix ) throws SOAPException
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addChildElement( se, localName, prefix ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addChildElement( se, localName, prefix ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addChildElement ( Dom d, String localName, String prefix, String uri ) throws SOAPException
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addChildElement( se, localName, prefix, uri ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addChildElement( se, localName, prefix, uri ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addNamespaceDeclaration ( Dom d, String prefix, String uri )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addNamespaceDeclaration( se, prefix, uri ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addNamespaceDeclaration( se, prefix, uri ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPElement _soapElement_addTextNode ( Dom d, String data )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_addTextNode( se, data ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_addTextNode( se, data ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String _soapElement_getAttributeValue ( Dom d, Name name )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getAttributeValue( se, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getAttributeValue( se, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator _soapElement_getChildElements ( Dom d, Name name )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getChildElements( se, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getChildElements( se, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Name _soapElement_getElementName ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getElementName( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getElementName( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String _soapElement_getNamespaceURI ( Dom d, String prefix )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getNamespaceURI( se, prefix ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getNamespaceURI( se, prefix ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator _soapElement_getVisibleNamespacePrefixes ( Dom d )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_getVisibleNamespacePrefixes( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_getVisibleNamespacePrefixes( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static boolean _soapElement_removeAttribute ( Dom d, Name name )
    {
        Master m = d.master();

        SOAPElement se = (SOAPElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapElement_removeAttribute( se, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapElement_removeAttribute( se, name ); }
        }
        finally
        {
            m.exit();
        }
    }

    //
    // Soap Envelope
    //

    public static SOAPBody _soapEnvelope_addBody ( Dom d ) throws SOAPException
    {
        Master m = d.master();

        SOAPEnvelope se = (SOAPEnvelope) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapEnvelope_addBody( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapEnvelope_addBody( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPBody _soapEnvelope_getBody ( Dom d ) throws SOAPException
    {
        Master m = d.master();

        SOAPEnvelope se = (SOAPEnvelope) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapEnvelope_getBody( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapEnvelope_getBody( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPHeader _soapEnvelope_getHeader ( Dom d ) throws SOAPException
    {
        Master m = d.master();

        SOAPEnvelope se = (SOAPEnvelope) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapEnvelope_getHeader( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapEnvelope_getHeader( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPHeader _soapEnvelope_addHeader ( Dom d ) throws SOAPException
    {
        Master m = d.master();

        SOAPEnvelope se = (SOAPEnvelope) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapEnvelope_addHeader( se ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapEnvelope_addHeader( se ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Name _soapEnvelope_createName ( Dom d, String localName )
    {
        Master m = d.master();

        SOAPEnvelope se = (SOAPEnvelope) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapEnvelope_createName( se, localName ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapEnvelope_createName( se, localName ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Name _soapEnvelope_createName ( Dom d, String localName, String prefix, String namespaceURI )
    {
        Master m = d.master();

        SOAPEnvelope se = (SOAPEnvelope) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapEnvelope_createName( se, localName, prefix, namespaceURI ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapEnvelope_createName( se, localName, prefix, namespaceURI ); }
        }
        finally
        {
            m.exit();
        }
    }

    //
    // Soap Header
    //

    public static Iterator soapHeader_examineAllHeaderElements ( Dom d )
    {
        Master m = d.master();

        SOAPHeader sh = (SOAPHeader) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeader_examineAllHeaderElements( sh ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeader_examineAllHeaderElements( sh ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static Iterator soapHeader_extractAllHeaderElements ( Dom d )
    {
        Master m = d.master();

        SOAPHeader sh = (SOAPHeader) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeader_extractAllHeaderElements( sh ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeader_extractAllHeaderElements( sh ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static Iterator soapHeader_examineHeaderElements ( Dom d, String actor )
    {
        Master m = d.master();

        SOAPHeader sh = (SOAPHeader) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeader_examineHeaderElements( sh, actor ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeader_examineHeaderElements( sh, actor ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static Iterator soapHeader_examineMustUnderstandHeaderElements ( Dom d, String mustUnderstandString )
    {
        Master m = d.master();

        SOAPHeader sh = (SOAPHeader) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeader_examineMustUnderstandHeaderElements( sh, mustUnderstandString ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeader_examineMustUnderstandHeaderElements( sh, mustUnderstandString ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static Iterator soapHeader_extractHeaderElements ( Dom d, String actor )
    {
        Master m = d.master();

        SOAPHeader sh = (SOAPHeader) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeader_extractHeaderElements( sh, actor ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeader_extractHeaderElements( sh, actor ); }
        }
        finally
        {
            m.exit();
        }
    }

    public static SOAPHeaderElement soapHeader_addHeaderElement ( Dom d, Name name )
    {
        Master m = d.master();

        SOAPHeader sh = (SOAPHeader) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeader_addHeaderElement( sh, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeader_addHeaderElement( sh, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    //
    // Soap Body
    //

    public static boolean soapBody_hasFault ( Dom d )
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_hasFault( sb ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_hasFault( sb ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPFault soapBody_addFault ( Dom d ) throws SOAPException
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_addFault( sb ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_addFault( sb ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPFault soapBody_getFault ( Dom d )
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_getFault( sb ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_getFault( sb ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPBodyElement soapBody_addBodyElement ( Dom d, Name name )
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_addBodyElement( sb, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_addBodyElement( sb, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPBodyElement soapBody_addDocument ( Dom d, Document document )
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_addDocument( sb, document ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_addDocument( sb, document ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPFault soapBody_addFault ( Dom d, Name name, String s ) throws SOAPException
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_addFault( sb, name, s ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_addFault( sb, name, s ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPFault soapBody_addFault ( Dom d, Name faultCode, String faultString, Locale locale ) throws SOAPException
    {
        Master m = d.master();

        SOAPBody sb = (SOAPBody) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapBody_addFault( sb, faultCode, faultString, locale ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapBody_addFault( sb, faultCode, faultString, locale ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    //
    // Soap Fault
    //

    public static void soapFault_setFaultString ( Dom d, String faultString )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapFault_setFaultString( sf, faultString ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapFault_setFaultString( sf, faultString ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapFault_setFaultString ( Dom d, String faultString, Locale locale )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapFault_setFaultString( sf, faultString, locale ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapFault_setFaultString( sf, faultString, locale ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapFault_setFaultCode ( Dom d, Name faultCodeName ) throws SOAPException
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapFault_setFaultCode( sf, faultCodeName ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapFault_setFaultCode( sf, faultCodeName ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapFault_setFaultActor ( Dom d, String faultActorString )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapFault_setFaultActor( sf, faultActorString ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapFault_setFaultActor( sf, faultActorString ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String soapFault_getFaultActor ( Dom d )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_getFaultActor( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_getFaultActor( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String soapFault_getFaultCode ( Dom d )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_getFaultCode( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_getFaultCode( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapFault_setFaultCode ( Dom d, String faultCode ) throws SOAPException
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapFault_setFaultCode( sf, faultCode ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapFault_setFaultCode( sf, faultCode ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Locale soapFault_getFaultStringLocale ( Dom d )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_getFaultStringLocale( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_getFaultStringLocale( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Name soapFault_getFaultCodeAsName ( Dom d )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_getFaultCodeAsName( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_getFaultCodeAsName( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String soapFault_getFaultString ( Dom d )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_getFaultString( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_getFaultString( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Detail soapFault_addDetail ( Dom d ) throws SOAPException
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_addDetail( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_addDetail( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Detail soapFault_getDetail ( Dom d )
    {
        Master m = d.master();

        SOAPFault sf = (SOAPFault) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapFault_getDetail( sf ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapFault_getDetail( sf ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    //
    // Soap Header Element
    //

    public static void soapHeaderElement_setMustUnderstand ( Dom d, boolean mustUnderstand )
    {
        Master m = d.master();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapHeaderElement_setMustUnderstand( she, mustUnderstand ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapHeaderElement_setMustUnderstand( she, mustUnderstand ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static boolean soapHeaderElement_getMustUnderstand ( Dom d )
    {
        Master m = d.master();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeaderElement_getMustUnderstand( she ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeaderElement_getMustUnderstand( she ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapHeaderElement_setActor ( Dom d, String actor )
    {
        Master m = d.master();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapHeaderElement_setActor( she, actor ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapHeaderElement_setActor( she, actor ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String soapHeaderElement_getActor ( Dom d )
    {
        Master m = d.master();

        SOAPHeaderElement she = (SOAPHeaderElement) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapHeaderElement_getActor( she ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapHeaderElement_getActor( she ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    //
    // Soap Header Element
    //

    public static DetailEntry detail_addDetailEntry ( Dom d, Name name )
    {
        Master m = d.master();

        Detail detail = (Detail) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.detail_addDetailEntry( detail, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.detail_addDetailEntry( detail, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator detail_getDetailEntries ( Dom d )
    {
        Master m = d.master();

        Detail detail = (Detail) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.detail_getDetailEntries( detail ); }
            else synchronized ( m ) { m.enter(); return m._saaj.detail_getDetailEntries( detail ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    //
    // Soap Header Element
    //

    public static void soapPart_removeAllMimeHeaders ( Dom d )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapPart_removeAllMimeHeaders( sp ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapPart_removeAllMimeHeaders( sp ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapPart_removeMimeHeader ( Dom d, String name )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapPart_removeMimeHeader( sp, name ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapPart_removeMimeHeader( sp, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator soapPart_getAllMimeHeaders ( Dom d )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapPart_getAllMimeHeaders( sp ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapPart_getAllMimeHeaders( sp ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static SOAPEnvelope soapPart_getEnvelope ( Dom d )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapPart_getEnvelope( sp ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapPart_getEnvelope( sp ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Source soapPart_getContent ( Dom d )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapPart_getContent( sp ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapPart_getContent( sp ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapPart_setContent ( Dom d, Source source )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapPart_setContent( sp, source ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapPart_setContent( sp, source ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static String[] soapPart_getMimeHeader ( Dom d, String name )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapPart_getMimeHeader( sp, name ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapPart_getMimeHeader( sp, name ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapPart_addMimeHeader ( Dom d, String name, String value )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapPart_addMimeHeader( sp, name, value ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapPart_addMimeHeader( sp, name, value ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void soapPart_setMimeHeader ( Dom d, String name, String value )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); m._saaj.soapPart_setMimeHeader( sp, name, value ); }
            else synchronized ( m ) { m.enter(); m._saaj.soapPart_setMimeHeader( sp, name, value ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator soapPart_getMatchingMimeHeaders ( Dom d, String[] names )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapPart_getMatchingMimeHeaders( sp, names ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapPart_getMatchingMimeHeaders( sp, names ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static Iterator soapPart_getNonMatchingMimeHeaders ( Dom d, String[] names )
    {
        Master m = d.master();

        SOAPPart sp = (SOAPPart) d;

        try
        {
            if (m.noSync())         { m.enter(); return m._saaj.soapPart_getNonMatchingMimeHeaders( sp, names ); }
            else synchronized ( m ) { m.enter(); return m._saaj.soapPart_getNonMatchingMimeHeaders( sp, names ); }
        }
        finally
        {
            m.exit();
        }
    }
}