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

import javax.xml.namespace.QName;

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

import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public interface Saaj
{
    public interface SaajCallback
    {
        void   setSaajData ( Node n, Object o );
        Object getSaajData ( Node n );

        Element createSoapElement ( QName name, QName parentName );

        Element importSoapElement ( Document doc, Element elem, boolean deep, QName parentName );
    }

    void setCallback ( SaajCallback callback );

    Class identifyElement ( QName name, QName parentName );
    
    void        soapNode_detachNode       ( javax.xml.soap.Node soapNode );
    void        soapNode_recycleNode      ( javax.xml.soap.Node node );
    String      soapNode_getValue         ( javax.xml.soap.Node node );
    void        soapNode_setValue         ( javax.xml.soap.Node node, String value );
    SOAPElement soapNode_getParentElement ( javax.xml.soap.Node node );
    void        soapNode_setParentElement ( javax.xml.soap.Node node, SOAPElement soapElement );

    void        soapElement_removeContents              ( SOAPElement soapElement );
    String      soapElement_getEncodingStyle            ( SOAPElement soapElement );
    void        soapElement_setEncodingStyle            ( SOAPElement soapElement, String encodingStyle );
    boolean     soapElement_removeNamespaceDeclaration  ( SOAPElement soapElement, String prefix );
    Iterator    soapElement_getAllAttributes            ( SOAPElement soapElement );
    Iterator    soapElement_getChildElements            ( SOAPElement parent );
    Iterator    soapElement_getNamespacePrefixes        ( SOAPElement soapElement );
    SOAPElement soapElement_addAttribute                ( SOAPElement soapElement, Name name, String value ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement parent, SOAPElement oldChild ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, Name name ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, String localName ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, String localName, String prefix ) throws SOAPException;
    SOAPElement soapElement_addChildElement             ( SOAPElement soapElement, String localName, String prefix, String uri ) throws SOAPException;
    SOAPElement soapElement_addNamespaceDeclaration     ( SOAPElement soapElement, String prefix, String uri );
    SOAPElement soapElement_addTextNode                 ( SOAPElement soapElement, String data );
    String      soapElement_getAttributeValue           ( SOAPElement soapElement, Name name );
    Iterator    soapElement_getChildElements            ( SOAPElement parent, Name name );
    Name        soapElement_getElementName              ( SOAPElement soapElement );
    String      soapElement_getNamespaceURI             ( SOAPElement soapElement, String prefix );
    Iterator    soapElement_getVisibleNamespacePrefixes ( SOAPElement soapElement );
    boolean     soapElement_removeAttribute             ( SOAPElement soapElement, Name name );

    SOAPBody   soapEnvelope_addBody    ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    SOAPBody   soapEnvelope_getBody    ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    SOAPHeader soapEnvelope_getHeader  ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    SOAPHeader soapEnvelope_addHeader  ( SOAPEnvelope soapEnvelope ) throws SOAPException;
    Name       soapEnvelope_createName ( SOAPEnvelope soapEnvelope, String localName );
    Name       soapEnvelope_createName ( SOAPEnvelope soapEnvelope, String localName, String prefix, String namespaceURI );

    Iterator          soapHeader_examineAllHeaderElements            ( SOAPHeader soapHeader );
    Iterator          soapHeader_extractAllHeaderElements            ( SOAPHeader soapHeader );
    Iterator          soapHeader_examineHeaderElements               ( SOAPHeader soapHeader, String actor );
    Iterator          soapHeader_examineMustUnderstandHeaderElements ( SOAPHeader soapHeader, String mustUnderstandString );
    Iterator          soapHeader_extractHeaderElements               ( SOAPHeader soapHeader, String actor );
    SOAPHeaderElement soapHeader_addHeaderElement                    ( SOAPHeader soapHeader, Name name );

    void         soapPart_removeAllMimeHeaders      ( SOAPPart soapPart );
    void         soapPart_removeMimeHeader          ( SOAPPart soapPart, String name );
    Iterator     soapPart_getAllMimeHeaders         ( SOAPPart soapPart );
    SOAPEnvelope soapPart_getEnvelope               ( SOAPPart soapPart );
    Source       soapPart_getContent                ( SOAPPart soapPart );
    void         soapPart_setContent                ( SOAPPart soapPart, Source source );
    String[]     soapPart_getMimeHeader             ( SOAPPart soapPart, String name );
    void         soapPart_addMimeHeader             ( SOAPPart soapPart, String name, String value );
    void         soapPart_setMimeHeader             ( SOAPPart soapPart, String name, String value );
    Iterator     soapPart_getMatchingMimeHeaders    ( SOAPPart soapPart, String[] names );
    Iterator     soapPart_getNonMatchingMimeHeaders ( SOAPPart soapPart, String[] names );

    boolean         soapBody_hasFault       ( SOAPBody soapBody );
    SOAPFault       soapBody_addFault       ( SOAPBody soapBody ) throws SOAPException;
    SOAPFault       soapBody_getFault       ( SOAPBody soapBody );
    SOAPBodyElement soapBody_addBodyElement ( SOAPBody soapBody, Name name );
    SOAPBodyElement soapBody_addDocument    ( SOAPBody soapBody, Document document );
    SOAPFault       soapBody_addFault       ( SOAPBody soapBody, Name name, String s ) throws SOAPException;
    SOAPFault       soapBody_addFault       ( SOAPBody soapBody, Name faultCode, String faultString, Locale locale ) throws SOAPException;

    Detail   soapFault_addDetail            ( SOAPFault soapFault ) throws SOAPException;
    Detail   soapFault_getDetail            ( SOAPFault soapFault );
    String   soapFault_getFaultActor        ( SOAPFault soapFault );
    String   soapFault_getFaultCode         ( SOAPFault soapFault );
    Name     soapFault_getFaultCodeAsName   ( SOAPFault soapFault );
    String   soapFault_getFaultString       ( SOAPFault soapFault );
    Locale   soapFault_getFaultStringLocale ( SOAPFault soapFault );
    void     soapFault_setFaultActor        ( SOAPFault soapFault, String faultActorString );
    void     soapFault_setFaultCode         ( SOAPFault soapFault, Name faultCodeName ) throws SOAPException;
    void     soapFault_setFaultCode         ( SOAPFault soapFault, String faultCode ) throws SOAPException;
    void     soapFault_setFaultString       ( SOAPFault soapFault, String faultString );
    void     soapFault_setFaultString       ( SOAPFault soapFault, String faultString, Locale locale );

    void    soapHeaderElement_setMustUnderstand ( SOAPHeaderElement soapHeaderElement, boolean mustUnderstand );
    boolean soapHeaderElement_getMustUnderstand ( SOAPHeaderElement soapHeaderElement );
    void    soapHeaderElement_setActor          ( SOAPHeaderElement soapHeaderElement, String actor );
    String  soapHeaderElement_getActor          ( SOAPHeaderElement soapHeaderElement );

    DetailEntry detail_addDetailEntry   ( Detail detail, Name name );
    Iterator    detail_getDetailEntries ( Detail detail );
}