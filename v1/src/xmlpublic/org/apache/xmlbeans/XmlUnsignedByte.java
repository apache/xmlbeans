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

/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#unsignedByte">xs:unsignedByte</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * Verified to be in the range 0..255 when validating.
 * <p>
 * As suggested by JAXB, convertible to Java short.
 */ 
public interface XmlUnsignedByte extends XmlUnsignedShort
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_unsignedByte");
    
    /** Returns this value as a short */
    public short getShortValue();
    /** Sets this value as a short */
    public void setShortValue(short s);

    /**
     * Returns this value as a short
     * @deprecated replaced with {@link #getShortValue}
     **/
    public short shortValue();
    /**
     * Sets this value as a short
     * @deprecated replaced with {@link #setShortValue}
     **/
    public void set(short s);

    /**
     * A class with methods for creating instances
     * of {@link XmlUnsignedByte}.
     */
    public static final class Factory
    {
        /** Creates an empty instance of {@link XmlUnsignedByte} */
        public static XmlUnsignedByte newInstance() {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        /** Creates an empty instance of {@link XmlUnsignedByte} */
        public static XmlUnsignedByte newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** Creates an immutable {@link XmlUnsignedByte} value */
        public static XmlUnsignedByte newValue(Object obj) {
          return (XmlUnsignedByte) type.newValue( obj ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a String. For example: "<code>&lt;xml-fragment&gt;123&lt;/xml-fragment&gt;</code>". */
        public static XmlUnsignedByte parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( s, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a String. For example: "<code>&lt;xml-fragment&gt;123&lt;/xml-fragment&gt;</code>". */
        public static XmlUnsignedByte parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( s, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a File. */
        public static XmlUnsignedByte parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( f, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a File. */
        public static XmlUnsignedByte parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( f, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a URL. */
        public static XmlUnsignedByte parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( u, type, null ); }

        /** Parses a {@link XmlUnsignedByte} fragment from a URL. */
        public static XmlUnsignedByte parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( u, type, options ); }

        /** Parses a {@link XmlUnsignedByte} fragment from an InputStream. */
        public static XmlUnsignedByte parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an InputStream. */
        public static XmlUnsignedByte parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a Reader. */
        public static XmlUnsignedByte parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a Reader. */
        public static XmlUnsignedByte parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a DOM Node. */
        public static XmlUnsignedByte parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from a DOM Node. */
        public static XmlUnsignedByte parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an XMLInputStream.
         * @deprecated Superceded by JSR 173 */
        public static XmlUnsignedByte parse(weblogic.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** Parses a {@link XmlUnsignedByte} fragment from an XMLInputStream.
         * @deprecated Superceded by JSR 173 */
        public static XmlUnsignedByte parse(weblogic.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return (XmlUnsignedByte) XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** Returns a validating XMLInputStream.
         * @deprecated Superceded by JSR 173 */
        public static weblogic.xml.stream.XMLInputStream newValidatingXMLInputStream(weblogic.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** Returns a validating XMLInputStream.
         * @deprecated Superceded by JSR 173 */
        public static weblogic.xml.stream.XMLInputStream newValidatingXMLInputStream(weblogic.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, weblogic.xml.stream.XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}

