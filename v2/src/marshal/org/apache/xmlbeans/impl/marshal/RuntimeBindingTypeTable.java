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

import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Table of TypeMarshaller and TypeUnmarshaller objects keyed by BindingType
 */
final class RuntimeBindingTypeTable
{
    //concurrent hashMap allows us to do hash lookups outside of any sync blocks,
    //and successful lookups involve no locking, which should be
    //99% of the cases in any sort of long running process
    private final Map initedTypeMap;

    private final Map tempTypeMap = new HashMap();

    //access to this object must be inside a synchronized block.
    private final FactoryTypeVisitor typeVisitor = new FactoryTypeVisitor();


    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String SOAPENC_NS = "http://schemas.xmlsoap.org/soap/encoding/";


    private static final ConcurrentReaderHashMap BUILTIN_TYPE_MAP;

    static
    {
        final RuntimeBindingTypeTable tbl =
            new RuntimeBindingTypeTable();
        tbl.addBuiltinTypes();
        BUILTIN_TYPE_MAP = (ConcurrentReaderHashMap)tbl.initedTypeMap;
    }


    static RuntimeBindingTypeTable createTable()
    {
        final RuntimeBindingTypeTable tbl =
            new RuntimeBindingTypeTable(
                (Map)BUILTIN_TYPE_MAP.clone()
            );
        return tbl;
    }


    private RuntimeBindingTypeTable(Map typeMap)
    {
        this.initedTypeMap = typeMap;
    }

    private RuntimeBindingTypeTable()
    {
        this(new ConcurrentReaderHashMap());
    }

    RuntimeBindingType createRuntimeType(BindingType type,
                                         BindingLoader binding_loader)
        throws XmlException
    {
        //return runtimeTypeFactory.createRuntimeType(type, this, binding_loader);

        assert type != null;
        RuntimeBindingType rtype = (RuntimeBindingType)initedTypeMap.get(type);
        if (rtype != null) return rtype;

        //safe but slow creation of new type.
        synchronized (this) {
            rtype = (RuntimeBindingType)tempTypeMap.get(type);
            if (rtype == null) {
                rtype = allocateType(type);
                tempTypeMap.put(type, rtype);
                rtype.external_initialize(this, binding_loader);
                initedTypeMap.put(type, rtype);
                tempTypeMap.remove(type); // save some memory.
            }
        }
        assert rtype != null;
        return rtype;
    }

    private RuntimeBindingType allocateType(BindingType type)
        throws XmlException
    {
        type.accept(typeVisitor);
        return typeVisitor.getRuntimeBindingType();
    }


    //overloaded, more strongly typed versions of createRuntimeType.
    //the idea being that this class maintains the matching of the
    //two type hiearchies and all casting is done here.
    private WrappedArrayRuntimeBindingType createRuntimeType(WrappedArrayType type,
                                                             BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, binding_loader);
        return (WrappedArrayRuntimeBindingType)rtt;
    }

    private ListArrayRuntimeBindingType createRuntimeType(ListArrayType type,
                                                          BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, binding_loader);
        return (ListArrayRuntimeBindingType)rtt;
    }


    private ByNameRuntimeBindingType createRuntimeType(ByNameBean type,
                                                       BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, binding_loader);
        return (ByNameRuntimeBindingType)rtt;
    }

    private SimpleContentRuntimeBindingType createRuntimeType(SimpleContentBean type,
                                                              BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, binding_loader);
        return (SimpleContentRuntimeBindingType)rtt;
    }

    private JaxrpcEnumRuntimeBindingType createRuntimeType(JaxrpcEnumType type,
                                                           BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, binding_loader);
        return (JaxrpcEnumRuntimeBindingType)rtt;
    }


    //avoids a cast to deal with overloaded methods
    private RuntimeBindingType createRuntimeTypeInternal(BindingType type,
                                                         BindingLoader loader)
        throws XmlException
    {
        return createRuntimeType(type, loader);
    }


    private void addBuiltinType(String xsdType,
                                JavaTypeName jName,
                                TypeConverter converter)
    {
        QName xml_type = new QName(XSD_NS, xsdType);
        addBuiltinType(xml_type, jName, converter);
    }

    private void addBuiltinSoapType(String xsdType,
                                    JavaTypeName jName,
                                    TypeConverter converter)
    {
        QName xml_type = new QName(SOAPENC_NS, xsdType);
        addBuiltinType(xml_type, jName, converter);
    }

    private void addBuiltinType(QName xml_type,
                                JavaTypeName jName,
                                TypeConverter converter)
    {
        final BindingLoader default_builtin_loader =
            BuiltinBindingLoader.getBuiltinBindingLoader(false);

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
        assert (btype instanceof BuiltinBindingType) :
            "unexpected type: " + btype;

        final BuiltinRuntimeBindingType builtin;
        try {
            builtin = new BuiltinRuntimeBindingType((BuiltinBindingType)btype,
                                                    converter);
        }
        catch (XmlException e) {
            throw new AssertionError(e);
        }
        initedTypeMap.put(btype, builtin);
    }


    private void addBuiltinSoapType(String xsdType,
                                    Class javaClass,
                                    TypeConverter converter)
    {
        final JavaTypeName jName = JavaTypeName.forClassName(javaClass.getName());
        addBuiltinSoapType(xsdType, jName, converter);
    }

    private void addBuiltinType(String xsdType,
                                Class javaClass,
                                TypeConverter converter)
    {
        final JavaTypeName jName = JavaTypeName.forClassName(javaClass.getName());
        addBuiltinType(xsdType, jName, converter);
    }

    private void addBuiltinTypes()
    {
        addBuiltinType("anyType", Object.class, new ObjectAnyTypeConverter());

        final FloatTypeConverter float_conv = new FloatTypeConverter();
        addBuiltinType("float", float.class, float_conv);
        addBuiltinType("float", Float.class, float_conv);

        final DoubleTypeConverter double_conv = new DoubleTypeConverter();
        addBuiltinType("double", double.class, double_conv);
        addBuiltinType("double", Double.class, double_conv);

        final IntegerTypeConverter integer_conv = new IntegerTypeConverter();
        final Class bigint = BigInteger.class;
        addBuiltinType("integer", bigint, integer_conv);
        addBuiltinType("nonPositiveInteger", bigint, integer_conv);
        addBuiltinType("negativeInteger", bigint, integer_conv);
        addBuiltinType("nonNegativeInteger", bigint, integer_conv);
        addBuiltinType("positiveInteger", bigint, integer_conv);
        addBuiltinType("unsignedLong", bigint, integer_conv);

        final TypeConverter i2iconv = new IntegerToIntTypeConverter();
        addBuiltinType("integer", int.class, i2iconv);
        addBuiltinType("nonPositiveInteger", int.class, i2iconv);
        addBuiltinType("negativeInteger", int.class, i2iconv);
        addBuiltinType("nonNegativeInteger", int.class, i2iconv);
        addBuiltinType("positiveInteger", int.class, i2iconv);
        addBuiltinType("unsignedLong", int.class, i2iconv);

        final DecimalTypeConverter decimal_conv = new DecimalTypeConverter();
        addBuiltinType("decimal", BigDecimal.class, decimal_conv);

        final LongTypeConverter long_conv = new LongTypeConverter();
        addBuiltinType("long", long.class, long_conv);
        addBuiltinType("long", Long.class, long_conv);
        addBuiltinType("unsignedInt", long.class, long_conv);
        addBuiltinType("unsignedInt", Long.class, long_conv);

        final IntTypeConverter int_conv = new IntTypeConverter();
        addBuiltinType("int", int.class, int_conv);
        addBuiltinType("int", Integer.class, int_conv);
        addBuiltinType("unsignedShort", int.class, int_conv);
        addBuiltinType("unsignedShort", Integer.class, int_conv);

        final ShortTypeConverter short_conv = new ShortTypeConverter();
        addBuiltinType("short", short.class, short_conv);
        addBuiltinType("short", Short.class, short_conv);
        addBuiltinType("unsignedByte", short.class, short_conv);
        addBuiltinType("unsignedByte", Short.class, short_conv);

        final ByteTypeConverter byte_conv = new ByteTypeConverter();
        addBuiltinType("byte", byte.class, byte_conv);
        addBuiltinType("byte", Byte.class, byte_conv);

        final BooleanTypeConverter boolean_conv = new BooleanTypeConverter();
        addBuiltinType("boolean", boolean.class, boolean_conv);
        addBuiltinType("boolean", Boolean.class, boolean_conv);

        final AnyUriToUriTypeConverter uri_uri_conv =
            new AnyUriToUriTypeConverter();
        addBuiltinType("anyURI", java.net.URI.class, uri_uri_conv);

        final Class str = String.class;
        addBuiltinType("anySimpleType", str, new AnySimpleTypeConverter());
        final StringTypeConverter string_conv = new StringTypeConverter();
        addBuiltinType("string", str, string_conv);
        addBuiltinType("normalizedString", str, string_conv);
        addBuiltinType("token", str, string_conv);
        addBuiltinType("language", str, string_conv);
        addBuiltinType("Name", str, string_conv);
        addBuiltinType("NCName", str, string_conv);
        addBuiltinType("NMTOKEN", str, string_conv);
        addBuiltinType("ID", str, string_conv);
        addBuiltinType("IDREF", str, string_conv);
        addBuiltinType("ENTITY", str, string_conv);

        final TypeConverter collapsing_string_conv =
            CollapseStringTypeConverter.getInstance();

        addBuiltinType("NOTATION", str, collapsing_string_conv);
        addBuiltinType("duration", str, collapsing_string_conv);
        addBuiltinType("gDay", str, collapsing_string_conv);
        addBuiltinType("gMonth", str, collapsing_string_conv);
        addBuiltinType("gMonthDay", str, collapsing_string_conv);
        addBuiltinType("gYear", str, collapsing_string_conv);
        addBuiltinType("gYearMonth", str, collapsing_string_conv);

        final AnyUriToStringTypeConverter uri_conv =
            new AnyUriToStringTypeConverter();
        addBuiltinType("anyURI", str, uri_conv);

        final Class str_array = (new String[0]).getClass();
        final StringListArrayConverter string_list_array_conv =
            new StringListArrayConverter();
        addBuiltinType("ENTITIES", str_array,
                       string_list_array_conv);
        addBuiltinType("IDREFS", str_array,
                       string_list_array_conv);
        addBuiltinType("NMTOKENS", str_array,
                       string_list_array_conv);

        final DurationTypeConverter gduration_conv =
            new DurationTypeConverter();
        addBuiltinType("duration", GDuration.class, gduration_conv);

        final Class calendar_class = java.util.Calendar.class;
        final JavaCalendarTypeConverter date_time_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_DATE_TIME);
        addBuiltinType("dateTime", calendar_class, date_time_conv);

        final JavaDateTypeConverter date_datetime_conv =
            new JavaDateTypeConverter(SchemaType.BTC_DATE_TIME);
        addBuiltinType("dateTime", java.util.Date.class, date_datetime_conv);

        final JavaCalendarTypeConverter time_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_TIME);
        addBuiltinType("time", calendar_class, time_conv);

        final JavaCalendarTypeConverter date_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_DATE);
        addBuiltinType("date", calendar_class, date_conv);

        final JavaDateTypeConverter date_date_conv =
            new JavaDateTypeConverter(SchemaType.BTC_DATE);
        addBuiltinType("date", java.util.Date.class, date_date_conv);

        final JavaCalendarTypeConverter gday_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_G_DAY);
        addBuiltinType("gDay", calendar_class, gday_conv);

        final JavaCalendarTypeConverter gmonth_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_G_MONTH);
        addBuiltinType("gMonth", calendar_class, gmonth_conv);

        final JavaCalendarTypeConverter gmonth_day_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_G_MONTH_DAY);
        addBuiltinType("gMonthDay", calendar_class, gmonth_day_conv);

        final JavaCalendarTypeConverter gyear_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_G_YEAR);
        addBuiltinType("gYear", calendar_class, gyear_conv);

        final JavaCalendarTypeConverter gyearmonth_conv =
            new JavaCalendarTypeConverter(SchemaType.BTC_G_YEAR_MONTH);
        addBuiltinType("gYearMonth", calendar_class, gyearmonth_conv);


        addBuiltinType("gDay",
                       int.class,
                       new IntDateTypeConverter(SchemaType.BTC_G_DAY));
        addBuiltinType("gMonth",
                       int.class,
                       new IntDateTypeConverter(SchemaType.BTC_G_MONTH));
        addBuiltinType("gYear",
                       int.class,
                       new IntDateTypeConverter(SchemaType.BTC_G_YEAR));


        final QNameTypeConverter qname_conv = new QNameTypeConverter();
        addBuiltinType("QName", QName.class, qname_conv);

        final JavaTypeName byte_array_jname =
            JavaTypeName.forArray(JavaTypeName.forString("byte"), 1);

        final Base64BinaryTypeConverter base64_conv =
            new Base64BinaryTypeConverter();
        addBuiltinType("base64Binary", byte_array_jname, base64_conv);

        final HexBinaryTypeConverter hexbin_conv = new HexBinaryTypeConverter();
        addBuiltinType("hexBinary", byte_array_jname, hexbin_conv);

        //TODO: InputStream based hexBinary and base64Binary converters

        //SOAPENC additions...
        addBuiltinSoapType("float", Float.class, float_conv);
        addBuiltinSoapType("double", Double.class, double_conv);
        addBuiltinSoapType("integer", bigint, integer_conv);
        addBuiltinSoapType("nonPositiveInteger", bigint, integer_conv);
        addBuiltinSoapType("negativeInteger", bigint, integer_conv);
        addBuiltinSoapType("nonNegativeInteger", bigint, integer_conv);
        addBuiltinSoapType("positiveInteger", bigint, integer_conv);
        addBuiltinSoapType("unsignedLong", bigint, integer_conv);
        addBuiltinSoapType("decimal", BigDecimal.class,
                           decimal_conv);
        addBuiltinSoapType("long", Long.class, long_conv);
        addBuiltinSoapType("unsignedInt", Long.class, long_conv);
        addBuiltinSoapType("int", Integer.class, int_conv);
        addBuiltinSoapType("unsignedShort", Integer.class, int_conv);
        addBuiltinSoapType("short", Short.class, short_conv);
        addBuiltinSoapType("unsignedByte", Short.class, short_conv);
        addBuiltinSoapType("byte", Byte.class, byte_conv);
        addBuiltinSoapType("boolean", Boolean.class, boolean_conv);

        addBuiltinSoapType("anyURI", java.net.URI.class, uri_uri_conv);

        addBuiltinSoapType("string", str, string_conv);
        addBuiltinSoapType("normalizedString", str, string_conv);
        addBuiltinSoapType("token", str, string_conv);
        addBuiltinSoapType("language", str, string_conv);
        addBuiltinSoapType("Name", str, string_conv);
        addBuiltinSoapType("NCName", str, string_conv);
        addBuiltinSoapType("NMTOKEN", str, string_conv);
        addBuiltinSoapType("ID", str, string_conv);
        addBuiltinSoapType("IDREF", str, string_conv);
        addBuiltinSoapType("ENTITY", str, string_conv);

        addBuiltinSoapType("NOTATION", str, collapsing_string_conv);
        addBuiltinSoapType("duration", str, collapsing_string_conv);
        addBuiltinSoapType("gDay", str, collapsing_string_conv);
        addBuiltinSoapType("gMonth", str, collapsing_string_conv);
        addBuiltinSoapType("gMonthDay", str, collapsing_string_conv);
        addBuiltinSoapType("gYear", str, collapsing_string_conv);
        addBuiltinSoapType("gYearMonth", str, collapsing_string_conv);

        addBuiltinSoapType("anyURI", str, uri_conv);

        addBuiltinSoapType("ENTITIES", str_array, string_list_array_conv);
        addBuiltinSoapType("IDREFS", str_array, string_list_array_conv);
        addBuiltinSoapType("NMTOKENS", str_array, string_list_array_conv);

        addBuiltinSoapType("duration", GDuration.class, gduration_conv);
        addBuiltinSoapType("dateTime", calendar_class, date_time_conv);
        addBuiltinSoapType("dateTime", java.util.Date.class, date_datetime_conv);
        addBuiltinSoapType("time", calendar_class, time_conv);
        addBuiltinSoapType("date", calendar_class, date_conv);
        addBuiltinSoapType("date", java.util.Date.class, date_date_conv);
        addBuiltinSoapType("gDay", calendar_class, gday_conv);
        addBuiltinSoapType("gMonth", calendar_class, gmonth_conv);
        addBuiltinSoapType("gMonthDay", calendar_class, gmonth_day_conv);
        addBuiltinSoapType("gYear", calendar_class, gyear_conv);
        addBuiltinSoapType("gYearMonth", calendar_class, gyearmonth_conv);

        addBuiltinSoapType("QName", QName.class, qname_conv);

        addBuiltinSoapType("base64Binary", byte_array_jname, base64_conv);
        addBuiltinSoapType("hexBinary", byte_array_jname, hexbin_conv);

    }

    private static TypeUnmarshaller createSimpleTypeUnmarshaller(SimpleBindingType stype,
                                                                 BindingLoader loader,
                                                                 RuntimeBindingTypeTable table)
        throws XmlException
    {

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
        //for actual ws facet cases
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

        TypeUnmarshaller um =
            table.createRuntimeType(resolved, loader).getUnmarshaller();
        if (um != null) return um;

        String msg = "unable to get simple type unmarshaller for " + stype +
            " resolved to " + resolved;
        throw new AssertionError(msg);
    }

    TypeUnmarshaller createUnmarshaller(BindingType binding_type,
                                        BindingLoader loader)
        throws XmlException
    {
        TypeVisitor type_visitor = new TypeVisitor(this, loader);
        binding_type.accept(type_visitor);
        final TypeUnmarshaller type_um = type_visitor.getUnmarshaller();

        type_um.initialize(this, loader);
        return type_um;
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
    private TypeMarshaller createMarshaller(BindingTypeName type_name,
                                            BindingLoader loader)
        throws XmlException
    {
        final BindingType binding_type = loader.getBindingType(type_name);
        if (binding_type == null) {
            final String msg = "unable to load type for " + type_name;
            throw new XmlException(msg);
        }

        return createMarshaller(binding_type, loader);
    }

    /**
     * find marshaller for given type.  Can return null if not found
     *
     * @param binding_type
     * @param loader
     * @return  marshaller or null if not found.
     * @throws XmlException
     */
    TypeMarshaller createMarshaller(BindingType binding_type,
                                    BindingLoader loader)
        throws XmlException
    {
        final TypeMarshaller m;

        //REVIEW: consider using visitor

        if (binding_type instanceof SimpleContentBean) {
            SimpleContentBean scb = (SimpleContentBean)binding_type;
            final SimpleContentRuntimeBindingType rtt =
                createRuntimeType(scb, loader);
            m = new SimpleContentBeanMarshaller(rtt);
        } else if (binding_type instanceof SimpleBindingType) {
            SimpleBindingType stype = (SimpleBindingType)binding_type;

            final BindingTypeName asif_name = stype.getAsIfBindingTypeName();
            if (asif_name == null)
                throw new XmlException("no asif for " + stype);

            m = createMarshaller(asif_name, loader);
        } else if (binding_type instanceof JaxrpcEnumType) {
            JaxrpcEnumType enum_type = (JaxrpcEnumType)binding_type;
            final JaxrpcEnumRuntimeBindingType rtt =
                createRuntimeType(enum_type, loader);
            m = new JaxrpcEnumMarsahller(rtt);
        } else if (binding_type instanceof ListArrayType) {
            ListArrayType la_type = (ListArrayType)binding_type;
            final ListArrayRuntimeBindingType rtt =
                createRuntimeType(la_type, loader);
            m = new ListArrayConverter(rtt);
        } else if (binding_type instanceof BuiltinBindingType) {
            final RuntimeBindingType rtt =
                createRuntimeType(binding_type, loader);
            m = rtt.getMarshaller();
            assert m != null;
        } else {
            //not a known simple type
            m = null;
        }

        return m;
    }


    private static final class TypeVisitor
        implements BindingTypeVisitor
    {
        private final BindingLoader loader;
        private final RuntimeBindingTypeTable typeTable;

        private TypeUnmarshaller typeUnmarshaller;

        public TypeVisitor(RuntimeBindingTypeTable runtimeBindingTypeTable,
                           BindingLoader loader)
        {
            this.typeTable = runtimeBindingTypeTable;
            this.loader = loader;
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
                typeTable.createRuntimeType(byNameBean, loader);

            typeUnmarshaller = new ByNameUnmarshaller(rtt);
        }


        public void visit(SimpleContentBean simpleContentBean)
            throws XmlException
        {
            SimpleContentRuntimeBindingType rtt =
                typeTable.createRuntimeType(simpleContentBean, loader);

            typeUnmarshaller = new SimpleContentUnmarshaller(rtt);
        }

        public void visit(SimpleBindingType simpleBindingType)
            throws XmlException
        {
            typeUnmarshaller =
                typeTable.createSimpleTypeUnmarshaller(simpleBindingType,
                                                       loader,
                                                       typeTable);
        }

        public void visit(JaxrpcEnumType jaxrpcEnumType)
            throws XmlException
        {
            JaxrpcEnumRuntimeBindingType rtt =
                typeTable.createRuntimeType(jaxrpcEnumType,
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
                typeTable.createRuntimeType(wrappedArrayType, loader);
            typeUnmarshaller = new WrappedArrayUnmarshaller(rtt);
        }

        public void visit(ListArrayType listArrayType)
            throws XmlException
        {
            ListArrayRuntimeBindingType rtt =
                typeTable.createRuntimeType(listArrayType, loader);
            typeUnmarshaller = new ListArrayConverter(rtt);
        }

        public TypeUnmarshaller getUnmarshaller()
        {
            assert typeUnmarshaller != null;
            return typeUnmarshaller;
        }

    }


    private static final class FactoryTypeVisitor
        implements BindingTypeVisitor
    {
        private RuntimeBindingType runtimeBindingType;

        public RuntimeBindingType getRuntimeBindingType()
        {
            return runtimeBindingType;
        }

        public void visit(BuiltinBindingType builtinBindingType)
            throws XmlException
        {
            runtimeBindingType = new BuiltinRuntimeBindingType(builtinBindingType);
        }

        public void visit(ByNameBean byNameBean)
            throws XmlException
        {
            runtimeBindingType = new ByNameRuntimeBindingType(byNameBean);
        }

        public void visit(SimpleContentBean simpleContentBean)
            throws XmlException
        {
            runtimeBindingType = new SimpleContentRuntimeBindingType(simpleContentBean);
        }

        public void visit(SimpleBindingType simpleBindingType)
            throws XmlException
        {
            runtimeBindingType = new SimpleRuntimeBindingType(simpleBindingType);
        }

        public void visit(JaxrpcEnumType jaxrpcEnumType)
            throws XmlException
        {
            runtimeBindingType = new JaxrpcEnumRuntimeBindingType(jaxrpcEnumType);
        }

        public void visit(SimpleDocumentBinding simpleDocumentBinding)
            throws XmlException
        {
            throw new AssertionError("not valid here: " + simpleDocumentBinding);
        }

        public void visit(WrappedArrayType wrappedArrayType)
            throws XmlException
        {
            runtimeBindingType = new WrappedArrayRuntimeBindingType(wrappedArrayType);
        }

        public void visit(ListArrayType listArrayType)
            throws XmlException
        {
            runtimeBindingType = new ListArrayRuntimeBindingType(listArrayType);
        }

    }


}
