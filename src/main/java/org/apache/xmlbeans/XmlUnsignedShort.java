/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans;

/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#unsignedShort">xs:unsignedShort</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * Verified to be in the range 0..65535 when validating.
 * <p>
 * Convertible to a Java int.
 */
public interface XmlUnsignedShort extends XmlUnsignedInt {
    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_unsignedShort");

    /**
     * Returns this value as an int
     */
    int getIntValue();

    /**
     * Sets this value as an int
     */
    void setIntValue(int v);

    /**
     * A class with methods for creating instances
     * of {@link XmlUnsignedShort}.
     */
    final class Factory {
        /**
         * Creates an empty instance of {@link XmlUnsignedShort}
         */
        public static XmlUnsignedShort newInstance() {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().newInstance(type, null);
        }

        /**
         * Creates an empty instance of {@link XmlUnsignedShort}
         */
        public static XmlUnsignedShort newInstance(org.apache.xmlbeans.XmlOptions options) {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().newInstance(type, options);
        }

        /**
         * Creates an immutable {@link XmlUnsignedShort} value
         */
        public static XmlUnsignedShort newValue(Object obj) {
            return (XmlUnsignedShort) type.newValue(obj);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a String. For example: "<code>&lt;xml-fragment&gt;12345&lt;/xml-fragment&gt;</code>".
         */
        public static XmlUnsignedShort parse(java.lang.String s) throws org.apache.xmlbeans.XmlException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(s, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a String. For example: "<code>&lt;xml-fragment&gt;12345&lt;/xml-fragment&gt;</code>".
         */
        public static XmlUnsignedShort parse(java.lang.String s, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(s, type, options);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a File.
         */
        public static XmlUnsignedShort parse(java.io.File f) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(f, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a File.
         */
        public static XmlUnsignedShort parse(java.io.File f, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(f, type, options);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a URL.
         */
        public static XmlUnsignedShort parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(u, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a URL.
         */
        public static XmlUnsignedShort parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(u, type, options);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from an InputStream.
         */
        public static XmlUnsignedShort parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(is, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from an InputStream.
         */
        public static XmlUnsignedShort parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(is, type, options);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a Reader.
         */
        public static XmlUnsignedShort parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(r, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a Reader.
         */
        public static XmlUnsignedShort parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(r, type, options);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a DOM Node.
         */
        public static XmlUnsignedShort parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(node, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from a DOM Node.
         */
        public static XmlUnsignedShort parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(node, type, options);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from an XMLStreamReader.
         */
        public static XmlUnsignedShort parse(javax.xml.stream.XMLStreamReader xsr) throws org.apache.xmlbeans.XmlException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(xsr, type, null);
        }

        /**
         * Parses a {@link XmlUnsignedShort} fragment from an XMLStreamReader.
         */
        public static XmlUnsignedShort parse(javax.xml.stream.XMLStreamReader xsr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
            return (XmlUnsignedShort) XmlBeans.getContextTypeLoader().parse(xsr, type, options);
        }

        private Factory() {
            // No instance of this class allowed
        }
    }
}

