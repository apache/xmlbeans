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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Factory class for creating new instances.  Note that if
 * a type can be inferred from the XML being loaded (for example,
 * by recognizing the document element QName), then the instance
 * returned by a factory will have the inferred type.  Otherwise
 * the Factory will returned an untyped document.
 */
@SuppressWarnings("unchecked")
public class XmlObjectFactory<T> extends DocumentFactory<T> {
    // anytype needs to be handled different while parsing - opposed to specific instances
    private final boolean isAnyType;

    public XmlObjectFactory(String typeHandle) {
        this(XmlBeans.getBuiltinTypeSystem(), typeHandle);
    }

    /**
     * This constructor is only used as a workaround for bootstrapping the XML schemas - don't use it!
     */
    public XmlObjectFactory(SchemaTypeSystem typeSystem, String typeHandle) {
        super(typeSystem, typeHandle);
        isAnyType = "_BI_anyType".equals(typeHandle);
    }

    /**
     * Creates a new, completely empty instance.
     */
    @Override
    public T newInstance() {
        return (T)XmlBeans.getContextTypeLoader().newInstance(getInnerType(), null);
    }

    /**
     * <p>Creates a new, completely empty instance, specifying options
     * for the root element's document type and/or whether to validate
     * value facets as they are set.</p>
     * <p>
     * Use the <em>options</em> parameter to specify the following:</p>
     *
     * <table>
     * <tr><th>To specify this</th><th>Use this method</th></tr>
     * <tr>
     *  <td>The document type for the root element.</td>
     *  <td>{@link XmlOptions#setDocumentType}</td>
     * </tr>
     * <tr>
     *  <td>Whether value facets should be checked as they are set.</td>
     *  <td>{@link XmlOptions#setValidateOnSet}</td>
     * </tr>
     * </table>
     *
     * @param options Options specifying root document type and/or value facet
     *                checking.
     * @return A new, empty instance of XmlObject.</li>
     */
    @Override
    public T newInstance(XmlOptions options) {
        return (T)XmlBeans.getContextTypeLoader().newInstance(getInnerType(), options);
    }

    /**
     * Creates an immutable {@link XmlObject} value
     */
    public T newValue(Object obj) {
        return (T)getType().newValue(obj);
    }

    /**
     * Parses the given {@link String} as XML.
     */
    @Override
    public T parse(String xmlAsString) throws XmlException {
        return (T)XmlBeans.getContextTypeLoader().parse(xmlAsString, getInnerType(), null);
    }

    /**
     * Parses the given {@link String} as XML.
     * <p>
     * Use the <em>options</em> parameter to specify the following:</p>
     *
     * <table>
     * <tr><th>To specify this</th><th>Use this method</th></tr>
     * <tr>
     *  <td>The document type for the root element.</td>
     *  <td>{@link XmlOptions#setDocumentType}</td>
     * </tr>
     * <tr>
     *  <td>To place line number annotations in the store when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadLineNumbers}</td>
     * </tr>
     * <tr>
     *  <td>To replace the document element with the specified QName when parsing.</td>
     *  <td>{@link XmlOptions#setLoadReplaceDocumentElement}</td>
     * </tr>
     * <tr>
     *  <td>To strip all insignificant whitespace when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripWhitespace}</td>
     * </tr>
     * <tr>
     *  <td>To strip all comments when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripComments}</td>
     * </tr>
     * <tr>
     *  <td>To strip all processing instructions when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripProcinsts}</td>
     * </tr>
     * <tr>
     *  <td>A map of namespace URI substitutions to use when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadSubstituteNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>Additional namespace mappings to be added when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadAdditionalNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>To trim the underlying XML text buffer immediately after parsing
     *  a document, resulting in a smaller memory footprint.</td>
     *  <td>{@link XmlOptions#setLoadTrimTextBuffer}</td>
     * </tr>
     * </table>
     *
     * @param xmlAsString The string to parse.
     * @param options     Options as specified.
     * @return A new instance containing the specified XML.
     */
    @Override
    public T parse(String xmlAsString, XmlOptions options) throws XmlException {
        return (T)XmlBeans.getContextTypeLoader().parse(xmlAsString, getInnerType(), options);
    }

    /**
     * Parses the given {@link File} as XML.
     */
    @Override
    public T parse(File file) throws XmlException, IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(file, getInnerType(), null);
    }

    /**
     * Parses the given {@link File} as XML.
     */
    @Override
    public T parse(File file, XmlOptions options) throws XmlException, IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(file, getInnerType(), options);
    }

    /**
     * Downloads the given {@link java.net.URL} as XML.
     */
    @Override
    public T parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(u, getInnerType(), null);
    }

    /**
     * Downloads the given {@link java.net.URL} as XML.
     */
    @Override
    public T parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(u, getInnerType(), options);
    }

    /**
     * Decodes and parses the given {@link InputStream} as XML.
     */
    @Override
    public T parse(InputStream is) throws XmlException, IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(is, getInnerType(), null);
    }

    /**
     * Decodes and parses the given {@link XMLStreamReader} as XML.
     */
    @Override
    public T parse(XMLStreamReader xsr) throws XmlException {
        return (T)XmlBeans.getContextTypeLoader().parse(xsr, getInnerType(), null);
    }

    /**
     * Decodes and parses the given {@link InputStream} as XML.
     * <p>
     * Use the <em>options</em> parameter to specify the following:</p>
     *
     * <table>
     * <tr><th>To specify this</th><th>Use this method</th></tr>
     * <tr>
     *  <td>The character encoding to use when parsing or writing a document.</td>
     *  <td>{@link XmlOptions#setCharacterEncoding}</td>
     * </tr>
     * <tr>
     *  <td>The document type for the root element.</td>
     *  <td>{@link XmlOptions#setDocumentType}</td>
     * </tr>
     * <tr>
     *  <td>Place line number annotations in the store when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadLineNumbers}</td>
     * </tr>
     * <tr>
     *  <td>Replace the document element with the specified QName when parsing.</td>
     *  <td>{@link XmlOptions#setLoadReplaceDocumentElement}</td>
     * </tr>
     * <tr>
     *  <td>Strip all insignificant whitespace when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripWhitespace}</td>
     * </tr>
     * <tr>
     *  <td>Strip all comments when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripComments}</td>
     * </tr>
     * <tr>
     *  <td>Strip all processing instructions when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripProcinsts}</td>
     * </tr>
     * <tr>
     *  <td>Set a map of namespace URI substitutions to use when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadSubstituteNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>Set additional namespace mappings to be added when parsing a document.</td>
     *  <td>{@link XmlOptions#setLoadAdditionalNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>Trim the underlying XML text buffer immediately after parsing
     *  a document, resulting in a smaller memory footprint.</td>
     *  <td>{@link XmlOptions#setLoadTrimTextBuffer}</td>
     * </tr>
     * </table>
     */
    @Override
    public T parse(InputStream is, XmlOptions options) throws XmlException, IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(is, getInnerType(), options);
    }

    /**
     * Parses the given {@link XMLStreamReader} as XML.
     */
    @Override
    public T parse(XMLStreamReader xsr, XmlOptions options) throws XmlException {
        return (T)XmlBeans.getContextTypeLoader().parse(xsr, getInnerType(), options);
    }

    /**
     * Parses the given {@link Reader} as XML.
     */
    @Override
    public T parse(Reader r) throws XmlException, IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(r, getInnerType(), null);
    }

    /**
     * Parses the given {@link Reader} as XML.
     */
    @Override
    public T parse(Reader r, XmlOptions options) throws XmlException, IOException {
        return (T)XmlBeans.getContextTypeLoader().parse(r, getInnerType(), options);
    }

    /**
     * Converts the given DOM {@link Node} into an XmlObject.
     */
    @Override
    public T parse(Node node) throws XmlException {
        return (T)XmlBeans.getContextTypeLoader().parse(node, getInnerType(), null);
    }

    /**
     * Converts the given DOM {@link Node} into an XmlObject.
     */
    @Override
    public T parse(Node node, XmlOptions options) throws XmlException {
        return (T)XmlBeans.getContextTypeLoader().parse(node, getInnerType(), options);
    }

    /**
     * Returns an {@link XmlSaxHandler} that can load an XmlObject from SAX events.
     */
    public XmlSaxHandler newXmlSaxHandler() {
        return XmlBeans.getContextTypeLoader().newXmlSaxHandler(getInnerType(), null);
    }

    /**
     * Returns an {@link XmlSaxHandler} that can load an XmlObject from SAX events.
     */
    public XmlSaxHandler newXmlSaxHandler(XmlOptions options) {
        return XmlBeans.getContextTypeLoader().newXmlSaxHandler(getInnerType(), options);
    }

    /**
     * Creates a new DOMImplementation object
     */
    public DOMImplementation newDomImplementation() {
        return XmlBeans.getContextTypeLoader().newDomImplementation(null);
    }

    /**
     * Creates a new DOMImplementation object, taking options
     */
    public DOMImplementation newDomImplementation(XmlOptions options) {
        return XmlBeans.getContextTypeLoader().newDomImplementation(options);
    }

    private SchemaType getInnerType() {
        return isAnyType ? null : getType();
    }
}
