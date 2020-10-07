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
import org.apache.xmlbeans.impl.common.DefaultClassLoaderResourceLoader;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XBeanDebug;
import org.apache.xmlbeans.impl.repackage.Repackager;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.impl.values.XmlObjectBase;
import org.apache.xmlbeans.impl.xb.xsdschema.AttributeGroupDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.GroupDocument;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;

import javax.xml.namespace.QName;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SchemaTypeSystemImpl extends SchemaTypeLoaderBase implements SchemaTypeSystem {
    public static final int DATA_BABE = 0xDA7ABABE;
    public static final int MAJOR_VERSION = 2;  // must match == to be compatible
    public static final int MINOR_VERSION = 24; // must be <= to be compatible
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
    static final int FLAG_SIMPLE_TYPE = 0x1;
    static final int FLAG_DOCUMENT_TYPE = 0x2;
    static final int FLAG_ORDERED = 0x4;
    static final int FLAG_BOUNDED = 0x8;
    static final int FLAG_FINITE = 0x10;
    static final int FLAG_NUMERIC = 0x20;
    static final int FLAG_STRINGENUM = 0x40;
    static final int FLAG_UNION_OF_LISTS = 0x80;
    static final int FLAG_HAS_PATTERN = 0x100;
    static final int FLAG_ORDER_SENSITIVE = 0x200;
    static final int FLAG_TOTAL_ORDER = 0x400;
    static final int FLAG_COMPILED = 0x800;
    static final int FLAG_BLOCK_EXT = 0x1000;
    static final int FLAG_BLOCK_REST = 0x2000;
    static final int FLAG_FINAL_EXT = 0x4000;
    static final int FLAG_FINAL_REST = 0x8000;
    static final int FLAG_FINAL_UNION = 0x10000;
    static final int FLAG_FINAL_LIST = 0x20000;
    static final int FLAG_ABSTRACT = 0x40000;
    static final int FLAG_ATTRIBUTE_TYPE = 0x80000;

    /**
     * regex to identify the type system holder package namespace
     */
    private static final Pattern packPat = Pattern.compile("^(.+)(\\.[^.]+){2}$");

    /**
     * This is to support the feature of a separate/private XMLBeans
     * distribution that will not colide with the public org apache
     * xmlbeans one.
     * METADATA_PACKAGE_GEN will be "" for the original and something like
     * com.mycompany.private.xmlbeans for a private distribution of XMLBeans.
     * <p>
     * There are two properties:
     * METADATA_PACKAGE_GEN - used for generating metadata
     * and METADATA_PACKAGE_LOAD - used for loading the metadata.
     * Most of the time they have the same value, with one exception, during the
     * repackage process scomp needs to load from old package and generate into
     * a new package.
     */
    public static String METADATA_PACKAGE_GEN = "org/apache/xmlbeans/metadata";
//    public static String METADATA_PACKAGE_GEN;
//    static
//    {
//        // fix for maven classloader
//        Package stsPackage = SchemaTypeSystem.class.getPackage();
//        String stsPackageName = (stsPackage==null) ?
//            SchemaTypeSystem.class.getName().substring(0, SchemaTypeSystem.class.getName().lastIndexOf(".")) :
//            stsPackage.getName();
//
//        METADATA_PACKAGE_GEN = stsPackageName.replace('.', '/') + "/metadata";
//    }

    private static String nameToPathString(String nameForSystem) {
        nameForSystem = nameForSystem.replace('.', '/');

        if (!nameForSystem.endsWith("/") && nameForSystem.length() > 0) {
            nameForSystem = nameForSystem + "/";
        }

        return nameForSystem;
    }

    protected SchemaTypeSystemImpl() {
        String fullname = getClass().getName();
        _name = fullname.substring(0, fullname.lastIndexOf('.'));
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Loading type system " + _name, 1);
        _basePackage = nameToPathString(_name);
        _classloader = getClass().getClassLoader();
        _linker = this;
        _resourceLoader = new ClassLoaderResourceLoader(_classloader);
        try {
            initFromHeader();
        } catch (Error | RuntimeException e) {
            XBeanDebug.logException(e);
            throw e;
        }
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Finished loading type system " + _name, -1);
    }

    public SchemaTypeSystemImpl(Class<?> indexclass) {
        String fullname = indexclass.getName();
        _name = fullname.substring(0, fullname.lastIndexOf('.'));
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Loading type system " + _name, 1);
        _basePackage = nameToPathString(_name);
        _classloader = indexclass.getClassLoader();
        _linker = SchemaTypeLoaderImpl.build(null, null, _classloader, getMetadataPath());
        _resourceLoader = new ClassLoaderResourceLoader(_classloader);
        try {
            initFromHeader();
        } catch (RuntimeException | Error e) {
            XBeanDebug.logException(e);
            throw e;
        }
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Finished loading type system " + _name, -1);
    }

    public static boolean fileContainsTypeSystem(File file, String name) {
        String indexname = nameToPathString(name) + "index.xsb";

        if (file.isDirectory()) {
            return (new File(file, indexname)).isFile();
        } else {
            try (ZipFile zipfile = new ZipFile(file)) {
                ZipEntry entry = zipfile.getEntry(indexname);
                return (entry != null && !entry.isDirectory());
            } catch (IOException e) {
                XBeanDebug.log("Problem loading SchemaTypeSystem, zipfilename " + file);
                XBeanDebug.logException(e);
                throw new SchemaTypeLoaderException(e.getMessage(), name, "index", SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }
    }

    public static SchemaTypeSystemImpl forName(String name, ClassLoader loader) {
        try {
            Class<?> c = Class.forName(name + "." + SchemaTypeCodePrinter.INDEX_CLASSNAME, true, loader);
            return (SchemaTypeSystemImpl) c.getField("typeSystem").get(null);
        } catch (Throwable e) {
            return null;
        }
    }

    public SchemaTypeSystemImpl(ResourceLoader resourceLoader, String name, SchemaTypeLoader linker) {
        _name = name;
        _basePackage = nameToPathString(_name);
        _linker = linker;
        _resourceLoader = resourceLoader;
        try {
            initFromHeader();
        } catch (RuntimeException | Error e) {
            XBeanDebug.logException(e);
            throw e;
        }
    }

    private void initFromHeader() {
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Reading unresolved handles for type system " + _name, 0);
        XsbReader reader = null;
        try {
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
            List<QName> typeNames = new ArrayList<>();
            List<QName> modelGroupNames = new ArrayList<>();
            List<QName> attributeGroupNames = new ArrayList<>();
            if (reader.atLeast(2, 15, 0)) {
                _redefinedGlobalTypes = reader.readQNameRefMapAsList(typeNames);
                _redefinedModelGroups = reader.readQNameRefMapAsList(modelGroupNames);
                _redefinedAttributeGroups = reader.readQNameRefMapAsList(attributeGroupNames);
            }
            if (reader.atLeast(2, 19, 0)) {
                _annotations = reader.readAnnotations();
            }

            buildContainers(typeNames, modelGroupNames, attributeGroupNames);
        } finally {
            if (reader != null) {
                reader.readEnd();
            }
        }
    }

    void saveIndex() {
        String handle = "index";
        XsbReader saver = new XsbReader(handle);
        saver.writeIndexData();
        saver.writeRealHeader(handle, FILETYPE_SCHEMAINDEX);
        saver.writeIndexData();
        saver.writeEnd();
    }

    void savePointers() {
        savePointersForComponents(globalElements(), getMetadataPath() + "/element/");
        savePointersForComponents(globalAttributes(), getMetadataPath() + "/attribute/");
        savePointersForComponents(modelGroups(), getMetadataPath() + "/modelgroup/");
        savePointersForComponents(attributeGroups(), getMetadataPath() + "/attributegroup/");
        savePointersForComponents(globalTypes(), getMetadataPath() + "/type/");
        savePointersForComponents(identityConstraints(), getMetadataPath() + "/identityconstraint/");
        savePointersForNamespaces(_namespaces, getMetadataPath() + "/namespace/");
        savePointersForClassnames(_typeRefsByClassname.keySet(), getMetadataPath() + "/javaname/");
        savePointersForComponents(redefinedModelGroups(), getMetadataPath() + "/redefinedmodelgroup/");
        savePointersForComponents(redefinedAttributeGroups(), getMetadataPath() + "/redefinedattributegroup/");
        savePointersForComponents(redefinedGlobalTypes(), getMetadataPath() + "/redefinedtype/");
    }

    void savePointersForComponents(SchemaComponent[] components, String dir) {
        for (SchemaComponent component : components) {
            savePointerFile(dir + QNameHelper.hexsafedir(component.getName()), _name);
        }
    }

    void savePointersForClassnames(Set<String> classnames, String dir) {
        for (String classname : classnames) {
            savePointerFile(dir + classname.replace('.', '/'), _name);
        }
    }

    void savePointersForNamespaces(Set<String> namespaces, String dir) {
        for (String ns : namespaces) {
            savePointerFile(dir + QNameHelper.hexsafedir(new QName(ns, "xmlns")), _name);
        }
    }

    void savePointerFile(String filename, String name) {
        XsbReader saver = new XsbReader(filename);
        saver.writeString(name);
        saver.writeRealHeader(filename, FILETYPE_SCHEMAPOINTER);
        saver.writeString(name);
        saver.writeEnd();
    }

    /**
     * The strategy here is to copy the compiled TypeSystemHolder.template class
     * to a new TypeSystemHolder.class needed by the schema type system.  When
     * saving a loader, we read the TypeSystemHolder.template class file and
     * swap out the utf8 string constants with new ones to create a new
     * TypeSystemHolder class file.  This saves us the need to rely on javac
     * to compile a generated .java file into the class file.
     * <p>
     * See the JVM spec on how to interpret the bytes of a class file.
     */
    void saveLoader() {
        String indexClassName = SchemaTypeCodePrinter.indexClassForSystem(this);
        String[] replace = makeClassStrings(indexClassName);
        assert replace.length == HOLDER_TEMPLATE_NAMES.length;

        Repackager repackager = null;
        if (_filer instanceof FilerImpl) {
            repackager = ((FilerImpl) _filer).getRepackager();
        }

        final String outName = indexClassName.replace('.', '/') + ".class";
        try (DataInputStream in = new DataInputStream(getHolder());
             DataOutputStream out = new DataOutputStream(_filer.createBinaryFile(outName))) {

            // java magic
            out.writeInt(in.readInt());

            // java minor and major version
            out.writeShort(in.readUnsignedShort());
            out.writeShort(in.readUnsignedShort());

            int poolsize = in.readUnsignedShort();
            out.writeShort(poolsize);

            // the constant pool is indexed from 1 to poolsize-1
            for (int i = 1; i < poolsize; i++) {
                int tag = in.readUnsignedByte();
                out.writeByte(tag);

                switch (tag) {
                    case CONSTANT_UTF8:
                        String value = in.readUTF();
                        out.writeUTF(repackageConstant(value, replace, repackager));
                        break;

                    case CONSTANT_CLASS:
                    case CONSTANT_STRING:
                        out.writeShort(in.readUnsignedShort());
                        break;

                    case CONSTANT_NAMEANDTYPE:
                    case CONSTANT_METHOD:
                    case CONSTANT_FIELD:
                    case CONSTANT_INTERFACEMETHOD:
                        out.writeShort(in.readUnsignedShort());
                        out.writeShort(in.readUnsignedShort());
                        break;

                    case CONSTANT_INTEGER:
                    case CONSTANT_FLOAT:
                        out.writeInt(in.readInt());
                        break;

                    case CONSTANT_LONG:
                    case CONSTANT_DOUBLE:
                        out.writeInt(in.readInt());
                        out.writeInt(in.readInt());
                        break;

                    default:
                        throw new RuntimeException("Unexpected constant type: " + tag);
                }
            }

            // we're done with the class' constant pool,
            // we can just copy the rest of the bytes
            copy(in, out);
        } catch (IOException e) {
            // ok
        }
    }

    private InputStream getHolder() {
        InputStream is = SchemaTypeSystemImpl.class.getResourceAsStream(HOLDER_TEMPLATE_CLASSFILE);
        if (is != null) {
            return is;
        }
        DefaultClassLoaderResourceLoader clLoader = new DefaultClassLoaderResourceLoader();
        is = clLoader.getResourceAsStream(HOLDER_TEMPLATE_CLASSFILE);
        if (is != null) {
            return is;
        }
        throw new SchemaTypeLoaderException("couldn't find resource: " + HOLDER_TEMPLATE_CLASSFILE,
            _name, null, SchemaTypeLoaderException.IO_EXCEPTION);
    }

    private static long copy(InputStream inp, OutputStream out) throws IOException {
        final byte[] buff = new byte[4096];
        long totalCount = 0;
        int readBytes;
        do {
            int todoBytes = buff.length;
            readBytes = inp.read(buff, 0, todoBytes);
            if (readBytes > 0) {
                out.write(buff, 0, readBytes);
                totalCount += readBytes;
            }
        } while (readBytes >= 0);

        return totalCount;
    }

    private static final String HOLDER_TEMPLATE_CLASS = "org.apache.xmlbeans.impl.schema.TypeSystemHolder";
    private static final String HOLDER_TEMPLATE_CLASSFILE = "TypeSystemHolder.template";
    private static final String[] HOLDER_TEMPLATE_NAMES = makeClassStrings(HOLDER_TEMPLATE_CLASS);

    // constant pool entry types
    private static final int CONSTANT_UTF8 = 1;
    // private static final int CONSTANT_UNICODE = 2;
    private static final int CONSTANT_INTEGER = 3;
    private static final int CONSTANT_FLOAT = 4;
    private static final int CONSTANT_LONG = 5;
    private static final int CONSTANT_DOUBLE = 6;
    private static final int CONSTANT_CLASS = 7;
    private static final int CONSTANT_STRING = 8;
    private static final int CONSTANT_FIELD = 9;
    private static final int CONSTANT_METHOD = 10;
    private static final int CONSTANT_INTERFACEMETHOD = 11;
    private static final int CONSTANT_NAMEANDTYPE = 12;

    // MAX_UNSIGNED_SHORT
    private static final int MAX_UNSIGNED_SHORT = Short.MAX_VALUE * 2 + 1;

    private static String repackageConstant(String value, String[] replace, Repackager repackager) {
        for (int i = 0; i < HOLDER_TEMPLATE_NAMES.length; i++) {
            if (HOLDER_TEMPLATE_NAMES[i].equals(value)) {
                return replace[i];
            }
        }

        if (repackager != null) {
            return repackager.repackage(new StringBuffer(value)).toString();
        }

        return value;
    }

    /**
     * Construct an array of Strings found in a class file for a classname.
     * For the class name 'a.b.C' it will generate an array of:
     * 'a.b.C', 'a/b/C', 'La/b/C;', and 'class$a$b$C'.
     */
    private static String[] makeClassStrings(String classname) {
        String[] result = new String[4];

        result[0] = classname;
        result[1] = classname.replace('.', '/');
        result[2] = "L" + result[1] + ";";
        result[3] = "class$" + classname.replace('.', '$');

        return result;
    }

    private Map<String, SchemaComponent.Ref> buildTypeRefsByClassname(Map<String, SchemaType> typesByClassname) {
        Map<String, SchemaComponent.Ref> result = new LinkedHashMap<>();
        for (String className : typesByClassname.keySet()) {
            result.put(className, typesByClassname.get(className).getRef());
        }
        return result;
    }

    private static Map<QName, SchemaComponent.Ref> buildComponentRefMap(SchemaComponent[] components) {
        return buildComponentRefMap(Arrays.asList(components));
    }

    private static Map<QName, SchemaComponent.Ref> buildComponentRefMap(List<? extends SchemaComponent> components) {
        return components.stream().collect(Collectors.toMap(SchemaComponent::getName, SchemaComponent::getComponentRef,
            (u, v) -> v, LinkedHashMap::new));
    }

    private static List<SchemaComponent.Ref> buildComponentRefList(SchemaComponent[] components) {
        return buildComponentRefList(Arrays.asList(components));
    }

    private static List<SchemaComponent.Ref> buildComponentRefList(List<? extends SchemaComponent> components) {
        return components.stream().map(SchemaComponent::getComponentRef).collect(Collectors.toList());
    }

    private static Map<QName, SchemaComponent.Ref> buildDocumentMap(SchemaType[] types) {
        return buildDocumentMap(Arrays.asList(types));
    }

    private static Map<QName, SchemaComponent.Ref> buildDocumentMap(List<? extends SchemaComponent> types) {
        Map<QName, SchemaComponent.Ref> result = new LinkedHashMap<>();
        for (SchemaComponent comp : types) {
            SchemaType type = (SchemaType) comp;
            result.put(type.getDocumentElementName(), type.getRef());
        }
        return result;
    }

    private static Map<QName, SchemaComponent.Ref> buildAttributeTypeMap(SchemaType[] types) {
        Map<QName, SchemaComponent.Ref> result = new LinkedHashMap<>();
        for (SchemaType type : types) {
            result.put(type.getAttributeTypeAttributeName(), type.getRef());
        }
        return result;
    }

    private static Map<QName, SchemaComponent.Ref> buildAttributeTypeMap(List<? extends SchemaComponent> types) {
        Map<QName, SchemaComponent.Ref> result = new LinkedHashMap<>();
        for (SchemaComponent comp : types) {
            SchemaType type = (SchemaType) comp;
            result.put(type.getAttributeTypeAttributeName(), type.getRef());
        }
        return result;
    }

    // Container operation
    private SchemaContainer getContainer(String namespace) {
        return _containers.get(namespace);
    }

    private void addContainer(String namespace) {
        SchemaContainer c = new SchemaContainer(namespace);
        c.setTypeSystem(this);
        _containers.put(namespace, c);
    }

    private SchemaContainer getContainerNonNull(String namespace) {
        SchemaContainer result = getContainer(namespace);
        if (result == null) {
            addContainer(namespace);
            result = getContainer(namespace);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends SchemaComponent.Ref> void buildContainersHelper(Map<QName, SchemaComponent.Ref> elements, BiConsumer<SchemaContainer, T> adder) {
        elements.forEach((k, v) -> adder.accept(getContainerNonNull(k.getNamespaceURI()), (T) v));
    }

    @SuppressWarnings("unchecked")
    private <T extends SchemaComponent.Ref> void buildContainersHelper(List<SchemaComponent.Ref> refs, List<QName> names, BiConsumer<SchemaContainer, T> adder) {
        Iterator<SchemaComponent.Ref> it = refs.iterator();
        Iterator<QName> itname = names.iterator();
        while (it.hasNext()) {
            String ns = itname.next().getNamespaceURI();
            SchemaContainer sc = getContainerNonNull(ns);
            adder.accept(sc, (T) it.next());
        }
    }

    // Only called during init
    private void buildContainers(List<QName> redefTypeNames, List<QName> redefModelGroupNames, List<QName> redefAttributeGroupNames) {
        // This method walks the reference maps and copies said references
        // into the appropriate container
        buildContainersHelper(_globalElements, SchemaContainer::addGlobalElement);
        buildContainersHelper(_globalAttributes, SchemaContainer::addGlobalAttribute);
        buildContainersHelper(_modelGroups, SchemaContainer::addModelGroup);
        buildContainersHelper(_attributeGroups, SchemaContainer::addAttributeGroup);
        buildContainersHelper(_identityConstraints, SchemaContainer::addIdentityConstraint);
        buildContainersHelper(_globalTypes, SchemaContainer::addGlobalType);
        buildContainersHelper(_attributeTypes, SchemaContainer::addAttributeType);

        // Some earlier .xsb versions don't have records for redefinitions
        if (_redefinedGlobalTypes != null && _redefinedModelGroups != null &&
            _redefinedAttributeGroups != null) {
            assert _redefinedGlobalTypes.size() == redefTypeNames.size();
            buildContainersHelper(_redefinedGlobalTypes, redefTypeNames, SchemaContainer::addRedefinedType);
            buildContainersHelper(_redefinedModelGroups, redefModelGroupNames, SchemaContainer::addRedefinedModelGroup);
            buildContainersHelper(_redefinedAttributeGroups, redefAttributeGroupNames, SchemaContainer::addRedefinedAttributeGroup);
        }
        // Some earlier .xsb versions don't have records for annotations
        if (_annotations != null && !_annotations.isEmpty()) {
            // BUGBUG(radup)
            _annotations.forEach(getContainerNonNull("")::addAnnotation);
        }
        _containers.values().forEach(SchemaContainer::setImmutable);
    }

    /**
     * This is the crux of the container work and role.
     * It makes a sweep over all containers and fixes each container's
     * typesystem to point to this typesystem.
     * Because SchemaComponents have a link to their containers, this has as
     * effect all components now indirectly pointing to this typesystem
     * even though they (as well as the typesystem itself) are immutable.
     */
    private void fixupContainers() {
        for (SchemaContainer container : _containers.values()) {
            container.setTypeSystem(this);
            container.setImmutable();
        }
    }

    private void assertContainersHelper(Map<QName, SchemaComponent.Ref> comp, Function<SchemaContainer, List<? extends SchemaComponent>> fun, Function<List<? extends SchemaComponent>, ? extends Map<QName, SchemaComponent.Ref>> fun2) {
        final Map<QName, SchemaComponent.Ref> temp = _containers.values().stream()
            .map(fun).map(fun2 == null ? SchemaTypeSystemImpl::buildComponentRefMap : fun2)
            .map(Map::entrySet).flatMap(Set::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assert comp.equals(temp);
    }

    private void assertContainersHelper(List<? extends SchemaComponent.Ref> comp, Function<SchemaContainer, List<? extends SchemaComponent>> fun) {
        final Set<SchemaComponent.Ref> temp = _containers.values().stream()
            .map(fun).map(SchemaTypeSystemImpl::buildComponentRefList)
            .flatMap(List::stream).collect(Collectors.toSet());
        assert new HashSet<>(comp).equals(temp);
    }

    private void assertContainersSynchronized() {
        boolean assertEnabled = false;
        // This code basically checks whether asserts are enabled so we don't do
        // all the work if they arent
        assert assertEnabled = true;
        if (!assertEnabled) {
            return;
        }

        assertContainersHelper(_globalElements, SchemaContainer::globalElements, null);
        assertContainersHelper(_globalAttributes, SchemaContainer::globalAttributes, null);
        assertContainersHelper(_modelGroups, SchemaContainer::modelGroups, null);
        assertContainersHelper(_modelGroups, SchemaContainer::modelGroups, null);
        assertContainersHelper(_redefinedModelGroups, SchemaContainer::redefinedModelGroups);
        assertContainersHelper(_attributeGroups, SchemaContainer::attributeGroups, null);
        assertContainersHelper(_redefinedAttributeGroups, SchemaContainer::redefinedAttributeGroups);
        assertContainersHelper(_globalTypes, SchemaContainer::globalTypes, null);
        assertContainersHelper(_redefinedGlobalTypes, SchemaContainer::redefinedGlobalTypes);
        assertContainersHelper(_documentTypes, SchemaContainer::documentTypes, SchemaTypeSystemImpl::buildDocumentMap);
        assertContainersHelper(_attributeTypes, SchemaContainer::attributeTypes, SchemaTypeSystemImpl::buildAttributeTypeMap);
        assertContainersHelper(_identityConstraints, SchemaContainer::identityConstraints, null);

        // annotations
        Set<SchemaAnnotation> temp3 = _containers.values().stream()
            .map(SchemaContainer::annotations).flatMap(List::stream).collect(Collectors.toSet());
        assert new HashSet<>(_annotations).equals(temp3);
        // namespaces
        Set<String> temp4 = _containers.values().stream()
            .map(SchemaContainer::getNamespace).collect(Collectors.toSet());
        assert _namespaces.equals(temp4);
    }

    private static Random _random;
    private static final byte[] _mask = new byte[128 / 8];

    /**
     * Fun, fun.  Produce 128 bits of uniqueness randomly.
     * We used to use SecureRandom, but now we don't because SecureRandom
     * hits the filesystem and hangs us on a filesystem lock.  It also eats
     * a thread and other expensive resources.. :-).
     * <p>
     * We don't really care that non-secure Random() can only do 48 bits of
     * randomness, since we're certainly not going to be called more than 2^48
     * times within our process lifetime.
     * <p>
     * Our real concern is that by seeding Random() with the current
     * time, two users will end up with the same bits if they start a
     * schema compilation within the same millisecond.  That makes the
     * probability of collision in the real world slightly too high.
     * We're going to have millions of users, remember?  With a million
     * users, and one-compilation-per-day each, we'd see a collision every
     * few months.
     * <p>
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
    private static synchronized void nextBytes(byte[] result) {
        if (_random == null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);

                // at least 10 bits of unqieueness, right?  Maybe even 50 or 60.
                daos.writeInt(System.identityHashCode(SchemaTypeSystemImpl.class));
                String[] props = new String[]{"user.name", "user.dir", "user.timezone", "user.country", "java.class.path", "java.home", "java.vendor", "java.version", "os.version"};
                for (String s : props) {
                    String prop = SystemProperties.getProperty(s);
                    if (prop != null) {
                        daos.writeUTF(prop);
                        daos.writeInt(System.identityHashCode(prop));
                    }
                }
                daos.writeLong(Runtime.getRuntime().freeMemory());
                daos.close();
                byte[] bytes = baos.toByteArray();
                for (int i = 0; i < bytes.length; i++) {
                    int j = i % _mask.length;
                    _mask[j] *= 21;
                    _mask[j] += i;
                }
            } catch (IOException e) {
                XBeanDebug.logException(e);
            }

            _random = new Random(System.currentTimeMillis());
        }
        _random.nextBytes(result);
        for (int i = 0; i < result.length; i++) {
            int j = i & _mask.length;
            result[i] ^= _mask[j];
        }
    }

    public SchemaTypeSystemImpl(String nameForSystem) {
        // if we have no name, select a random one
        if (nameForSystem == null) {
            // get 128 random bits (that'll be 32 hex digits)
            byte[] bytes = new byte[128 / 8];
            nextBytes(bytes);
            nameForSystem = "s" + new String(HexBin.encode(bytes), StandardCharsets.ISO_8859_1);
        }

        _name = SchemaTypeSystemImpl.METADATA_PACKAGE_GEN.replace('/', '.') + ".system." + nameForSystem;
        _basePackage = nameToPathString(_name);
        _classloader = null;
    }

    public void loadFromStscState(StscState state) {
        assert (_classloader == null);
        _localHandles = new HandlePool();
        _globalElements = buildComponentRefMap(state.globalElements());
        _globalAttributes = buildComponentRefMap(state.globalAttributes());
        _modelGroups = buildComponentRefMap(state.modelGroups());
        _redefinedModelGroups = buildComponentRefList(state.redefinedModelGroups());
        _attributeGroups = buildComponentRefMap(state.attributeGroups());
        _redefinedAttributeGroups = buildComponentRefList(state.redefinedAttributeGroups());
        _globalTypes = buildComponentRefMap(state.globalTypes());
        _redefinedGlobalTypes = buildComponentRefList(state.redefinedGlobalTypes());
        _documentTypes = buildDocumentMap(state.documentTypes());
        _attributeTypes = buildAttributeTypeMap(state.attributeTypes());
        _typeRefsByClassname = buildTypeRefsByClassname(state.typesByClassname());
        _identityConstraints = buildComponentRefMap(state.idConstraints());
        _annotations = state.annotations();
        _namespaces = new HashSet<>(Arrays.asList(state.getNamespaces()));
        _containers = state.getContainerMap();
        fixupContainers();
        // Checks that data in the containers matches the lookup maps
        assertContainersSynchronized();
        setDependencies(state.getDependencies());
    }

    final SchemaTypeSystemImpl getTypeSystem() {
        return this;
    }

    void setDependencies(SchemaDependencies deps) {
        _deps = deps;
    }

    SchemaDependencies getDependencies() {
        return _deps;
    }

    // EXPERIMENTAL
    public boolean isIncomplete() {
        return _incomplete;
    }

    // EXPERIMENTAL
    void setIncomplete(boolean incomplete) {
        _incomplete = incomplete;
    }

    static class StringPool {
        private final List<String> intsToStrings = new ArrayList<>();
        private final Map<String, Integer> stringsToInts = new HashMap<>();
        private final String _handle;
        private final String _name;

        /**
         * Constructs an empty StringPool to be filled with strings.
         */
        StringPool(String handle, String name) {
            _handle = handle;
            _name = name;
            intsToStrings.add(null);
        }

        int codeForString(String str) {
            if (str == null) {
                return 0;
            }
            Integer result = stringsToInts.get(str);
            if (result == null) {
                result = intsToStrings.size();
                intsToStrings.add(str);
                stringsToInts.put(str, result);
            }
            return result;
        }

        String stringForCode(int code) {
            if (code == 0) {
                return null;
            }
            return intsToStrings.get(code);
        }

        void writeTo(DataOutputStream output) {
            if (intsToStrings.size() >= MAX_UNSIGNED_SHORT) {
                throw new SchemaTypeLoaderException("Too many strings (" + intsToStrings.size() + ")", _name, _handle, SchemaTypeLoaderException.INT_TOO_LARGE);
            }

            try {
                output.writeShort(intsToStrings.size());
                boolean isNext = false;
                for (String str : intsToStrings) {
                    if (isNext) {
                        output.writeUTF(str);
                    }
                    isNext = true;
                }
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        void readFrom(DataInputStream input) {
            if (intsToStrings.size() != 1 || stringsToInts.size() != 0) {
                throw new IllegalStateException();
            }

            try {
                int size = input.readUnsignedShort();
                for (int i = 1; i < size; i++) {
                    String str = input.readUTF().intern();
                    int code = codeForString(str);
                    if (code != i) {
                        throw new IllegalStateException();
                    }
                }
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage() == null ? e.getMessage() : "IO Exception", _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }
    }

    class HandlePool {
        private final Map<String, SchemaComponent.Ref> _handlesToRefs = new LinkedHashMap<>();
        // populated on write
        private final Map<SchemaComponent, String> _componentsToHandles = new LinkedHashMap<>();
        private boolean _started;

        /**
         * Constructs an empty HandlePool to be populated.
         */
        HandlePool() {
        }

        private String addUniqueHandle(SchemaComponent obj, String base) {
            // we lowercase handles because of case-insensitive Windows filenames!!!
            base = base.toLowerCase(Locale.ROOT);
            String handle = base;
            for (int index = 2; _handlesToRefs.containsKey(handle); index++) {
                handle = base + index;
            }
            _handlesToRefs.put(handle, obj.getComponentRef());
            _componentsToHandles.put(obj, handle);
            return handle;
        }

        String handleForComponent(SchemaComponent comp) {
            if (comp == null) {
                return null;
            }
            if (comp.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            if (comp instanceof SchemaType) {
                return handleForType((SchemaType) comp);
            }
            if (comp instanceof SchemaGlobalElement) {
                return handleForElement((SchemaGlobalElement) comp);
            }
            if (comp instanceof SchemaGlobalAttribute) {
                return handleForAttribute((SchemaGlobalAttribute) comp);
            }
            if (comp instanceof SchemaModelGroup) {
                return handleForModelGroup((SchemaModelGroup) comp);
            }
            if (comp instanceof SchemaAttributeGroup) {
                return handleForAttributeGroup((SchemaAttributeGroup) comp);
            }
            if (comp instanceof SchemaIdentityConstraint) {
                return handleForIdentityConstraint((SchemaIdentityConstraint) comp);
            }
            throw new IllegalStateException("Component type cannot have a handle");
        }

        String handleForElement(SchemaGlobalElement element) {
            if (element == null) {
                return null;
            }
            if (element.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            String handle = _componentsToHandles.get(element);
            if (handle == null) {
                handle = addUniqueHandle(element, NameUtil.upperCamelCase(element.getName().getLocalPart()) + "Element");
            }
            return handle;
        }

        String handleForAttribute(SchemaGlobalAttribute attribute) {
            if (attribute == null) {
                return null;
            }
            if (attribute.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            String handle = _componentsToHandles.get(attribute);
            if (handle == null) {
                handle = addUniqueHandle(attribute, NameUtil.upperCamelCase(attribute.getName().getLocalPart()) + "Attribute");
            }
            return handle;
        }

        String handleForModelGroup(SchemaModelGroup group) {
            if (group == null) {
                return null;
            }
            if (group.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            String handle = _componentsToHandles.get(group);
            if (handle == null) {
                handle = addUniqueHandle(group, NameUtil.upperCamelCase(group.getName().getLocalPart()) + "ModelGroup");
            }
            return handle;
        }

        String handleForAttributeGroup(SchemaAttributeGroup group) {
            if (group == null) {
                return null;
            }
            if (group.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            String handle = _componentsToHandles.get(group);
            if (handle == null) {
                handle = addUniqueHandle(group, NameUtil.upperCamelCase(group.getName().getLocalPart()) + "AttributeGroup");
            }
            return handle;
        }

        String handleForIdentityConstraint(SchemaIdentityConstraint idc) {
            if (idc == null) {
                return null;
            }
            if (idc.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            String handle = _componentsToHandles.get(idc);
            if (handle == null) {
                handle = addUniqueHandle(idc, NameUtil.upperCamelCase(idc.getName().getLocalPart()) + "IdentityConstraint");
            }
            return handle;
        }

        String handleForType(SchemaType type) {
            if (type == null) {
                return null;
            }
            if (type.getTypeSystem() != getTypeSystem()) {
                throw new IllegalArgumentException("Cannot supply handles for types from another type system");
            }
            String handle = _componentsToHandles.get(type);
            if (handle == null) {
                QName name = type.getName();
                String suffix = "";
                if (name == null) {
                    if (type.isDocumentType()) {
                        name = type.getDocumentElementName();
                        suffix = "Doc";
                    } else if (type.isAttributeType()) {
                        name = type.getAttributeTypeAttributeName();
                        suffix = "AttrType";
                    } else if (type.getContainerField() != null) {
                        name = type.getContainerField().getName();
                        suffix = type.getContainerField().isAttribute() ? "Attr" : "Elem";
                    }
                }

                String baseName;
                String uniq = Integer.toHexString(type.toString().hashCode() | 0x80000000).substring(4).toUpperCase(Locale.ROOT);
                if (name == null) {
                    baseName = "Anon" + uniq + "Type";
                } else {
                    baseName = NameUtil.upperCamelCase(name.getLocalPart()) + uniq + suffix + "Type";
                }

                handle = addUniqueHandle(type, baseName);
            }

            return handle;
        }

        SchemaComponent.Ref refForHandle(String handle) {
            if (handle == null) {
                return null;
            }

            return _handlesToRefs.get(handle);
        }

        void startWriteMode() {
            _started = true;
            _componentsToHandles.clear();
            for (String handle : _handlesToRefs.keySet()) {
                SchemaComponent comp = _handlesToRefs.get(handle).getComponent();
                _componentsToHandles.put(comp, handle);
            }
        }

    }

    private final String _name;
    private final String _basePackage;

    // EXPERIMENTAL: recovery from compilation errors and partial type systems
    private boolean _incomplete = false;

    // classloader is available for sts's that were compiled and loaded, not dynamic ones
    private ClassLoader _classloader;

    // the loader for loading .xsb resources
    private ResourceLoader _resourceLoader;

    // the following is used to link references during load
    SchemaTypeLoader _linker;

    private HandlePool _localHandles;
    private Filer _filer;

    // top-level annotations
    private List<SchemaAnnotation> _annotations;

    // container
    private Map<String, SchemaContainer> _containers = new HashMap<>();
    // dependencies
    private SchemaDependencies _deps;

    private List<SchemaComponent.Ref> _redefinedModelGroups;
    private List<SchemaComponent.Ref> _redefinedAttributeGroups;
    private List<SchemaComponent.Ref> _redefinedGlobalTypes;

    // actual type system data, map QNames -> SchemaComponent.Ref
    private Map<QName, SchemaComponent.Ref> _globalElements;
    private Map<QName, SchemaComponent.Ref> _globalAttributes;
    private Map<QName, SchemaComponent.Ref> _modelGroups;
    private Map<QName, SchemaComponent.Ref> _attributeGroups;
    private Map<QName, SchemaComponent.Ref> _globalTypes;
    private Map<QName, SchemaComponent.Ref> _documentTypes;
    private Map<QName, SchemaComponent.Ref> _attributeTypes;
    private Map<QName, SchemaComponent.Ref> _identityConstraints = Collections.emptyMap();
    private Map<String, SchemaComponent.Ref> _typeRefsByClassname = new HashMap<>();
    private Set<String> _namespaces;

    static private final SchemaType[] EMPTY_ST_ARRAY = new SchemaType[0];
    static private final SchemaGlobalElement[] EMPTY_GE_ARRAY = new SchemaGlobalElement[0];
    static private final SchemaGlobalAttribute[] EMPTY_GA_ARRAY = new SchemaGlobalAttribute[0];
    static private final SchemaModelGroup[] EMPTY_MG_ARRAY = new SchemaModelGroup[0];
    static private final SchemaAttributeGroup[] EMPTY_AG_ARRAY = new SchemaAttributeGroup[0];
    static private final SchemaIdentityConstraint[] EMPTY_IC_ARRAY = new SchemaIdentityConstraint[0];
    static private final SchemaAnnotation[] EMPTY_ANN_ARRAY = new SchemaAnnotation[0];

    public void saveToDirectory(File classDir) {
        save(new FilerImpl(classDir, null, null, false, false));
    }

    public void save(Filer filer) {
        if (_incomplete) {
            throw new IllegalStateException("Incomplete SchemaTypeSystems cannot be saved.");
        }

        if (filer == null) {
            throw new IllegalArgumentException("filer must not be null");
        }
        _filer = filer;

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

        saveLoader();
    }

    void saveTypesRecursively(SchemaType[] types) {
        for (SchemaType type : types) {
            if (type.getTypeSystem() != getTypeSystem()) {
                continue;
            }
            saveType(type);
            saveTypesRecursively(type.getAnonymousTypes());
        }
    }

    public void saveGlobalElements(SchemaGlobalElement[] elts) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        for (SchemaGlobalElement elt : elts) {
            saveGlobalElement(elt);
        }
    }

    public void saveGlobalAttributes(SchemaGlobalAttribute[] attrs) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        for (SchemaGlobalAttribute attr : attrs) {
            saveGlobalAttribute(attr);
        }
    }

    public void saveModelGroups(SchemaModelGroup[] groups) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        for (SchemaModelGroup group : groups) {
            saveModelGroup(group);
        }
    }

    public void saveAttributeGroups(SchemaAttributeGroup[] groups) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        for (SchemaAttributeGroup group : groups) {
            saveAttributeGroup(group);
        }
    }

    public void saveIdentityConstraints(SchemaIdentityConstraint[] idcs) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        for (SchemaIdentityConstraint idc : idcs) {
            saveIdentityConstraint(idc);
        }
    }

    public void saveGlobalElement(SchemaGlobalElement elt) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        String handle = _localHandles.handleForElement(elt);
        XsbReader saver = new XsbReader(handle);
        saver.writeParticleData((SchemaParticle) elt);
        saver.writeString(elt.getSourceName());
        saver.writeRealHeader(handle, FILETYPE_SCHEMAELEMENT);
        saver.writeParticleData((SchemaParticle) elt);
        saver.writeString(elt.getSourceName());
        saver.writeEnd();
    }

    public void saveGlobalAttribute(SchemaGlobalAttribute attr) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        String handle = _localHandles.handleForAttribute(attr);
        XsbReader saver = new XsbReader(handle);
        saver.writeAttributeData(attr);
        saver.writeString(attr.getSourceName());
        saver.writeRealHeader(handle, FILETYPE_SCHEMAATTRIBUTE);
        saver.writeAttributeData(attr);
        saver.writeString(attr.getSourceName());
        saver.writeEnd();
    }

    public void saveModelGroup(SchemaModelGroup grp) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        String handle = _localHandles.handleForModelGroup(grp);
        XsbReader saver = new XsbReader(handle);
        saver.writeModelGroupData(grp);
        saver.writeRealHeader(handle, FILETYPE_SCHEMAMODELGROUP);
        saver.writeModelGroupData(grp);
        saver.writeEnd();
    }

    public void saveAttributeGroup(SchemaAttributeGroup grp) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        String handle = _localHandles.handleForAttributeGroup(grp);
        XsbReader saver = new XsbReader(handle);
        saver.writeAttributeGroupData(grp);
        saver.writeRealHeader(handle, FILETYPE_SCHEMAATTRIBUTEGROUP);
        saver.writeAttributeGroupData(grp);
        saver.writeEnd();
    }

    public void saveIdentityConstraint(SchemaIdentityConstraint idc) {
        if (_incomplete) {
            throw new IllegalStateException("This SchemaTypeSystem cannot be saved.");
        }
        String handle = _localHandles.handleForIdentityConstraint(idc);
        XsbReader saver = new XsbReader(handle);
        saver.writeIdConstraintData(idc);
        saver.writeRealHeader(handle, FILETYPE_SCHEMAIDENTITYCONSTRAINT);
        saver.writeIdConstraintData(idc);
        saver.writeEnd();
    }

    void saveType(SchemaType type) {
        String handle = _localHandles.handleForType(type);
        XsbReader saver = new XsbReader(handle);
        saver.writeTypeData(type);
        saver.writeRealHeader(handle, FILETYPE_SCHEMATYPE);
        saver.writeTypeData(type);
        saver.writeEnd();
    }

    public static String crackPointer(InputStream stream) {
        try (DataInputStream input = new DataInputStream(stream)) {

            int magic = input.readInt();
            if (magic != DATA_BABE) {
                return null;
            }

            int majorver = input.readShort();
            int minorver = input.readShort();

            if (majorver != MAJOR_VERSION) {
                return null;
            }

            if (minorver > MINOR_VERSION) {
                return null;
            }

            if (minorver >= 18) {
                input.readShort(); // release number present in atLeast(2, 18, 0)
            }

            int actualfiletype = input.readShort();
            if (actualfiletype != FILETYPE_SCHEMAPOINTER) {
                return null;
            }

            StringPool stringPool = new StringPool("pointer", "unk");
            stringPool.readFrom(input);

            return stringPool.stringForCode(input.readShort());
        } catch (IOException e) {
            return null;
        }
    }

    private class XsbReader {
        DataInputStream _input;
        DataOutputStream _output;
        StringPool _stringPool;
        String _handle;
        private int _majorver;
        private int _minorver;
        private int _releaseno;
        int _actualfiletype;

        public XsbReader(String handle, int filetype) {
            String resourcename = _basePackage + handle + ".xsb";
            InputStream rawinput = getLoaderStream(resourcename);
            if (rawinput == null) {
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Could not locate compiled schema resource " + resourcename, _name, handle, SchemaTypeLoaderException.NO_RESOURCE);
            }

            _input = new DataInputStream(rawinput);
            _handle = handle;

            int magic = readInt();
            if (magic != DATA_BABE) {
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Wrong magic cookie", _name, handle, SchemaTypeLoaderException.WRONG_MAGIC_COOKIE);
            }

            _majorver = readShort();
            _minorver = readShort();

            if (_majorver != MAJOR_VERSION) {
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Wrong major version - expecting " + MAJOR_VERSION + ", got " + _majorver, _name, handle, SchemaTypeLoaderException.WRONG_MAJOR_VERSION);
            }

            if (_minorver > MINOR_VERSION) {
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Incompatible minor version - expecting up to " + MINOR_VERSION + ", got " + _minorver, _name, handle, SchemaTypeLoaderException.WRONG_MINOR_VERSION);
            }

            // Clip to 14 because we're not backward compatible with earlier
            // minor versions.  Remove this when upgrading to a new major
            // version

            if (_minorver < 14) {
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: Incompatible minor version - expecting at least 14, got " + _minorver, _name, handle, SchemaTypeLoaderException.WRONG_MINOR_VERSION);
            }

            if (atLeast(2, 18, 0)) {
                _releaseno = readShort();
            }

            int actualfiletype = readShort();
            if (actualfiletype != filetype && filetype != 0xFFFF) {
                throw new SchemaTypeLoaderException("XML-BEANS compiled schema: File has the wrong type - expecting type " + filetype + ", got type " + actualfiletype, _name, handle, SchemaTypeLoaderException.WRONG_FILE_TYPE);
            }

            _stringPool = new StringPool(_handle, _name);
            _stringPool.readFrom(_input);

            _actualfiletype = actualfiletype;
        }

        protected boolean atLeast(int majorver, int minorver, int releaseno) {
            if (_majorver > majorver) {
                return true;
            }
            if (_majorver < majorver) {
                return false;
            }
            if (_minorver > minorver) {
                return true;
            }
            if (_minorver < minorver) {
                return false;
            }
            return (_releaseno >= releaseno);
        }

        protected boolean atMost(int majorver, int minorver, int releaseno) {
            if (_majorver > majorver) {
                return false;
            }
            if (_majorver < majorver) {
                return true;
            }
            if (_minorver > minorver) {
                return false;
            }
            if (_minorver < minorver) {
                return true;
            }
            return (_releaseno <= releaseno);
        }

        int getActualFiletype() {
            return _actualfiletype;
        }

        XsbReader(String handle) {
            _handle = handle;
            _stringPool = new StringPool(_handle, _name);
        }

        void writeRealHeader(String handle, int filetype) {
            // hackeroo: if handle contains a "/" it's not relative.
            String resourcename;

            if (handle.indexOf('/') >= 0) {
                resourcename = handle + ".xsb";
            } else {
                resourcename = _basePackage + handle + ".xsb";
            }

            OutputStream rawoutput = getSaverStream(resourcename);
            if (rawoutput == null) {
                throw new SchemaTypeLoaderException("Could not write compiled schema resource " + resourcename, _name, handle, SchemaTypeLoaderException.NOT_WRITEABLE);
            }

            _output = new DataOutputStream(rawoutput);
            _handle = handle;

            writeInt(DATA_BABE);
            writeShort(MAJOR_VERSION);
            writeShort(MINOR_VERSION);
            writeShort(RELEASE_NUMBER);
            writeShort(filetype);

            _stringPool.writeTo(_output);
        }

        void readEnd() {
            try {
                if (_input != null) {
                    _input.close();
                }
            } catch (IOException e) {
                // oh, well.
            }
            _input = null;
            _stringPool = null;
            _handle = null;
        }

        void writeEnd() {
            try {
                if (_output != null) {
                    _output.flush();
                    _output.close();
                }
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
            _output = null;
            _stringPool = null;
            _handle = null;
        }

        int fileTypeFromComponentType(int componentType) {
            switch (componentType) {
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

        void writeIndexData() {
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
            writeAnnotations(annotations());
        }

        void writeHandlePool(HandlePool pool) {
            writeShort(pool._componentsToHandles.size());
            pool._componentsToHandles.forEach((comp, handle) -> {
                writeString(handle);
                writeShort(fileTypeFromComponentType(comp.getComponentType()));
            });
        }

        void readHandlePool(HandlePool pool) {
            if (pool._handlesToRefs.size() != 0 || pool._started) {
                throw new IllegalStateException("Nonempty handle set before read");
            }

            int size = readShort();
            for (int i = 0; i < size; i++) {
                String handle = readString();
                int code = readShort();
                SchemaComponent.Ref result;
                switch (code) {
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

        int readShort() {
            try {
                return _input.readUnsignedShort();
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        void writeShort(int s) {
            if (s >= MAX_UNSIGNED_SHORT || s < -1) {
                throw new SchemaTypeLoaderException("Value " + s + " out of range: must fit in a 16-bit unsigned short.", _name, _handle, SchemaTypeLoaderException.INT_TOO_LARGE);
            }
            if (_output != null) {
                try {
                    _output.writeShort(s);
                } catch (IOException e) {
                    throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
                }
            }
        }

        int readInt() {
            try {
                return _input.readInt();
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        void writeInt(int i) {
            if (_output != null) {
                try {
                    _output.writeInt(i);
                } catch (IOException e) {
                    throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
                }
            }
        }

        String readString() {
            return _stringPool.stringForCode(readShort());
        }

        void writeString(String str) {
            int code = _stringPool.codeForString(str);
            writeShort(code);
        }

        QName readQName() {
            String namespace = readString();
            String localname = readString();
            if (localname == null) {
                return null;
            }
            return new QName(namespace, localname);
        }

        void writeQName(QName qname) {
            if (qname == null) {
                writeString(null);
                writeString(null);
                return;
            }
            writeString(qname.getNamespaceURI());
            writeString(qname.getLocalPart());
        }

        SOAPArrayType readSOAPArrayType() {
            QName qName = readQName();
            String dimensions = readString();
            if (qName == null) {
                return null;
            }
            return new SOAPArrayType(qName, dimensions);
        }

        void writeSOAPArrayType(SOAPArrayType arrayType) {
            if (arrayType == null) {
                writeQName(null);
                writeString(null);
            } else {
                writeQName(arrayType.getQName());
                writeString(arrayType.soap11DimensionString());
            }
        }

        void writeAnnotation(SchemaAnnotation a) {
            // Write attributes
            if (a == null) {
                writeInt(-1);
                return;
            }
            SchemaAnnotation.Attribute[] attributes = a.getAttributes();
            writeInt(attributes.length);
            for (SchemaAnnotation.Attribute attribute : attributes) {
                QName name = attribute.getName();
                String value = attribute.getValue();
                String valueURI = attribute.getValueUri();
                writeQName(name);
                writeString(value);
                writeString(valueURI);
            }

            // Write documentation items
            XmlObject[] documentationItems = a.getUserInformation();
            writeInt(documentationItems.length);
            XmlOptions opt = new XmlOptions().setSaveOuter().setSaveAggressiveNamespaces();
            for (XmlObject doc : documentationItems) {
                writeString(doc.xmlText(opt));
            }

            // Write application info items
            XmlObject[] appInfoItems = a.getApplicationInformation();
            writeInt(appInfoItems.length);
            for (XmlObject doc : appInfoItems) {
                writeString(doc.xmlText(opt));
            }
        }

        SchemaAnnotation readAnnotation(SchemaContainer c) {
            if (!atLeast(2, 19, 0)) {
                return null; // no annotations for this version of the file
            }
            // Read attributes
            int n = readInt();
            if (n == -1) {
                return null;
            }
            SchemaAnnotation.Attribute[] attributes =
                new SchemaAnnotation.Attribute[n];
            for (int i = 0; i < n; i++) {
                QName name = readQName();
                String value = readString();
                String valueUri = null;
                if (atLeast(2, 24, 0)) {
                    valueUri = readString();
                }
                attributes[i] = new SchemaAnnotationImpl.AttributeImpl(name, value, valueUri);
            }

            // Read documentation items
            n = readInt();
            String[] docStrings = new String[n];
            for (int i = 0; i < n; i++) {
                docStrings[i] = readString();
            }

            // Read application info items
            n = readInt();
            String[] appInfoStrings = new String[n];
            for (int i = 0; i < n; i++) {
                appInfoStrings[i] = readString();
            }

            return new SchemaAnnotationImpl(c, appInfoStrings,
                docStrings, attributes);
        }

        void writeAnnotations(SchemaAnnotation[] anns) {
            writeInt(anns.length);
            for (SchemaAnnotation ann : anns) {
                writeAnnotation(ann);
            }
        }

        List<SchemaAnnotation> readAnnotations() {
            int n = readInt();
            List<SchemaAnnotation> result = new ArrayList<>(n);
            // BUGBUG(radup)
            SchemaContainer container = getContainerNonNull("");
            for (int i = 0; i < n; i++) {
                result.add(readAnnotation(container));
            }
            return result;
        }

        SchemaComponent.Ref readHandle() {
            String handle = readString();
            if (handle == null) {
                return null;
            }

            if (handle.charAt(0) != '_') {
                return _localHandles.refForHandle(handle);
            }

            switch (handle.charAt(2)) {
                case 'I': // _BI_ - built-in schema type system
                    SchemaType st = (SchemaType) BuiltinSchemaTypeSystem.get().resolveHandle(handle);
                    if (st != null) {
                        return st.getRef();
                    }
                    st = (SchemaType) XQuerySchemaTypeSystem.get().resolveHandle(handle);
                    return st.getRef();
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
                    if (attr == null) {
                        throw new SchemaTypeLoaderException("Cannot resolve attribute for handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
                    }
                    return attr.getType().getRef();
                case 'S': // _XS_ - external ref to element's type
                    // deprecated: replaced by _XY_
                    SchemaGlobalElement elem = _linker.findElement(QNameHelper.forPretty(handle, 4));
                    if (elem == null) {
                        throw new SchemaTypeLoaderException("Cannot resolve element for handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
                    }
                    return elem.getType().getRef();
                case 'O': // _XO_ - external ref to document type
                    return _linker.findDocumentTypeRef(QNameHelper.forPretty(handle, 4));
                case 'Y': // _XY_ - external ref to any possible type
                    SchemaType type = _linker.typeForSignature(handle.substring(4));
                    if (type == null) {
                        throw new SchemaTypeLoaderException("Cannot resolve type for handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
                    }
                    return type.getRef();
                default:
                    throw new SchemaTypeLoaderException("Cannot resolve handle " + handle, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
            }
        }

        void writeHandle(SchemaComponent comp) {
            if (comp == null || comp.getTypeSystem() == getTypeSystem()) {
                writeString(_localHandles.handleForComponent(comp));
                return;
            }

            switch (comp.getComponentType()) {
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
                    SchemaType type = (SchemaType) comp;
                    if (type.isBuiltinType()) {
                        writeString("_BI_" + type.getName().getLocalPart());
                        return;
                    }

                    // fix for CR120759 - added output of types _XR_ & _XS_
                    // when an attribute (_XR_) or element (_XS_) declaration
                    // uses ref to refer to an attribute or element in another
                    // schema and the type of that attribute or element
                    // is an anonymous (local) type
                    // kkrouse 02/1/2005: _XR_ and _XS_ refs are replaced by _XY_
                    if (type.getName() != null) {
                        writeString("_XT_" + QNameHelper.pretty(type.getName()));
                    } else if (type.isDocumentType()) {
                        // Substitution groups will create document types that
                        // extend from other document types, possibly in
                        // different jars
                        writeString("_XO_" + QNameHelper.pretty(type.getDocumentElementName()));
                    } else {
                        // fix for XMLBEANS-105:
                        // save out the external type reference using the type's signature.
                        writeString("_XY_" + type.toString());
                    }

                    return;

                default:
                    assert (false);
                    throw new SchemaTypeLoaderException("Cannot write handle for component " + comp, _name, _handle, SchemaTypeLoaderException.BAD_HANDLE);
            }
        }

        SchemaType.Ref readTypeRef() {
            return (SchemaType.Ref) readHandle();
        }

        void writeType(SchemaType type) {
            writeHandle(type);
        }

        Map<QName, SchemaComponent.Ref> readQNameRefMap() {
            Map<QName, SchemaComponent.Ref> result = new HashMap<>();
            int size = readShort();
            for (int i = 0; i < size; i++) {
                QName name = readQName();
                SchemaComponent.Ref obj = readHandle();
                result.put(name, obj);
            }
            return result;
        }

        List<SchemaComponent.Ref> readQNameRefMapAsList(List<QName> names) {
            int size = readShort();
            List<SchemaComponent.Ref> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                QName name = readQName();
                SchemaComponent.Ref obj = readHandle();
                result.add(obj);
                names.add(name);
            }
            return result;
        }

        void writeQNameMap(SchemaComponent[] components) {
            writeShort(components.length);
            for (SchemaComponent component : components) {
                writeQName(component.getName());
                writeHandle(component);
            }
        }

        void writeDocumentTypeMap(SchemaType[] doctypes) {
            writeShort(doctypes.length);
            for (SchemaType doctype : doctypes) {
                writeQName(doctype.getDocumentElementName());
                writeHandle(doctype);
            }
        }

        void writeAttributeTypeMap(SchemaType[] attrtypes) {
            writeShort(attrtypes.length);
            for (SchemaType attrtype : attrtypes) {
                writeQName(attrtype.getAttributeTypeAttributeName());
                writeHandle(attrtype);
            }
        }

        SchemaType.Ref[] readTypeRefArray() {
            int size = readShort();
            SchemaType.Ref[] result = new SchemaType.Ref[size];
            for (int i = 0; i < size; i++) {
                result[i] = readTypeRef();
            }
            return result;
        }

        void writeTypeArray(SchemaType[] array) {
            writeShort(array.length);
            for (SchemaType schemaType : array) {
                writeHandle(schemaType);
            }
        }

        Map<String, SchemaComponent.Ref> readClassnameRefMap() {
            Map<String, SchemaComponent.Ref> result = new HashMap<>();
            int size = readShort();
            for (int i = 0; i < size; i++) {
                String name = readString();
                SchemaComponent.Ref obj = readHandle();
                result.put(name, obj);
            }
            return result;
        }

        void writeClassnameMap(Map<String, SchemaComponent.Ref> typesByClass) {
            writeShort(typesByClass.size());
            typesByClass.forEach((className, ref) -> {
                writeString(className);
                writeHandle(((SchemaType.Ref) ref).get());
            });
        }

        Set<String> readNamespaces() {
            Set<String> result = new HashSet<>();
            int size = readShort();
            for (int i = 0; i < size; i++) {
                String ns = readString();
                result.add(ns);
            }
            return result;
        }

        void writeNamespaces(Set<String> namespaces) {
            writeShort(namespaces.size());
            namespaces.forEach(this::writeString);
        }

        OutputStream getSaverStream(String name) {
            try {
                return _filer.createBinaryFile(name);
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        InputStream getLoaderStream(String resourcename) {
            return _resourceLoader.getResourceAsStream(resourcename);
        }

        void checkContainerNotNull(SchemaContainer container, QName name) {
            if (container == null) {
                throw new LinkageError("Loading of resource " + name + '.' + _handle +
                                       "failed, information from " + name + ".index.xsb is " +
                                       " out of sync (or conflicting index files found)");
            }
        }

        /**
         * Finishes loading an element after the header has already been loaded.
         */
        public SchemaGlobalElement finishLoadingElement() {
            String handle = null;
            try {
                int particleType = readShort();
                if (particleType != SchemaParticle.ELEMENT) {
                    throw new SchemaTypeLoaderException("Wrong particle type ", _name, _handle, SchemaTypeLoaderException.BAD_PARTICLE_TYPE);
                }
                int particleFlags = readShort();
                BigInteger minOccurs = readBigInteger();
                BigInteger maxOccurs = readBigInteger();
                QNameSet transitionRules = readQNameSet();
                QName name = readQName();
                SchemaContainer container = getContainer(name.getNamespaceURI());
                checkContainerNotNull(container, name);
                SchemaGlobalElementImpl impl = new SchemaGlobalElementImpl(container);
                impl.setParticleType(particleType);
                impl.setMinOccurs(minOccurs);
                impl.setMaxOccurs(maxOccurs);
                impl.setTransitionRules(transitionRules,
                    (particleFlags & FLAG_PART_SKIPPABLE) != 0);
                impl.setNameAndTypeRef(name, readTypeRef());
                impl.setDefault(readString(), (particleFlags & FLAG_PART_FIXED) != 0, null);
                if (atLeast(2, 16, 0)) {
                    impl.setDefaultValue(readXmlValueObject());
                }
                impl.setNillable((particleFlags & FLAG_PART_NILLABLE) != 0);
                impl.setBlock((particleFlags & FLAG_PART_BLOCKEXT) != 0,
                    (particleFlags & FLAG_PART_BLOCKREST) != 0,
                    (particleFlags & FLAG_PART_BLOCKSUBST) != 0);
                impl.setWsdlArrayType(readSOAPArrayType());
                impl.setAbstract((particleFlags & FLAG_PART_ABSTRACT) != 0);
                impl.setAnnotation(readAnnotation(container));
                impl.setFinal(
                    (particleFlags & FLAG_PART_FINALEXT) != 0,
                    (particleFlags & FLAG_PART_FINALREST) != 0);

                if (atLeast(2, 17, 0)) {
                    impl.setSubstitutionGroup((SchemaGlobalElement.Ref) readHandle());
                }

                int substGroupCount = readShort();
                for (int i = 0; i < substGroupCount; i++) {
                    impl.addSubstitutionGroupMember(readQName());
                }
                SchemaIdentityConstraint.Ref[] idcs = new SchemaIdentityConstraint.Ref[readShort()];

                for (int i = 0; i < idcs.length; i++) {
                    idcs[i] = (SchemaIdentityConstraint.Ref) readHandle();
                }

                impl.setIdentityConstraints(idcs);
                impl.setFilename(readString());
                return impl;
            } catch (SchemaTypeLoaderException e) {
                throw e;
            } catch (Exception e) {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            } finally {
                readEnd();
            }
        }

        public SchemaGlobalAttribute finishLoadingAttribute() {
            try {
                QName name = readQName();
                SchemaContainer container = getContainer(name.getNamespaceURI());
                checkContainerNotNull(container, name);
                SchemaGlobalAttributeImpl impl = new SchemaGlobalAttributeImpl(container);
                loadAttribute(impl, name, container);
                impl.setFilename(readString());

                return impl;
            } catch (SchemaTypeLoaderException e) {
                throw e;
            } catch (Exception e) {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            } finally {
                readEnd();
            }
        }

        SchemaModelGroup finishLoadingModelGroup() {
            QName name = readQName();
            SchemaContainer container = getContainer(name.getNamespaceURI());
            checkContainerNotNull(container, name);
            SchemaModelGroupImpl impl = new SchemaModelGroupImpl(container);

            try {
                impl.init(name, readString(), readShort() == 1,
                    atLeast(2, 22, 0) ? readString() : null,
                    atLeast(2, 22, 0) ? readString() : null,
                    atLeast(2, 15, 0) && readShort() == 1,
                    GroupDocument.Factory.parse(readString()).getGroup(), readAnnotation(container), null);
                if (atLeast(2, 21, 0)) {
                    impl.setFilename(readString());
                }
                return impl;
            } catch (SchemaTypeLoaderException e) {
                throw e;
            } catch (Exception e) {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            } finally {
                readEnd();
            }
        }

        SchemaIdentityConstraint finishLoadingIdentityConstraint() {
            try {
                QName name = readQName();
                SchemaContainer container = getContainer(name.getNamespaceURI());
                checkContainerNotNull(container, name);
                SchemaIdentityConstraintImpl impl = new SchemaIdentityConstraintImpl(container);
                impl.setName(name);
                impl.setConstraintCategory(readShort());
                impl.setSelector(readString());
                impl.setAnnotation(readAnnotation(container));

                String[] fields = new String[readShort()];
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = readString();
                }
                impl.setFields(fields);

                if (impl.getConstraintCategory() == SchemaIdentityConstraint.CC_KEYREF) {
                    impl.setReferencedKey((SchemaIdentityConstraint.Ref) readHandle());
                }

                int mapCount = readShort();
                Map<String, String> nsMappings = new HashMap<>();
                for (int i = 0; i < mapCount; i++) {
                    String prefix = readString();
                    String uri = readString();
                    nsMappings.put(prefix, uri);
                }
                impl.setNSMap(nsMappings);

                if (atLeast(2, 21, 0)) {
                    impl.setFilename(readString());
                }

                return impl;
            } catch (SchemaTypeLoaderException e) {
                throw e;
            } catch (Exception e) {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            } finally {
                readEnd();
            }
        }

        SchemaAttributeGroup finishLoadingAttributeGroup() {
            QName name = readQName();
            SchemaContainer container = getContainer(name.getNamespaceURI());
            checkContainerNotNull(container, name);
            SchemaAttributeGroupImpl impl = new SchemaAttributeGroupImpl(container);

            try {
                impl.init(name, readString(), readShort() == 1,
                    atLeast(2, 22, 0) ? readString() : null,
                    atLeast(2, 15, 0) && readShort() == 1,
                    AttributeGroupDocument.Factory.parse(readString()).getAttributeGroup(),
                    readAnnotation(container), null);
                if (atLeast(2, 21, 0)) {
                    impl.setFilename(readString());
                }
                return impl;
            } catch (SchemaTypeLoaderException e) {
                throw e;
            } catch (Exception e) {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            } finally {
                readEnd();
            }
        }

        public SchemaType finishLoadingType() {
            try {
                SchemaContainer cNonNull = getContainerNonNull(""); //HACKHACK
                SchemaTypeImpl impl = new SchemaTypeImpl(cNonNull, true);
                impl.setName(readQName());
                impl.setOuterSchemaTypeRef(readTypeRef());
                impl.setBaseDepth(readShort());
                impl.setBaseTypeRef(readTypeRef());
                impl.setDerivationType(readShort());
                impl.setAnnotation(readAnnotation(null));

                switch (readShort()) {
                    case FIELD_GLOBAL:
                        impl.setContainerFieldRef(readHandle());
                        break;
                    case FIELD_LOCALATTR:
                        impl.setContainerFieldIndex((short) 1, readShort());
                        break;
                    case FIELD_LOCALELT:
                        impl.setContainerFieldIndex((short) 2, readShort());
                        break;
                }
                // TODO (radup) find the right solution here
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

                int complexVariety = SchemaType.NOT_COMPLEX_TYPE;
                if (isComplexType) {
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

                    if (atLeast(2, 23, 0)) {
                        impl.setContentBasedOnTypeRef(readTypeRef());
                    }

                    // Attribute Model Table
                    SchemaAttributeModelImpl attrModel = new SchemaAttributeModelImpl();

                    int attrCount = readShort();
                    for (int i = 0; i < attrCount; i++) {
                        attrModel.addAttribute(readAttributeData());
                    }

                    attrModel.setWildcardSet(readQNameSet());
                    attrModel.setWildcardProcess(readShort());

                    // Attribute Property Table
                    Map<QName, SchemaProperty> attrProperties = new LinkedHashMap<>();
                    int attrPropCount = readShort();
                    for (int i = 0; i < attrPropCount; i++) {
                        SchemaProperty prop = readPropertyData();
                        if (!prop.isAttribute()) {
                            throw new SchemaTypeLoaderException("Attribute property " + i + " is not an attribute", _name, _handle, SchemaTypeLoaderException.WRONG_PROPERTY_TYPE);
                        }
                        attrProperties.put(prop.getName(), prop);
                    }

                    SchemaParticle contentModel = null;
                    Map<QName, SchemaProperty> elemProperties = null;
                    int isAll = 0;

                    if (complexVariety == SchemaType.ELEMENT_CONTENT || complexVariety == SchemaType.MIXED_CONTENT) {
                        // Content Model Tree
                        isAll = readShort();
                        SchemaParticle[] parts = readParticleArray();
                        if (parts.length == 1) {
                            contentModel = parts[0];
                        } else if (parts.length == 0) {
                            contentModel = null;
                        } else {
                            throw new SchemaTypeLoaderException("Content model not well-formed", _name, _handle, SchemaTypeLoaderException.MALFORMED_CONTENT_MODEL);
                        }

                        // Element Property Table

                        elemProperties = new LinkedHashMap<>();
                        int elemPropCount = readShort();
                        for (int i = 0; i < elemPropCount; i++) {
                            SchemaProperty prop = readPropertyData();
                            if (prop.isAttribute()) {
                                throw new SchemaTypeLoaderException("Element property " + i + " is not an element", _name, _handle, SchemaTypeLoaderException.WRONG_PROPERTY_TYPE);
                            }
                            elemProperties.put(prop.getName(), prop);
                        }
                    }

                    impl.setContentModel(contentModel, attrModel, elemProperties, attrProperties, isAll == 1);
                    StscComplexTypeResolver.WildcardResult wcElt = StscComplexTypeResolver.summarizeEltWildcards(contentModel);
                    StscComplexTypeResolver.WildcardResult wcAttr = StscComplexTypeResolver.summarizeAttrWildcards(attrModel);
                    impl.setWildcardSummary(wcElt.typedWildcards, wcElt.hasWildcards, wcAttr.typedWildcards, wcAttr.hasWildcards);
                }

                if (!isComplexType || complexVariety == SchemaType.SIMPLE_CONTENT) {
                    int simpleVariety = readShort();
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
                    int facetCount = readShort();
                    for (int i = 0; i < facetCount; i++) {
                        int facetCode = readShort();
                        facets[facetCode] = readXmlValueObject();
                        fixedFacets[facetCode] = (readShort() == 1);
                    }
                    impl.setBasicFacets(facets, fixedFacets);

                    impl.setWhiteSpaceRule(readShort());

                    impl.setPatternFacet((flags & FLAG_HAS_PATTERN) != 0);

                    int patternCount = readShort();
                    org.apache.xmlbeans.impl.regex.RegularExpression[] patterns = new org.apache.xmlbeans.impl.regex.RegularExpression[patternCount];
                    for (int i = 0; i < patternCount; i++) {
                        patterns[i] = new org.apache.xmlbeans.impl.regex.RegularExpression(readString(), "X");
                    }
                    impl.setPatterns(patterns);

                    int enumCount = readShort();
                    XmlValueRef[] enumValues = new XmlValueRef[enumCount];
                    for (int i = 0; i < enumCount; i++) {
                        enumValues[i] = readXmlValueObject();
                    }
                    impl.setEnumerationValues(enumCount == 0 ? null : enumValues);

                    impl.setBaseEnumTypeRef(readTypeRef());
                    if (isStringEnum) {
                        int seCount = readShort();
                        SchemaStringEnumEntry[] entries = new SchemaStringEnumEntry[seCount];
                        for (int i = 0; i < seCount; i++) {
                            entries[i] = new SchemaStringEnumEntryImpl(readString(), readShort(), readString());
                        }
                        impl.setStringEnumEntries(entries);
                    }

                    switch (simpleVariety) {
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
                // Set the container for global, attribute or document types
                if (impl.getName() != null) {
                    SchemaContainer container = getContainer(impl.getName().getNamespaceURI());
                    checkContainerNotNull(container, impl.getName());
                    impl.setContainer(container);
                } else if (impl.isDocumentType()) {
                    QName name = impl.getDocumentElementName();
                    if (name != null) {
                        SchemaContainer container = getContainer(name.getNamespaceURI());
                        checkContainerNotNull(container, name);
                        impl.setContainer(container);
                    }
                } else if (impl.isAttributeType()) {
                    QName name = impl.getAttributeTypeAttributeName();
                    if (name != null) {
                        SchemaContainer container = getContainer(name.getNamespaceURI());
                        checkContainerNotNull(container, name);
                        impl.setContainer(container);
                    }
                }

                return impl;
            } catch (SchemaTypeLoaderException e) {
                throw e;
            } catch (Exception e) {
                throw new SchemaTypeLoaderException("Cannot load type from typesystem", _name, _handle, SchemaTypeLoaderException.NESTED_EXCEPTION, e);
            } finally {
                readEnd();
            }
        }

        void writeTypeData(SchemaType type) {
            writeQName(type.getName());
            writeType(type.getOuterType());
            writeShort(((SchemaTypeImpl) type).getBaseDepth());
            writeType(type.getBaseType());
            writeShort(type.getDerivationType());
            writeAnnotation(type.getAnnotation());
            if (type.getContainerField() == null) {
                writeShort(FIELD_NONE);
            } else if (type.getOuterType().isAttributeType() || type.getOuterType().isDocumentType()) {
                writeShort(FIELD_GLOBAL);
                writeHandle((SchemaComponent) type.getContainerField());
            } else if (type.getContainerField().isAttribute()) {
                writeShort(FIELD_LOCALATTR);
                writeShort(((SchemaTypeImpl) type.getOuterType()).getIndexForLocalAttribute((SchemaLocalAttribute) type.getContainerField()));
            } else {
                writeShort(FIELD_LOCALELT);
                writeShort(((SchemaTypeImpl) type.getOuterType()).getIndexForLocalElement((SchemaLocalElement) type.getContainerField()));
            }
            writeString(type.getFullJavaName());
            writeString(type.getFullJavaImplName());
            writeTypeArray(type.getAnonymousTypes());
            writeShort(type.getAnonymousUnionMemberOrdinal());

            int flags = 0;
            if (type.isSimpleType()) {
                flags |= FLAG_SIMPLE_TYPE;
            }
            if (type.isDocumentType()) {
                flags |= FLAG_DOCUMENT_TYPE;
            }
            if (type.isAttributeType()) {
                flags |= FLAG_ATTRIBUTE_TYPE;
            }
            if (type.ordered() != SchemaType.UNORDERED) {
                flags |= FLAG_ORDERED;
            }
            if (type.ordered() == SchemaType.TOTAL_ORDER) {
                flags |= FLAG_TOTAL_ORDER;
            }
            if (type.isBounded()) {
                flags |= FLAG_BOUNDED;
            }
            if (type.isFinite()) {
                flags |= FLAG_FINITE;
            }
            if (type.isNumeric()) {
                flags |= FLAG_NUMERIC;
            }
            if (type.hasStringEnumValues()) {
                flags |= FLAG_STRINGENUM;
            }
            if (((SchemaTypeImpl) type).isUnionOfLists()) {
                flags |= FLAG_UNION_OF_LISTS;
            }
            if (type.hasPatternFacet()) {
                flags |= FLAG_HAS_PATTERN;
            }
            if (type.isOrderSensitive()) {
                flags |= FLAG_ORDER_SENSITIVE;
            }

            if (type.blockExtension()) {
                flags |= FLAG_BLOCK_EXT;
            }
            if (type.blockRestriction()) {
                flags |= FLAG_BLOCK_REST;
            }
            if (type.finalExtension()) {
                flags |= FLAG_FINAL_EXT;
            }
            if (type.finalRestriction()) {
                flags |= FLAG_FINAL_EXT;
            }
            if (type.finalList()) {
                flags |= FLAG_FINAL_LIST;
            }
            if (type.finalUnion()) {
                flags |= FLAG_FINAL_UNION;
            }
            if (type.isAbstract()) {
                flags |= FLAG_ABSTRACT;
            }

            writeInt(flags);

            if (!type.isSimpleType()) {
                writeShort(type.getContentType());

                writeType(type.getContentBasedOnType());

                // Attribute Model Table
                SchemaAttributeModel attrModel = type.getAttributeModel();
                SchemaLocalAttribute[] attrs = attrModel.getAttributes();

                writeShort(attrs.length);
                for (SchemaLocalAttribute attr : attrs) {
                    writeAttributeData(attr);
                }

                writeQNameSet(attrModel.getWildcardSet());
                writeShort(attrModel.getWildcardProcess());

                // Attribute Property Table
                SchemaProperty[] attrProperties = type.getAttributeProperties();
                writeShort(attrProperties.length);
                for (SchemaProperty attrProperty : attrProperties) {
                    writePropertyData(attrProperty);
                }

                if (type.getContentType() == SchemaType.ELEMENT_CONTENT ||
                    type.getContentType() == SchemaType.MIXED_CONTENT) {
                    // Content Model Tree
                    writeShort(type.hasAllContent() ? 1 : 0);
                    SchemaParticle[] parts;
                    if (type.getContentModel() != null) {
                        parts = new SchemaParticle[]{type.getContentModel()};
                    } else {
                        parts = new SchemaParticle[0];
                    }

                    writeParticleArray(parts);

                    // Element Property Table
                    SchemaProperty[] eltProperties = type.getElementProperties();
                    writeShort(eltProperties.length);
                    for (SchemaProperty eltProperty : eltProperties) {
                        writePropertyData(eltProperty);
                    }
                }
            }

            if (type.isSimpleType() || type.getContentType() == SchemaType.SIMPLE_CONTENT) {
                writeShort(type.getSimpleVariety());

                int facetCount = 0;
                for (int i = 0; i <= SchemaType.LAST_FACET; i++) {
                    if (type.getFacet(i) != null) {
                        facetCount++;
                    }
                }
                writeShort(facetCount);
                for (int i = 0; i <= SchemaType.LAST_FACET; i++) {
                    XmlAnySimpleType facet = type.getFacet(i);
                    if (facet != null) {
                        writeShort(i);
                        writeXmlValueObject(facet);
                        writeShort(type.isFacetFixed(i) ? 1 : 0);
                    }
                }

                writeShort(type.getWhiteSpaceRule());

                org.apache.xmlbeans.impl.regex.RegularExpression[] patterns = ((SchemaTypeImpl) type).getPatternExpressions();
                writeShort(patterns.length);
                for (org.apache.xmlbeans.impl.regex.RegularExpression pattern : patterns) {
                    writeString(pattern.getPattern());
                }

                XmlAnySimpleType[] enumValues = type.getEnumerationValues();
                if (enumValues == null) {
                    writeShort(0);
                } else {
                    writeShort(enumValues.length);
                    for (XmlAnySimpleType enumValue : enumValues) {
                        writeXmlValueObject(enumValue);
                    }
                }

                // new for version 2.3
                writeType(type.getBaseEnumType());
                if (type.hasStringEnumValues()) {
                    SchemaStringEnumEntry[] entries = type.getStringEnumEntries();
                    writeShort(entries.length);
                    for (SchemaStringEnumEntry entry : entries) {
                        writeString(entry.getString());
                        writeShort(entry.getIntValue());
                        writeString(entry.getEnumName());
                    }
                }

                switch (type.getSimpleVariety()) {
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

        /*
        void readExtensionsList() {
            int count = readShort();
            assert count == 0;

            for (int i = 0; i < count; i++) {
                readString();
                readString();
                readString();
            }
        }
         */

        SchemaLocalAttribute readAttributeData() {
            SchemaLocalAttributeImpl result = new SchemaLocalAttributeImpl();
            loadAttribute(result, readQName(), null);
            return result;
        }


        void loadAttribute(SchemaLocalAttributeImpl result, QName name, SchemaContainer container) {
            // name, type, use, deftext, defval, fixed, soaparraytype, annotation
            result.init(name, readTypeRef(), readShort(), readString(), null, atLeast(2, 16, 0) ? readXmlValueObject() : null, readShort() == 1, readSOAPArrayType(), readAnnotation(container), null);
        }

        void writeAttributeData(SchemaLocalAttribute attr) {
            writeQName(attr.getName());
            writeType(attr.getType());
            writeShort(attr.getUse());
            writeString(attr.getDefaultText());
            writeXmlValueObject(attr.getDefaultValue());
            writeShort(attr.isFixed() ? 1 : 0);
            writeSOAPArrayType(((SchemaWSDLArrayType) attr).getWSDLArrayType());
            writeAnnotation(attr.getAnnotation());
        }

        void writeIdConstraintData(SchemaIdentityConstraint idc) {
            writeQName(idc.getName());
            writeShort(idc.getConstraintCategory());
            writeString(idc.getSelector());
            writeAnnotation(idc.getAnnotation());

            String[] fields = idc.getFields();
            writeShort(fields.length);
            for (String field : fields) {
                writeString(field);
            }


            if (idc.getConstraintCategory() == SchemaIdentityConstraint.CC_KEYREF) {
                writeHandle(idc.getReferencedKey());
            }

            Map<String, String> mappings = idc.getNSMap();
            writeShort(mappings.size());
            mappings.forEach((prefix, uri) -> {
                writeString(prefix);
                writeString(uri);
            });
            writeString(idc.getSourceName());
        }

        SchemaParticle[] readParticleArray() {
            SchemaParticle[] result = new SchemaParticle[readShort()];
            for (int i = 0; i < result.length; i++) {
                result[i] = readParticleData();
            }
            return result;
        }

        void writeParticleArray(SchemaParticle[] spa) {
            writeShort(spa.length);
            for (SchemaParticle schemaParticle : spa) {
                writeParticleData(schemaParticle);
            }
        }

        SchemaParticle readParticleData() {
            int particleType = readShort();
            SchemaParticleImpl result;
            if (particleType != SchemaParticle.ELEMENT) {
                result = new SchemaParticleImpl();
            } else {
                result = new SchemaLocalElementImpl();
            }
            loadParticle(result, particleType);
            return result;
        }

        void loadParticle(SchemaParticleImpl result, int particleType) {
            int particleFlags = readShort();

            result.setParticleType(particleType);
            result.setMinOccurs(readBigInteger());
            result.setMaxOccurs(readBigInteger());

            result.setTransitionRules(readQNameSet(),
                (particleFlags & FLAG_PART_SKIPPABLE) != 0);

            switch (particleType) {
                case SchemaParticle.WILDCARD:
                    result.setWildcardSet(readQNameSet());
                    result.setWildcardProcess(readShort());
                    break;

                case SchemaParticle.ELEMENT:
                    SchemaLocalElementImpl lresult = (SchemaLocalElementImpl) result;
                    lresult.setNameAndTypeRef(readQName(), readTypeRef());
                    lresult.setDefault(readString(), (particleFlags & FLAG_PART_FIXED) != 0, null);
                    if (atLeast(2, 16, 0)) {
                        lresult.setDefaultValue(readXmlValueObject());
                    }
                    lresult.setNillable((particleFlags & FLAG_PART_NILLABLE) != 0);
                    lresult.setBlock((particleFlags & FLAG_PART_BLOCKEXT) != 0,
                        (particleFlags & FLAG_PART_BLOCKREST) != 0,
                        (particleFlags & FLAG_PART_BLOCKSUBST) != 0);
                    lresult.setWsdlArrayType(readSOAPArrayType());
                    lresult.setAbstract((particleFlags & FLAG_PART_ABSTRACT) != 0);
                    lresult.setAnnotation(readAnnotation(null));

                    SchemaIdentityConstraint.Ref[] idcs = new SchemaIdentityConstraint.Ref[readShort()];

                    for (int i = 0; i < idcs.length; i++) {
                        idcs[i] = (SchemaIdentityConstraint.Ref) readHandle();
                    }

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

        void writeParticleData(SchemaParticle part) {
            writeShort(part.getParticleType());
            short flags = 0;
            if (part.isSkippable()) {
                flags |= FLAG_PART_SKIPPABLE;
            }
            if (part.getParticleType() == SchemaParticle.ELEMENT) {
                SchemaLocalElement lpart = (SchemaLocalElement) part;
                if (lpart.isFixed()) {
                    flags |= FLAG_PART_FIXED;
                }
                if (lpart.isNillable()) {
                    flags |= FLAG_PART_NILLABLE;
                }
                if (lpart.blockExtension()) {
                    flags |= FLAG_PART_BLOCKEXT;
                }
                if (lpart.blockRestriction()) {
                    flags |= FLAG_PART_BLOCKREST;
                }
                if (lpart.blockSubstitution()) {
                    flags |= FLAG_PART_BLOCKSUBST;
                }
                if (lpart.isAbstract()) {
                    flags |= FLAG_PART_ABSTRACT;
                }

                if (lpart instanceof SchemaGlobalElement) {
                    SchemaGlobalElement gpart = (SchemaGlobalElement) lpart;
                    if (gpart.finalExtension()) {
                        flags |= FLAG_PART_FINALEXT;
                    }
                    if (gpart.finalRestriction()) {
                        flags |= FLAG_PART_FINALREST;
                    }
                }
            }
            writeShort(flags);
            writeBigInteger(part.getMinOccurs());
            writeBigInteger(part.getMaxOccurs());
            writeQNameSet(part.acceptedStartNames());

            switch (part.getParticleType()) {
                case SchemaParticle.WILDCARD:
                    writeQNameSet(part.getWildcardSet());
                    writeShort(part.getWildcardProcess());
                    break;

                case SchemaParticle.ELEMENT:
                    SchemaLocalElement lpart = (SchemaLocalElement) part;
                    writeQName(lpart.getName());
                    writeType(lpart.getType());
                    writeString(lpart.getDefaultText());
                    writeXmlValueObject(lpart.getDefaultValue());
                    writeSOAPArrayType(((SchemaWSDLArrayType) lpart).getWSDLArrayType());
                    writeAnnotation(lpart.getAnnotation());
                    if (lpart instanceof SchemaGlobalElement) {
                        SchemaGlobalElement gpart = (SchemaGlobalElement) lpart;

                        writeHandle(gpart.substitutionGroup());

                        QName[] substGroupMembers = gpart.substitutionGroupMembers();
                        writeShort(substGroupMembers.length);
                        for (QName substGroupMember : substGroupMembers) {
                            writeQName(substGroupMember);
                        }
                    }

                    SchemaIdentityConstraint[] idcs = lpart.getIdentityConstraints();

                    writeShort(idcs.length);
                    for (SchemaIdentityConstraint idc : idcs) {
                        writeHandle(idc);
                    }

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

        SchemaProperty readPropertyData() {
            SchemaPropertyImpl prop = new SchemaPropertyImpl();
            prop.setName(readQName());
            prop.setTypeRef(readTypeRef());
            int propflags = readShort();
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
            if (atMost(2, 19, 0)) {
                prop.setJavaSetterDelimiter(readQNameSet());
            }
            if (atLeast(2, 16, 0)) {
                prop.setDefaultValue(readXmlValueObject());
            }

            if (!prop.isAttribute() && atLeast(2, 17, 0)) {
                int size = readShort();
                Set<QName> qnames = new LinkedHashSet<>(size);
                for (int i = 0; i < size; i++) {
                    qnames.add(readQName());
                }
                prop.setAcceptedNames(qnames);
            }
            prop.setImmutable();
            return prop;
        }

        void writePropertyData(SchemaProperty prop) {
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
            writeXmlValueObject(prop.getDefaultValue());

            if (!prop.isAttribute()) {
                QName[] names = prop.acceptedNames();
                writeShort(names.length);
                for (QName name : names) {
                    writeQName(name);
                }
            }
        }

        void writeModelGroupData(SchemaModelGroup grp) {
            SchemaModelGroupImpl impl = (SchemaModelGroupImpl) grp;
            writeQName(impl.getName());
            writeString(impl.getTargetNamespace());
            writeShort(impl.getChameleonNamespace() != null ? 1 : 0);
            writeString(impl.getElemFormDefault()); // new for version 2.22
            writeString(impl.getAttFormDefault()); // new for version 2.22
            writeShort(impl.isRedefinition() ? 1 : 0); // new for version 2.15
            writeString(impl.getParseObject().xmlText(new XmlOptions().setSaveOuter()));
            writeAnnotation(impl.getAnnotation());
            writeString(impl.getSourceName());
        }

        void writeAttributeGroupData(SchemaAttributeGroup grp) {
            SchemaAttributeGroupImpl impl = (SchemaAttributeGroupImpl) grp;
            writeQName(impl.getName());
            writeString(impl.getTargetNamespace());
            writeShort(impl.getChameleonNamespace() != null ? 1 : 0);
            writeString(impl.getFormDefault()); // new for version 2.22
            writeShort(impl.isRedefinition() ? 1 : 0); // new for version 2.15
            writeString(impl.getParseObject().xmlText(new XmlOptions().setSaveOuter()));
            writeAnnotation(impl.getAnnotation());
            writeString(impl.getSourceName());
        }

        XmlValueRef readXmlValueObject() {
            SchemaType.Ref typeref = readTypeRef();
            if (typeref == null) {
                return null;
            }
            int btc = readShort();
            switch (btc) {
                default:
                    assert (false);
                case 0:
                    return new XmlValueRef(typeref, null);
                case 0xFFFF: {
                    int size = readShort();
                    List<XmlValueRef> values = new ArrayList<>();
                    // BUGBUG: this was: writeShort(values.size());
                    writeShort(size);
                    for (int i = 0; i < size; i++) {
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
                    return new XmlValueRef(typeref, readDouble());
            }
        }

        void writeXmlValueObject(XmlAnySimpleType value) {
            SchemaType type = value == null ? null : value.schemaType();
            writeType(type);
            if (type == null) {
                return;
            }

            SchemaType iType = ((SimpleValue) value).instanceType();
            if (iType == null) {
                writeShort(0);
            } else if (iType.getSimpleVariety() == SchemaType.LIST) {
                writeShort(-1);
                List<? extends XmlAnySimpleType> values = ((XmlObjectBase) value).xgetListValue();
                writeShort(values.size());
                values.forEach(this::writeXmlValueObject);
            } else {
                int btc = iType.getPrimitiveType().getBuiltinTypeCode();
                writeShort(btc);
                switch (btc) {
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
                        writeByteArray(((SimpleValue) value).getByteArrayValue());
                        break;

                    case SchemaType.BTC_QNAME:
                    case SchemaType.BTC_NOTATION:
                        writeQName(((SimpleValue) value).getQNameValue());
                        break;

                    case SchemaType.BTC_FLOAT:
                        writeDouble(((SimpleValue) value).getFloatValue());
                        break;

                    case SchemaType.BTC_DOUBLE:
                        writeDouble(((SimpleValue) value).getDoubleValue());
                        break;
                }
            }
        }

        double readDouble() {
            try {
                return _input.readDouble();
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        void writeDouble(double d) {
            if (_output != null) {
                try {
                    _output.writeDouble(d);
                } catch (IOException e) {
                    throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
                }
            }
        }

        QNameSet readQNameSet() {
            int flag = readShort();

            Set<String> uriSet = new HashSet<>();
            int uriCount = readShort();
            for (int i = 0; i < uriCount; i++) {
                uriSet.add(readString());
            }

            Set<QName> qnameSet1 = new HashSet<>();
            int qncount1 = readShort();
            for (int i = 0; i < qncount1; i++) {
                qnameSet1.add(readQName());
            }

            Set<QName> qnameSet2 = new HashSet<>();
            int qncount2 = readShort();
            for (int i = 0; i < qncount2; i++) {
                qnameSet2.add(readQName());
            }

            if (flag == 1) {
                return QNameSet.forSets(uriSet, null, qnameSet1, qnameSet2);
            } else {
                return QNameSet.forSets(null, uriSet, qnameSet2, qnameSet1);
            }
        }

        void writeQNameSet(QNameSet set) {
            boolean invert = (set.excludedURIs() != null);
            writeShort(invert ? 1 : 0);

            Set<String> uriSet = invert ? set.excludedURIs() : set.includedURIs();
            assert (uriSet != null);
            writeShort(uriSet.size());
            uriSet.forEach(this::writeString);

            Set<QName> qnameSet1 = invert ? set.excludedQNamesInIncludedURIs() : set.includedQNamesInExcludedURIs();
            writeShort(qnameSet1.size());
            qnameSet1.forEach(this::writeQName);

            Set<QName> qnameSet2 = invert ? set.includedQNamesInExcludedURIs() : set.excludedQNamesInIncludedURIs();
            writeShort(qnameSet2.size());
            qnameSet2.forEach(this::writeQName);
        }

        byte[] readByteArray() {
            try {
                int len = _input.readShort();
                byte[] result = new byte[len];
                _input.readFully(result);
                return result;
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        void writeByteArray(byte[] ba) {
            try {
                writeShort(ba.length);
                if (_output != null) {
                    _output.write(ba);
                }
            } catch (IOException e) {
                throw new SchemaTypeLoaderException(e.getMessage(), _name, _handle, SchemaTypeLoaderException.IO_EXCEPTION, e);
            }
        }

        BigInteger readBigInteger() {
            byte[] result = readByteArray();
            if (result.length == 0) {
                return null;
            }
            if (result.length == 1 && result[0] == 0) {
                return BigInteger.ZERO;
            }
            if (result.length == 1 && result[0] == 1) {
                return BigInteger.ONE;
            }
            return new BigInteger(result);
        }

        void writeBigInteger(BigInteger bi) {
            if (bi == null) {
                writeShort(0);
            } else if (bi.signum() == 0) {
                writeByteArray(SINGLE_ZERO_BYTE);
            } else {
                writeByteArray(bi.toByteArray());
            }
        }

    }

    static final byte[] SINGLE_ZERO_BYTE = {0};

    public SchemaType typeForHandle(String handle) {
        synchronized (_resolvedHandles) {
            return (SchemaType) _resolvedHandles.get(handle);
        }
    }

    public SchemaType typeForClassname(String classname) {
        SchemaType.Ref ref = (SchemaType.Ref) _typeRefsByClassname.get(classname);
        return (ref != null) ? ref.get() : null;
    }

    public SchemaComponent resolveHandle(String handle) {
        SchemaComponent result;

        synchronized (_resolvedHandles) {
            result = _resolvedHandles.get(handle);
        }
        if (result == null) {
            XsbReader reader = new XsbReader(handle, 0xFFFF);
            int filetype = reader.getActualFiletype();
            switch (filetype) {
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

            synchronized (_resolvedHandles) {
                if (!_resolvedHandles.containsKey(handle)) {
                    _resolvedHandles.put(handle, result);
                } else {
                    result = _resolvedHandles.get(handle);
                }
            }
        }
        return result;
    }

    private final Map<String, SchemaComponent> _resolvedHandles = new HashMap<>();
    private boolean _allNonGroupHandlesResolved = false;

    public void resolve() {
        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolve called type system " + _name, 0);
        if (_allNonGroupHandlesResolved) {
            return;
        }

        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Resolving all handles for type system " + _name, 1);

        List<SchemaComponent.Ref> refs = new ArrayList<>();
        refs.addAll(_globalElements.values());
        refs.addAll(_globalAttributes.values());
        refs.addAll(_globalTypes.values());
        refs.addAll(_documentTypes.values());
        refs.addAll(_attributeTypes.values());
        refs.addAll(_identityConstraints.values());

        for (SchemaComponent.Ref ref : refs) {
            // Forces ref to be resolved
            ref.getComponent();
        }

        XBeanDebug.trace(XBeanDebug.TRACE_SCHEMA_LOADING, "Finished resolving type system " + _name, -1);
        _allNonGroupHandlesResolved = true;
    }


    public boolean isNamespaceDefined(String namespace) {
        return _namespaces.contains(namespace);
    }

    public SchemaType.Ref findTypeRef(QName name) {
        return (SchemaType.Ref) _globalTypes.get(name);
    }

    public SchemaType.Ref findDocumentTypeRef(QName name) {
        return (SchemaType.Ref) _documentTypes.get(name);
    }

    public SchemaType.Ref findAttributeTypeRef(QName name) {
        return (SchemaType.Ref) _attributeTypes.get(name);
    }

    public SchemaGlobalElement.Ref findElementRef(QName name) {
        return (SchemaGlobalElement.Ref) _globalElements.get(name);
    }

    public SchemaGlobalAttribute.Ref findAttributeRef(QName name) {
        return (SchemaGlobalAttribute.Ref) _globalAttributes.get(name);
    }

    public SchemaModelGroup.Ref findModelGroupRef(QName name) {
        return (SchemaModelGroup.Ref) _modelGroups.get(name);
    }

    public SchemaAttributeGroup.Ref findAttributeGroupRef(QName name) {
        return (SchemaAttributeGroup.Ref) _attributeGroups.get(name);
    }

    public SchemaIdentityConstraint.Ref findIdentityConstraintRef(QName name) {
        return (SchemaIdentityConstraint.Ref) _identityConstraints.get(name);
    }

    private static <T, U> U[] refHelper(Map<QName, SchemaComponent.Ref> map, Function<T, U> fun, IntFunction<U[]> target, U[] emptyTarget) {
        return refHelper(map == null ? null : map.values(), fun, target, emptyTarget);
    }

    private static <T, U> U[] refHelper(Collection<SchemaComponent.Ref> list, Function<T, U> fun, IntFunction<U[]> target, U[] emptyTarget) {
        //noinspection unchecked
        return (list == null || list.isEmpty()) ? emptyTarget : list.stream().map(e -> (T) e).map(fun).toArray(target);
    }

    public SchemaType[] globalTypes() {
        return refHelper(_globalTypes, SchemaType.Ref::get, SchemaType[]::new, EMPTY_ST_ARRAY);
    }

    public SchemaType[] redefinedGlobalTypes() {
        return refHelper(_redefinedGlobalTypes, SchemaType.Ref::get, SchemaType[]::new, EMPTY_ST_ARRAY);
    }

    public InputStream getSourceAsStream(String sourceName) {
        if (!sourceName.startsWith("/")) {
            sourceName = "/" + sourceName;
        }

        return _resourceLoader.getResourceAsStream(getMetadataPath() + "/src" + sourceName);
    }

    SchemaContainer[] containers() {
        return _containers.values().toArray(new SchemaContainer[0]);
    }

    public SchemaType[] documentTypes() {
        return refHelper(_documentTypes, SchemaType.Ref::get, SchemaType[]::new, EMPTY_ST_ARRAY);
    }

    public SchemaType[] attributeTypes() {
        return refHelper(_attributeTypes, SchemaType.Ref::get, SchemaType[]::new, EMPTY_ST_ARRAY);
    }

    public SchemaGlobalElement[] globalElements() {
        return refHelper(_globalElements, SchemaGlobalElement.Ref::get, SchemaGlobalElement[]::new, EMPTY_GE_ARRAY);
    }

    public SchemaGlobalAttribute[] globalAttributes() {
        return refHelper(_globalAttributes, SchemaGlobalAttribute.Ref::get, SchemaGlobalAttribute[]::new, EMPTY_GA_ARRAY);
    }

    public SchemaModelGroup[] modelGroups() {
        return refHelper(_modelGroups, SchemaModelGroup.Ref::get, SchemaModelGroup[]::new, EMPTY_MG_ARRAY);
    }

    public SchemaModelGroup[] redefinedModelGroups() {
        return refHelper(_redefinedModelGroups, SchemaModelGroup.Ref::get, SchemaModelGroup[]::new, EMPTY_MG_ARRAY);
    }

    public SchemaAttributeGroup[] attributeGroups() {
        return refHelper(_attributeGroups, SchemaAttributeGroup.Ref::get, SchemaAttributeGroup[]::new, EMPTY_AG_ARRAY);
    }

    public SchemaAttributeGroup[] redefinedAttributeGroups() {
        return refHelper(_redefinedAttributeGroups, SchemaAttributeGroup.Ref::get, SchemaAttributeGroup[]::new, EMPTY_AG_ARRAY);
    }

    public SchemaAnnotation[] annotations() {
        return (_annotations == null || _annotations.isEmpty()) ? EMPTY_ANN_ARRAY : _annotations.toArray(EMPTY_ANN_ARRAY);
    }

    public SchemaIdentityConstraint[] identityConstraints() {
        return refHelper(_identityConstraints, SchemaIdentityConstraint.Ref::get, SchemaIdentityConstraint[]::new, EMPTY_IC_ARRAY);
    }

    public ClassLoader getClassLoader() {
        return _classloader;
    }

    /**
     * Used INTERNALLY ONLY by the code output AFTER the type system has
     * been saved and a handle has been established for each type.
     */
    public String handleForType(SchemaType type) {
        return _localHandles.handleForType(type);
    }

    public String getName() {
        return _name;
    }

    public SchemaTypeSystem typeSystemForName(String name) {
        return (name != null && name.equals(_name)) ? this : null;
    }


    /**
     * Provide method to be overriden by user typesystems using a different metadata path
     *
     * @return the metadata directory
     * @since XmlBeans 3.1.0
     */
    public String getMetadataPath() {
        Matcher m = packPat.matcher(_name);
        m.find();
        return m.group(1).replace('.', '/');
    }
}
