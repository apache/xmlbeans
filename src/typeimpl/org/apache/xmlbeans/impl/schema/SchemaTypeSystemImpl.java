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

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XBeanDebug;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.math.BigInteger;


import org.apache.xmlbeans.impl.regex.RegularExpression;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.impl.values.XmlObjectBase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.SchemaTypeLoaderException;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument.Config;
import org.apache.xml.xmlbeans.x2004.x02.xbean.config.ConfigDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3.x2001.xmlSchema.GroupDocument;
import org.w3.x2001.xmlSchema.AttributeGroupDocument;
import org.w3.x2001.xmlSchema.SchemaDocument.Schema;

public class SchemaTypeSystemImpl extends SchemaTypeLoaderBase implements SchemaTypeSystem
{
    public static final int DATA_BABE = 0xDA7ABABE;
    public static final int MAJOR_VERSION = 2;  // must match == to be compatible
    public static final int MINOR_VERSION = 18; // must be <= to be compatible
    public static final int RELEASE_NUMBER = 0; // should be compatible even if < or >

    public static final int FILETYPE_SCHEMAINDEX = 1;
    public static final int FILETYPE_SCHEMATYPE = 2;
    public static final int FILETYPE_SCHEMAELEMENT = 3;
    public static final int FILETYPE_SCHEMAATTRIBUTE = 4;
    public static final int FILETYPE_SCHEMAPOINTER = 5;
    public static final int FILETYPE_SCHEMAMODELGROUP = 6;
    public static final int FILETYPE_SCHEMAATTRIBUTEGROUP = 7;
    public static final int FILETYPE_SCHEMAIDENTITYCONSTRAINT = 8;

    public static final int FLAG_PART_SKIPPABLE = 1;
    public static final int FLAG_PART_FIXED = 4;
    public static final int FLAG_PART_NILLABLE = 8;
    public static final int FLAG_PART_BLOCKEXT = 16;
    public static final int FLAG_PART_BLOCKREST = 32;
    public static final int FLAG_PART_BLOCKSUBST = 64;
    public static final int FLAG_PART_ABSTRACT = 128;
    public static final int FLAG_PART_FINALEXT = 256;
    public static final int FLAG_PART_FINALREST = 512;

    public static final int FLAG_PROP_ISATTR = 1;
    public static final int FLAG_PROP_JAVASINGLETON = 2;
    public static final int FLAG_PROP_JAVAOPTIONAL = 4;
    public static final int FLAG_PROP_JAVAARRAY = 8;

    public static final int FIELD_NONE = 0;
    public static final int FIELD_GLOBAL = 1;
    public static final int FIELD_LOCALATTR = 2;
    public static final int FIELD_LOCALELT = 3;

    // type flags
    static final int FLAG_SIMPLE_TYPE     = 0x1;
    static final int FLAG_DOCUMENT_TYPE   = 0x2;
    static final int FLAG_ORDERED         = 0x4;
    static final int FLAG_BOUNDED         = 0x8;
    static final int FLAG_FINITE          = 0x10;
    static final int FLAG_NUMERIC         = 0x20;
    static final int FLAG_STRINGENUM      = 0x40;
    static final int FLAG_UNION_OF_LISTS  = 0x80;
    static final int FLAG_HAS_PATTERN     = 0x100;
    static final int FLAG_ORDER_SENSITIVE = 0x200;
    static final int FLAG_TOTAL_ORDER     = 0x400;
    static final int FLAG_COMPILED        = 0x800;
    static final int FLAG_BLOCK_EXT       = 0x1000;
    static final int FLAG_BLOCK_REST      = 0x2000;
    static final int FLAG_FINAL_EXT       = 0x4000;
    static final int FLAG_FINAL_REST      = 0x8000;
    static final int FLAG_FINAL_UNION     = 0x10000;
    static final int FLAG_FINAL_LIST      = 0x20000;
    static final int FLAG_ABSTRACT        = 0x40000;
    static final int FLAG_ATTRIBUTE_TYPE  = 0x80000;


    private static String nameToPathString(String nameForSystem)
    {
        nameForSystem = nameForSystem.replace('.', '/');

        if (!nameForSystem.endsWith("/") && nameForSystem.length() > 0)
            nameForSystem = nameForSystem + "/";

        return nameForSystem;
    }

    public SchemaTypeSystemImpl(Class indexclass)
    {
        String fullname = indexclass.getName();
        _name = fullname.substring(0, fullname.lastIndexOf('.'));
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Loading type system " + _name, 1);
        _basePackage = nameToPathString(_name);
        _classloader = indexclass.getClassLoader();
        _linker = SchemaTypeLoaderImpl.build(null, null, _classloader);
        _resourceLoader = new ClassLoaderResourceLoader(_classloader);
        initFromHeader();
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Finished loading type system " + _name, -1);
    }

    private static final Schema[] EMPTY_SCHEMA_ARRAY = new Schema[0];
    private static final Config[] EMPTY_CONFIG_ARRAY = new Config[0];

    public static SchemaTypeSystemImpl forSchemaXml(
        XmlObject[] input, SchemaTypeLoader linkTo, XmlOptions options)
            throws XmlException
    {
        options = XmlOptions.maskNull(options);
        ArrayList schemas = new ArrayList();
        ArrayList configs = new ArrayList();

        for (int i = 0; i < input.length; i++)
        {
            if (input[i] instanceof Schema)
                schemas.add(input[i]);
            else if (input[i] instanceof SchemaDocument && ((SchemaDocument)input[i]).getSchema() != null)
                schemas.add(((SchemaDocument)input[i]).getSchema());
            else if (input[i] instanceof Config)
                configs.add(input[i]);
            else if (input[i] instanceof ConfigDocument && ((ConfigDocument)input[i]).getConfig() != null)
                configs.add(((ConfigDocument)input[i]).getConfig());
            else
                throw new XmlException("Thread " + Thread.currentThread().getName() +  ": The " + i + "th supplied input is not a schema or a config document: its type is " + input[i].schemaType());
        }



        Collection userErrors = (Collection)options.get(XmlOptions.ERROR_LISTENER);
        XmlErrorWatcher errorWatcher = new XmlErrorWatcher(userErrors);

        SchemaTypeSystemImpl stsi =
            SchemaTypeSystemCompiler.compileImpl(
                null, (Schema[])schemas.toArray(EMPTY_SCHEMA_ARRAY),
                (Config[])configs.toArray(EMPTY_CONFIG_ARRAY), linkTo, options, errorWatcher, false, null, null);

        if (errorWatcher.hasError())
        {
            throw new XmlException(errorWatcher.firstError());
        }

        return stsi;
    }

    public static boolean fileContainsTypeSystem(File file, String name)
    {
        String indexname = nameToPathString(name) + "index.xsb";

        if (file.isDirectory())
        {
            return (new File(file, indexname)).isFile();
        }
        else
        {
            ZipFile zipfile = null;
            try
            {
                zipfile = new ZipFile(file);
                ZipEntry entry = zipfile.getEntry(indexname);
                return (entry != null && !entry.isDirectory());
            }
            catch (IOException e)
            {
                XBeanDebug.log("Problem loading SchemaTypeSystem, zipfilename " + file);
                XBeanDebug.logException(e);
                throw new SchemaTypeLoaderException(e.getMessage(), name, "index", SchemaTypeLoaderException.IO_EXCEPTION);
            }
            finally
            {
                if (zipfile != null)
                    try { zipfile.close(); } catch (IOException e) {}
            }
        }
    }

    public static SchemaTypeSystemImpl forName(String name, ClassLoader loader)
    {
        try
        {
            Class c = Class.forName(name + "." + SchemaTypeCodePrinter.INDEX_CLASSNAME, true, loader);
            return (SchemaTypeSystemImpl)c.getField("typeSystem").get(null);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public SchemaTypeSystemImpl(ResourceLoader resourceLoader, String name, SchemaTypeLoader linker)
    {
        _name = name;
        _basePackage = nameToPathString(_name);
        _linker = linker;
        _resourceLoader = resourceLoader;
        try
        {
            initFromHeader();
        }
        catch (RuntimeException e)
        {
            XBeanDebug.logException(e);
            throw e;
        }
        catch (Error e)
        {
            XBeanDebug.logException(e);
            throw e;
        }
    }

    private void initFromHeader()
    {
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Reading unresolved handles for type system " + _name, 0);
        XsbReader reader = null;
        try
        {
            // Read the index file, which starts with a header.
            reader = new XsbReader("index", FILETYPE_SCHEMAINDEX);

            // has a handle pool (count, handle/type, handle/type...)
            _localHandles = new HandlePool();
            reader.readHandlePool(_localHandles);

            // then a qname map of global elements (count, qname/handle, qname/handle...)
            _globalElements = reader.readQNameRefMap();

            // qname map of global attributes
            _globalAttributes = reader.readQNameRefMap();

            // qname map of model groups
            _modelGroups = reader.readQNameRefMap();

            // qname map of attribute groups
            _attributeGroups = reader.readQNameRefMap();

            _identityConstraints = reader.readQNameRefMap();

            // qname map of global types
            _globalTypes = reader.readQNameRefMap();

            // qname map of document types, by the qname of the contained element
            _documentTypes = reader.readQNameRefMap();

            // qname mape of attribute types, by the qname of the contained attribute
            _attributeTypes = reader.readQNameRefMap();

            // string map of all types, by fully qualified classname
            _typeRefsByClassname = reader.readClassnameRefMap();

            _namespaces = reader.readNamespaces();

            // support for redefine, at the end of the file
            if (reader.atLeast(2, 15, 0))
            {
                _redefinedGlobalTypes = reader.readQNameRefMap();
                _redefinedModelGroups = reader.readQNameRefMap();
                _redefinedAttributeGroups = reader.readQNameRefMap();
            }
        }
        finally
        {
            if (reader != null)
                reader.readEnd();
        }
    }

    void saveIndex()
    {
        String handle = "index";
        XsbReader saver = new XsbReader(handle);
        saver.writeIndexData();
        saver.writeRealHeader(handle, FILETYPE_SCHEMAINDEX);
        saver.writeIndexData();
        saver.writeEnd();
    }

    void savePointers()
    {
        savePointersForComponents(globalElements(), "schema/element/");
        savePointersForComponents(globalAttributes(), "schema/attribute/");
        savePointersForComponents(modelGroups(), "schema/modelgroup/");
        savePointersForComponents(attributeGroups(), "schema/attributegroup/");
        savePointersForComponents(globalTypes(), "schema/type/");
        savePointersForComponents(identityConstraints(), "schema/identityconstraint/");
        savePointersForNamespaces(_namespaces, "schema/namespace/");
        savePointersForClassnames(_typeRefsByClassname.keySet(), "schema/javaname/");
        savePointersForComponents(redefinedModelGroups(), "schema/redefinedmodelgroup/");
        savePointersForComponents(redefinedAttributeGroups(), "schema/redefinedattributegroup/");
        savePointersForComponents(redefinedGlobalTypes(), "schema/redefinedtype/");
    }

    void savePointersForComponents(SchemaComponent[] components, String dir)
    {
        for (int i = 0; i < components.length; i++)
        {
            savePointerFile(dir + QNameHelper.hexsafedir(components[i].getName()), _name);
        }
    }

    void savePointersForClassnames(Set classnames, String dir)
    {
        for (Iterator i = classnames.iterator(); i.hasNext(); )
        {
            String classname = (String)i.next();
            savePointerFile(dir + classname.replace('.', '/'), _name);
        }
    }

    void savePointersForNamespaces(Set namespaces, String dir)
    {
        for (Iterator i = namespaces.iterator(); i.hasNext(); )
        {
            String ns = (String)i.next();
            savePointerFile(dir + QNameHelper.hexsafedir(new QName(ns, "xmlns")), _name);
        }
    }

    void savePointerFile(String filename, String name)
    {
        XsbReader saver = new XsbReader(filename);
        saver.writeString(name);
        saver.writeRealHeader(filename, FILETYPE_SCHEMAPOINTER);
        saver.writeString(name);
        saver.writeEnd();
    }

    /**
     * Only used in the nonbootstrapped case.
     */
    private Map buildTypeRefsByClassname()
    {
        List allSeenTypes = new ArrayList();
        Map result = new LinkedHashMap();
        allSeenTypes.addAll(Arrays.asList(documentTypes()));
        allSeenTypes.addAll(Arrays.asList(attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(globalTypes()));

        // now fully javaize everything deeply.
        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType gType = (SchemaType)allSeenTypes.get(i);
            String className = gType.getFullJavaName();
            if (className != null)
            {
                result.put(className.replace('$', '.'), gType.getRef());
            }
            allSeenTypes.addAll(Arrays.asList(gType.getAnonymousTypes()));
        }
        return result;
    }

    private Map buildTypeRefsByClassname(Map typesByClassname)
    {
        Map result = new LinkedHashMap();
        for (Iterator i = typesByClassname.keySet().iterator(); i.hasNext(); )
        {
            String className = (String)i.next();
            result.put(className, ((SchemaType)typesByClassname.get(className)).getRef());
        }
        return result;
    }

    private static Map buildComponentRefMap(SchemaComponent[] components)
    {
        Map result = new LinkedHashMap();
        for (int i = 0; i < components.length; i++)
            result.put(components[i].getName(), components[i].getComponentRef());
        return result;
    }

    private static Map buildDocumentMap(SchemaType[] types)
    {
        Map result = new LinkedHashMap();
        for (int i = 0; i < types.length; i++)
            result.put(types[i].getDocumentElementName(), types[i].getRef());
        return result;
    }

    private static Map buildAttributeTypeMap(SchemaType[] types)
    {
        Map result = new LinkedHashMap();
        for (int i = 0; i < types.length; i++)
            result.put(types[i].getAttributeTypeAttributeName(), types[i].getRef());
        return result;
    }

    private static Random _random;
    private static byte[] _mask = new byte[128 / 8];

    /**
     * Fun, fun.  Produce 128 bits of uniqueness randomly.
     * We used to use SecureRandom, but now we don't because SecureRandom
     * hits the filesystem and hangs us on a filesystem lock.  It also eats
     * a thread and other expensive resources.. :-).
     *
     * We don't really care that non-secure Random() can only do 48 bits of
     * randomness, since we're certainly not going to be called more than 2^48
     * times within our process lifetime.
     *
     * Our real concern is that by seeding Random() with the current
     * time, two users will end up with the same bits if they start a
     * schema compilation within the same millisecond.  That makes the
     * probability of collision in the real world slightly too high.
     * We're going to have millions of users, remember?  With a million
     * users, and one-compilation-per-day each, we'd see a collision every
     * few months.
     *
     * So we'll just xor the results of random with our few extra
     * bits of information computed below to help reduce the probability
     * of collision by a few decimal places.  To collide, you will have had
     * to have the same amount of free memory, the same user name, timezone,
     * and country, the same current directory, the same java classpath,
     * the same operating system and jvm version, and the same choices of
     * identity hashcodes for a few objects. And be started within the same
     * millisecond. Or you can collide if you have a cosmic 128-bit mathematical
     * coincidence. No worries.
     */
    private static synchronized void nextBytes(byte[] result)
    {
        if (_random == null)
        {
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);

                // at least 10 bits of unqieueness, right?  Maybe even 50 or 60.
                daos.writeInt(System.identityHashCode(SchemaTypeSystemImpl.class));
                String[] props = new String[] { "user.name", "user.dir", "user.timezone", "user.country", "java.class.path", "java.home", "java.vendor", "java.version", "os.version" };
                for (int i = 0; i < props.length; i++)
                {
                    String prop = System.getProperty(props[i]);
                    if (prop != null)
                    {
                        daos.writeUTF(prop);
                        daos.writeInt(System.identityHashCode(prop));
                    }
                }
                daos.writeLong(Runtime.getRuntime().freeMemory());
                daos.close();
                byte[] bytes = baos.toByteArray();
                for (int i = 0; i < bytes.length; i++)
                {
                    int j = i % _mask.length;
                    _mask[j] *= 21;
                    _mask[j] += i;
                }
            }
            catch (IOException e)
            {
                XBeanDebug.logException(e);
            }

            _random = new Random(System.currentTimeMillis());
        }
        _random.nextBytes(result);
        for (int i = 0; i < result.length; i++)
        {
            int j = i & _mask.length;
            result[i] ^= _mask[j];
        }
    }

    public SchemaTypeSystemImpl(String nameForSystem)
    {
        // if we have no name, select a random one
        if (nameForSystem == null)
        {
            // get 128 random bits (that'll be 32 hex digits)
            byte[] bytes = new byte[128/8];
            nextBytes(bytes);
            nameForSystem = "s" + new String(HexBin.encode(bytes));
        }

        _name = "schema.system." + nameForSystem;
        _basePackage = nameToPathString(_name);
        _classloader = null;

    }

    public void loadFromBuilder(SchemaGlobalElement[] globalElements,
                                SchemaGlobalAttribute[] globalAttributes,
                                SchemaType[] globalTypes,
                                SchemaType[] documentTypes,
                                SchemaType[] attributeTypes)
    {
        assert(_classloader == null);
        _localHandles = new HandlePool();
        _globalElements = buildComponentRefMap(globalElements);
        _globalAttributes = buildComponentRefMap(globalAttributes);
        _globalTypes = buildComponentRefMap(globalTypes);
        _documentTypes = buildDocumentMap(documentTypes);
        _attributeTypes = buildDocumentMap(attributeTypes);
        _typeRefsByClassname = buildTypeRefsByClassname();
        _namespaces = new HashSet();
    }

    public void loadFromStscState(StscState state)
    {
        assert(_classloader == null);
        _localHandles = new HandlePool();
        _globalElements = buildComponentRefMap(state.globalElements());
        _globalAttributes = buildComponentRefMap(state.globalAttributes());
        _modelGroups = buildComponentRefMap(state.modelGroups());
        _redefinedModelGroups = buildComponentRefMap(state.redefinedModelGroups());
        _attributeGroups = buildComponentRefMap(state.attributeGroups());
        _redefinedAttributeGroups = buildComponentRefMap(state.redefinedAttributeGroups());
        _globalTypes = buildComponentRefMap(state.globalTypes());
        _redefinedGlobalTypes = buildComponentRefMap(state.redefinedGlobalTypes());
        _documentTypes = buildDocumentMap(state.documentTypes());
        _attributeTypes = buildAttributeTypeMap(state.attributeTypes());
        _typeRefsByClassname = buildTypeRefsByClassname(state.typesByClassname());
        _identityConstraints = buildComponentRefMap(state.idConstraints());
        _namespaces = new HashSet(Arrays.asList(state.getNamespaces()));
    }

    final SchemaTypeSystemImpl getTypeSystem()
    {
        return this;
    }

    static class StringPool
    {
        private List intsToStrings = new ArrayList();
        private Map stringsToInts = new HashMap();
        private String _handle;
        private String _name;

        /**
         * Constructs an empty StringPool to be filled with strings.
         */
        StringPool(String handle, String name)
        {
            _handle = handle;
            _name = name;
            intsToStrings.add(null);
        }

        int codeForString(String str)
        {
            if (str == null)
                return 0;
            Integer result = (Integer)stringsToInts.get(str);
            if (result == null)
            {
                result = new Integer(intsToStrings.size());
                intsToStrings.add(str);
                stringsToInts.put(str, result);
            }
            return result.intValue();
        }

        String stringForCode(int code)
        {
            if (code == 0)
                return null;
            return (String)intsToStrings.get(code);
        }

        void writeTo(DataOutputStream output)
        {
            if (intsToStrings.size() > Short.MAX_VALUE)
                throw new SchemaTypeLoaderException("Too many strings (" + intsToStrings.size() + ")", _name, _handle, SchemaTypeLoaderException.INT_TOO_LARGE);

            try
            {
                output.writeShort(intsToStrings.size());
                Iterator i = intsToStrings.iterator();
                for (i.next(); i.hasNext(); )
                {
                    String str = (String)i.next();
                    output.writeUTF(str);
                }
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        void readFrom(DataInputStream input)
        {
            if (intsToStrings.size() != 1 || stringsToInts.size() != 0)
                throw new IllegalStateException();

            try
            {
                int size = input.readShort();
                for (int i = 1; i < size; i++)
                {
                    String str = input.readUTF().intern();
                    int code = codeForString(str);
                    if (code != i)
                        throw new IllegalStateException();
                }
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage() == null ? e.getMessage() : "IO Exception", _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }
    }

    class HandlePool
    {
        private Map _handlesToRefs = new LinkedHashMap();
        private Map _componentsToHandles = new LinkedHashMap(); // populated on write
        private boolean _started;

        /**
         * Constructs an empty HandlePool to be populated.
         */
        HandlePool()
        {
        }

        private String addUniqueHandle(SchemaComponent obj, String base)
        {
            base = base.toLowerCase();  // we lowercase handles because of case-insensitive Windows filenames!!!
            String handle = base;
            for (int index = 2; _handlesToRefs.containsKey(handle); index++)
            {
                handle = base + index;
            }
            _handlesToRefs.put(handle, obj.getComponentRef());
            _componentsToHandles.put(obj, handle);
            return handle;
        }

        String handleForComponent(SchemaComponent comp)
        {
            if (comp == null)
                return null;
            if (comp.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            if (comp instanceof SchemaType)
                return handleForType((SchemaType)comp);
            if (comp instanceof SchemaGlobalElement)
                return handleForElement((SchemaGlobalElement)comp);
            if (comp instanceof SchemaGlobalAttribute)
                return handleForAttribute((SchemaGlobalAttribute)comp);
            if (comp instanceof SchemaModelGroup)
                return handleForModelGroup((SchemaModelGroup)comp);
            if (comp instanceof SchemaAttributeGroup)
                return handleForAttributeGroup((SchemaAttributeGroup)comp);
            if (comp instanceof SchemaIdentityConstraint)
                return handleForIdentityConstraint((SchemaIdentityConstraint)comp);
            throw new IllegalStateException("Component type cannot have a handle");
        }

        String handleForElement(SchemaGlobalElement element)
        {
            if (element == null)
                return null;
            if (element.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            String handle = (String)_componentsToHandles.get(element);
            if (handle == null)
                handle = addUniqueHandle(element, NameUtil.upperCamelCase(element.getName().getLocalPart()) + "Element");
            return handle;
        }

        String handleForAttribute(SchemaGlobalAttribute attribute)
        {
            if (attribute == null)
                return null;
            if (attribute.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            String handle = (String)_componentsToHandles.get(attribute);
            if (handle == null)
                handle = addUniqueHandle(attribute, NameUtil.upperCamelCase(attribute.getName().getLocalPart()) + "Attribute");
            return handle;
        }

        String handleForModelGroup(SchemaModelGroup group)
        {
            if (group == null)
                return null;
            if (group.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            String handle = (String)_componentsToHandles.get(group);
            if (handle == null)
                handle = addUniqueHandle(group, NameUtil.upperCamelCase(group.getName().getLocalPart()) + "ModelGroup");
            return handle;
        }

        String handleForAttributeGroup(SchemaAttributeGroup group)
        {
            if (group == null)
                return null;
            if (group.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            String handle = (String)_componentsToHandles.get(group);
            if (handle == null)
                handle = addUniqueHandle(group, NameUtil.upperCamelCase(group.getName().getLocalPart()) + "AttributeGroup");
            return handle;
        }

        String handleForIdentityConstraint(SchemaIdentityConstraint idc)
        {
            if (idc == null)
                return null;
            if (idc.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            String handle = (String)_componentsToHandles.get(idc);
            if (handle == null)
                handle = addUniqueHandle(idc, NameUtil.upperCamelCase(idc.getName().getLocalPart()) + "IdentityConstraint");
            return handle;
        }

        String handleForType(SchemaType type)
        {
            if (type == null)
                return null;
            if (type.getTypeSystem() != getTypeSystem())
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            String handle = (String)_componentsToHandles.get(type);
            if (handle == null)
            {
                QName name = type.getName();
                String suffix = "";
                if (name == null)
                {
                    if (type.isDocumentType())
                    {
                        name = type.getDocumentElementName();
                        suffix = "Doc";
                    }
                    else if (type.isAttributeType())
                    {
                        name = type.getAttributeTypeAttributeName();
                        suffix = "AttrType";
                    }
                    else if (type.getContainerField() != null)
                    {
                        name = type.getContainerField().getName();
                        suffix = type.getContainerField().isAttribute() ? "Attr" : "Elem";
                    }
                }

                String baseName;
                String uniq = Integer.toHexString(type.toString().hashCode() | 0x80000000).substring(4).toUpperCase();
                if (name == null)
                    baseName = "Anon" + uniq + "Type";
                else
                    baseName = NameUtil.upperCamelCase(name.getLocalPart()) + uniq + suffix + "Type";

                handle = addUniqueHandle(type, baseName);
            }

            return handle;
        }

        SchemaComponent.Ref refForHandle(String handle)
        {
            if (handle == null)
                return null;

            return (SchemaComponent.Ref)_handlesToRefs.get(handle);
        }

        Set getAllHandles()
        {
            return _handlesToRefs.keySet();
        }

        void startWriteMode()
        {
            _started = true;
            _componentsToHandles = new LinkedHashMap();
            for (Iterator i = _handlesToRefs.keySet().iterator(); i.hasNext(); )
            {
                String handle = (String)i.next();
//                System.err.println("Writing preexisting handle " + handle);
                SchemaComponent comp = ((SchemaComponent.Ref)_handlesToRefs.get(handle)).getComponent();
                _componentsToHandles.put(comp, handle);
            }
        }

    }

    private String _name;
    private String _basePackage;

    // classloader is available for sts's that were compiled and loaded, not dynamic ones
    private ClassLoader _classloader;

    // the loader for loading .xsb resources
    private ResourceLoader _resourceLoader;

    // the following is used to link references during load
    SchemaTypeLoader _linker;

    private HandlePool _localHandles;
    private File _baseSaveDir;

    // actual type system data, map QNames -> SchemaComponent.Ref
    private Map _redefinedModelGroups;
    private Map _redefinedAttributeGroups;
    private Map _redefinedGlobalTypes;

    private Map _globalElements;
    private Map _globalAttributes;
    private Map _modelGroups;
    private Map _attributeGroups;
    private Map _globalTypes;
    private Map _documentTypes;
    private Map _attributeTypes;
    private Map _identityConstraints = Collections.EMPTY_MAP;
    private Map _typeRefsByClassname = new HashMap();
    private Set _namespaces;

    static private final SchemaType[] EMPTY_ST_ARRAY = new SchemaType[0];
    static private final SchemaGlobalElement[] EMPTY_GE_ARRAY = new SchemaGlobalElement[0];
    static private final SchemaGlobalAttribute[] EMPTY_GA_ARRAY = new SchemaGlobalAttribute[0];
    static private final SchemaModelGroup[] EMPTY_MG_ARRAY = new SchemaModelGroup[0];
    static private final SchemaAttributeGroup[] EMPTY_AG_ARRAY = new SchemaAttributeGroup[0];
    static private final SchemaIdentityConstraint[] EMPTY_IC_ARRAY = new SchemaIdentityConstraint[0];

    public void saveToDirectory(File classDir)
    {
        _baseSaveDir = classDir;

        _localHandles.startWriteMode();
        saveTypesRecursively(globalTypes());
        saveTypesRecursively(documentTypes());
        saveTypesRecursively(attributeTypes());
        saveGlobalElements(globalElements());
        saveGlobalAttributes(globalAttributes());
        saveModelGroups(modelGroups());
        saveAttributeGroups(attributeGroups());
        saveIdentityConstraints(identityConstraints());

        saveTypesRecursively(redefinedGlobalTypes());
        saveModelGroups(redefinedModelGroups());
        saveAttributeGroups(redefinedAttributeGroups());

        saveIndex();
        savePointers();
    }

    void saveTypesRecursively(SchemaType[] types)
    {
        for (int i = 0; i < types.length; i++)
        {
            if (types[i].getTypeSystem() != getTypeSystem())
                continue;
            saveType(types[i]);
            saveTypesRecursively(types[i].getAnonymousTypes());
        }
    }

    public void saveGlobalElements(SchemaGlobalElement[] elts)
    {
        for (int i = 0; i < elts.length; i++)
        {
            saveGlobalElement(elts[i]);
        }
    }

    public void saveGlobalAttributes(SchemaGlobalAttribute[] attrs)
    {
        for (int i = 0; i < attrs.length; i++)
        {
            saveGlobalAttribute(attrs[i]);
        }
    }

    public void saveModelGroups(SchemaModelGroup[] groups)
    {
        for (int i = 0; i < groups.length; i++)
        {
            saveModelGroup(groups[i]);
        }
    }

    public void saveAttributeGroups(SchemaAttributeGroup[] groups)
    {
        for (int i = 0; i < groups.length; i++)
        {
            saveAttributeGroup(groups[i]);
        }
    }

    public void saveIdentityConstraints(SchemaIdentityConstraint[] idcs)
    {
        for (int i = 0; i < idcs.length; i++)
        {
            saveIdentityConstraint(idcs[i]);
        }
    }

    public void saveGlobalElement(SchemaGlobalElement elt)
    {
        String handle = _localHandles.handleForElement(elt);
        XsbReader saver = new XsbReader(handle);
        saver.writeParticleData((SchemaParticle)elt);
        saver.writeString(elt.getSourceName());
        saver.writeRealHeader(handle, FILETYPE_SCHEMAELEMENT);
        saver.writeParticleData((SchemaParticle)elt);
        saver.writeString(elt.getSourceName());
        saver.writeEnd();
    }

    public void saveGlobalAttribute(SchemaGlobalAttribute attr)
    {
        String handle = _localHandles.handleForAttribute(attr);
        XsbReader saver = new XsbReader(handle);
        saver.writeAttributeData(attr);
        saver.writeString(attr.getSourceName());
        saver.writeRealHeader(handle, FILETYPE_SCHEMAATTRIBUTE);
        saver.writeAttributeData(attr);
        saver.writeString(attr.getSourceName());
        saver.writeEnd();
    }

    public void saveModelGroup(SchemaModelGroup grp)
    {
        String handle = _localHandles.handleForModelGroup(grp);
        XsbReader saver = new XsbReader(handle);
        saver.writeModelGroupData(grp);
        saver.writeRealHeader(handle, FILETYPE_SCHEMAMODELGROUP);
        saver.writeModelGroupData(grp);
        saver.writeEnd();
    }

    public void saveAttributeGroup(SchemaAttributeGroup grp)
    {
        String handle = _localHandles.handleForAttributeGroup(grp);
        XsbReader saver = new XsbReader(handle);
        saver.writeAttributeGroupData(grp);
        saver.writeRealHeader(handle, FILETYPE_SCHEMAATTRIBUTEGROUP);
        saver.writeAttributeGroupData(grp);
        saver.writeEnd();
    }

    public void saveIdentityConstraint(SchemaIdentityConstraint idc)
    {
        String handle = _localHandles.handleForIdentityConstraint(idc);
        XsbReader saver = new XsbReader(handle);
        saver.writeIdConstraintData(idc);
        saver.writeRealHeader(handle, FILETYPE_SCHEMAIDENTITYCONSTRAINT);
        saver.writeIdConstraintData(idc);
        saver.writeEnd();
    }

    void saveType(SchemaType type)
    {
        String handle = _localHandles.handleForType(type);
        XsbReader saver = new XsbReader(handle);
        saver.writeTypeData(type);
        saver.writeRealHeader(handle, FILETYPE_SCHEMATYPE);
        saver.writeTypeData(type);
        saver.writeEnd();
    }

    public static String crackPointer(InputStream stream)
    {
        DataInputStream input = null;
        try
        {
            input = new DataInputStream(stream);

            int magic = input.readInt();
            if (magic != DATA_BABE)
                return null;

            int majorver = input.readShort();
            int minorver = input.readShort();

            if (majorver != MAJOR_VERSION)
                return null;

            if (minorver > MINOR_VERSION)
                return null;

            if (majorver > 2 || majorver == 2 && minorver >= 18)
                input.readShort(); // release number present in atLeast(2, 18, 0)

            int actualfiletype = input.readShort();
            if (actualfiletype != FILETYPE_SCHEMAPOINTER)
                return null;

            StringPool stringPool = new StringPool("pointer", "unk");
            stringPool.readFrom(input);

            return stringPool.stringForCode(input.readShort());
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            if (input != null)
                try { input.close(); } catch (IOException e) {}
        }
    }

    private class XsbReader
    {
        DataInputStream _input;
        DataOutputStream _output;
        StringPool _stringPool;
        String _handle;
        private int _majorver;
        private int _minorver;
        private int _releaseno;
        int _actualfiletype;

        public XsbReader(String handle, int filetype)
        {
            String resourcename = _basePackage + handle + ".xsb";
            InputStream rawinput = getLoaderStream(resourcename);
            if (rawinput == null)
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Could not locate compiled schema resource " + resourcename, _name, handle, SchemaTypeLoaderException.NO_RESOURCE);

            _input = new DataInputStream(rawinput);
            _handle = handle;

            int magic = readInt();
            if (magic != DATA_BABE)
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Wrong magic cookie", _name, handle, SchemaTypeLoaderException.WRONG_MAGIC_COOKIE);

            _majorver = readShort();
            _minorver = readShort();

            if (_majorver != MAJOR_VERSION)
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Wrong major version - expecting " + MAJOR_VERSION + ", got " + _majorver, _name, handle, SchemaTypeLoaderException.WRONG_MAJOR_VERSION);

            if (_minorver > MINOR_VERSION)
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Incompatible minor version - expecting up to " + MINOR_VERSION + ", got " + _minorver, _name, handle, SchemaTypeLoaderException.WRONG_MINOR_VERSION);

            // Clip to 14 because we're not backward compatible with earlier
            // minor versions.  Remove this when upgrading to a new major
            // version

            if (_minorver < 14)
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Incompatible minor version - expecting at least 14, got " + _minorver, _name, handle, SchemaTypeLoaderException.WRONG_MINOR_VERSION);

            if (atLeast(2, 18, 0))
                _releaseno = readShort();

            int actualfiletype = readShort();
            if (actualfiletype != filetype && filetype != -1)
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: File has the wrong type - expecting type " + filetype + ", got type " + actualfiletype, _name, handle, SchemaTypeLoaderException.WRONG_FILE_TYPE);

            _stringPool = new StringPool(_handle, _name);
            _stringPool.readFrom(_input);

            _actualfiletype = actualfiletype;
        }

        protected boolean atLeast(int majorver, int minorver, int releaseno)
        {
            if (_majorver > majorver)
                return true;
            if (_majorver < majorver)
                return false;
            if (_minorver > minorver)
                return true;
            if (_minorver < minorver)
                return false;
            return (_releaseno >= releaseno);
        }

        protected boolean atMost(int majorver, int minorver, int releaseno)
        {
            if (_majorver > majorver)
                return false;
            if (_majorver < majorver)
                return true;
            if (_minorver > minorver)
                return false;
            if (_minorver < minorver)
                return true;
            return (_releaseno <= releaseno);
        }

        int getActualFiletype()
        {
            return _actualfiletype;
        }

        XsbReader(String handle)
        {
            _handle = handle;
            _stringPool = new StringPool(_handle, _name);
        }

        void writeRealHeader(String handle, int filetype)
        {
            // hackeroo: if handle contains a "/" it's not relative.
            String resourcename;

            if (handle.indexOf('/') >= 0)
                resourcename = handle + ".xsb";
            else
                resourcename = _basePackage + handle + ".xsb";

            OutputStream rawoutput = getSaverStream(resourcename);
            if (rawoutput == null)
                throw new SchemaTypeLoaderException("Could not write compiled schema resource " + resourcename, _name, handle, SchemaTypeLoaderException.NOT_WRITEABLE);

            _output = new DataOutputStream(rawoutput);
            _handle = handle;

            writeInt(DATA_BABE);
            writeShort(MAJOR_VERSION);
            writeShort(MINOR_VERSION);
            writeShort(RELEASE_NUMBER);
            writeShort(filetype);

            _stringPool.writeTo(_output);
        }

        void readEnd()
        {
            try
            {
                if (_input != null)
                    _input.close();
            }
            catch (IOException e)
            {
                // oh, well.
            }
            _input = null;
            _stringPool = null;
            _handle = null;
        }

        void writeEnd()
        {
            try
            {
                if (_output != null)
                {
                    _output.flush();
                    _output.close();
                }
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
            _output = null;
            _stringPool = null;
            _handle = null;
        }

        int fileTypeFromComponentType(int componentType)
        {
            switch (componentType)
            {
                case SchemaComponent.TYPE:
                    return SchemaTypeSystemImpl.FILETYPE_SCHEMATYPE;
                case SchemaComponent.ELEMENT:
                    return SchemaTypeSystemImpl.FILETYPE_SCHEMAELEMENT;
                case SchemaComponent.ATTRIBUTE:
                    return SchemaTypeSystemImpl.FILETYPE_SCHEMAATTRIBUTE;
                case SchemaComponent.MODEL_GROUP:
                    return SchemaTypeSystemImpl.FILETYPE_SCHEMAMODELGROUP;
                case SchemaComponent.ATTRIBUTE_GROUP:
                    return SchemaTypeSystemImpl.FILETYPE_SCHEMAATTRIBUTEGROUP;
                case SchemaComponent.IDENTITY_CONSTRAINT:
                    return SchemaTypeSystemImpl.FILETYPE_SCHEMAIDENTITYCONSTRAINT;
                default:
                    throw new IllegalStateException("Unexpected component type");
            }
        }

        void writeIndexData()
        {
            // has a handle pool (count, handle/type, handle/type...)
            writeHandlePool(_localHandles);

            // then a qname map of global elements (count, qname/handle, qname/handle...)
            writeQNameMap(globalElements());

            // qname map of global attributes
            writeQNameMap(globalAttributes());

            // qname map of model groups
            writeQNameMap(modelGroups());

            // qname map of attribute groups
            writeQNameMap(attributeGroups());

            // qname map of identity constraints
            writeQNameMap(identityConstraints());

            // qname map of global types
            writeQNameMap(globalTypes());

            // qname map of document types, by the qname of the contained element
            writeDocumentTypeMap(documentTypes());

            // qname map of attribute types, by the qname of the contained attribute
            writeAttributeTypeMap(attributeTypes());

            // all the types by classname
            writeClassnameMap(_typeRefsByClassname);

            // all the namespaces
            writeNamespaces(_namespaces);

            // VERSION 2.15 and newer below
            writeQNameMap(redefinedGlobalTypes());
            writeQNameMap(redefinedModelGroups());
            writeQNameMap(redefinedAttributeGroups());
        }

        void writeHandlePool(HandlePool pool)
        {
            writeShort(pool._componentsToHandles.size());
            for (Iterator i = pool._componentsToHandles.keySet().iterator(); i.hasNext(); )
            {
                SchemaComponent comp = (SchemaComponent)i.next();
                String handle = (String)pool._componentsToHandles.get(comp);
                int code = fileTypeFromComponentType(comp.getComponentType());
                writeString(handle);
                writeShort(code);
            }
        }

        void readHandlePool(HandlePool pool)
        {
            if (pool._handlesToRefs.size() != 0 || pool._started)
                throw new IllegalStateException("Nonempty handle set before read");

            int size = readShort();
            for (int i = 0; i < size; i++)
            {
                String handle = readString();
                short code = readShort();
                Object result;
                switch (code)
                {
                    case FILETYPE_SCHEMATYPE:
                        result = new SchemaType.Ref(getTypeSystem(), handle);
                        break;
                    case FILETYPE_SCHEMAELEMENT:
                        result = new SchemaGlobalElement.Ref(getTypeSystem(), handle);
                        break;
                    case FILETYPE_SCHEMAATTRIBUTE:
                        result = new SchemaGlobalAttribute.Ref(getTypeSystem(), handle);
                        break;
                    case FILETYPE_SCHEMAMODELGROUP:
                        result = new SchemaModelGroup.Ref(getTypeSystem(), handle);
                        break;
                    case FILETYPE_SCHEMAATTRIBUTEGROUP:
                        result = new SchemaAttributeGroup.Ref(getTypeSystem(), handle);
                        break;
                    case FILETYPE_SCHEMAIDENTITYCONSTRAINT:
                        result = new SchemaIdentityConstraint.Ref(getTypeSystem(), handle);
                        break;
                    default:
                        throw new SchemaTypeLoaderException("Schema index has an unrecognized entry of type " + code, _name, handle, SchemaTypeLoaderException.UNRECOGNIZED_INDEX_ENTRY);
                }
                pool._handlesToRefs.put(handle, result);
            }
        }

        short readShort()
        {
            try
            {
                return _input.readShort();
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        void writeShort(int s)
        {
            if (s > Short.MAX_VALUE || s < Short.MIN_VALUE)
                throw new SchemaTypeLoaderException("Value " + s + " out of range: must fit in a 16-bit short.", _name, _handle, SchemaTypeLoaderException.INT_TOO_LARGE);
            if (_output != null)
            {
                try
                {
                    _output.writeShort(s);
                }
                catch (IOException e)
                {
                    throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
                }
            }
        }

        int readInt()
        {
            try
            {
                return _input.readInt();
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        void writeInt(int i)
        {
            if (_output != null)
            {
                try
                {
                    _output.writeInt(i);
                }
                catch (IOException e)
                {
                    throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
                }
            }
        }

        String readString()
        {
            return _stringPool.stringForCode(readShort());
        }

        void writeString(String str)
        {
            int code = _stringPool.codeForString(str);
            writeShort(code);
        }

        QName readQName()
        {
            String namespace = readString();
            String localname = readString();
            if (localname == null)
                return null;
            return new QName(namespace, localname);
        }

        void writeQName(QName qname)
        {
            if (qname == null)
            {
                writeString(null);
                writeString(null);
                return;
            }
            writeString(qname.getNamespaceURI());
            writeString(qname.getLocalPart());
        }

        SOAPArrayType readSOAPArrayType()
        {
            QName qName = readQName();
            String dimensions = readString();
            if (qName == null)
                return null;
            return new SOAPArrayType(qName, dimensions);
        }

        void writeSOAPArrayType(SOAPArrayType arrayType)
        {
            if (arrayType == null)
            {
                writeQName(null);
                writeString(null);
            }
            else
            {
                writeQName(arrayType.getQName());
                writeString(arrayType.soap11DimensionString());
            }
        }

        SchemaComponent.Ref readHandle()
        {
            String handle = readString();
            if (handle == null)
                return null;

            if (handle.charAt(0) != '_')
                return _localHandles.refForHandle(handle);

            switch (handle.charAt(2))
            {
                case 'I': // _BI_ - built-in schema type system
                    return ((SchemaType)BuiltinSchemaTypeSystem.get().resolveHandle(handle)).getRef();
                case 'T': // _XT_ - external type
                    return _linker.findTypeRef(QNameHelper.forPretty(handle, 4));
                case 'E': // _XE_ - external element
                    return _linker.findElementRef(QNameHelper.forPretty(handle, 4));
                case 'A': // _XA_ - external attribute
                    return _linker.findAttributeRef(QNameHelper.forPretty(handle, 4));
                case 'M': // _XM_ - external model group
                    return _linker.findModelGroupRef(QNameHelper.forPretty(handle, 4));
                case 'N': // _XN_ - external attribute group
                    return _linker.findAttributeGroupRef(QNameHelper.forPretty(handle, 4));
                case 'D': // _XD_ - external identity constraint
                    return _linker.findIdentityConstraintRef(QNameHelper.forPretty(handle, 4));
                case 'R': // _XR_ - external ref to attribute's type
                    // deprecated: replaced by _XY_
                    SchemaGlobalAttribute attr = _linker.findAttribute(QNameHelper.forPretty(handle, 4));
                    if (attr == null)
                        throw new SchemaTypeLoaderException("Cannot resolve attribute for handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
                    return attr.getType().getRef();
                case 'S': // _XS_ - external ref to element's type
                    // deprecated: replaced by _XY_
                    SchemaGlobalElement elem = _linker.findElement(QNameHelper.forPretty(handle, 4));
                    if (elem == null)
                        throw new SchemaTypeLoaderException("Cannot resolve element for handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
                    return elem.getType().getRef();
                case 'O': // _XO_ - external ref to document type
                    return _linker.findDocumentTypeRef(QNameHelper.forPretty(handle, 4));
                case 'Y': // _XY_ - external ref to any possible type
                    SchemaType type = _linker.typeForSignature(handle.substring(4));
                    if (type == null)
                        throw new SchemaTypeLoaderException("Cannot resolve type for handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
                    return type.getRef();
                default:
                    throw new SchemaTypeLoaderException("Cannot resolve handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
            }
        }

        void writeHandle(SchemaComponent comp)
        {
            if (comp == null || comp.getTypeSystem() == getTypeSystem())
            {
                writeString(_localHandles.handleForComponent(comp));
                return;
            }

            switch (comp.getComponentType())
            {
                case SchemaComponent.ATTRIBUTE:
                    writeString("_XA_" + QNameHelper.pretty(comp.getName()));
                    return;
                case SchemaComponent.MODEL_GROUP:
                    writeString("_XM_" + QNameHelper.pretty(comp.getName()));
                    return;
                case SchemaComponent.ATTRIBUTE_GROUP:
                    writeString("_XN_" + QNameHelper.pretty(comp.getName()));
                    return;
                case SchemaComponent.ELEMENT:
                    writeString("_XE_" + QNameHelper.pretty(comp.getName()));
                    return;
                case SchemaComponent.IDENTITY_CONSTRAINT:
                    writeString("_XD_" + QNameHelper.pretty(comp.getName()));
                    return;
                case SchemaComponent.TYPE:
                    SchemaType type = (SchemaType)comp;
                    if (type.isBuiltinType())
                    {
                        writeString("_BI_" + type.getName().getLocalPart());
                        return;
                    }

                    // fix for CR120759 - added output of types _XR_ & _XS_
                    // when an attribute (_XR_) or element (_XS_) declaration
                    // uses ref to refer to an attribute or element in another
                    // schema and the type of that attribute or element
                    // is an anonymous (local) type
                    // kkrouse 02/1/2005: _XR_ and _XS_ refs are replaced by _XY_
                    if (type.getName() != null)
                    {
                        writeString("_XT_" + QNameHelper.pretty(type.getName()));
                    }
                    else if (type.isDocumentType())
                    {
                        // Substitution groups will create document types that
                        // extend from other document types, possibly in
                        // different jars
                        writeString("_XO_" + QNameHelper.pretty(type.getDocumentElementName()));
                    }
                    else
                    {
                        // fix for XMLBEANS-105:
                        // save out the external type reference using the type's signature.
                        writeString("_XY_" + type.toString());
                    }

                    return;

                default:
                    assert(false);
                    throw new SchemaTypeLoaderException("Cannot write handle for component " + comp, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
            }
        }

        SchemaType.Ref readTypeRef()
        {
            return (SchemaType.Ref)readHandle();
        }

        void writeType(SchemaType type)
        {
            writeHandle(type);
        }

        Map readQNameRefMap()
        {
            Map result = new HashMap();
            int size = readShort();
            for (int i = 0; i < size; i++)
            {
                QName name = readQName();
                SchemaComponent.Ref obj = readHandle();
                result.put(name, obj);
            }
            return result;
        }

        void writeQNameMap(SchemaComponent[] components)
        {
            writeShort(components.length);
            for (int i = 0; i < components.length; i++)
            {
                writeQName(components[i].getName());
                writeHandle(components[i]);
            }
        }

        void writeDocumentTypeMap(SchemaType[] doctypes)
        {
            writeShort(doctypes.length);
            for (int i = 0; i < doctypes.length; i++)
            {
                writeQName(doctypes[i].getDocumentElementName());
                writeHandle(doctypes[i]);
            }
        }

        void writeAttributeTypeMap(SchemaType[] attrtypes)
        {
            writeShort(attrtypes.length);
            for (int i = 0; i < attrtypes.length; i++)
            {
                writeQName(attrtypes[i].getAttributeTypeAttributeName());
                writeHandle(attrtypes[i]);
            }
        }

        SchemaType.Ref[] readTypeRefArray()
        {
            int size = readShort();
            SchemaType.Ref[] result = new SchemaType.Ref[size];
            for (int i = 0; i < size; i++)
            {
                result[i] = readTypeRef();
            }
            return result;
        }

        void writeTypeArray(SchemaType[] array)
        {
            writeShort(array.length);
            for (int i = 0; i < array.length; i++)
            {
                writeHandle(array[i]);
            }
        }

        Map readClassnameRefMap()
        {
            Map result = new HashMap();
            int size = readShort();
            for (int i = 0; i < size; i++)
            {
                String name = readString();
                SchemaComponent.Ref obj = readHandle();
                result.put(name, obj);
            }
            return result;
        }

        void writeClassnameMap(Map typesByClass)
        {
            writeShort(typesByClass.size());
            for (Iterator i = typesByClass.keySet().iterator(); i.hasNext(); )
            {
                String className = (String)i.next();
                writeString(className);
                writeHandle(((SchemaType.Ref)typesByClass.get(className)).get());
            }
        }

        Set readNamespaces()
        {
            Set result = new HashSet();
            int size = readShort();
            for (int i = 0; i < size; i++)
            {
                String ns = readString();
                result.add(ns);
            }
            return result;
        }

        void writeNamespaces(Set namespaces)
        {
            writeShort(namespaces.size());
            for (Iterator i = namespaces.iterator(); i.hasNext(); )
            {
                String ns = (String)i.next();
                writeString(ns);
            }
        }

        OutputStream getSaverStream(String name)
        {
            File targetFile = new File(_baseSaveDir, name);
            try
            {
                targetFile.getParentFile().mkdirs();
                return new FileOutputStream(targetFile);
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        InputStream getLoaderStream(String resourcename)
        {
            return _resourceLoader.getResourceAsStream(resourcename);
        }

        /**
         * Finishes loading an element after the header has already been loaded.
         */
        public SchemaGlobalElement finishLoadingElement()
        {
            String handle = null;
            try
            {
                SchemaGlobalElementImpl impl = new SchemaGlobalElementImpl(getTypeSystem());
                short particleType = readShort();
                if (particleType != SchemaParticle.ELEMENT)
                    throw new SchemaTypeLoaderException("Wrong particle type ", _name, _handle, SchemaTypeLoaderException.BAD_PARTICLE_TYPE);
                loadParticle(impl, SchemaParticle.ELEMENT, true);
                impl.setFilename(readString());
                return impl;
            }
            catch (SchemaTypeLoaderException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            }
            finally
            {
                readEnd();
            }
        }

        public SchemaGlobalAttribute finishLoadingAttribute()
        {
            SchemaGlobalAttributeImpl impl = new SchemaGlobalAttributeImpl(getTypeSystem());
            try
            {
                loadAttribute(impl);
                impl.setFilename(readString());

                return impl;
            }
            catch (SchemaTypeLoaderException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            }
            finally
            {
                readEnd();
            }
        }

        SchemaModelGroup finishLoadingModelGroup()
        {
            SchemaModelGroupImpl impl = new SchemaModelGroupImpl(getTypeSystem());

            try
            {
                loadModelGroup(impl);
                return impl;
            }
            catch (SchemaTypeLoaderException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            }
            finally
            {
                readEnd();
            }
        }

        SchemaIdentityConstraint finishLoadingIdentityConstraint()
        {
            try {
                SchemaIdentityConstraintImpl impl = new SchemaIdentityConstraintImpl(getTypeSystem());
                impl.setName(readQName());
                impl.setConstraintCategory(readShort());
                impl.setSelector(readString());

                String[] fields = new String[readShort()];
                for (int i = 0 ; i < fields.length ; i++)
                    fields[i] = readString();
                impl.setFields(fields);

                if (impl.getConstraintCategory() == SchemaIdentityConstraint.CC_KEYREF)
                    impl.setReferencedKey((SchemaIdentityConstraint.Ref)readHandle());

                int mapCount = readShort();
                Map nsMappings = new HashMap();
                for (int i = 0 ; i < mapCount ; i++)
                {
                    String prefix = readString();
                    String uri = readString();
                    nsMappings.put(prefix, uri);
                }
                impl.setNSMap(nsMappings);

                return impl;
            }
            catch (SchemaTypeLoaderException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            }
            finally
            {
                readEnd();
            }
        }

        SchemaAttributeGroup finishLoadingAttributeGroup()
        {
            SchemaAttributeGroupImpl impl = new SchemaAttributeGroupImpl(getTypeSystem());

            try
            {
                loadAttributeGroup(impl);
                return impl;
            }
            catch (SchemaTypeLoaderException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            }
            finally
            {
                readEnd();
            }
        }

        public SchemaType finishLoadingType()
        {
            try
            {
                SchemaTypeImpl impl = new SchemaTypeImpl(getTypeSystem(), true);
                impl.setName(readQName());
                impl.setOuterSchemaTypeRef(readTypeRef());
                impl.setBaseDepth(readShort());
                impl.setBaseTypeRef(readTypeRef());
                impl.setDerivationType(readShort());

                switch (readShort())
                {
                    case FIELD_GLOBAL:
                        impl.setContainerFieldRef(readHandle());
                        break;
                    case FIELD_LOCALATTR:
                        impl.setContainerFieldIndex((short)1, readShort());
                        break;
                    case FIELD_LOCALELT:
                        impl.setContainerFieldIndex((short)2, readShort());
                        break;
                }
                String jn = readString();
                impl.setFullJavaName(jn == null ? "" : jn);
                jn = readString();
                impl.setFullJavaImplName(jn == null ? "" : jn);

                impl.setAnonymousTypeRefs(readTypeRefArray());

                impl.setAnonymousUnionMemberOrdinal(readShort());

                int flags;
                flags = readInt();


                boolean isComplexType = ((flags & FLAG_SIMPLE_TYPE) == 0);
                impl.setCompiled((flags & FLAG_COMPILED) != 0);
                impl.setDocumentType((flags & FLAG_DOCUMENT_TYPE) != 0);
                impl.setAttributeType((flags & FLAG_ATTRIBUTE_TYPE) != 0);
                impl.setSimpleType(!isComplexType);

                short complexVariety = SchemaType.NOT_COMPLEX_TYPE;
                if (isComplexType)
                {
                    impl.setAbstractFinal((flags & FLAG_ABSTRACT) != 0,
                        (flags & FLAG_FINAL_EXT) != 0,
                        (flags & FLAG_FINAL_REST) != 0,
                        (flags & FLAG_FINAL_LIST) != 0,
                        (flags & FLAG_FINAL_UNION) != 0);
                    impl.setBlock((flags & FLAG_BLOCK_EXT) != 0,
                        (flags & FLAG_BLOCK_REST) != 0);

                    impl.setOrderSensitive((flags & FLAG_ORDER_SENSITIVE) != 0);
                    complexVariety = readShort();
                    impl.setComplexTypeVariety(complexVariety);

                    // Attribute Model Table
                    SchemaAttributeModelImpl attrModel = new SchemaAttributeModelImpl();

                    short attrCount = readShort();
                    for (int i = 0; i < attrCount; i++)
                        attrModel.addAttribute(readAttributeData());

                    attrModel.setWildcardSet(readQNameSet());
                    attrModel.setWildcardProcess(readShort());

                    // Attribute Property Table
                    Map attrProperties = new LinkedHashMap();
                    short attrPropCount = readShort();
                    for (int i = 0; i < attrPropCount; i++)
                    {
                        SchemaProperty prop = readPropertyData();
                        if (!prop.isAttribute())
                            throw new SchemaTypeLoaderException("Attribute property " + i + " is not an attribute", _name, _handle, SchemaTypeLoaderException.WRONG_PROPERTY_TYPE);
                        attrProperties.put(prop.getName(), prop);
                    }

                    SchemaParticle contentModel = null;
                    Map elemProperties = null;
                    short isAll = 0;

                    if (complexVariety == SchemaType.ELEMENT_CONTENT || complexVariety == SchemaType.MIXED_CONTENT)
                    {
                        // Content Model Tree
                        isAll = readShort();
                        SchemaParticle[] parts = readParticleArray();
                        if (parts.length == 1)
                            contentModel = parts[0];
                        else if (parts.length == 0)
                            contentModel = null;
                        else
                            throw new SchemaTypeLoaderException("Content model not well-formed", _name, _handle, SchemaTypeLoaderException.MALFORMED_CONTENT_MODEL);

                        // Element Property Table

                        elemProperties = new LinkedHashMap();
                        short elemPropCount = readShort();
                        for (int i = 0; i < elemPropCount; i++)
                        {
                            SchemaProperty prop = readPropertyData();
                            if (prop.isAttribute())
                                throw new SchemaTypeLoaderException("Element property " + i + " is not an element", _name, _handle, SchemaTypeLoaderException.WRONG_PROPERTY_TYPE);
                            elemProperties.put(prop.getName(), prop);
                        }
                    }

                    impl.setContentModel(contentModel, attrModel, elemProperties, attrProperties, isAll == 1);
                    StscComplexTypeResolver.WildcardResult wcElt = StscComplexTypeResolver.summarizeEltWildcards(contentModel);
                    StscComplexTypeResolver.WildcardResult wcAttr = StscComplexTypeResolver.summarizeAttrWildcards(attrModel);
                    impl.setWildcardSummary(wcElt.typedWildcards, wcElt.hasWildcards, wcAttr.typedWildcards, wcAttr.hasWildcards);
                }

                if (!isComplexType || complexVariety == SchemaType.SIMPLE_CONTENT)
                {
                    short simpleVariety = readShort();
                    impl.setSimpleTypeVariety(simpleVariety);

                    boolean isStringEnum = ((flags & FLAG_STRINGENUM) != 0);

                    impl.setOrdered((flags & FLAG_ORDERED) != 0 ? SchemaType.UNORDERED : ((flags & FLAG_TOTAL_ORDER) != 0 ? SchemaType.TOTAL_ORDER : SchemaType.PARTIAL_ORDER));
                    impl.setBounded((flags & FLAG_BOUNDED) != 0);
                    impl.setFinite((flags & FLAG_FINITE) != 0);
                    impl.setNumeric((flags & FLAG_NUMERIC) != 0);
                    impl.setUnionOfLists((flags & FLAG_UNION_OF_LISTS) != 0);
                    impl.setSimpleFinal((flags & FLAG_FINAL_REST) != 0,
                        (flags & FLAG_FINAL_LIST) != 0,
                        (flags & FLAG_FINAL_UNION) != 0);

                    XmlValueRef[] facets = new XmlValueRef[SchemaType.LAST_FACET + 1];
                    boolean[] fixedFacets = new boolean[SchemaType.LAST_FACET + 1];
                    short facetCount = readShort();
                    for (int i = 0; i < facetCount; i++)
                    {
                        short facetCode = readShort();
                        facets[facetCode] = readXmlValueObject();
                        fixedFacets[facetCode] = (readShort() == 1);
                    }
                    impl.setBasicFacets(facets, fixedFacets);

                    impl.setWhiteSpaceRule(readShort());

                    impl.setPatternFacet((flags & FLAG_HAS_PATTERN) != 0);

                    short patternCount = readShort();
                    org.apache.xmlbeans.impl.regex.RegularExpression[] patterns = new org.apache.xmlbeans.impl.regex.RegularExpression[patternCount];
                    for (int i = 0; i < patternCount; i++)
                    {
                        patterns[i] = new org.apache.xmlbeans.impl.regex.RegularExpression(readString(), "X");
                    }
                    impl.setPatterns(patterns);

                    short enumCount = readShort();
                    XmlValueRef[] enumValues = new XmlValueRef[enumCount];
                    for (int i = 0; i < enumCount; i++)
                    {
                        enumValues[i] = readXmlValueObject();
                    }
                    impl.setEnumerationValues(enumCount == 0 ? null : enumValues);

                    impl.setBaseEnumTypeRef(readTypeRef());
                    if (isStringEnum)
                    {
                        short seCount = readShort();
                        SchemaStringEnumEntry[] entries = new SchemaStringEnumEntry[seCount];
                        for (int i = 0; i < seCount; i++)
                        {
                            entries[i] = new SchemaStringEnumEntryImpl(readString(), readShort(), readString());
                        }
                        impl.setStringEnumEntries(entries);
                    }

                    switch (simpleVariety)
                    {
                        case SchemaType.ATOMIC:
                            impl.setPrimitiveTypeRef(readTypeRef());
                            impl.setDecimalSize(readInt());
                            break;

                        case SchemaType.LIST:
                            impl.setPrimitiveTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
                            impl.setListItemTypeRef(readTypeRef());
                            break;

                        case SchemaType.UNION:
                            impl.setPrimitiveTypeRef(BuiltinSchemaTypeSystem.ST_ANY_SIMPLE.getRef());
                            impl.setUnionMemberTypeRefs(readTypeRefArray());
                            break;

                        default:
                            throw new SchemaTypeLoaderException("Simple type does not have a recognized variety", _name, _handle, SchemaTypeLoaderException.WRONG_SIMPLE_VARIETY);
                    }
                }

                impl.setFilename(readString());

                return impl;
            }
            catch (SchemaTypeLoaderException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            }
            finally
            {
                readEnd();
            }
        }

        void writeTypeData(SchemaType type)
        {
            writeQName(type.getName());
            writeType(type.getOuterType());
            writeShort(((SchemaTypeImpl)type).getBaseDepth());
            writeType(type.getBaseType());
            writeShort(type.getDerivationType());
            if (type.getContainerField() == null)
            {
                writeShort(FIELD_NONE);
            }
            else if (type.getOuterType().isAttributeType() || type.getOuterType().isDocumentType())
            {
                writeShort(FIELD_GLOBAL);
                writeHandle((SchemaComponent)type.getContainerField());
            }
            else if (type.getContainerField().isAttribute())
            {
                writeShort(FIELD_LOCALATTR);
                writeShort(((SchemaTypeImpl)type.getOuterType()).getIndexForLocalAttribute((SchemaLocalAttribute)type.getContainerField()));
            }
            else
            {
                writeShort(FIELD_LOCALELT);
                writeShort(((SchemaTypeImpl)type.getOuterType()).getIndexForLocalElement((SchemaLocalElement)type.getContainerField()));
            }
            writeString(type.getFullJavaName());
            writeString(type.getFullJavaImplName());
            writeTypeArray(type.getAnonymousTypes());
            writeShort(type.getAnonymousUnionMemberOrdinal());

            int flags = 0;
            if (type.isSimpleType())
                flags |= FLAG_SIMPLE_TYPE;
            if (type.isDocumentType())
                flags |= FLAG_DOCUMENT_TYPE;
            if (type.isAttributeType())
                flags |= FLAG_ATTRIBUTE_TYPE;
            if (type.ordered() != SchemaType.UNORDERED)
                flags |= FLAG_ORDERED;
            if (type.ordered() == SchemaType.TOTAL_ORDER)
                flags |= FLAG_TOTAL_ORDER;
            if (type.isBounded())
                flags |= FLAG_BOUNDED;
            if (type.isFinite())
                flags |= FLAG_FINITE;
            if (type.isNumeric())
                flags |= FLAG_NUMERIC;
            if (type.hasStringEnumValues())
                flags |= FLAG_STRINGENUM;
            if (((SchemaTypeImpl)type).isUnionOfLists())
                flags |= FLAG_UNION_OF_LISTS;
            if (type.hasPatternFacet())
                flags |= FLAG_HAS_PATTERN;
            if (type.isOrderSensitive())
                flags |= FLAG_ORDER_SENSITIVE;

            if (type.blockExtension())
                flags |= FLAG_BLOCK_EXT;
            if (type.blockRestriction())
                flags |= FLAG_BLOCK_REST;
            if (type.finalExtension())
                flags |= FLAG_FINAL_EXT;
            if (type.finalRestriction())
                flags |= FLAG_FINAL_EXT;
            if (type.finalList())
                flags |= FLAG_FINAL_LIST;
            if (type.finalUnion())
                flags |= FLAG_FINAL_UNION;
            if (type.isAbstract())
                flags |= FLAG_ABSTRACT;

            writeInt(flags);

            if (!type.isSimpleType())
            {
                writeShort(type.getContentType());

                // Attribute Model Table
                SchemaAttributeModel attrModel = type.getAttributeModel();
                SchemaLocalAttribute[] attrs = attrModel.getAttributes();

                writeShort(attrs.length);
                for (int i = 0; i < attrs.length; i++)
                    writeAttributeData(attrs[i]);

                writeQNameSet(attrModel.getWildcardSet());
                writeShort(attrModel.getWildcardProcess());

                // Attribute Property Table
                SchemaProperty[] attrProperties = type.getAttributeProperties();
                writeShort(attrProperties.length);
                for (int i = 0; i < attrProperties.length; i++)
                    writePropertyData(attrProperties[i]);

                if (type.getContentType() == SchemaType.ELEMENT_CONTENT ||
                    type.getContentType() == SchemaType.MIXED_CONTENT)
                {
                    // Content Model Tree
                    writeShort(type.hasAllContent() ? 1 : 0);
                    SchemaParticle[] parts;
                    if (type.getContentModel() != null)
                        parts = new SchemaParticle[] { type.getContentModel() };
                    else
                        parts = new SchemaParticle[0];

                    writeParticleArray(parts);

                    // Element Property Table
                    SchemaProperty[] eltProperties = type.getElementProperties();
                    writeShort(eltProperties.length);
                    for (int i = 0; i < eltProperties.length; i++)
                        writePropertyData(eltProperties[i]);
                }
            }

            if (type.isSimpleType() || type.getContentType() == SchemaType.SIMPLE_CONTENT)
            {
                writeShort(type.getSimpleVariety());

                int facetCount = 0;
                for (int i = 0; i <= SchemaType.LAST_FACET; i++)
                    if (type.getFacet(i) != null)
                        facetCount++;
                writeShort(facetCount);
                for (int i = 0; i <= SchemaType.LAST_FACET; i++)
                {
                    XmlAnySimpleType facet = type.getFacet(i);
                    if (facet != null)
                    {
                        writeShort(i);
                        writeXmlValueObject(facet);
                        writeShort(type.isFacetFixed(i) ? 1 : 0);
                    }
                }

                writeShort(type.getWhiteSpaceRule());

                org.apache.xmlbeans.impl.regex.RegularExpression[] patterns = ((SchemaTypeImpl)type).getPatternExpressions();
                writeShort(patterns.length);
                for (int i = 0; i < patterns.length; i++)
                    writeString(patterns[i].getPattern());

                XmlAnySimpleType[] enumValues = type.getEnumerationValues();
                if (enumValues == null)
                    writeShort(0);
                else
                {
                    writeShort(enumValues.length);
                    for (int i = 0; i < enumValues.length; i++)
                        writeXmlValueObject(enumValues[i]);
                }

                // new for version 2.3
                writeType(type.getBaseEnumType());
                if (type.hasStringEnumValues())
                {
                    SchemaStringEnumEntry[] entries = type.getStringEnumEntries();
                    writeShort(entries.length);
                    for (int i = 0; i < entries.length; i++)
                    {
                        writeString(entries[i].getString());
                        writeShort(entries[i].getIntValue());
                        writeString(entries[i].getEnumName());
                    }
                }

                switch (type.getSimpleVariety())
                {
                    case SchemaType.ATOMIC:
                        writeType(type.getPrimitiveType());
                        writeInt(type.getDecimalSize());
                        break;

                    case SchemaType.LIST:
                        writeType(type.getListItemType());
                        break;

                    case SchemaType.UNION:
                        writeTypeArray(type.getUnionMemberTypes());
                        break;
                }
            }

            writeString(type.getSourceName());
        }

        void readExtensionsList()
        {
            int count = readShort();
            assert count == 0;

            for (int i = 0; i < count; i++)
            {
                readString();
                readString();
                readString();
            }
        }

        SchemaLocalAttribute readAttributeData()
        {
            SchemaLocalAttributeImpl result = new SchemaLocalAttributeImpl();
            loadAttribute(result);
            return result;
        }

        void loadModelGroup(SchemaModelGroupImpl result)
        {
            try
            {
                result.init(readQName(), readString(), readShort() == 1, atLeast(2, 15, 0) ? readShort() == 1 : false, GroupDocument.Factory.parse( readString() ).getGroup());
            }
            catch ( XmlException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        void loadAttributeGroup(SchemaAttributeGroupImpl result)
        {
            try
            {
                result.init( readQName(), readString(), readShort() == 1, atLeast(2, 15, 0) ? readShort() == 1 : false, AttributeGroupDocument.Factory.parse( readString() ).getAttributeGroup());
            }
            catch ( XmlException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        void loadAttribute(SchemaLocalAttributeImpl result)
        {
            // name, type, use, deftext, defval, fixed, soaparraytype
            result.init(readQName(), readTypeRef(), readShort(), readString(), null, atLeast(2, 16, 0) ? readXmlValueObject() : null, readShort() == 1, readSOAPArrayType());
        }

        void writeAttributeData(SchemaLocalAttribute attr)
        {
            writeQName(attr.getName());
            writeType(attr.getType());
            writeShort(attr.getUse());
            writeString(attr.getDefaultText());
            writeXmlValueObject(attr.getDefaultValue());
            writeShort(attr.isFixed() ? 1 : 0);
            writeSOAPArrayType(((SchemaWSDLArrayType)attr).getWSDLArrayType());
        }

        void writeIdConstraintData(SchemaIdentityConstraint idc)
        {
            writeQName(idc.getName());
            writeShort(idc.getConstraintCategory());
            writeString(idc.getSelector());

            String[] fields = idc.getFields();
            writeShort(fields.length);
            for (int i = 0 ; i < fields.length ; i++)
                writeString(fields[i]);


            if (idc.getConstraintCategory() == SchemaIdentityConstraint.CC_KEYREF)
                writeHandle(idc.getReferencedKey());

            Set mappings = idc.getNSMap().entrySet();
            writeShort(mappings.size());
            for (Iterator it = mappings.iterator() ; it.hasNext() ; ) {
                Map.Entry e = (Map.Entry)it.next();
                String prefix = (String)e.getKey();
                String uri = (String)e.getValue();

                writeString(prefix);
                writeString(uri);
            }

        }

        SchemaParticle[] readParticleArray()
        {
            SchemaParticle[] result = new SchemaParticle[readShort()];
            for (int i = 0; i < result.length; i++)
                result[i] = readParticleData();
            return result;
        }

        void writeParticleArray(SchemaParticle[] spa)
        {
            writeShort(spa.length);
            for (int i = 0; i < spa.length; i++)
                writeParticleData(spa[i]);
        }

        SchemaParticle readParticleData()
        {
            short particleType = readShort();
            SchemaParticleImpl result;
            if (particleType != SchemaParticle.ELEMENT)
                result = new SchemaParticleImpl();
            else
                result = new SchemaLocalElementImpl();
            loadParticle(result, particleType, false);
            return result;
        }

        void loadParticle(SchemaParticleImpl result, int particleType, boolean global)
        {
            short particleFlags = readShort();

            result.setParticleType(particleType);
            result.setMinOccurs(readBigInteger());
            result.setMaxOccurs(readBigInteger());

            result.setTransitionRules(readQNameSet(),
                                      (particleFlags & FLAG_PART_SKIPPABLE) != 0);

            switch (particleType)
            {
                case SchemaParticle.WILDCARD:
                    result.setWildcardSet(readQNameSet());
                    result.setWildcardProcess(readShort());
                    break;

                case SchemaParticle.ELEMENT:
                    SchemaLocalElementImpl lresult = (SchemaLocalElementImpl)result;
                    lresult.setNameAndTypeRef(readQName(), readTypeRef());
                    lresult.setDefault(readString(), (particleFlags & FLAG_PART_FIXED) != 0, null);
                    if (atLeast(2, 16, 0))
                        lresult.setDefaultValue(readXmlValueObject());
                    lresult.setNillable((particleFlags & FLAG_PART_NILLABLE) != 0);
                    lresult.setBlock((particleFlags & FLAG_PART_BLOCKEXT) != 0,
                                     (particleFlags & FLAG_PART_BLOCKREST) != 0,
                                     (particleFlags & FLAG_PART_BLOCKSUBST) != 0);
                    lresult.setWsdlArrayType(readSOAPArrayType());
                    lresult.setAbstract((particleFlags & FLAG_PART_ABSTRACT) != 0);
                    if (global)
                    {
                        SchemaGlobalElementImpl gresult = (SchemaGlobalElementImpl)lresult;
                        gresult.setFinal(
                                     (particleFlags & FLAG_PART_FINALEXT) != 0,
                                     (particleFlags & FLAG_PART_FINALREST) != 0);

                        if (atLeast(2, 17, 0))
                            gresult.setSubstitutionGroup((SchemaGlobalElement.Ref)readHandle());

                        short substGroupCount = readShort();
                        for (int i = 0; i < substGroupCount; i++)
                        {
                            gresult.addSubstitutionGroupMember(readQName());
                        }
                    }

                    SchemaIdentityConstraint.Ref[] idcs = new SchemaIdentityConstraint.Ref[readShort()];

                    for (int i = 0 ; i < idcs.length ; i++)
                        idcs[i] = (SchemaIdentityConstraint.Ref)readHandle();

                    lresult.setIdentityConstraints(idcs);

                    break;

                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                case SchemaParticle.CHOICE:
                    result.setParticleChildren(readParticleArray());
                    break;

                default:
                    throw new SchemaTypeLoaderException("Unrecognized particle type ", _name, _handle, SchemaTypeLoaderException.BAD_PARTICLE_TYPE);
            }
        }

        void writeParticleData(SchemaParticle part)
        {
            writeShort(part.getParticleType());
            short flags = 0;
            if (part.isSkippable())
                flags |= FLAG_PART_SKIPPABLE;
            if (part.getParticleType() == SchemaParticle.ELEMENT)
            {
                SchemaLocalElement lpart = (SchemaLocalElement)part;
                if (lpart.isFixed())
                    flags |= FLAG_PART_FIXED;
                if (lpart.isNillable())
                    flags |= FLAG_PART_NILLABLE;
                if (lpart.blockExtension())
                    flags |= FLAG_PART_BLOCKEXT;
                if (lpart.blockRestriction())
                    flags |= FLAG_PART_BLOCKREST;
                if (lpart.blockSubstitution())
                    flags |= FLAG_PART_BLOCKSUBST;
                if (lpart.isAbstract())
                    flags |= FLAG_PART_ABSTRACT;

                if (lpart instanceof SchemaGlobalElement)
                {
                    SchemaGlobalElement gpart = (SchemaGlobalElement)lpart;
                    if (gpart.finalExtension())
                        flags |= FLAG_PART_FINALEXT;
                    if (gpart.finalRestriction())
                        flags |= FLAG_PART_FINALREST;
                }
            }
            writeShort(flags);
            writeBigInteger(part.getMinOccurs());
            writeBigInteger(part.getMaxOccurs());
            writeQNameSet(part.acceptedStartNames());

            switch (part.getParticleType())
            {
                case SchemaParticle.WILDCARD:
                    writeQNameSet(part.getWildcardSet());
                    writeShort(part.getWildcardProcess());
                    break;

                case SchemaParticle.ELEMENT:
                    SchemaLocalElement lpart = (SchemaLocalElement)part;
                    writeQName(lpart.getName());
                    writeType(lpart.getType());
                    writeString(lpart.getDefaultText());
                    writeXmlValueObject(lpart.getDefaultValue());
                    writeSOAPArrayType(((SchemaWSDLArrayType)lpart).getWSDLArrayType());
                    if (lpart instanceof SchemaGlobalElement)
                    {
                        SchemaGlobalElement gpart = (SchemaGlobalElement)lpart;

                        writeHandle(gpart.substitutionGroup());

                        QName[] substGroupMembers = gpart.substitutionGroupMembers();
                        writeShort(substGroupMembers.length);
                        for (int i = 0; i < substGroupMembers.length; i++)
                            writeQName(substGroupMembers[i]);
                    }

                    SchemaIdentityConstraint[] idcs = lpart.getIdentityConstraints();

                    writeShort(idcs.length);
                    for (int i = 0 ; i < idcs.length ; i++)
                        writeHandle(idcs[i]);

                    break;

                case SchemaParticle.ALL:
                case SchemaParticle.SEQUENCE:
                case SchemaParticle.CHOICE:
                    writeParticleArray(part.getParticleChildren());
                    break;

                default:
                    throw new SchemaTypeLoaderException("Unrecognized particle type ", _name, _handle, SchemaTypeLoaderException.BAD_PARTICLE_TYPE);
            }
        }

        SchemaProperty readPropertyData()
        {
            SchemaPropertyImpl prop = new SchemaPropertyImpl();
            prop.setName(readQName());
            prop.setTypeRef(readTypeRef());
            short propflags = readShort();
            prop.setAttribute((propflags & FLAG_PROP_ISATTR) != 0);
            prop.setContainerTypeRef(readTypeRef());
            prop.setMinOccurs(readBigInteger());
            prop.setMaxOccurs(readBigInteger());
            prop.setNillable(readShort());
            prop.setDefault(readShort());
            prop.setFixed(readShort());
            prop.setDefaultText(readString());

            prop.setJavaPropertyName(readString());
            prop.setJavaTypeCode(readShort());
            prop.setExtendsJava(readTypeRef(),
                    (propflags & FLAG_PROP_JAVASINGLETON) != 0,
                    (propflags & FLAG_PROP_JAVAOPTIONAL) != 0,
                    (propflags & FLAG_PROP_JAVAARRAY) != 0);
            prop.setJavaSetterDelimiter(readQNameSet());
            if (atLeast(2, 16, 0))
                prop.setDefaultValue(readXmlValueObject());

            if (!prop.isAttribute() && atLeast(2, 17, 0))
            {
                short size = readShort();
                LinkedHashSet qnames = new LinkedHashSet(size);
                for (int i = 0 ; i < size ; i++)
                    qnames.add(readQName());
                prop.setAcceptedNames(qnames);
            }
            prop.setImmutable();
            return prop;
        }

        void writePropertyData(SchemaProperty prop)
        {
            writeQName(prop.getName());
            writeType(prop.getType());
            writeShort((prop.isAttribute() ? FLAG_PROP_ISATTR : 0) |
                       (prop.extendsJavaSingleton() ? FLAG_PROP_JAVASINGLETON : 0) |
                       (prop.extendsJavaOption() ? FLAG_PROP_JAVAOPTIONAL : 0) |
                       (prop.extendsJavaArray() ? FLAG_PROP_JAVAARRAY : 0));
            writeType(prop.getContainerType());
            writeBigInteger(prop.getMinOccurs());
            writeBigInteger(prop.getMaxOccurs());
            writeShort(prop.hasNillable());
            writeShort(prop.hasDefault());
            writeShort(prop.hasFixed());
            writeString(prop.getDefaultText());

            writeString(prop.getJavaPropertyName());
            writeShort(prop.getJavaTypeCode());
            writeType(prop.javaBasedOnType());
            writeQNameSet(prop.getJavaSetterDelimiter());
            writeXmlValueObject(prop.getDefaultValue());

            if (! prop.isAttribute())
            {
                QName[] names = prop.acceptedNames();
                writeShort(names.length);
                for (int i = 0 ; i < names.length ; i++)
                    writeQName(names[i]);
            }
        }

        void writeModelGroupData(SchemaModelGroup grp)
        {
            SchemaModelGroupImpl impl = (SchemaModelGroupImpl)grp;
            writeQName(impl.getName());
            writeString(impl.getTargetNamespace());
            writeShort(impl.getChameleonNamespace() != null ? 1 : 0);
            writeShort(impl.isRedefinition() ? 1 : 0); // new for version 2.15
            writeString(impl.getParseObject().xmlText(new XmlOptions().setSaveOuter()));
        }

        void writeAttributeGroupData(SchemaAttributeGroup grp)
        {
            SchemaAttributeGroupImpl impl = (SchemaAttributeGroupImpl)grp;
            writeQName(impl.getName());
            writeString(impl.getTargetNamespace());
            writeShort(impl.getChameleonNamespace() != null ? 1 : 0);
            writeShort(impl.isRedefinition() ? 1 : 0); // new for version 2.15
            writeString(impl.getParseObject().xmlText(new XmlOptions().setSaveOuter()));
        }

        XmlValueRef readXmlValueObject()
        {
            SchemaType.Ref typeref = readTypeRef();
            if (typeref == null)
                return null;
            int btc = readShort();
            switch (btc)
            {
                default:
                    assert(false);
                case 0:
                    return new XmlValueRef(typeref, null);
                case -1:
                    {
                        int size = readShort();
                        List values = new ArrayList();
                        writeShort(values.size());
                        for (int i = 0; i < size; i++)
                        {
                            values.add(readXmlValueObject());
                        }
                        return new XmlValueRef(typeref, values);
                    }


                case SchemaType.BTC_ANY_SIMPLE:
                case SchemaType.BTC_ANY_URI:
                case SchemaType.BTC_STRING:
                case SchemaType.BTC_DURATION:
                case SchemaType.BTC_DATE_TIME:
                case SchemaType.BTC_TIME:
                case SchemaType.BTC_DATE:
                case SchemaType.BTC_G_YEAR_MONTH:
                case SchemaType.BTC_G_YEAR:
                case SchemaType.BTC_G_MONTH_DAY:
                case SchemaType.BTC_G_DAY:
                case SchemaType.BTC_G_MONTH:
                case SchemaType.BTC_DECIMAL:
                case SchemaType.BTC_BOOLEAN:
                    return new XmlValueRef(typeref, readString());

                case SchemaType.BTC_BASE_64_BINARY:
                case SchemaType.BTC_HEX_BINARY:
                    return new XmlValueRef(typeref, readByteArray());

                case SchemaType.BTC_QNAME:
                case SchemaType.BTC_NOTATION:
                    return new XmlValueRef(typeref, readQName());

                case SchemaType.BTC_FLOAT:
                case SchemaType.BTC_DOUBLE:
                    return new XmlValueRef(typeref, new Double(readDouble()));
            }
        }

        void writeXmlValueObject(XmlAnySimpleType value)
        {
            SchemaType type = value == null ? null : value.schemaType();
            writeType(type);
            if (type == null)
                return;

            SchemaType iType = ((SimpleValue)value).instanceType();
            if (iType == null)
            {
                writeShort(0);
            }
            else if (iType.getSimpleVariety() == SchemaType.LIST)
            {
                writeShort(-1);
                List values = ((XmlObjectBase)value).xgetListValue();
                writeShort(values.size());
                for (Iterator i = values.iterator(); i.hasNext(); )
                {
                    writeXmlValueObject((XmlAnySimpleType)i.next());
                }
            }
            else
            {
                int btc = iType.getPrimitiveType().getBuiltinTypeCode();
                writeShort(btc);
                switch (btc)
                {
                    case SchemaType.BTC_ANY_SIMPLE:
                    case SchemaType.BTC_ANY_URI:
                    case SchemaType.BTC_STRING:
                    case SchemaType.BTC_DURATION:
                    case SchemaType.BTC_DATE_TIME:
                    case SchemaType.BTC_TIME:
                    case SchemaType.BTC_DATE:
                    case SchemaType.BTC_G_YEAR_MONTH:
                    case SchemaType.BTC_G_YEAR:
                    case SchemaType.BTC_G_MONTH_DAY:
                    case SchemaType.BTC_G_DAY:
                    case SchemaType.BTC_G_MONTH:
                    case SchemaType.BTC_DECIMAL:
                    case SchemaType.BTC_BOOLEAN:
                        writeString(value.getStringValue());
                        break;

                    case SchemaType.BTC_BASE_64_BINARY:
                    case SchemaType.BTC_HEX_BINARY:
                        writeByteArray(((SimpleValue)value).getByteArrayValue());
                        break;

                    case SchemaType.BTC_QNAME:
                    case SchemaType.BTC_NOTATION:
                        writeQName(((SimpleValue)value).getQNameValue());
                        break;

                    case SchemaType.BTC_FLOAT:
                        writeDouble(((SimpleValue)value).getFloatValue());
                        break;

                    case SchemaType.BTC_DOUBLE:
                        writeDouble(((SimpleValue)value).getDoubleValue());
                        break;
                }
            }
        }

        double readDouble()
        {
            try
            {
                return _input.readDouble();
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        void writeDouble(double d)
        {
            if (_output != null)
            {
                try
                {
                    _output.writeDouble(d);
                }
                catch (IOException e)
                {
                    throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
                }
            }
        }

        QNameSet readQNameSet()
        {
            short flag = readShort();

            Set uriSet = new HashSet();
            short uriCount = readShort();
            for (int i = 0; i < uriCount; i++)
                uriSet.add(readString());

            Set qnameSet1 = new HashSet();
            short qncount1 = readShort();
            for (int i = 0; i < qncount1; i++)
                qnameSet1.add(readQName());

            Set qnameSet2 = new HashSet();
            short qncount2 = readShort();
            for (int i = 0; i < qncount2; i++)
                qnameSet2.add(readQName());

            if (flag == 1)
                return QNameSet.forSets(uriSet, null, qnameSet1, qnameSet2);
            else
                return QNameSet.forSets(null, uriSet, qnameSet2, qnameSet1);
        }

        void writeQNameSet(QNameSet set)
        {
            boolean invert = (set.excludedURIs() != null);
            writeShort(invert ? 1 : 0);

            Set uriSet = invert ? set.excludedURIs() : set.includedURIs();
            writeShort(uriSet.size());
            for (Iterator i = uriSet.iterator(); i.hasNext(); )
                writeString((String)i.next());

            Set qnameSet1 = invert ? set.excludedQNamesInIncludedURIs() : set.includedQNamesInExcludedURIs();
            writeShort(qnameSet1.size());
            for (Iterator i = qnameSet1.iterator(); i.hasNext(); )
                writeQName((QName)i.next());

            Set qnameSet2 = invert ? set.includedQNamesInExcludedURIs() : set.excludedQNamesInIncludedURIs();
            writeShort(qnameSet2.size());
            for (Iterator i = qnameSet2.iterator(); i.hasNext(); )
                writeQName((QName)i.next());
        }

        byte[] readByteArray()
        {
            try
            {
                short len = _input.readShort();
                byte[] result = new byte[len];
                _input.readFully(result);
                return result;
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        void writeByteArray(byte[] ba)
        {
            try
            {
                writeShort(ba.length);
                if (_output != null)
                    _output.write(ba);
            }
            catch (IOException e)
            {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION);
            }
        }

        BigInteger readBigInteger()
        {
            byte[] result = readByteArray();
            if (result.length == 0)
                return null;
            if (result.length == 1 && result[0] == 0)
                return BigInteger.ZERO;
            if (result.length == 1 && result[0] == 1)
                return BigInteger.ONE;
            return new BigInteger(result);
        }

        void writeBigInteger(BigInteger bi)
        {
            if (bi == null)
            {
                writeShort(0);
            }
            else if (bi.signum() == 0)
            {
                writeByteArray(SINGLE_ZERO_BYTE);
            }
            else
            {
                writeByteArray(bi.toByteArray());
            }
        }

    }

    static final byte[] SINGLE_ZERO_BYTE = new byte[] { (byte)0 };

    public SchemaType typeForHandle(String handle)
    {
        synchronized (_resolvedHandles)
        {
            return (SchemaType)_resolvedHandles.get(handle);
        }
    }

    public SchemaType typeForClassname(String classname)
    {
        SchemaType.Ref ref = (SchemaType.Ref)_typeRefsByClassname.get(classname);
        return (ref != null) ? ref.get() : null;
    }

    public SchemaComponent resolveHandle(String handle)
    {
        SchemaComponent result;

        synchronized (_resolvedHandles)
        {
            result = (SchemaComponent)_resolvedHandles.get(handle);
        }
        if (result == null)
        {
            XsbReader reader = new XsbReader(handle, -1);
            int filetype = reader.getActualFiletype();
            switch (filetype)
            {
                case FILETYPE_SCHEMATYPE:
                    XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving type for handle " + handle, 0);
                    result = reader.finishLoadingType();
                    break;
                case FILETYPE_SCHEMAELEMENT:
                    XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving element for handle " + handle, 0);
                    result = reader.finishLoadingElement();
                    break;
                case FILETYPE_SCHEMAATTRIBUTE:
                    XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving attribute for handle " + handle, 0);
                    result = reader.finishLoadingAttribute();
                    break;
                case FILETYPE_SCHEMAMODELGROUP:
                    XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving model group for handle " + handle, 0);
                    result = reader.finishLoadingModelGroup();
                    break;
                case FILETYPE_SCHEMAATTRIBUTEGROUP:
                    XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving attribute group for handle " + handle, 0);
                    result = reader.finishLoadingAttributeGroup();
                    break;
                case FILETYPE_SCHEMAIDENTITYCONSTRAINT:
                    XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving id constraint for handle " + handle, 0);
                    result = reader.finishLoadingIdentityConstraint();
                    break;
                default:
                    throw new IllegalStateException("Illegal handle type");
            }

            synchronized (_resolvedHandles)
            {
                if (!_resolvedHandles.containsKey(handle))
                    _resolvedHandles.put(handle, result);
                else
                    result = (SchemaComponent)_resolvedHandles.get(handle);
            }
        }
        return result;
    }

    private final Map _resolvedHandles = new HashMap();
    private boolean _allNonGroupHandlesResolved = false;

    public void resolve()
    {
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolve called type system " + _name, 0);
        if (_allNonGroupHandlesResolved)
            return;

        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving all handles for type system " + _name, 1);

        List refs = new ArrayList();
        refs.addAll(_globalElements.values());
        refs.addAll(_globalAttributes.values());
        refs.addAll(_globalTypes.values());
        refs.addAll(_documentTypes.values());
        refs.addAll(_attributeTypes.values());
        refs.addAll(_identityConstraints.values());

        for (Iterator i = refs.iterator(); i.hasNext(); )
        {
            SchemaComponent.Ref ref = (SchemaComponent.Ref)i.next();
            ref.getComponent(); // Forces ref to be resolved
        }

        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Finished resolving type system " + _name, -1);
        _allNonGroupHandlesResolved = true;
    }


    public boolean isNamespaceDefined(String namespace)
    {
        return _namespaces.contains(namespace);
    }

    public SchemaType.Ref findTypeRef(QName name)
    {
        return (SchemaType.Ref)_globalTypes.get(name);
    }

    public SchemaType.Ref findDocumentTypeRef(QName name)
    {
        return (SchemaType.Ref)_documentTypes.get(name);
    }

    public SchemaType.Ref findAttributeTypeRef(QName name)
    {
        return (SchemaType.Ref)_attributeTypes.get(name);
    }

    public SchemaGlobalElement.Ref findElementRef(QName name)
    {
        return (SchemaGlobalElement.Ref)_globalElements.get(name);
    }

    public SchemaGlobalAttribute.Ref findAttributeRef(QName name)
    {
        return (SchemaGlobalAttribute.Ref)_globalAttributes.get(name);
    }

    public SchemaModelGroup.Ref findModelGroupRef(QName name)
    {
        return (SchemaModelGroup.Ref)_modelGroups.get(name);
    }

    public SchemaAttributeGroup.Ref findAttributeGroupRef(QName name)
    {
        return (SchemaAttributeGroup.Ref)_attributeGroups.get(name);
    }

    public SchemaIdentityConstraint.Ref findIdentityConstraintRef(QName name)
    {
        return (SchemaIdentityConstraint.Ref)_identityConstraints.get(name);
    }

    public SchemaType[] globalTypes()
    {
        if (_globalTypes.isEmpty())
            return EMPTY_ST_ARRAY;

        SchemaType[] result = new SchemaType[_globalTypes.size()];
        int j = 0;
        for (Iterator i = _globalTypes.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType.Ref)i.next()).get();
        return result;
    }

    public SchemaType[] redefinedGlobalTypes()
    {
        if (_redefinedGlobalTypes.isEmpty())
            return EMPTY_ST_ARRAY;

        SchemaType[] result = new SchemaType[_redefinedGlobalTypes.size()];
        int j = 0;
        for (Iterator i = _redefinedGlobalTypes.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType.Ref)i.next()).get();
        return result;
    }

    public InputStream getSourceAsStream(String sourceName)
    {
        if (!sourceName.startsWith("/"))
            sourceName = "/" + sourceName;

        return _resourceLoader.getResourceAsStream("schema/src" + sourceName);
    }

    public SchemaType[] documentTypes()
    {
        if (_documentTypes.isEmpty())
            return EMPTY_ST_ARRAY;

        SchemaType[] result = new SchemaType[_documentTypes.size()];
        int j = 0;
        for (Iterator i = _documentTypes.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType.Ref)i.next()).get();
        return result;
    }

    public SchemaType[] attributeTypes()
    {
        if (_attributeTypes.isEmpty())
            return EMPTY_ST_ARRAY;

        SchemaType[] result = new SchemaType[_attributeTypes.size()];
        int j = 0;
        for (Iterator i = _attributeTypes.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaType.Ref)i.next()).get();
        return result;
    }

    public SchemaGlobalElement[] globalElements()
    {
        if (_globalElements.isEmpty())
            return EMPTY_GE_ARRAY;

        SchemaGlobalElement[] result = new SchemaGlobalElement[_globalElements.size()];
        int j = 0;
        for (Iterator i = _globalElements.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaGlobalElement.Ref)i.next()).get();
        return result;
    }

    public SchemaGlobalAttribute[] globalAttributes()
    {
        if (_globalAttributes.isEmpty())
            return EMPTY_GA_ARRAY;

        SchemaGlobalAttribute[] result = new SchemaGlobalAttribute[_globalAttributes.size()];
        int j = 0;
        for (Iterator i = _globalAttributes.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaGlobalAttribute.Ref)i.next()).get();
        return result;
    }

    public SchemaModelGroup[] modelGroups()
    {
        if (_modelGroups.isEmpty())
            return EMPTY_MG_ARRAY;

        SchemaModelGroup[] result = new SchemaModelGroup[_modelGroups.size()];
        int j = 0;
        for (Iterator i = _modelGroups.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaModelGroup.Ref)i.next()).get();
        return result;
    }

    public SchemaModelGroup[] redefinedModelGroups()
    {
        if (_redefinedModelGroups.isEmpty())
            return EMPTY_MG_ARRAY;

        SchemaModelGroup[] result = new SchemaModelGroup[_redefinedModelGroups.size()];
        int j = 0;
        for (Iterator i = _redefinedModelGroups.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaModelGroup.Ref)i.next()).get();
        return result;
    }

    public SchemaAttributeGroup[] attributeGroups()
    {
        if (_attributeGroups.isEmpty())
            return EMPTY_AG_ARRAY;

        SchemaAttributeGroup[] result = new SchemaAttributeGroup[_attributeGroups.size()];
        int j = 0;
        for (Iterator i = _attributeGroups.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaAttributeGroup.Ref)i.next()).get();
        return result;
    }

    public SchemaAttributeGroup[] redefinedAttributeGroups()
    {
        if (_redefinedAttributeGroups.isEmpty())
            return EMPTY_AG_ARRAY;

        SchemaAttributeGroup[] result = new SchemaAttributeGroup[_redefinedAttributeGroups.size()];
        int j = 0;
        for (Iterator i = _redefinedAttributeGroups.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaAttributeGroup.Ref)i.next()).get();
        return result;
    }

    public SchemaIdentityConstraint[] identityConstraints()
    {
        if (_identityConstraints.isEmpty())
            return EMPTY_IC_ARRAY;

        SchemaIdentityConstraint[] result = new SchemaIdentityConstraint[_identityConstraints.size()];
        int j = 0;
        for (Iterator i = _identityConstraints.values().iterator(); i.hasNext(); j++)
            result[j] = ((SchemaIdentityConstraint.Ref)i.next()).get();
        return result;
    }

    public ClassLoader getClassLoader()
    {
        return _classloader;
    }

    /**
     * Used INTERNALLY ONLY by the code generator AFTER the type system has
     * been saved and a handle has been established for each type.
     */
    public String handleForType(SchemaType type)
    {
        return _localHandles.handleForType(type);
    }

    public String getName()
    {
        return _name;
    }

    public SchemaTypeSystem typeSystemForName(String name)
    {
        if (_name != null && name.equals(_name))
            return this;
        return null;
    }
}

