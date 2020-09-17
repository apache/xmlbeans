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

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * Represents a searchable set of XML Schema component definitions.
 * <p>
 * SchemaTypeLoader is somewhat analogous to {@link java.lang.ClassLoader},
 * because it is responsible for finding {@link SchemaComponent} definitions
 * by name, yet it is not responsible for being able to enumerate all the
 * component definitons available. (If you wish to enumerate component
 * definitions, see {@link SchemaTypeSystem}.) There are some ways in which
 * SchemaTypeSystems are dissimilar from ClassLoaders, however.
 * Since XML Schema has a number of instance-oriented typing mechanisms
 * (such as wildcards) that do not exist in Java, a SchemaTypeLoader is
 * not associated with a type; instead, a SchemaTypeLoader is associated
 * with each XML instance.
 * <p>
 * Every XML instance is loaded within the context of a SchemaTypeLoader;
 * the SchemaTypeLoader for an instance is used to resolve all type definitions
 * within the instance and for applying type-sensitive methods such as
 * {@link XmlObject#validate}.
 * <p>
 * Normally the SchemaTypeLoader being used for all instances is the
 * context type loader (that is, the SchemaTypeLoader returned from
 * {@link XmlBeans#getContextTypeLoader()}).  The context type loader
 * consults the thread's context ClassLoader (see {@link Thread#getContextClassLoader()})
 * to find schema type defintions that are available on the classpath.
 * The net result is that you can use schema types simply by putting
 * their compiled schema JARs on your classpath.
 * If you wish to load instances using a different SchemaTypeLoader, then you must
 * call {@link #parse} methods on the SchemaTypeLoader instance explicitly
 * rather than using the normal convenient Factory methods.
 * <p>
 * A SchemaTypeLoader can be obtained by dynamically loading XSD files
 * using {@link XmlBeans#loadXsd}, or by assembling other SchemaTypeLoaders
 * or SchemaTypeSystems on a path using {@link XmlBeans#typeLoaderUnion}.
 *
 * @see XmlBeans#loadXsd
 * @see XmlBeans#getContextTypeLoader
 * @see XmlBeans#typeLoaderUnion
 * @see SchemaTypeSystem
 */
public interface SchemaTypeLoader {
    /**
     * Returns the type with the given name, or null if none.
     */
    SchemaType findType(QName name);

    /**
     * Returns the document type rooted at the given element name, or null if none.
     */
    SchemaType findDocumentType(QName name);

    /**
     * Returns the attribute type containing the given attribute name, or null if none.
     */
    SchemaType findAttributeType(QName name);

    /**
     * Returns the global element defintion with the given name, or null if none.
     */
    SchemaGlobalElement findElement(QName name);

    /**
     * Returns the global attribute defintion with the given name, or null if none.
     */
    SchemaGlobalAttribute findAttribute(QName name);

    /**
     * Returns the model group defintion with the given name, or null if none.
     */
    SchemaModelGroup findModelGroup(QName name);

    /**
     * Returns the attribute group defintion with the given name, or null if none.
     */
    SchemaAttributeGroup findAttributeGroup(QName name);

    /**
     * True if the typeloader contains any definitions in the given namespace.
     */
    boolean isNamespaceDefined(String namespace);

    /**
     * Used for on-demand loading.
     */
    SchemaType.Ref findTypeRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaType.Ref findDocumentTypeRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaType.Ref findAttributeTypeRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaGlobalElement.Ref findElementRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaGlobalAttribute.Ref findAttributeRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaModelGroup.Ref findModelGroupRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaAttributeGroup.Ref findAttributeGroupRef(QName name);

    /**
     * Used for on-demand loading.
     */
    SchemaIdentityConstraint.Ref findIdentityConstraintRef(QName name);

    /**
     * Finds a type for a given signature string
     */
    SchemaType typeForSignature(String signature);

    /**
     * Finds a type for a given fully-qualified XML Bean classname
     */
    SchemaType typeForClassname(String classname);

    /**
     * Loads original XSD source as a stream.  See {@link SchemaType#getSourceName}.
     */
    InputStream getSourceAsStream(String sourceName);

    /**
     * Compiles an XPath
     */
    String compilePath(String pathExpr, XmlOptions options) throws XmlException;

    /**
     * Compiles an XQuery
     */
    String compileQuery(String queryExpr, XmlOptions options) throws XmlException;

    /**
     * Creates an instance of the given type.
     */
    XmlObject newInstance(SchemaType type, XmlOptions options);

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(String xmlText, SchemaType type, XmlOptions options) throws XmlException;

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(File file, SchemaType type, XmlOptions options) throws XmlException, IOException;

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(URL file, SchemaType type, XmlOptions options) throws XmlException, IOException;

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(InputStream jiois, SchemaType type, XmlOptions options) throws XmlException, IOException;

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(XMLStreamReader xsr, SchemaType type, XmlOptions options) throws XmlException;

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(Reader jior, SchemaType type, XmlOptions options) throws XmlException, IOException;

    /**
     * Parses an instance of the given type.
     */
    XmlObject parse(Node node, SchemaType type, XmlOptions options) throws XmlException;

    /**
     * Returns an XmlSaxHandler that can parse an instance of the given type.
     */
    XmlSaxHandler newXmlSaxHandler(SchemaType type, XmlOptions options);

    /**
     * Returns a DOMImplementation.
     */
    DOMImplementation newDomImplementation(XmlOptions options);
}
