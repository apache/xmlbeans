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

package org.apache.xmlbeans;

import javax.xml.stream.XMLStreamReader;


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Simple_Type_Definition">xs:anySimpleType</a> type.
 * <p>
 * All simple types are convertible to {@link String}. 
 */ 
public interface XmlAnySimpleType extends XmlObject
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_anySimpleType");
    
    /**
     * Returns the value as a {@link String}
     * @deprecated replaced by {@link #getStringValue}
     **/
    String stringValue();
    /**
     * Sets the value as a {@link String}
     * @deprecated replaced by {@link #setStringValue}
     **/
    void set(String s);

    /**
     * Returns the value as a {@link String}
     **/
    String getStringValue();

    /**
     * Sets the value as a {@link String}
     **/
    void setStringValue(String s);

    /**
     * A class with methods for creating instances
     * of {@link XmlAnySimpleType}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlAnySimpleType} */
        public static XmlAnySimpleType newInstance() {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlAnySimpleType} */
        public static XmlAnySimpleType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlAnySimpleType} value */
        public static XmlAnySimpleType newValue(Object obj) {
          return type.newValue( obj ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a String. For example: "<code>&lt;xml-fragment&gt;anything&lt;/xml-fragment&gt;</code>". */
        public static XmlAnySimpleType parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a String. For example: "<code>&lt;xml-fragment&gt;anything&lt;/xml-fragment&gt;</code>". */
        public static XmlAnySimpleType parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a File. */
        public static XmlAnySimpleType parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a File. */
        public static XmlAnySimpleType parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a URL. */
        public static XmlAnySimpleType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlAnySimpleType} fragment from a URL. */
        public static XmlAnySimpleType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlAnySimpleType} fragment from an InputStream. */
        public static XmlAnySimpleType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from an InputStream. */
        public static XmlAnySimpleType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a Reader. */
        public static XmlAnySimpleType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a Reader. */
        public static XmlAnySimpleType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a DOM Node. */
        public static XmlAnySimpleType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from a DOM Node. */
        public static XmlAnySimpleType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from an XMLInputStream. */
        public static XmlAnySimpleType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from an XMLInputStream. */
        public static XmlAnySimpleType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from an XMLStreamReader. */
        public static XmlAnySimpleType parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( xsr, type, null ); }
        
        /** Parses a {@link XmlAnySimpleType} fragment from an XMLStreamReader. */
        public static XmlAnySimpleType parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException{
          return (XmlAnySimpleType) XmlBeans.getContextTypeLoader().parse( xsr, type, options ); }
        
        /** Returns a validating XMLInputStream. */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** Returns a validating XMLInputStream. */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}

