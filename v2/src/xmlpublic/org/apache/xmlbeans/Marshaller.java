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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

/**
 * A Marshaller object is used to convert Java objects to XML documents.
 * The object is thread safe and stateless.
 */
public interface Marshaller
{

    /**
     * @deprecated use XmlOptions based method instead
     *
     * Get an XMLStreamReader object that represents the Java object as XML.
     * Note that the object's contents are accessed on demand, so modifying
     * the object while reading from the reader will produce undefined results.
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * @param obj
     * @param nscontext  initial NamespaceContext representing initial defined namespaces
     * @return  XMLStreamReader representing the XML content
     * @throws XmlException
     */
    XMLStreamReader marshal(Object obj,
                            NamespaceContext nscontext)
        throws XmlException;


    /**
     * Get an XMLStreamReader object that represents the Java object as XML.
     * Note that the object's contents are accessed on demand, so modifying
     * the object while reading from the reader will produce undefined results.
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>The encoding for the document, as described in
     * {@link XmlOptions#setCharacterEncoding}.</li>
     * </ul>
     *
     *
     * @param obj
     * @param options
     * @return  XMLStreamReader representing the XML content
     * @throws XmlException
     */
    XMLStreamReader marshal(Object obj,
                            XmlOptions options)
        throws XmlException;


    /**
     * Write an XML representation of the Java object to the provided output.
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     * @param obj
     * @param writer
     * @throws XmlException
     */
    void marshal(XMLStreamWriter writer, Object obj)
        throws XmlException;

    /**
     * Write an XML representation of the Java object to the provided output.
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>The encoding for the document, as described in
     * {@link XmlOptions#setCharacterEncoding}.</li>
     * </ul>B
     *
     *
     * @param obj
     * @param writer
     * @param options
     * @throws XmlException
     */
    void marshal(XMLStreamWriter writer, Object obj, XmlOptions options)
        throws XmlException;


    /**
     * Write an XML representation of the Java object to the provided output
     * as a complete xml document using the default encoding
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * An XML Declaration will be written declaring the encoding if one was
     * set via XmlOptions
     *
     * @param out
     * @param obj
     * @throws XmlException
     */
    void marshal(OutputStream out, Object obj)
        throws XmlException;


    /**
     * Write an XML representation of the Java object to the provided output
     * as a complete xml document using the default encoding
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * An XML Declaration will be written declaring the encoding if one was
     * set via XmlOptions
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>The encoding for the document, as described in
     * {@link XmlOptions#setCharacterEncoding}.</li>
     * <li>Whether to pretty print the output, as described in
     * {@link XmlOptions#setSavePrettyPrint}.</li>
     * <li>level of indenting when pretty printing, as described in
     * {@link XmlOptions#setSavePrettyPrintIndent}.</li>
     * </ul>
     *
     * @param out
     * @param obj
     * @param options
     * @throws XmlException
     */
    void marshal(OutputStream out, Object obj, XmlOptions options)
        throws XmlException;


    /**
     * @deprecated use XmlOptions based method instead
     *
     * Write an XML representation of the Java object to the provided output
     * as a complete xml document
     *
     * The object is expected to correspond to a global element in a schema.
     * The first matching global element will be used as the root element.
     *
     * An XML Declaration will be written declaring the encoding.
     *
     * @param out
     * @param obj
     * @param encoding      encoding used when writing the document
     * @throws XmlException
     */
    void marshal(OutputStream out, Object obj, String encoding)
        throws XmlException;


    /**
     * @deprecated use XmlOptions version
     *
     * Get an XMLStreamReader object that represents the given java type.

     * It is the responsibility of the caller to ensure that
     * obj is an instanceof javaType

     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * @param obj
     * @param elementName
     * @param schemaType
     * @param javaType   the java type in the format returned by Class.getName()
     * @param namespaceContext
     * @return
     * @throws XmlException
     */
    XMLStreamReader marshalType(Object obj,
                                QName elementName,
                                QName schemaType,
                                String javaType,
                                NamespaceContext namespaceContext)
        throws XmlException;


    /**
     * Get an XMLStreamReader object that represents the given java type.

     * It is the responsibility of the caller to ensure that
     * obj is an instanceof javaType

     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * </ul>
     *
     * @param obj
     * @param elementName
     * @param schemaType
     * @param javaType the java type in the format returned by Class.getName()
     * @param options
     * @return
     * @throws XmlException
     */
    XMLStreamReader marshalType(Object obj,
                                QName elementName,
                                QName schemaType,
                                String javaType,
                                XmlOptions options)
        throws XmlException;


    /**
     * Get an XMLStreamReader object that represents the given java type.

     * It is the responsibility of the caller to ensure that
     * obj is an instanceof javaType

     * As of this writing (11/22/2003), the returned reader will NOT contain
     * a START_DOCUMENT or END_DOCUMENT element.
     * The reader's first event is a START_ELEMENT event.
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * </ul>
     *
     * @param obj
     * @param elementName  name of global element from a known schema
     * @param javaType the java type in the format returned by Class.getName()
     * @param options
     * @return
     * @throws XmlException
     */
    XMLStreamReader marshalElement(Object obj,
                                   QName elementName,
                                   String javaType,
                                   XmlOptions options)
        throws XmlException;

    /**
     * Write an XML representation of the Java object to the provided output.
     *
     * It is the responsibility of the caller to ensure that
     * obj is an instanceof javaType
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     * @param writer
     * @param obj
     * @param elementName
     * @param schemaType
     * @param javaType the java type in the format returned by Class.getName()
     * @throws XmlException
     */
    void marshalType(XMLStreamWriter writer,
                     Object obj,
                     QName elementName,
                     QName schemaType,
                     String javaType)
        throws XmlException;


    /**
     * Write an XML representation of the Java object to the provided output.
     *
     * It is the responsibility of the caller to ensure that
     * obj is an instanceof javaType
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * </ul>
     *
     * @param writer
     * @param obj
     * @param elementName
     * @param schemaType
     * @param javaType the java type in the format returned by Class.getName()
     * @throws XmlException
     */
    void marshalType(XMLStreamWriter writer,
                     Object obj,
                     QName elementName,
                     QName schemaType,
                     String javaType,
                     XmlOptions options)
        throws XmlException;


    /**
     * Write an XML representation of the Java object to the provided output.
     *
     * It is the responsibility of the caller to ensure that
     * obj is an instanceof javaType
     *
     * As of this writing (11/22/2003), this method will NOT write
     * a START_DOCUMENT or END_DOCUMENT element.
     * The first event written will be a START_ELEMENT event.
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * </ul>
     *
     * @param writer
     * @param obj
     * @param elementName  name of global element from a known schema
     * @param javaType the java type in the format returned by Class.getName()
     * @throws XmlException
     */
    void marshalElement(XMLStreamWriter writer,
                        Object obj,
                        QName elementName,
                        String javaType,
                        XmlOptions options)
        throws XmlException;

}
