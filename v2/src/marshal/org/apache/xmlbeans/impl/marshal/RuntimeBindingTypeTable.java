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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeVisitor;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.JaxrpcEnumType;
import org.apache.xmlbeans.impl.binding.bts.ListArrayType;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.SimpleContentBean;
import org.apache.xmlbeans.impl.binding.bts.SimpleDocumentBinding;
import org.apache.xmlbeans.impl.binding.bts.WrappedArrayType;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Table of TypeMarshaller and TypeUnmarshaller objects keyed by BindingType
 */
final class RuntimeBindingTypeTable
{
    private final Map unmarshallerMap;
    private final Map marshallerMap;
    private final RuntimeTypeFactory runtimeTypeFactory;

    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private static final ConcurrentReaderHashMap BUILTIN_MARSHALLER_MAP;
    private static final ConcurrentReaderHashMap BUILTIN_UNMARSHALLER_MAP;

    static
    {
        final RuntimeBindingTypeTable tbl =
            new RuntimeBindingTypeTable(null);
        tbl.addBuiltins();
        BUILTIN_UNMARSHALLER_MAP = (ConcurrentReaderHashMap)tbl.unmarshallerMap;
        BUILTIN_MARSHALLER_MAP = (ConcurrentReaderHashMap)tbl.marshallerMap;
    }

    static RuntimeBindingTypeTable createTable()
    {
        final RuntimeBindingTypeTable tbl =
            new RuntimeBindingTypeTable((Map)BUILTIN_UNMARSHALLER_MAP.clone(),
                                        (Map)BUILTIN_MARSHALLER_MAP.clone(),
                                        new RuntimeTypeFactory());
        return tbl;
    }


    private RuntimeBindingTypeTable(Map unmarshallerMap,
                                    Map marshallerMap,
                                    RuntimeTypeFactory runtimeTypeFactory)
    {
        this.unmarshallerMap = unmarshallerMap;
        this.marshallerMap = marshallerMap;
        this.runtimeTypeFactory = runtimeTypeFactory;
    }

    private RuntimeBindingTypeTable(RuntimeTypeFactory runtimeTypeFactory)
    {
        this(new ConcurrentReaderHashMap(),
             new ConcurrentReaderHashMap(),
             runtimeTypeFactory);
    }

    private TypeUnmarshaller createTypeUnmarshaller(BindingType type,
                                                    BindingLoader loader)
        throws XmlException
    {
        final TypeUnmarshaller type_um;

        type_um = createTypeUnmarshallerInternal(type, loader);

        putTypeUnmarshaller(type, type_um);
        type_um.initialize(this, loader);
        return type_um;
    }

    private TypeUnmarshaller createTypeUnmarshallerInternal(BindingType type,
                                                            BindingLoader loader)
        throws XmlException
    {
        TypeVisitor type_visitor =
            new TypeVisitor(this, loader, runtimeTypeFactory);
        type.accept(type_visitor);
        return type_visitor.getUnmarshaller();
    }

    RuntimeTypeFactory getRuntimeTypeFactory()
    {
        return runtimeTypeFactory;
    }

    TypeUnmarshaller getOrCreateTypeUnmarshaller(BindingType type,
                                                 BindingLoader loader)
        throws XmlException
    {
        TypeUnmarshaller um = (TypeUnmarshaller)unmarshallerMap.get(type);
        if (um == null) {
            um = createTypeUnmarshaller(type, loader);
        }
        return um;
    }


    TypeUnmarshaller getTypeUnmarshaller(BindingType type)
    {
        return (TypeUnmarshaller)unmarshallerMap.get(type);
    }

    TypeMarshaller getTypeMarshaller(BindingType type)
    {
        return (TypeMarshaller)marshallerMap.get(type);
    }

    private void putTypeUnmarshaller(BindingType type, TypeUnmarshaller um)
    {
        assert type != null;
        assert um != null;

        unmarshallerMap.put(type, um);
    }

    private void putTypeMarshaller(BindingType type, TypeMarshaller m)
    {
        assert type != null;
        assert m != null;

        marshallerMap.put(type, m);
    }

    private void addXsdBuiltin(String xsdType,
                               Class javaClass,
                               TypeConverter converter)
    {
        final JavaTypeName jName = JavaTypeName.forClassName(javaClass.getName());
        addXsdBuiltin(xsdType, jName, converter);
    }

    private void addXsdBuiltin(String xsdType,
                               JavaTypeName jName,
                               TypeConverter converter)
    {
        final BindingLoader default_builtin_loader =
            BuiltinBindingLoader.getBuiltinBindingLoader(false);

        QName xml_type = new QName(XSD_NS, xsdType);
        XmlTypeName xName = XmlTypeName.forTypeNamed(xml_type);
        final BindingTypeName btname = BindingTypeName.forPair(jName, xName);
        BindingType btype = default_builtin_loader.getBindingType(btname);
        if (btype == null) {
            final BindingLoader jaxrpc_builtin_loader =
                BuiltinBindingLoader.getBuiltinBindingLoader(true);
            btype = jaxrpc_builtin_loader.getBindingType(btname);
        }
        if (btype == null) {
            throw new AssertionError("failed to find builtin for java:" + jName +
                                     " - xsd:" + xName);
        }
        putTypeMarshaller(btype, converter);
        putTypeUnmarshaller(btype, converter);

        assert getTypeMarshaller(btype) == converter;
        assert getTypeUnmarshaller(btype) == converter;
    }


    private void addBuiltins()
    {
        addXsdBuiltin("anyType", Object.class, new ObjectAnyTypeConverter());

        final FloatTypeConverter float_conv = new FloatTypeConverter();
        addXsdBuiltin("float", float.class, float_conv);
        addXsdBuiltin("float", Float.class, float_conv);

        final DoubleTypeConverter double_conv = new DoubleTypeConverter();
        addXsdBuiltin("double", double.class, double_conv);
        addXsdBuiltin("double", Double.class, double_conv);

        final IntegerTypeConverter integer_conv = new IntegerTypeConverter();
        final Class bigint = BigInteger.class;
        addXsdBuiltin("integer", bigint, integer_conv);
        addXsdBuiltin("nonPositiveInteger", bigint, integer_conv);
        addXsdBuiltin("negativeInteger", bigint, integer_conv);
        addXsdBuiltin("nonNegativeInteger", bigint, integer_conv);
        addXsdBuiltin("positiveInteger", bigint, integer_conv);
        addXsdBuiltin("unsignedLong", bigint, integer_conv);

        addXsdBuiltin("decimal", BigDecimal.class,
                      new DecimalTypeConverter());

        final LongTypeConverter long_conv = new LongTypeConverter();
        addXsdBuiltin("long", long.class, long_conv);
        addXsdBuiltin("long", Long.class, long_conv);
        addXsdBuiltin("unsignedInt", long.class, long_conv);
        addXsdBuiltin("unsignedInt", Long.class, long_conv);

        final IntTypeConverter int_conv = new IntTypeConverter();
        addXsdBuiltin("int", int.class, int_conv);
        addXsdBuiltin("int", Integer.class, int_conv);
        addXsdBuiltin("unsignedShort", int.class, int_conv);
        addXsdBuiltin("unsignedShort", Integer.class, int_conv);

        final ShortTypeConverter short_conv = new ShortTypeConverter();
        addXsdBuiltin("short", short.class, short_conv);
        addXsdBuiltin("short", Short.class, short_conv);
        addXsdBuiltin("unsignedByte", short.class, short_conv);
        addXsdBuiltin("unsignedByte", Short.class, short_conv);

        final ByteTypeConverter byte_conv = new ByteTypeConverter();
        addXsdBuiltin("byte", byte.class, byte_conv);
        addXsdBuiltin("byte", Byte.class, byte_conv);

        final BooleanTypeConverter boolean_conv = new BooleanTypeConverter();
        addXsdBuiltin("boolean", boolean.class, boolean_conv);
        addXsdBuiltin("boolean", Boolean.class, boolean_conv);

        addXsdBuiltin("anyURI",
                      java.net.URI.class,
                      new AnyUriToUriTypeConverter());

        final Class str = String.class;
        addXsdBuiltin("anySimpleType", str, new AnySimpleTypeConverter());
        final StringTypeConverter string_conv = new StringTypeConverter();
        addXsdBuiltin("string", str, string_conv);
        addXsdBuiltin("normalizedString", str, string_conv);
        addXsdBuiltin("token", str, string_conv);
        addXsdBuiltin("language", str, string_conv);
        addXsdBuiltin("Name", str, string_conv);
        addXsdBuiltin("NCName", str, string_conv);
        addXsdBuiltin("NMTOKEN", str, string_conv);
        addXsdBuiltin("ID", str, string_conv);
        addXsdBuiltin("IDREF", str, string_conv);
        addXsdBuiltin("ENTITY", str, string_conv);

        final TypeConverter collapsing_string_conv =
            CollapseStringTypeConverter.getInstance();

        addXsdBuiltin("duration", str, collapsing_string_conv);
        addXsdBuiltin("gDay", str, collapsing_string_conv);
        addXsdBuiltin("gMonth", str, collapsing_string_conv);
        addXsdBuiltin("gMonthDay", str, collapsing_string_conv);
        addXsdBuiltin("gYear", str, collapsing_string_conv);
        addXsdBuiltin("gYearMonth", str, collapsing_string_conv);

        addXsdBuiltin("anyURI",
                      str,
                      new AnyUriToStringTypeConverter());

        final Class str_array = (new String[0]).getClass();
        final StringListArrayConverter string_list_array_conv =
            new StringListArrayConverter();
        addXsdBuiltin("ENTITIES", str_array,
                      string_list_array_conv);
        addXsdBuiltin("IDREFS", str_array,
                      string_list_array_conv);
        addXsdBuiltin("NMTOKENS", str_array,
                      string_list_array_conv);

        addXsdBuiltin("duration",
                      GDuration.class,
                      new DurationTypeConverter());

        final Class calendar_class = java.util.Calendar.class;
        addXsdBuiltin("dateTime",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_DATE_TIME));

        addXsdBuiltin("dateTime",
                      java.util.Date.class,
                      new JavaDateTypeConverter(SchemaType.BTC_DATE_TIME));

        addXsdBuiltin("time",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_TIME));

        addXsdBuiltin("date",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_DATE));

        addXsdBuiltin("date",
                      java.util.Date.class,
                      new JavaDateTypeConverter(SchemaType.BTC_DATE));

        addXsdBuiltin("gDay",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_G_DAY));

        addXsdBuiltin("gMonth",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_G_MONTH));

        addXsdBuiltin("gMonthDay",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_G_MONTH_DAY));

        addXsdBuiltin("gYear",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_G_YEAR));

        addXsdBuiltin("gYearMonth",
                      calendar_class,
                      new JavaCalendarTypeConverter(SchemaType.BTC_G_YEAR_MONTH));


        addXsdBuiltin("gDay",
                      int.class,
                      new IntDateTypeConverter(SchemaType.BTC_G_DAY));
        addXsdBuiltin("gMonth",
                      int.class,
                      new IntDateTypeConverter(SchemaType.BTC_G_MONTH));
        addXsdBuiltin("gYear",
                      int.class,
                      new IntDateTypeConverter(SchemaType.BTC_G_YEAR));


        addXsdBuiltin("QName",
                      QName.class,
                      new QNameTypeConverter());

        final JavaTypeName byte_array_jname =
            JavaTypeName.forArray(JavaTypeName.forString("byte"), 1);

        addXsdBuiltin("base64Binary",
                      byte_array_jname,
                      new Base64BinaryTypeConverter());

        addXsdBuiltin("hexBinary",
                      byte_array_jname,
                      new HexBinaryTypeConverter());

        //TODO: InputStream based hexBinary and base64Binary converters
    }

    private static TypeUnmarshaller createSimpleTypeUnmarshaller(SimpleBindingType stype,
                                                                 BindingLoader loader,
                                                                 RuntimeBindingTypeTable table)
        throws XmlException
    {
        TypeUnmarshaller um = table.getTypeUnmarshaller(stype);
        if (um != null) return um;

        int curr_ws = XmlWhitespace.WS_UNSPECIFIED;
        SimpleBindingType curr = stype;
        BuiltinBindingType resolved;

        while (true) {
            //we want to keep the first whitespace setting as we walk up
            if (curr_ws == XmlWhitespace.WS_UNSPECIFIED) {
                curr_ws = curr.getWhitespace();
            }

            BindingTypeName asif_name = curr.getAsIfBindingTypeName();
            if (asif_name != null) {
                BindingType asif_new = loader.getBindingType(asif_name);
                if (asif_new instanceof BuiltinBindingType) {
                    resolved = (BuiltinBindingType)asif_new;
                    break;
                } else if (asif_new instanceof SimpleBindingType) {
                    curr = (SimpleBindingType)asif_new;
                } else {
                    String msg = "invalid as-xml type: " + asif_name +
                        " on type: " + curr.getName();
                    throw new XmlException(msg);
                }
            } else {
                throw new XmlException("missing as-xml type on " +
                                       curr.getName());
            }
        }
        assert resolved != null;


        //special processing for whitespace facets.
        //TODO: assert that our type is derived from xsd:string
        switch (curr_ws) {
            case XmlWhitespace.WS_UNSPECIFIED:
                break;
            case XmlWhitespace.WS_PRESERVE:
                return PreserveStringTypeConverter.getInstance();
            case XmlWhitespace.WS_REPLACE:
                return ReplaceStringTypeConverter.getInstance();
            case XmlWhitespace.WS_COLLAPSE:
                return CollapseStringTypeConverter.getInstance();
            default:
                throw new AssertionError("invalid whitespace: " + curr_ws);
        }


        um = table.getTypeUnmarshaller(resolved);
        if (um != null) return um;

        String msg = "unable to get simple type unmarshaller for " + stype +
            " resolved to " + resolved;
        throw new AssertionError(msg);
    }

    TypeUnmarshaller lookupUnmarshaller(BindingTypeName type_name,
                                        BindingLoader loader)
        throws XmlException
    {
        assert type_name != null;

        final BindingType binding_type = loader.getBindingType(type_name);
        if (binding_type == null) {
            throw new XmlException("failed to load type: " + type_name);
        }

        return lookupUnmarshaller(binding_type, loader);
    }

    TypeUnmarshaller lookupUnmarshaller(BindingType binding_type,
                                        BindingLoader loader)
        throws XmlException
    {
        TypeUnmarshaller um =
            this.getOrCreateTypeUnmarshaller(binding_type, loader);
        if (um == null) {
            throw new AssertionError("failed to get unmarshaller for " +
                                     binding_type);
        }
        return um;
    }

    /**
     *
     * find marshaller for given type.  Can return null if not found
     *
     * @param type_name
     * @param loader
     * @return marshaller or null if not found.
     * @throws XmlException
     */
    TypeMarshaller lookupMarshaller(BindingTypeName type_name,
                                    BindingLoader loader)
        throws XmlException
    {
        final BindingType binding_type = loader.getBindingType(type_name);
        if (binding_type == null) {
            final String msg = "unable to load type for " + type_name;
            throw new XmlException(msg);
        }

        return lookupMarshaller(binding_type, loader);
    }

    /**
     * find marshaller for given type.  Can return null if not found
     *
     * @param binding_type
     * @param loader
     * @return  marshaller or null if not found.
     * @throws XmlException
     */
    TypeMarshaller lookupMarshaller(BindingType binding_type,
                                    BindingLoader loader)
        throws XmlException
    {
        TypeMarshaller m = this.getTypeMarshaller(binding_type);
        if (m != null) return m;

        if (binding_type instanceof SimpleContentBean) {
            SimpleContentBean scb = (SimpleContentBean)binding_type;
            final SimpleContentRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(scb, this, loader);
            m = new SimpleContentBeanMarshaller(rtt, this, loader);
        } else if (binding_type instanceof SimpleBindingType) {
            SimpleBindingType stype = (SimpleBindingType)binding_type;

            final BindingTypeName asif_name = stype.getAsIfBindingTypeName();
            if (asif_name == null)
                throw new XmlException("no asif for " + stype);

            m = lookupMarshaller(asif_name, loader);
        } else if (binding_type instanceof JaxrpcEnumType) {
            JaxrpcEnumType enum_type = (JaxrpcEnumType)binding_type;
            final JaxrpcEnumRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(enum_type, this, loader);
            m = new JaxrpcEnumMarsahller(rtt);
        } else if (binding_type instanceof ListArrayType) {
            ListArrayType la_type = (ListArrayType)binding_type;
            final ListArrayRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(la_type, this, loader);
            m = new ListArrayConverter(rtt);
        }

        if (m != null)
            putTypeMarshaller(binding_type, m);

        return m;
    }


    private static final class TypeVisitor
        implements BindingTypeVisitor
    {
        private final BindingLoader loader;
        private final RuntimeTypeFactory runtimeTypeFactory;
        private final RuntimeBindingTypeTable runtimeBindingTypeTable;

        private TypeUnmarshaller typeUnmarshaller;

        public TypeVisitor(RuntimeBindingTypeTable runtimeBindingTypeTable,
                           BindingLoader loader,
                           RuntimeTypeFactory runtimeTypeFactory)
        {
            this.runtimeBindingTypeTable = runtimeBindingTypeTable;
            this.loader = loader;
            this.runtimeTypeFactory = runtimeTypeFactory;
        }

        public void visit(BuiltinBindingType builtinBindingType)
            throws XmlException
        {
            throw new AssertionError("internal error: no builtin unmarshaller for " +
                                     builtinBindingType);
        }

        public void visit(ByNameBean byNameBean)
            throws XmlException
        {
            ByNameRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(byNameBean,
                                                     runtimeBindingTypeTable,
                                                     loader);

            typeUnmarshaller = new ByNameUnmarshaller(rtt);
        }


        public void visit(SimpleContentBean simpleContentBean)
            throws XmlException
        {
            SimpleContentRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(simpleContentBean,
                                                     runtimeBindingTypeTable,
                                                     loader);

            typeUnmarshaller = new SimpleContentUnmarshaller(rtt);
        }

        public void visit(SimpleBindingType simpleBindingType)
            throws XmlException
        {
            typeUnmarshaller =
                createSimpleTypeUnmarshaller(simpleBindingType, loader,
                                             runtimeBindingTypeTable);
        }

        public void visit(JaxrpcEnumType jaxrpcEnumType)
            throws XmlException
        {
            JaxrpcEnumRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(jaxrpcEnumType,
                                                     runtimeBindingTypeTable,
                                                     loader);

            typeUnmarshaller = new JaxrpcEnumUnmarshaller(rtt);
        }

        public void visit(SimpleDocumentBinding simpleDocumentBinding)
            throws XmlException
        {
            throw new AssertionError("type not allowed here" +
                                     simpleDocumentBinding);
        }

        public void visit(WrappedArrayType wrappedArrayType)
            throws XmlException
        {
            WrappedArrayRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(wrappedArrayType,
                                                     runtimeBindingTypeTable,
                                                     loader);
            typeUnmarshaller = new WrappedArrayUnmarshaller(rtt);
        }

        public void visit(ListArrayType listArrayType)
            throws XmlException
        {
            ListArrayRuntimeBindingType rtt =
                runtimeTypeFactory.createRuntimeType(listArrayType,
                                                     runtimeBindingTypeTable,
                                                     loader);
            typeUnmarshaller = new ListArrayConverter(rtt);
        }

        public TypeUnmarshaller getUnmarshaller()
        {
            assert typeUnmarshaller != null;
            return typeUnmarshaller;
        }

    }

}
