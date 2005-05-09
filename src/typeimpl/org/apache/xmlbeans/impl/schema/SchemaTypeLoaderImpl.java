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

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XBeanDebug;
import javax.xml.namespace.QName;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.IdentityHashMap;

import java.lang.ref.SoftReference;

public class SchemaTypeLoaderImpl extends SchemaTypeLoaderBase
{
    private ResourceLoader _resourceLoader;
    private ClassLoader _classLoader;
    private SchemaTypeLoader[] _searchPath;

    private Map _classpathTypeSystems;
    private Map _classLoaderTypeSystems;
    private Map _elementCache;
    private Map _attributeCache;
    private Map _modelGroupCache;
    private Map _attributeGroupCache;
    private Map _idConstraintCache;
    private Map _typeCache;
    private Map _documentCache;
    private Map _classnameCache;

    // The following maintains a cache of SchemaTypeLoaders per ClassLoader per Thread.
    // I use soft references to allow the garbage collector to reclain the type loaders
    // and/pr class loaders at will.

    private static ThreadLocal _cachedTypeSystems =
        new ThreadLocal() { protected Object initialValue() { return new ArrayList(); } };

    public static SchemaTypeLoaderImpl getContextTypeLoader ( )
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        ArrayList a = (ArrayList) _cachedTypeSystems.get();

        int candidate = -1;
        SchemaTypeLoaderImpl result = null;

        for ( int i = 0 ; i < a.size() ; i++ )
        {
            SchemaTypeLoaderImpl tl = (SchemaTypeLoaderImpl) ((SoftReference) a.get( i )).get();

            if (tl == null)
                a.remove( i-- );
            else if (tl._classLoader == cl)
            {
                candidate = i;
                result = tl;
            }
        }

        if (candidate == -1)
        {
            result =  new SchemaTypeLoaderImpl( new SchemaTypeLoader[] { BuiltinSchemaTypeSystem.get() } , null, cl );
            a.add( new SoftReference( result ) );
            candidate = a.size() - 1;
        }

        if (candidate > 0)
        {
            Object t = a.get( 0 );
            a.set( 0, a.get( candidate ) );
            a.set( candidate, t );
        }

        return result;
    }
    
    public static SchemaTypeLoader build(SchemaTypeLoader[] searchPath, ResourceLoader resourceLoader, ClassLoader classLoader)
    {
        if (searchPath == null)
        {
            searchPath = EMPTY_SCHEMATYPELOADER_ARRAY;
        }
        else
        {
            // assemble a flattened search path with no duplicates
            SubLoaderList list = new SubLoaderList();
            for (int i = 0; i < searchPath.length; i++)
            {
                if (searchPath[i] == null)
                    throw new IllegalArgumentException("searchPath[" + i + "] is null");
                if (!(searchPath[i] instanceof SchemaTypeLoaderImpl))
                    list.add(searchPath[i]);
                else
                {
                    SchemaTypeLoaderImpl sub = (SchemaTypeLoaderImpl)searchPath[i];
                    if (sub._classLoader != null || sub._resourceLoader != null)
                        list.add(sub);
                    else for (int j = 0; j < sub._searchPath.length; j++)
                        list.add(sub._searchPath[j]);
                }
            }
            searchPath = list.toArray();
        }

        if (searchPath.length == 1 && resourceLoader == null && classLoader == null)
            return searchPath[0];

        return new SchemaTypeLoaderImpl(searchPath, resourceLoader, classLoader);
    }

    /**
     * Just used to avoid duplicate path entries
     */
    private static class SubLoaderList
    {
        private List theList = new ArrayList();
        private Map seen = new IdentityHashMap();

        private boolean add(SchemaTypeLoader loader)
        {
            if (seen.containsKey(loader))
                return false;
            theList.add(loader);
            seen.put(loader, null);
            return true;
        }

        private SchemaTypeLoader[] toArray()
        {
            if (theList.size() == 0)
                return EMPTY_SCHEMATYPELOADER_ARRAY;
            return (SchemaTypeLoader[])theList.toArray(new SchemaTypeLoader[theList.size()]);
        }
    }

    /**
     * Constructs a SchemaTypeLoaderImpl that searches for objects
     * in the following order:
     *
     * (1) First on the searchPath of other SchemaTypeSystems supplied,
     *     in order that they are listed.
     * (2) Next on the classpath of .jar files or directories supplied,
     *     in the order that they are listed. When types are returned in
     *     this way, they are instantiated from a private typesystem.
     *     In other words, if a type is loaded from another SchemaTypeLoaderImpl
     *     that was initialized on the same file, the instance of the type will
     *     be different.
     * (3) Finally on the classloader supplied.
     */
    private SchemaTypeLoaderImpl(SchemaTypeLoader[] searchPath, ResourceLoader resourceLoader, ClassLoader classLoader)
    {
        if (searchPath == null)
            _searchPath = EMPTY_SCHEMATYPELOADER_ARRAY;
        else
            _searchPath = searchPath;
        _resourceLoader = resourceLoader;
        _classLoader = classLoader;

        initCaches();
    }

    /**
     * Initializes the caches.
     */
    private final void initCaches()
    {
        _classpathTypeSystems = Collections.synchronizedMap(new HashMap());
        _classLoaderTypeSystems = Collections.synchronizedMap(new HashMap());
        _elementCache = Collections.synchronizedMap(new HashMap());
        _attributeCache = Collections.synchronizedMap(new HashMap());
        _modelGroupCache = Collections.synchronizedMap(new HashMap());
        _attributeGroupCache = Collections.synchronizedMap(new HashMap());
        _idConstraintCache = Collections.synchronizedMap(new HashMap());
        _typeCache = Collections.synchronizedMap(new HashMap());
        _documentCache = Collections.synchronizedMap(new HashMap());
        _classnameCache = Collections.synchronizedMap(new HashMap());
    }

    SchemaTypeSystemImpl typeSystemForComponent(String searchdir, QName name)
    {
        String searchfor = searchdir + QNameHelper.hexsafedir(name) + ".xsb";
        String tsname = null;

        if (_resourceLoader != null)
            tsname = crackEntry(_resourceLoader, searchfor);

        if (_classLoader != null)
            tsname = crackEntry(_classLoader, searchfor);

        if (tsname != null)
            return (SchemaTypeSystemImpl)typeSystemForName(tsname);

        return null;
    }

    public SchemaTypeSystem typeSystemForName(String name)
    {
        if (_resourceLoader != null)
        {
            SchemaTypeSystem result = getTypeSystemOnClasspath(name);
            if (result != null)
                return result;
        }

        if (_classLoader != null)
        {
            SchemaTypeSystem result = getTypeSystemOnClassloader(name);
            if (result != null)
                return result;
        }
        return null;
    }

    SchemaTypeSystemImpl typeSystemForClassname(String searchdir, String name)
    {
        String searchfor = searchdir + name.replace('.', '/') + ".xsb";

        if (_resourceLoader != null)
        {
            String tsname = crackEntry(_resourceLoader, searchfor);
            if (tsname != null)
                return getTypeSystemOnClasspath(tsname);
        }

        if (_classLoader != null)
        {
            String tsname = crackEntry(_classLoader, searchfor);
            if (tsname != null)
                return getTypeSystemOnClassloader(tsname);
        }

        return null;
    }

    SchemaTypeSystemImpl getTypeSystemOnClasspath(String name)
    {
        SchemaTypeSystemImpl result = (SchemaTypeSystemImpl)_classpathTypeSystems.get(name);
        if (result == null)
        {
            result = new SchemaTypeSystemImpl(_resourceLoader, name, this);
            _classpathTypeSystems.put(name, result);
        }
        return result;
    }

    SchemaTypeSystemImpl getTypeSystemOnClassloader(String name)
    {
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Finding type system " + name + " on classloader", 0);
        SchemaTypeSystemImpl result = (SchemaTypeSystemImpl)_classLoaderTypeSystems.get(name);
        if (result == null)
        {
            XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Type system " + name + " not cached - consulting field", 0);
            result = SchemaTypeSystemImpl.forName(name, _classLoader);
            _classLoaderTypeSystems.put(name, result);
        }
        return result;
    }

    static String crackEntry(ResourceLoader loader, String searchfor)
    {
        InputStream is = loader.getResourceAsStream(searchfor);
        if (is == null)
            return null;
        return crackPointer(is);
    }

    static String crackEntry(ClassLoader loader, String searchfor)
    {
        InputStream stream = loader.getResourceAsStream(searchfor);
        if (stream == null)
            return null;
        return crackPointer(stream);
    }

    static String crackPointer(InputStream stream)
    {
        return SchemaTypeSystemImpl.crackPointer(stream);
    }

    public boolean isNamespaceDefined(String namespace)
    {
        for (int i = 0; i < _searchPath.length; i++)
            if (_searchPath[i].isNamespaceDefined(namespace))
                return true;
        
        SchemaTypeSystem sts = typeSystemForComponent("schema/namespace/", new QName(namespace, "xmlns"));
        return (sts != null);
    }

    public SchemaType.Ref findTypeRef(QName name)
    {
        SchemaType.Ref result = (SchemaType.Ref)_typeCache.get(name);
        if (result == null && !_typeCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findTypeRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/type/", name);
                if (ts != null)
                {
                    result = ts.findTypeRef(name);
                    assert(result != null) : "Type system registered type " + QNameHelper.pretty(name) + " but does not return it";
                }
            }
            _typeCache.put(name, result);
        }
        return result;
    }

    public SchemaType typeForClassname(String classname)
    {
        classname = classname.replace('$', '.');

        SchemaType result = (SchemaType)_classnameCache.get(classname);
        if (result == null && !_classnameCache.containsKey(classname))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].typeForClassname(classname)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForClassname("schema/javaname/", classname);
                if (ts != null)
                {
                    result = ts.typeForClassname(classname);
                    assert(result != null) : "Type system registered type " + classname + " but does not return it";
                }
            }
            _classnameCache.put(classname, result);
        }
        return result;
    }

    public SchemaType.Ref findDocumentTypeRef(QName name)
    {
        SchemaType.Ref result = (SchemaType.Ref)_documentCache.get(name);
        if (result == null && !_documentCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findDocumentTypeRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/element/", name);
                if (ts != null)
                {
                    result = ts.findDocumentTypeRef(name);
                    assert(result != null) : "Type system registered element " + QNameHelper.pretty(name) + " but does not contain document type";
                }
            }
            _documentCache.put(name, result);
        }
        return result;
    }

    public SchemaType.Ref findAttributeTypeRef(QName name)
    {
        SchemaType.Ref result = (SchemaType.Ref)_attributeCache.get(name);
        if (result == null && !_attributeCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findAttributeTypeRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/attribute/", name);
                if (ts != null)
                {
                    result = ts.findAttributeTypeRef(name);
                    assert(result != null) : "Type system registered attribute " + QNameHelper.pretty(name) + " but does not contain attribute type";
                }
            }
            _attributeCache.put(name, result);
        }
        return result;
    }

    public SchemaGlobalElement.Ref findElementRef(QName name)
    {
        SchemaGlobalElement.Ref result = (SchemaGlobalElement.Ref)_elementCache.get(name);
        if (result == null && !_elementCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findElementRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/element/", name);
                if (ts != null)
                {
                    result = ts.findElementRef(name);
                    assert(result != null) : "Type system registered element " + QNameHelper.pretty(name) + " but does not return it";
                }
            }
            _elementCache.put(name, result);
        }
        return result;
    }

    public SchemaGlobalAttribute.Ref findAttributeRef(QName name)
    {
        SchemaGlobalAttribute.Ref result = (SchemaGlobalAttribute.Ref)_attributeCache.get(name);
        if (result == null && !_attributeCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findAttributeRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/attribute/", name);
                if (ts != null)
                {
                    result = ts.findAttributeRef(name);
                    assert(result != null) : "Type system registered attribute " + QNameHelper.pretty(name) + " but does not return it";
                }
            }
            _attributeCache.put(name, result);
        }
        return result;
    }

    public SchemaModelGroup.Ref findModelGroupRef(QName name)
    {
        SchemaModelGroup.Ref result = (SchemaModelGroup.Ref)_modelGroupCache.get(name);
        if (result == null && !_modelGroupCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findModelGroupRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/modelgroup/", name);
                if (ts != null)
                {
                    result = ts.findModelGroupRef(name);
                    assert(result != null) : "Type system registered model group " + QNameHelper.pretty(name) + " but does not return it";
                }
            }
            _modelGroupCache.put(name, result);
        }
        return result;
    }

    public SchemaAttributeGroup.Ref findAttributeGroupRef(QName name)
    {
        SchemaAttributeGroup.Ref result = (SchemaAttributeGroup.Ref)_attributeGroupCache.get(name);
        if (result == null && !_attributeGroupCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findAttributeGroupRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/attributegroup/", name);
                if (ts != null)
                {
                    result = ts.findAttributeGroupRef(name);
                    assert(result != null) : "Type system registered attribute group " + QNameHelper.pretty(name) + " but does not return it";
                }
            }
            _attributeGroupCache.put(name, result);
        }
        return result;
    }

    public SchemaIdentityConstraint.Ref findIdentityConstraintRef(QName name)
    {
        SchemaIdentityConstraint.Ref result = (SchemaIdentityConstraint.Ref)_idConstraintCache.get(name);
        if (result == null && !_idConstraintCache.containsKey(name))
        {
            for (int i = 0; i < _searchPath.length; i++)
                if (null != (result = _searchPath[i].findIdentityConstraintRef(name)))
                    break;
            if (result == null)
            {
                SchemaTypeSystem ts = typeSystemForComponent("schema/identityconstraint/", name);
                if (ts != null)
                {
                    result = ts.findIdentityConstraintRef(name);
                    assert(result != null) : "Type system registered identity constraint " + QNameHelper.pretty(name) + " but does not return it";
                }
            }
            _idConstraintCache.put(name, result);
        }
        return result;
    }

    public InputStream getSourceAsStream(String sourceName)
    {
        InputStream result = null;

        if (!sourceName.startsWith("/"))
            sourceName = "/" + sourceName;

        if (_resourceLoader != null)
            result = _resourceLoader.getResourceAsStream("schema/src" + sourceName);

        if (result == null && _classLoader != null)
            return _classLoader.getResourceAsStream("schema/src" + sourceName);

        return result;
    }

    private static final SchemaTypeLoader[] EMPTY_SCHEMATYPELOADER_ARRAY = new SchemaTypeLoader[0];
}
