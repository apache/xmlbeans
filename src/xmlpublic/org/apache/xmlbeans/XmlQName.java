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

package org.apache.xmlbeans;

import javax.xml.namespace.QName;


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#QName">xs:QName</a> type.
 * <p>
 * A QName is the logical combination of an XML namespace URI and a localName.
 * Although in an XML instance document, a QName appears as "prefix:localName",
 * the logical value of a QName does NOT contain any information about the
 * prefix, only the namespace URI to which the prefix maps.  For example,
 * two QNames "a:hello" and "b:hello" are perfectly equivalent if "a:" in
 * the first instance maps to the same URI as "b:" in the second instance.
 * <p>
 * Convertible to {@link javax.xml.namespace.QName}.
 */ 
public interface XmlQName extends XmlAnySimpleType
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_QName");
    
    /** Returns this value as a {@link QName} */
    QName getQNameValue();
    /** Sets this value as a {@link QName} */
    void setQNameValue(QName name);

    /**
     * Returns this value as a {@link QName}
     * @deprecated replaced with {@link #getQNameValue}
     **/
    QName qNameValue();
    /**
     * Sets this value as a {@link QName}
     * @deprecated replaced with {@link #setQNameValue}
     **/
    void set(QName name);
    
    /**
     * A class with methods for creating instances
     * of {@link XmlQName}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlQName} */
        public static XmlQName newInstance() {
          return (XmlQName) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlQName} */
        public static XmlQName newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlQName) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlQName} value */
        public static XmlQName newValue(Object obj) {
          return (XmlQName) type.newValue( obj ); }
        
        /** Parses a {@link XmlQName} fragment from a String. For example: "<code>&lt;xml-fragment xmlns:x="http://openuri.org/"&gt;x:sample&lt;/xml-fragment&gt;</code>". */
        public static XmlQName parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a String. For example: "<code>&lt;xml-fragment xmlns:x="http://openuri.org/"&gt;x:sample&lt;/xml-fragment&gt;</code>". */
        public static XmlQName parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a File. */
        public static XmlQName parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a File. */
        public static XmlQName parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a URL. */
        public static XmlQName parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlQName} fragment from a URL. */
        public static XmlQName parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlQName} fragment from an InputStream. */
        public static XmlQName parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from an InputStream. */
        public static XmlQName parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a Reader. */
        public static XmlQName parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a Reader. */
        public static XmlQName parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from a DOM Node. */
        public static XmlQName parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from a DOM Node. */
        public static XmlQName parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlQName} fragment from an XMLInputStream. */
        public static XmlQName parse(weblogic.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlQName} fragment from an XMLInputStream. */
        public static XmlQName parse(weblogic.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return (XmlQName) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Returns a validating XMLInputStream. */
        public static weblogic.xml.stream.XMLInputStream newValidatingXMLInputStream(weblogic.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** Returns a validating XMLInputStream. */
        public static weblogic.xml.stream.XMLInputStream newValidatingXMLInputStream(weblogic.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}

