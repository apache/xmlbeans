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

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.schema.PathResourceLoader;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.store.Locale;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;

/**
 * Provides an assortment of utilities
 * for managing XML Bean types, type systems, QNames, paths,
 * and queries.
 */
public final class XmlBeans {
    private static final String HOLDER_CLASS_NAME = "TypeSystemHolder";
    private static final String TYPE_SYSTEM_FIELD = "typeSystem";

    private static String XMLBEANS_TITLE = "org.apache.xmlbeans";
    private static String XMLBEANS_VERSION = "5.0.2";
    private static String XMLBEANS_VENDOR = "Apache Software Foundation";

    static {
        Package pkg = XmlBeans.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            XMLBEANS_TITLE = pkg.getImplementationTitle();
            XMLBEANS_VERSION = pkg.getImplementationVersion();
            XMLBEANS_VENDOR = pkg.getImplementationVendor();
        }
    }

    /**
     * Returns the XmlBeans Package title, "org.apache.xmlbeans",
     * the value of
     * {@link Package#getImplementationTitle() XmlBeans.class.getPackage().getImplementationTitle()}.
     */
    public static final String getTitle() {
        return XMLBEANS_TITLE;
    }

    /**
     * Returns the XmlBeans vendor, "Apache Software Foundation",
     * the value of
     * {@link Package#getImplementationVendor() XmlBeans.class.getPackage().getImplementationVendor()}.
     */
    public static final String getVendor() {
        return XMLBEANS_VENDOR;
    }

    /**
     * Returns the XmlBeans version,
     * the value of
     * {@link Package#getImplementationVersion() XmlBeans.class.getPackage().getImplementationVersion()}.
     */
    public static final String getVersion() {
        return XMLBEANS_VERSION;
    }

    /**
     * Thread local QName cache for general use
     */
    private static final ThreadLocal _threadLocalLoaderQNameCache =
        new ThreadLocal() {
            protected Object initialValue() {
                return new SoftReference(new QNameCache(32));
            }
        };

    public static void clearThreadLocals() {
        // clear thread local here
        _threadLocalLoaderQNameCache.remove();
    }

    /**
     * Returns a thread local QNameCache
     */
    public static QNameCache getQNameCache() {
        SoftReference softRef = (SoftReference) _threadLocalLoaderQNameCache.get();
        QNameCache qnameCache = (QNameCache) (softRef).get();
        if (qnameCache == null) {
            qnameCache = new QNameCache(32);
            _threadLocalLoaderQNameCache.set(new SoftReference(qnameCache));
        }
        return qnameCache;
    }

    /**
     * Obtains a name from the thread local QNameCache
     */
    public static QName getQName(String localPart) {
        return getQNameCache().getName("", localPart);
    }

    /**
     * Obtains a name from the thread local QNameCache
     */

    public static QName getQName(String namespaceUri, String localPart) {
        return getQNameCache().getName(namespaceUri, localPart);
    }

    private static RuntimeException causedException(RuntimeException e, Throwable cause) {
        e.initCause(cause);

        return e;
    }

    /**
     * Compiles an XPath, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached.
     */
    public static String compilePath(String pathExpr) throws XmlException {
        return compilePath(pathExpr, null);
    }

    /**
     * Compiles an XPath, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached;
     * takes an option for specifying text that indicates the name of context node.
     * The default is "this", as in "$this".
     *
     * @param options Options for the path. For example, you can call
     *                the {@link XmlOptions#setXqueryCurrentNodeVar(String) XmlOptions.setXqueryCurrentNodeVar(String)}
     *                method to specify a particular name for the expression
     *                variable that indicates the context node.
     */
    public static String compilePath(String pathExpr, XmlOptions options)
        throws XmlException {
        return getContextTypeLoader().compilePath(pathExpr, options);
    }

    /**
     * Compiles an XQuery, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached.
     */
    public static String compileQuery(String queryExpr) throws XmlException {
        return compileQuery(queryExpr, null);
    }

    /**
     * Compiles an XQuery, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached;
     * takes an option for specifying text that indicates the context node.
     *
     * @param options Options for the query. For example, you can call
     *                the {@link XmlOptions#setXqueryCurrentNodeVar(String) XmlOptions.setXqueryCurrentNodeVar(String)}
     *                method to specify a particular name for the expression
     *                variable that indicates the context node and the
     *                {@link XmlOptions#setXqueryVariables(java.util.Map) XmlOptions.setXqueryVariables(Map)}
     *                method to map external variable names to values.
     */
    public static String compileQuery(String queryExpr, XmlOptions options)
        throws XmlException {
        return getContextTypeLoader().compileQuery(queryExpr, options);
    }

    /**
     * Gets the SchemaTypeLoader based on the current thread's context
     * ClassLoader. This is the SchemaTypeLoader that is used to assign
     * schema types to XML documents by default. The SchemaTypeLoader is
     * also consulted to resolve wildcards and xsi:type attributes.
     * <p>
     * The "parse" methods of XmlBeans all delegate to the
     * "parseInstance" methods of the context type loader.
     */
    public static SchemaTypeLoader getContextTypeLoader() {
        return SchemaTypeLoaderImpl.getContextTypeLoader();
    }

    /**
     * Returns the builtin type system. This SchemaTypeSystem contains
     * only the 46 builtin types defined by the XML Schema specification.
     */
    public static SchemaTypeSystem getBuiltinTypeSystem() {
        return BuiltinSchemaTypeSystem.get();
    }

    /**
     * Creates an XmlCursor for a DOM node which is implemented by XmlBwans
     */
    public static XmlCursor nodeToCursor(Node n) {
        return Locale.nodeToCursor(n);
    }

    /**
     * Creates an XmlObject for a DOM node which is implemented by XmlBwans
     */
    public static XmlObject nodeToXmlObject(Node n) {
        return Locale.nodeToXmlObject(n);
    }

    /**
     * Creates an XmlObject for a DOM node which is implemented by XmlBwans
     */
    public static XMLStreamReader nodeToXmlStreamReader(Node n) {
        return Locale.nodeToXmlStream(n);
    }

    /**
     * Returns the XmlObject for a DOM node which is implemented by XmlBwans
     */
    public static Node streamToNode(XMLStreamReader xs) {
        return Locale.streamToNode(xs);
    }

    /**
     * Returns the SchemaTypeSystem that results from compiling the XML
     * schema definitions passed.
     * <p>
     * Just like compileXsd, but uses the context type loader for
     * linking, and returns a unioned typeloader that is suitable for
     * creating instances.
     */
    public static SchemaTypeLoader loadXsd(XmlObject... schemas) throws XmlException {
        return loadXsd(schemas, null);
    }

    /**
     * <p>Returns the SchemaTypeSystem that results from compiling the XML
     * schema definitions passed in <em>schemas</em>.</p>
     *
     * <p>This is just like compileXsd, but uses the context type loader for
     * linking, and returns a unioned typeloader that is suitable for
     * creating instances.</p>
     *
     * <p>Use the <em>options</em> parameter to specify one or both of the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>Whether validation should not be done when building the SchemaTypeSystem,
     * as described in {@link XmlOptions#setCompileNoValidation}.</li>
     * </ul>
     *
     * @param schemas The schema definitions from which to build the schema type system.
     * @param options Options specifying an error listener and/or validation behavior.
     */
    public static SchemaTypeLoader loadXsd(XmlObject[] schemas, XmlOptions options) throws XmlException {
        SchemaTypeSystem sts = SchemaTypeSystemCompiler.compile(null, null, schemas, null, getContextTypeLoader(), null, options);
        return (sts == null) ? null : typeLoaderUnion(sts, getContextTypeLoader());
    }


    /**
     * <p>Returns the SchemaTypeSystem that results from compiling the XML
     * schema definitions passed.</p>
     *
     * <p>The XmlObjects passed in should be w3c &lt;schema&gt; elements whose type
     * is org.w3c.x2001.xmlSchema.Schema. (That is, schema elements in
     * the XML namespace http://www.w3c.org/2001/XMLSchema.)  Also
     * org.w3c.x2001.xmlSchema.SchemaDocument is permitted.</p>
     *
     * <p>The optional second argument is a SchemaTypeLoader which will be
     * consulted for already-compiled schema types which may be linked
     * while processing the given schemas.</p>
     *
     * <p>The SchemaTypeSystem that is returned should be combined
     * (via {@link #typeLoaderUnion}) with the typepath typeloader in order
     * to create a typeloader that can be used for creating and validating
     * instances.</p>
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>Whether validation should not be done when building the SchemaTypeSystem,
     * as described in {@link XmlOptions#setCompileNoValidation}.</li>
     * </ul>
     *
     * @param schemas  The schema definitions from which to build the schema type system.
     * @param typepath The path to already-compiled schema types for linking while processing.
     * @param options  Options specifying an error listener and/or validation behavior.
     */
    public static SchemaTypeSystem compileXsd(XmlObject[] schemas, SchemaTypeLoader typepath, XmlOptions options) throws XmlException {
        return compileXmlBeans(null, null, schemas, null, typepath, null, options);
    }

    /**
     * <p>Returns the SchemaTypeSystem that results from augumenting the
     * SchemaTypeSystem passed in by incrementally adding the given XML
     * schema definitions.</p>
     *
     * <p>These could be new definitions (if the Schema document is not recorded into
     * the existing SchemaTypeSystem), modifications to the already existing
     * definitions (if the Schema document is already recorded in the existing
     * SchemaTypeSystem), or deletions (if the Schema document is already recorded
     * in the existing SchemaTypeSystem and the new definitions are empty).
     * The identity of documents is established using
     * {@link XmlDocumentProperties#getSourceName}, so if the caller choses to
     * construct the Schema definitions using other methods than parsing an
     * XML document, they should make sure that the names returned by that
     * method are consistent with the caller's intent (add/modify).</p>
     *
     * <p>The XmlObjects passed in should be w3c &lt;schema&gt; elements whose type
     * is org.w3c.x2001.xmlSchema.Schema. (That is, schema elements in
     * the XML namespace http://www.w3c.org/2001/XMLSchema.)  Also
     * org.w3c.x2001.xmlSchema.SchemaDocument is permitted.</p>
     *
     * <p>The optional second argument is a SchemaTypeLoader which will be
     * consulted for already-compiled schema types which may be linked
     * while processing the given schemas.</p>
     *
     * <p>The SchemaTypeSystem that is returned should be combined
     * (via {@link #typeLoaderUnion}) with the typepath typeloader in order
     * to create a typeloader that can be used for creating and validating
     * instances.</p>
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>Whether validation should not be done when building the SchemaTypeSystem,
     * as described in {@link XmlOptions#setCompileNoValidation}.</li>
     * </ul>
     *
     * @param schemas  The schema definitions from which to build the schema type system.
     * @param typepath The path to already-compiled schema types for linking while processing.
     * @param options  Options specifying an error listener and/or validation behavior.
     */
    public static SchemaTypeSystem compileXsd(SchemaTypeSystem system, XmlObject[] schemas, SchemaTypeLoader typepath, XmlOptions options) throws XmlException {
        return compileXmlBeans(null, system, schemas, null, typepath, null, options);
    }

    /**
     * <p>Returns the SchemaTypeSystem that results from augumenting the
     * SchemaTypeSystem passed in by incrementally adding the given XML
     * schema definitions.</p>
     *
     * <p>These could be new definitions (if the Schema document is not recorded into
     * the existing SchemaTypeSystem), modifications to the already existing
     * definitions (if the Schema document is already recorded in the existing
     * SchemaTypeSystem), or deletions (if the Schema document is already recorded
     * in the existing SchemaTypeSystem and the new definitions are empty).
     * The identity of documents is established using
     * {@link XmlDocumentProperties#getSourceName}, so if the caller choses to
     * construct the Schema definitions using other methods than parsing an
     * XML document, they should make sure that the names returned by that
     * method are consistent with the caller's intent (add/modify).</p>
     *
     * <p>The XmlObjects passed in should be w3c &lt;schema&gt; elements whose type
     * is org.w3c.x2001.xmlSchema.Schema. (That is, schema elements in
     * the XML namespace http://www.w3c.org/2001/XMLSchema.)  Also
     * org.w3c.x2001.xmlSchema.SchemaDocument is permitted.</p>
     *
     * <p>The optional name argument is used to name the compiled schema type system.
     * A randomly generated name will be used if the name is null.</p>
     *
     * <p>The optional {@link BindingConfig} argument is used to control the shape
     * of the generated code. A <code>BindingConfig</code> isn't used if <code>Filer</code>
     * is null.</p>
     *
     * <p>The optional SchemaTypeLoader argument will be
     * consulted for already-compiled schema types which may be linked
     * while processing the given schemas. If not specified, the context
     * typeloader (as returned by {@link #getContextTypeLoader}) will be used.</p>
     *
     * <p>The optional {@link Filer} argument is used to create new binary or source
     * files which are the product of the compilation.  If the Filer is null, the
     * schema binaries (.xsb) files and source files won't be generated.</p>
     *
     * <p>The SchemaTypeSystem that is returned should be combined
     * (via {@link #typeLoaderUnion}) with the typepath typeloader in order
     * to create a typeloader that can be used for creating and validating
     * instances.</p>
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * <li>Whether validation should not be done when building the SchemaTypeSystem,
     * as described in {@link XmlOptions#setCompileNoValidation}.</li>
     * </ul>
     *
     * @param name     The type system name or null to use a randomly generated name.
     * @param system   A pre-existing SchemaTypeSystem used in incremental compilation.
     * @param schemas  The schema definitions from which to build the schema type system.
     * @param config   The configuration controls the code generation shape.
     * @param typepath The path to already-compiled schema types for linking while processing.
     * @param filer    The Filer instance used to create binary binding files and source text files.
     * @param options  Options specifying an error listener and/or validation behavior.
     */
    public static SchemaTypeSystem compileXmlBeans(String name, SchemaTypeSystem system, XmlObject[] schemas, BindingConfig config, SchemaTypeLoader typepath, Filer filer, XmlOptions options) throws XmlException {
        return SchemaTypeSystemCompiler.compile(name, system, schemas, config, typepath != null ? typepath : getContextTypeLoader(), filer, options);
    }


    /**
     * Returns the union of a list of typeLoaders. The returned
     * SchemaTypeLoader searches the given list of SchemaTypeLoaders
     * in order from first to last.
     */
    public static SchemaTypeLoader typeLoaderUnion(SchemaTypeLoader... typeLoaders) {
        return (typeLoaders.length == 1) ? typeLoaders[0] : SchemaTypeLoaderImpl.build(typeLoaders, null, null);
    }

    /**
     * Returns a SchemaTypeLoader that searches for compiled schema types
     * in the given ClassLoader.
     */
    public static SchemaTypeLoader typeLoaderForClassLoader(ClassLoader loader) {
        return SchemaTypeLoaderImpl.build(null, null, loader);
    }

    /**
     * Returns a SchemaTypeLoader that searches for compiled schema types
     * in the given ResourceLoader.
     *
     * @see XmlBeans#resourceLoaderForPath(File[])
     */
    public static SchemaTypeLoader typeLoaderForResource(ResourceLoader resourceLoader) {
        return SchemaTypeLoaderImpl.build(null, resourceLoader, null);
    }

    /**
     * Returns the SchemaTypeSystem of the given name (as returned by
     * {@link SchemaTypeSystem#getName}) for the given ClassLoader.
     * <p>
     * Note: you will almost always need typeLoaderForClassLoader()
     * instead (see {@link XmlBeans#typeLoaderForClassLoader}).
     */
    public static SchemaTypeSystem typeSystemForClassLoader(ClassLoader loader, String stsName) {
        try {
            ClassLoader cl = loader == null ? Thread.currentThread().getContextClassLoader() : loader;
            Class clazz = cl.loadClass(stsName + "." + HOLDER_CLASS_NAME);
            SchemaTypeSystem sts = (SchemaTypeSystem)
                (clazz.getDeclaredField(TYPE_SYSTEM_FIELD).get(null));
            if (sts == null) {
                throw new RuntimeException("SchemaTypeSystem is null for field " +
                                           TYPE_SYSTEM_FIELD + " on class with name " + stsName +
                                           "." + HOLDER_CLASS_NAME +
                                           ". Please verify the version of xmlbeans.jar is correct.");
            }
            return sts;
        } catch (ClassNotFoundException e) {
            throw causedException(new RuntimeException("Cannot load SchemaTypeSystem. " +
                                                       "Unable to load class with name " + stsName + "." + HOLDER_CLASS_NAME +
                                                       ". Make sure the generated binary files are on the classpath."), e);
        } catch (NoSuchFieldException e) {
            throw causedException(new RuntimeException("Cannot find field " +
                                                       TYPE_SYSTEM_FIELD + " on class " + stsName + "." + HOLDER_CLASS_NAME +
                                                       ". Please verify the version of xmlbeans.jar is correct."), e);
        } catch (IllegalAccessException e) {
            throw causedException(new RuntimeException("Field " +
                                                       TYPE_SYSTEM_FIELD + " on class " + stsName + "." + HOLDER_CLASS_NAME +
                                                       "is not accessible. Please verify the version of xmlbeans.jar is correct."), e);
        }
    }

    /**
     * Returns a new ResourceLoader for a search path where each component of
     * the path is either a directory or a compiled xmlbeans jar.
     */
    public static ResourceLoader resourceLoaderForPath(File[] path) {
        return new PathResourceLoader(path);
    }

    /**
     * Returns the SchemaType from a corresponding XmlObject subclass,
     * or null if none.
     */
    public static SchemaType typeForClass(Class c) {
        if (c == null || !XmlObject.class.isAssignableFrom(c)) {
            return null;
        }

        try {
            Field typeField = c.getField("type");

            if (typeField == null) {
                return null;
            }

            return (SchemaType) typeField.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static SchemaType getNoType() {
        return org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem.getNoType();
    }

    /**
     * The SchemaType object given to an XmlObject instance when
     * no type can be determined.
     * <p>
     * The NO_TYPE is the universal derived type.  That is, it is
     * derived from all other schema types, and no instances of the
     * NO_TYPE are valid. (It is not to be confused with the anyType,
     * which is the universal base type from which all other types
     * can be derived, and of which all instances are valid.)
     */
    public static final SchemaType NO_TYPE = getNoType();

    private XmlBeans() {
    }
}
