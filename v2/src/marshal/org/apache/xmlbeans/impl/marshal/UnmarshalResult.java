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

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.ObjectFactory;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.SimpleDocumentBinding;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;
import org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;

/**
 * An UnmarshalResult holds the mutable state using by an Unmarshaller
 * during unmarshalling.  Example contents are an id -> object table
 * for href processing, and the position in the xml document.
 *
 * The UnmarshalResult is purposefullly unsynchronized.
 * Only one thread should ever be accessing this object, and a new one will be
 * required for each unmarshalling pass.
 */
final class UnmarshalResult
{
    //per binding context objects
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;
    private final SchemaTypeLoaderProvider schemaTypeLoaderProvider;

    //our state
    private XMLStreamReaderExt baseReader;
    private final XmlOptions options;
    private final Collection errors;
    private final XsiAttributeHolder xsiAttributeHolder =
        new XsiAttributeHolder();
    private boolean gotXsiAttributes;
    private BitSet defaultAttributeBits;
    private int currentAttributeIndex = INVALID;
    private int currentAttributeCount = INVALID;

    private static final int INVALID = -1;


    UnmarshalResult(BindingLoader bindingLoader,
                    RuntimeBindingTypeTable typeTable,
                    SchemaTypeLoaderProvider provider,
                    XmlOptions options)
    {
        this.bindingLoader = bindingLoader;
        this.typeTable = typeTable;
        this.schemaTypeLoaderProvider = provider;
        this.options = options;
        this.errors = BindingContextImpl.extractErrorHandler(options);
    }

    private RuntimeBindingType getRuntimeType(BindingType type)
        throws XmlException
    {
        return typeTable.createRuntimeType(type, bindingLoader);
    }

    private void enrichXmlStream(XMLStreamReader reader)
    {
        assert reader != null;

        baseReader = createExtendedReader(reader);
        updateAttributeState();
    }


    private static XMLStreamReaderExt createExtendedReader(XMLStreamReader reader)
    {
        if (reader instanceof XMLStreamReaderExt) {
            return (XMLStreamReaderExt)reader;
        } else {
            return new XMLStreamReaderExtImpl(reader);
        }
    }

    private BindingType lookupBindingType(QName xsi_type)
    {
        XmlTypeName xname = XmlTypeName.forTypeNamed(xsi_type);
        final BindingTypeName btname = bindingLoader.lookupPojoFor(xname);
        if (btname == null) {
            addError("unknown type: " + xsi_type);
            return null;
        }
        final BindingType binding_type = bindingLoader.getBindingType(btname);
        if (binding_type == null) {
            addError("unknown binding type: " + binding_type);
        }
        return binding_type;
    }

    void addError(String msg)
    {
        addError(msg, baseReader.getLocation());
    }

    private void addWarning(String msg)
    {
        Location location = baseReader.getLocation();
        assert location != null;
        MarshalStreamUtils.addError(errors, msg,
                                    XmlError.SEVERITY_WARNING,
                                    location);
    }


    void addError(String msg, Location location)
    {
        assert location != null;
        MarshalStreamUtils.addError(errors, msg, location);
    }

    Collection getErrors()
    {
        return errors;
    }

    Object unmarshalDocument(XMLStreamReader reader)
        throws XmlException
    {
        if (isValidating()) {
            ValidatingXMLStreamReader vr = new ValidatingXMLStreamReader();
            final SchemaTypeLoader schemaTypeLoader =
                schemaTypeLoaderProvider.getSchemaTypeLoader();
            vr.init(reader, false, null, schemaTypeLoader, options, errors);
            enrichXmlStream(vr);
        } else {
            enrichXmlStream(reader);
        }
        advanceToFirstItemOfInterest();
        BindingType bindingType = determineRootType();
        return unmarshalBindingType(bindingType);
    }


    private Object unmarshalBindingType(BindingType bindingType)
        throws XmlException
    {
        this.updateAttributeState();

        final TypeUnmarshaller um;
        final ObjectFactory of = extractObjectFactory();

        try {
            final RuntimeBindingType rtt = getRuntimeType(bindingType);
            if (of == null) {
                if (hasXsiNil())
                    um = NullUnmarshaller.getInstance();
                else
                    um = rtt.getUnmarshaller();
                return um.unmarshal(this);
            } else {
                final Object initial_obj = of.createObject(rtt.getJavaType());
                um = rtt.getUnmarshaller();
                um.unmarshal(initial_obj, this);
                return initial_obj;
            }
        }
        catch (InvalidLexicalValueException ilve) {
            //top level simple types can end up here for invalid lexical values
            assert !errors.isEmpty();
            return null;
        }
    }

    private ObjectFactory extractObjectFactory()
    {
        if (options == null) return null;

        return
            (ObjectFactory)options.get(XmlOptions.UNMARSHAL_INITIAL_OBJECT_FACTORY);
    }

    Object unmarshalType(XMLStreamReader reader,
                         QName schemaType,
                         String javaType)
        throws XmlException
    {
        doctorStream(schemaType, reader);

        final QName xsi_type = getXsiType();

        BindingType btype = null;

        if (xsi_type != null) {
            btype = getPojoTypeFromXsiType(xsi_type);
        }

        if (btype == null) {
            btype = determineBindingType(schemaType, javaType);
        }

        if (btype == null) {
            final String msg = "unable to find binding type for " +
                schemaType + " : " + javaType;
            throw new XmlException(msg);
        }
        return unmarshalBindingType(btype);
    }

    private void doctorStream(QName schemaType,
                              XMLStreamReader reader)
        throws XmlException
    {
        if (isValidating()) {
            ValidatingXMLStreamReader vr = new ValidatingXMLStreamReader();
            SchemaTypeLoader schemaTypeLoader =
                schemaTypeLoaderProvider.getSchemaTypeLoader();
            SchemaType schema_type = schemaTypeLoader.findType(schemaType);
            if (schema_type == null) {
                String e = "unable to locate definition of type " +
                    schemaType + " in supplied schema type system";
                throw new XmlException(e);
            }
            vr.init(reader, false, schema_type, schemaTypeLoader, options, errors);
            reader = vr; //note changing param
        }

        enrichXmlStream(reader);
    }


    Object unmarshalElement(XMLStreamReader reader,
                            QName globalElement,
                            String javaType)
        throws XmlException
    {
        final BindingType binding_type =
            determineTypeForGlobalElement(globalElement);
        final XmlTypeName type_name = binding_type.getName().getXmlName();
        assert type_name.isGlobal();
        assert type_name.isSchemaType();
        final QName schema_type = type_name.getQName();
        doctorStream(schema_type, reader);

        final QName xsi_type = getXsiType();

        BindingType btype = null;

        if (xsi_type != null) {
            btype = getPojoTypeFromXsiType(xsi_type);
        }

        if (btype == null) {
            btype = determineBindingType(schema_type, javaType);
        }

        if (btype == null) {
            final String msg = "unable to find binding type for " +
                schema_type + " : " + javaType;
            throw new XmlException(msg);
        }
        return unmarshalBindingType(btype);
    }


    private boolean isValidating()
    {
        if (options == null) return false;

        return options.hasOption(XmlOptions.UNMARSHAL_VALIDATE);
    }

    private BindingType determineBindingType(QName schemaType, String javaType)
    {
        XmlTypeName xname = XmlTypeName.forTypeNamed(schemaType);
        JavaTypeName jname = JavaTypeName.forClassName(javaType);
        BindingTypeName btname = BindingTypeName.forPair(jname, xname);
        return bindingLoader.getBindingType(btname);
    }

    private BindingType determineRootType()
        throws XmlException
    {
        QName xsi_type = this.getXsiType();

        BindingType retval = null;
        if (xsi_type != null) {
            retval = getPojoTypeFromXsiType(xsi_type);
        }

        if (retval == null) {
            QName root_elem_qname = new QName(this.getNamespaceURI(),
                                              this.getLocalName());
            retval = determineTypeForGlobalElement(root_elem_qname);
        }

        return retval;
    }

    private BindingType determineTypeForGlobalElement(QName elem)
        throws XmlException
    {
        final XmlTypeName type_name =
            XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, elem);
        BindingType doc_binding_type = getPojoBindingType(type_name, true);
        SimpleDocumentBinding sd = (SimpleDocumentBinding)doc_binding_type;
        return getPojoBindingType(sd.getTypeOfElement(), true);
    }

    //will return null on error and log errors
    private BindingType getPojoTypeFromXsiType(QName xsi_type)
        throws XmlException
    {
        final XmlTypeName type_name = XmlTypeName.forTypeNamed(xsi_type);
        final BindingType pojoBindingType = getPojoBindingType(type_name, false);
        assert !(pojoBindingType instanceof SimpleDocumentBinding);
        return pojoBindingType;
    }


    private BindingType getPojoBindingType(final XmlTypeName type_name,
                                           boolean fail_fast)
        throws XmlException
    {
        final BindingTypeName btName = bindingLoader.lookupPojoFor(type_name);
        if (btName == null) {
            final String msg = "failed to load java type corresponding " +
                "to " + type_name;
            if (fail_fast) {
                throw new XmlException(msg);
            } else {
                addError(msg);
                return null;
            }

        }

        BindingType bt = bindingLoader.getBindingType(btName);

        if (bt == null) {
            final String msg = "failed to load BindingType for " + btName;
            if (fail_fast) {
                throw new XmlException(msg);
            } else {
                addError(msg);
                return null;
            }
        }

        return bt;
    }


    // ======================= xml access methods =======================


    Location getLocation()
    {
        return baseReader.getLocation();
    }

    String getStringValue() throws XmlException
    {
        try {
            return baseReader.getStringValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    String getStringValue(int ws) throws XmlException
    {
        try {
            return baseReader.getStringValue(ws);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    boolean getBooleanValue() throws XmlException
    {
        try {
            return baseReader.getBooleanValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    byte getByteValue() throws XmlException
    {
        try {
            return baseReader.getByteValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    short getShortValue() throws XmlException
    {
        try {
            return baseReader.getShortValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    int getIntValue() throws XmlException
    {
        try {
            return baseReader.getIntValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    long getLongValue() throws XmlException
    {
        try {
            return baseReader.getLongValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    BigInteger getBigIntegerValue() throws XmlException
    {
        try {
            return baseReader.getBigIntegerValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    BigDecimal getBigDecimalValue() throws XmlException
    {
        try {
            return baseReader.getBigDecimalValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    float getFloatValue() throws XmlException
    {
        try {
            return baseReader.getFloatValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    double getDoubleValue() throws XmlException
    {
        try {
            return baseReader.getDoubleValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    InputStream getHexBinaryValue() throws XmlException
    {
        try {
            return baseReader.getHexBinaryValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    InputStream getBase64Value() throws XmlException
    {
        try {
            return baseReader.getBase64Value();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    XmlCalendar getCalendarValue() throws XmlException
    {
        try {
            return baseReader.getCalendarValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    String getAnyUriValue() throws XmlException
    {
        try {
            return baseReader.getStringValue(XMLStreamReaderExt.WS_COLLAPSE);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    Date getDateValue() throws XmlException
    {
        try {
            final GDate val = baseReader.getGDateValue();
            return val == null ? null : val.getDate();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    GDate getGDateValue() throws XmlException
    {
        try {
            return baseReader.getGDateValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    GDuration getGDurationValue() throws XmlException
    {
        try {
            return baseReader.getGDurationValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    QName getQNameValue() throws XmlException
    {
        try {
            return baseReader.getQNameValue();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    String getAttributeStringValue() throws XmlException
    {
        try {
            return baseReader.getAttributeStringValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    String getAttributeStringValue(int whitespace_style)
        throws XmlException
    {
        try {
            return baseReader.getAttributeStringValue(currentAttributeIndex,
                                                      whitespace_style);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    boolean getAttributeBooleanValue() throws XmlException
    {
        try {
            return baseReader.getAttributeBooleanValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    byte getAttributeByteValue() throws XmlException
    {
        try {
            return baseReader.getAttributeByteValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    short getAttributeShortValue() throws XmlException
    {
        try {
            return baseReader.getAttributeShortValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    int getAttributeIntValue() throws XmlException
    {
        try {
            return baseReader.getAttributeIntValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    long getAttributeLongValue() throws XmlException
    {
        try {
            return baseReader.getAttributeLongValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    BigInteger getAttributeBigIntegerValue() throws XmlException
    {
        try {
            return baseReader.getAttributeBigIntegerValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    BigDecimal getAttributeBigDecimalValue() throws XmlException
    {
        try {
            return baseReader.getAttributeBigDecimalValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    float getAttributeFloatValue() throws XmlException
    {
        try {
            return baseReader.getAttributeFloatValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    double getAttributeDoubleValue() throws XmlException
    {
        try {
            return baseReader.getAttributeDoubleValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    String getAttributeAnyUriValue() throws XmlException
    {
        try {
            return baseReader.getAttributeStringValue(currentAttributeIndex,
                                                      XMLStreamReaderExt.WS_COLLAPSE);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    InputStream getAttributeHexBinaryValue() throws XmlException
    {
        try {
            return baseReader.getAttributeHexBinaryValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    InputStream getAttributeBase64Value() throws XmlException
    {
        try {
            return baseReader.getAttributeBase64Value(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    XmlCalendar getAttributeCalendarValue() throws XmlException
    {
        try {
            return baseReader.getAttributeCalendarValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    Date getAttributeDateValue() throws XmlException
    {
        try {
            GDate val = baseReader.getAttributeGDateValue(currentAttributeIndex);
            return val == null ? null : val.getDate();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    GDate getAttributeGDateValue() throws XmlException
    {
        try {
            return baseReader.getAttributeGDateValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    GDuration getAttributeGDurationValue() throws XmlException
    {
        try {
            return baseReader.getAttributeGDurationValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    QName getAttributeQNameValue() throws XmlException
    {
        try {
            return baseReader.getAttributeQNameValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }


    /**
     * return the QName value found for xsi:type
     * or null if neither one was found
     */
    private QName getXsiType()
        throws XmlException
    {
        if (!gotXsiAttributes) {
            getXsiAttributes();
        }
        assert gotXsiAttributes;
        return xsiAttributeHolder.xsiType;
    }

    boolean hasXsiNil() throws XmlException
    {
        if (!gotXsiAttributes) {
            getXsiAttributes();
        }
        assert gotXsiAttributes;
        return xsiAttributeHolder.hasXsiNil;
    }

    private void getXsiAttributes() throws XmlException
    {
        try {
            MarshalStreamUtils.getXsiAttributes(xsiAttributeHolder,
                                                baseReader, errors);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
        gotXsiAttributes = true;
    }

    /**
     *
     * @return  false if we hit an end element (any end element at all)
     */
    boolean advanceToNextStartElement()
        throws XmlException
    {
        final boolean ret;
        ret = MarshalStreamUtils.advanceToNextStartElement(baseReader);
        updateAttributeState();
        return ret;
    }


    private void advanceToFirstItemOfInterest()
        throws XmlException
    {
        assert baseReader != null;
        MarshalStreamUtils.advanceToFirstItemOfInterest(baseReader);
    }

    int next() throws XmlException
    {
        try {
            final int new_state = baseReader.next();
            updateAttributeState();
            return new_state;
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    boolean hasNext() throws XmlException
    {
        try {
            return baseReader.hasNext();
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }


    private void updateAttributeState()
    {
        xsiAttributeHolder.reset();
        gotXsiAttributes = false;
        if (defaultAttributeBits != null) {
            defaultAttributeBits.clear();
        }
        if (baseReader.isStartElement()) {
            currentAttributeCount = baseReader.getAttributeCount();
            currentAttributeIndex = 0;
        } else {
            currentAttributeIndex = INVALID;
            currentAttributeCount = INVALID;
        }
    }


    boolean isStartElement()
    {
        return baseReader.isStartElement();
    }

    boolean isEndElement()
    {
        return baseReader.isEndElement();
    }

    private int getAttributeCount()
    {
        assert baseReader.isStartElement();

        return baseReader.getAttributeCount();
    }

    String getLocalName()
    {
        return baseReader.getLocalName();
    }

    String getNamespaceURI()
    {
        return baseReader.getNamespaceURI();
    }

    void skipElement()
        throws XmlException
    {
        MarshalStreamUtils.skipElement(baseReader);
        updateAttributeState();
    }


    void advanceAttribute()
    {
        assert hasMoreAttributes();
        assert currentAttributeCount != INVALID;
        assert currentAttributeIndex != INVALID;

        currentAttributeIndex++;

        assert currentAttributeIndex <= currentAttributeCount;
    }

    boolean hasMoreAttributes()
    {
        assert baseReader.isStartElement();

        assert currentAttributeCount != INVALID;
        assert currentAttributeIndex != INVALID;

        return (currentAttributeIndex < currentAttributeCount);
    }

    String getCurrentAttributeNamespaceURI()
    {
        assert currentAttributeCount != INVALID;
        assert currentAttributeIndex != INVALID;

        return baseReader.getAttributeNamespace(currentAttributeIndex);
    }

    String getCurrentAttributeLocalName()
    {
        assert currentAttributeCount != INVALID;
        assert currentAttributeIndex != INVALID;

        return baseReader.getAttributeLocalName(currentAttributeIndex);
    }

    void attributePresent(int att_idx)
    {
        if (defaultAttributeBits == null) {
            int bits_size = getAttributeCount();
            defaultAttributeBits = new BitSet(bits_size);
        }

        defaultAttributeBits.set(att_idx);
    }

    boolean isAttributePresent(int att_idx)
    {
        if (defaultAttributeBits == null)
            return false;

        return defaultAttributeBits.get(att_idx);
    }

    void setNextElementDefault(String lexical_default)
        throws XmlException
    {
        try {
            baseReader.setDefaultValue(lexical_default);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Do the supplied localname, uri pair match the given qname?
     *
     * @param qn          name of element
     * @param localname   candidate localname
     * @param uri         candidtate uri
     * @return
     */
    static boolean doesElementMatch(QName qn, String localname, String uri)
    {
        if (qn.getLocalPart().equals(localname)) {
            //QNames always uses "" for no namespace, but the incoming uri
            //might use null or "".
            return qn.getNamespaceURI().equals(uri == null ? "" : uri);
        }
        return false;
    }

    RuntimeBindingType determineActualRuntimeType(RuntimeBindingType expected)
        throws XmlException
    {
        final QName xsi_type = getXsiType();


        if (xsi_type != null && !xsi_type.equals(expected.getSchemaTypeName())) {
            final BindingType binding_type = lookupBindingType(xsi_type);
            if (binding_type != null) {
                final RuntimeBindingType actual_rtt =
                    typeTable.createRuntimeType(binding_type, bindingLoader);
                if (isCompatibleTypeSubstitution(expected, actual_rtt)) {
                    return actual_rtt;
                } else {
                    String e = "invalid type substitution: " +
                        xsi_type + " for " + expected.getSchemaTypeName() +
                        " due to incompatible java types (" +
                        actual_rtt.getJavaType().getName() +
                        " for " + expected.getJavaType().getName() +
                        ") -- using declared type";
                    addWarning(e);
                }
            }
        }

        return expected;
    }


    //is xsi:type substitution ok.  only checks java compat for now.
    private static boolean isCompatibleTypeSubstitution(RuntimeBindingType expected,
                                                        RuntimeBindingType actual)
    {
        if (expected == actual) return true;

        final Class expected_type = expected.getJavaType();
        final Class actual_type = actual.getJavaType();
        if (expected_type == actual_type) return true;
        if (expected_type.equals(actual_type)) return true;

        return expected_type.isAssignableFrom(actual_type);
    }


    NamespaceContext getNamespaceContext()
    {
        return baseReader.getNamespaceContext();
    }

}

