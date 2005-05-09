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

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.ref.SoftReference;


/**
 * Provides an assortment of utilities
 * for managing XML Bean types, type systems, QNames, paths,
 * and queries.
 */
public final class XmlBeans
{
    private static String XMLBEANS_TITLE = "org.apache.xmlbeans";
    private static String XMLBEANS_VERSION = "1.0.4";
    private static String XMLBEANS_VENDOR = "Apache Software Foundation";

    static
    {
        Package pkg = XmlBeans.class.getPackage();
        if (pkg != null)
        {
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
    public static final String getTitle()
    {
        return XMLBEANS_TITLE;
    }

    /**
     * Returns the XmlBeans vendor, "Apache Software Foundation",
     * the value of
     * {@link Package#getImplementationVendor() XmlBeans.class.getPackage().getImplementationVendor()}.
     */
    public static final String getVendor()
    {
        return XMLBEANS_VENDOR;
    }

    /**
     * Returns the XmlBeans version
     * the value of
     * {@link Package#getImplementationVersion() XmlBeans.class.getPackage().getImplementationVersion()}.
     */
    public static final String getVersion()
    {
        return XMLBEANS_VERSION;
    }

    /**
     * Thread local QName cache for general use
     */
    private static final ThreadLocal _threadLocalLoaderQNameCache =
        new ThreadLocal()
        {
            protected Object initialValue()
            {
                return new SoftReference(new QNameCache( 32 ));
            }
        };

    /**
     * Returns a thread local QNameCache
     */
    public static QNameCache getQNameCache ( )
    {
        SoftReference softRef = (SoftReference)_threadLocalLoaderQNameCache.get();
        QNameCache qnameCache = (QNameCache) (softRef).get();
        if (qnameCache==null)
        {
            qnameCache = new QNameCache( 32 );
            _threadLocalLoaderQNameCache.set(new SoftReference(qnameCache));
        }
        return qnameCache;
    }

    /**
     * Obtains a name from the thread local QNameCache
     */
    public static QName getQName ( String localPart )
    {
        return getQNameCache().getName( "",  localPart );
    }

    /**
     * Obtains a name from the thread local QNameCache
     */

    public static QName getQName ( String namespaceUri, String localPart )
    {
        return getQNameCache().getName( namespaceUri,  localPart );
    }

    private static final Method _getContextTypeLoaderMethod = buildGetContextTypeLoaderMethod();
    private static final Method _getBuiltinSchemaTypeSystemMethod = buildGetBuiltinSchemaTypeSystemMethod();
    private static final Method _getNoTypeMethod = buildGetNoTypeMethod();
    private static final Method _typeLoaderBuilderMethod = buildTypeLoaderBuilderMethod();
    private static final Method _compilationMethod = buildCompilationMethod();

    private static RuntimeException causedException(RuntimeException e, Throwable cause)
    {
        e.initCause(cause);
        return e;
    }

    private static XmlException wrappedException(Throwable e)
    {
        if (e instanceof XmlException)
            return (XmlException) e;

        return new XmlException( e.getMessage(), e );
    }

    private static Method buildGetContextTypeLoaderMethod()
    {
        try
        {
            return Class.forName("org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl", false, XmlBeans.class.getClassLoader()).getMethod("getContextTypeLoader", new Class[0]);
        }
        catch (Exception e)
        {
            throw causedException(new IllegalStateException("Cannot load SchemaTypeLoaderImpl: verify that xbean.jar is on the classpath"), e);
        }
    }


    private static final Method buildGetBuiltinSchemaTypeSystemMethod()
    {
        try
        {
            return Class.forName("org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem", false, XmlBeans.class.getClassLoader()).getMethod("get", new Class[0]);
        }
        catch (Exception e)
        {
            throw causedException(new IllegalStateException("Cannot load BuiltinSchemaTypeSystem: verify that xbean.jar is on the classpath"), e);
        }
    }

    private static final Method buildGetNoTypeMethod()
    {
        try
        {
            return Class.forName("org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem", false, XmlBeans.class.getClassLoader()).getMethod("getNoType", new Class[0]);
        }
        catch (Exception e)
        {
            throw causedException(new IllegalStateException("Cannot load BuiltinSchemaTypeSystem: verify that xbean.jar is on the classpath"), e);
        }
    }


    private static final Method buildTypeLoaderBuilderMethod()
    {
        try
        {
            Class resourceLoaderClass = Class.forName("org.apache.xmlbeans.impl.schema.ResourceLoader", false, XmlBeans.class.getClassLoader());
            return Class.forName("org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl", false, XmlBeans.class.getClassLoader()).getMethod("build", new Class[] { SchemaTypeLoader[].class, resourceLoaderClass, ClassLoader.class });
        }
        catch (Exception e)
        {
            throw causedException(new IllegalStateException("Cannot load SchemaTypeLoaderImpl: verify that xbean.jar is on the classpath"), e);
        }
    }


    private static final Method buildCompilationMethod()
    {
        try
        {
            return Class.forName("org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl", false, XmlBeans.class.getClassLoader()).getMethod("forSchemaXml", new Class[] { XmlObject[].class, SchemaTypeLoader.class, XmlOptions.class });
        }
        catch (Exception e)
        {
            throw causedException(new IllegalStateException("Cannot load SchemaTypeSystemImpl: verify that xbean.jar is on the classpath"), e);
        }
    }

    /**
     * Compiles an XPath, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached.
     */
    public static String compilePath ( String pathExpr ) throws XmlException
    {
        return compilePath( pathExpr, null );
    }
    
    /**
     * Compiles an XPath, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached; 
     * takes an option for specifying text that indicates the name of context node.
     * The default is "this", as in "$this".
     * 
     * @param  options  Options for the path. For example, you can call 
     * the {@link XmlOptions#setXqueryCurrentNodeVar(String) XmlOptions.setXqueryCurrentNodeVar(String)}
     * method to specify a particular name for the expression 
     * variable that indicates the context node.
     */
    public static String compilePath ( String pathExpr, XmlOptions options )
        throws XmlException
    {
        return getContextTypeLoader().compilePath( pathExpr, options );
    }
    
    /**
     * Compiles an XQuery, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached.
     */
    public static String compileQuery ( String queryExpr ) throws XmlException
    {
        return compileQuery( queryExpr, null );
    }
    
    /**
     * Compiles an XQuery, returning a String equal to that which was passed,
     * but whose identity is that of one which has been precompiled and cached;
     * takes an option for specifying text that indicates the context node.
     * 
     * @param  options  Options for the query. For example, you can call 
     * the {@link XmlOptions#setXqueryCurrentNodeVar(String) XmlOptions.setXqueryCurrentNodeVar(String)}
     * method to specify a particular name for the expression 
     * variable that indicates the context node and the
     * {@link XmlOptions#setXqueryVariables(Map) XmlOptions.setXqueryVariables(Map)}
     * method to map external variable names to values.
     */
    public static String compileQuery ( String queryExpr, XmlOptions options )
        throws XmlException
    {
        return getContextTypeLoader().compileQuery( queryExpr, options );
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
    public static SchemaTypeLoader getContextTypeLoader()
    {
        try
        {
            return (SchemaTypeLoader)_getContextTypeLoaderMethod.invoke(null, null);
        }
        catch (IllegalAccessException e)
        {
            throw causedException(new IllegalStateException("No access to SchemaTypeLoaderImpl.getContextTypeLoader(): verify that version of xbean.jar is correct"), e);
        }
        catch (InvocationTargetException e)
        {
            throw causedException(new IllegalStateException(e.getMessage()), e.getCause());
        }
    }

    /**
     * Returns the builtin type system. This SchemaTypeSystem contains
     * only the 46 builtin types defined by the XML Schema specification.
     */
    public static SchemaTypeSystem getBuiltinTypeSystem()
    {
        try
        {
            return (SchemaTypeSystem)_getBuiltinSchemaTypeSystemMethod.invoke(null, null);
        }
        catch (IllegalAccessException e)
        {
            throw causedException(new IllegalStateException("No access to BuiltinSchemaTypeSystem.get(): verify that version of xbean.jar is correct"), e);
        }
        catch (InvocationTargetException e)
        {
            throw causedException(new IllegalStateException(e.getMessage()), e.getCause());
        }
    }

    /**
     * Returns the SchemaTypeSystem that results from compiling the XML
     * schema definitions passed.
     * <p>
     * Just like compileTypeSystem, but uses the context type loader for
     * linking, and returns a unioned typeloader that is suitable for
     * creating instances.
     */
    public static SchemaTypeLoader loadXsd(XmlObject[] schemas) throws XmlException
    {
        return loadXsd(schemas, null);
    }
    
    /**
     * <p>Returns the SchemaTypeSystem that results from compiling the XML
     * schema definitions passed in <em>schemas</em>.</p>
     * 
     * <p>This is just like compileTypeSystem, but uses the context type loader for
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
    public static SchemaTypeLoader loadXsd(XmlObject[] schemas, XmlOptions options) throws XmlException
    {
        try
        {
            SchemaTypeSystem sts =
                (SchemaTypeSystem)
                    _compilationMethod.invoke(
                        null, new Object[] { schemas, getContextTypeLoader(), options });

            if (sts == null)
                return null;
            
            return
                typeLoaderUnion(
                    new SchemaTypeLoader[] { sts, getContextTypeLoader() } );
        }
        catch (IllegalAccessException e)
        {
            throw causedException(new IllegalStateException("No access to SchemaTypeLoaderImpl.forSchemaXml(): verify that version of xbean.jar is correct"), e);
        }
        catch (InvocationTargetException e)
        {
            throw wrappedException(e.getCause());
        }
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
     * @param schemas The schema definitions from which to build the schema type system.
     * @param typepath The path to already-compiled schema types for linking while processing.
     * @param options Options specifying an error listener and/or validation behavior.
     */
    public static SchemaTypeSystem compileXsd(XmlObject[] schemas, SchemaTypeLoader typepath, XmlOptions options) throws XmlException
    {
        if (typepath == null)
            throw new IllegalArgumentException("Must supply a SchemaTypeLoader for compiletime linking");

        try
        {
            return (SchemaTypeSystem)_compilationMethod.invoke(null, new Object[] { schemas, typepath, options });
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("No access to SchemaTypeLoaderImpl.forSchemaXml(): verify that version of xbean.jar is correct");
        }
        catch (InvocationTargetException e)
        {
            throw wrappedException(e.getCause());
        }
    }
    
    /**
     * Returns the union of a list of typeLoaders. The returned
     * SchemaTypeLoader searches the given list of SchemaTypeLoaders
     * in order from first to last.
     */
    public static SchemaTypeLoader typeLoaderUnion(SchemaTypeLoader[] typeLoaders)
    {
        try
        {
            if (typeLoaders.length == 1)
                return typeLoaders[0];

            return (SchemaTypeLoader)_typeLoaderBuilderMethod.invoke(null, new Object[] {typeLoaders, null, null});
        }
        catch (IllegalAccessException e)
        {
            throw causedException(new IllegalStateException("No access to SchemaTypeLoaderImpl: verify that version of xbean.jar is correct"), e);
        }
        catch (InvocationTargetException e)
        {
            throw causedException(new IllegalStateException(e.getMessage()), e.getCause());
        }
    }

    /**
     * Returns a SchemaTypeLoader that searches for compiled schema types
     * in the given ClassLoader.
     */
    public static SchemaTypeLoader typeLoaderForClassLoader(ClassLoader loader)
    {
        try
        {
            return (SchemaTypeLoader)_typeLoaderBuilderMethod.invoke(null, new Object[] {null, null, loader});
        }
        catch (IllegalAccessException e)
        {
            throw causedException(new IllegalStateException("No access to SchemaTypeLoaderImpl: verify that version of xbean.jar is correct"), e);
        }
        catch (InvocationTargetException e)
        {
            throw causedException( new IllegalStateException(e.getMessage()), e );
        }
    }

    /**
     * Returns the SchemaType from a corresponding XmlObject subclass,
     * or null if none.
     */
    public static SchemaType typeForClass(Class c)
    {
        if (c == null || !XmlObject.class.isAssignableFrom(c))
            return null;

        try
        {
            Field typeField = c.getField("type");
            
            if (typeField == null)
                return null;

            return (SchemaType)typeField.get(null);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static SchemaType getNoType()
    {
        try
        {
            return (SchemaType)_getNoTypeMethod.invoke(null, null);
        }
        catch (IllegalAccessException e)
        {
            throw causedException(new IllegalStateException("No access to SchemaTypeLoaderImpl.getContextTypeLoader(): verify that version of xbean.jar is correct"), e);
        }
        catch (InvocationTargetException e)
        {
            throw causedException(new IllegalStateException(e.getMessage()), e.getCause());
        }
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
    public static SchemaType NO_TYPE = getNoType();

    private XmlBeans ( ) { }
}
