/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/


package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.SimpleDocumentBinding;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;

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

    //our state
    private XMLStreamReaderExt baseReader;
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
                    XmlOptions options)
    {
        this.bindingLoader = bindingLoader;
        this.typeTable = typeTable;
        this.errors = BindingContextImpl.extractErrorHandler(options);
    }


    void setXmlStream(XMLStreamReader reader)
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

    RuntimeBindingTypeTable getTypeTable()
    {
        return typeTable;
    }


    //returns null and updates errors if there was a problem.
    TypeUnmarshaller getTypeUnmarshaller(QName xsi_type)
    {
        XmlTypeName xname = XmlTypeName.forTypeNamed(xsi_type);
        BindingType binding_type =
            bindingLoader.getBindingType(bindingLoader.lookupPojoFor(xname));
        if (binding_type == null) {
            addError("unknown type: " + xsi_type);
            return null;
        }
        TypeUnmarshaller um =
            typeTable.getTypeUnmarshaller(binding_type);
        if (um == null) {
            String msg = "unable to locate unmarshaller for " +
                binding_type.getName();
            addError(msg);
            return null;
        }
        return um;
    }

    private void addError(String msg)
    {
        MarshalStreamUtils.addError(errors, msg,
                                    baseReader.getLocation(),
                                    "<unknown>");
    }


    void addError(String msg, Location location)
    {
        MarshalStreamUtils.addError(errors, msg,
                                    location,
                                    "<unknown>");
    }


    Object unmarshal(XMLStreamReader reader)
        throws XmlException
    {
        setXmlStream(reader);
        advanceToFirstItemOfInterest();
        BindingType bindingType = determineRootType();
        return unmarshalBindingType(bindingType);
    }


    private Object unmarshalBindingType(BindingType bindingType)
    {
        TypeUnmarshaller um =
            typeTable.getOrCreateTypeUnmarshaller(bindingType, bindingLoader);

        this.updateAttributeState();

        return um.unmarshal(this);
    }

    Object unmarshalType(XMLStreamReader reader,
                         QName schemaType,
                         String javaType)
        throws XmlException
    {
        setXmlStream(reader);

        BindingType btype = determineBindingType(schemaType, javaType);
        if (btype == null) {
            final String msg = "unable to find binding type for " +
                schemaType + " : " + javaType;
            throw new XmlException(msg);
        }
        return unmarshalBindingType(btype);
    }

    private BindingType determineBindingType(QName schemaType, String javaType)
    {
        XmlTypeName xname = XmlTypeName.forTypeNamed(schemaType);
        JavaTypeName jname = JavaTypeName.forString(javaType);
        BindingTypeName btname = BindingTypeName.forPair(jname, xname);
        return bindingLoader.getBindingType(btname);
    }

    private BindingType determineRootType()
        throws XmlException
    {
        QName xsi_type = this.getXsiType();

        if (xsi_type == null) {
            QName root_elem_qname = new QName(this.getNamespaceURI(),
                                              this.getLocalName());
            final XmlTypeName type_name =
                XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, root_elem_qname);
            final BindingType doc_binding_type = getPojoBindingType(type_name);
            SimpleDocumentBinding sd = (SimpleDocumentBinding)doc_binding_type;
            return getPojoBindingType(sd.getTypeOfElement());
        } else {
            //TODO: we are too trusting of the xsi type -- if we don't know about
            //that type we should just use the expected type for the element,
            //add an error and keep going
            final XmlTypeName type_name = XmlTypeName.forTypeNamed(xsi_type);
            final BindingType pojoBindingType = getPojoBindingType(type_name);
            assert !(pojoBindingType instanceof SimpleDocumentBinding);
            return pojoBindingType;
        }
    }

    private BindingType getPojoBindingType(final XmlTypeName type_name)
        throws XmlException
    {
        final BindingTypeName btName = bindingLoader.lookupPojoFor(type_name);
        if (btName == null) {
            throw new XmlException("failed to load java type" +
                                   " corresponding to " + type_name);
        }

        BindingType bt = bindingLoader.getBindingType(btName);

        if (bt == null) {
            throw new XmlException("failed to load BindingType for " + btName);
        }

        return bt;
    }


    // ======================= xml access methods =======================

    String getStringValue()
    {
        try {
            return baseReader.getStringValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    String getStringValue(int ws)
    {
        try {
            return baseReader.getStringValue(ws);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    boolean getBooleanValue()
    {
        try {
            return baseReader.getBooleanValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    byte getByteValue()
    {
        try {
            return baseReader.getByteValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    short getShortValue()
    {
        try {
            return baseReader.getShortValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    int getIntValue()
    {
        try {
            return baseReader.getIntValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    long getLongValue()
    {
        try {
            return baseReader.getLongValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    BigInteger getBigIntegerValue()
    {
        try {
            return baseReader.getBigIntegerValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    BigDecimal getBigDecimalValue()
    {
        try {
            return baseReader.getBigDecimalValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    float getFloatValue()
    {
        try {
            return baseReader.getFloatValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    double getDoubleValue()
    {
        try {
            return baseReader.getDoubleValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    InputStream getHexBinaryValue()
    {
        try {
            return baseReader.getHexBinaryValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    InputStream getBase64Value()
    {
        try {
            return baseReader.getBase64Value();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    XmlCalendar getCalendarValue()
    {
        try {
            return baseReader.getCalendarValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    String getAnyUriValue()
    {
        try {
            return baseReader.getStringValue(XMLStreamReaderExt.WS_COLLAPSE);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    Date getDateValue()
    {
        try {
            final GDate val = baseReader.getGDateValue();
            return val == null ? null : val.getDate();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    GDate getGDateValue()
    {
        try {
            return baseReader.getGDateValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    GDuration getGDurationValue()
    {
        try {
            return baseReader.getGDurationValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    QName getQNameValue()
    {
        try {
            return baseReader.getQNameValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    String getAttributeStringValue()
    {
        try {
            return baseReader.getAttributeStringValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    boolean getAttributeBooleanValue()
    {
        try {
            return baseReader.getAttributeBooleanValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    byte getAttributeByteValue()
    {
        try {
            return baseReader.getAttributeByteValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    short getAttributeShortValue()
    {
        try {
            return baseReader.getAttributeShortValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    int getAttributeIntValue()
    {
        try {
            return baseReader.getAttributeIntValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    long getAttributeLongValue()
    {
        try {
            return baseReader.getAttributeLongValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    BigInteger getAttributeBigIntegerValue()
    {
        try {
            return baseReader.getAttributeBigIntegerValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    BigDecimal getAttributeBigDecimalValue()
    {
        try {
            return baseReader.getAttributeBigDecimalValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    float getAttributeFloatValue()
    {
        try {
            return baseReader.getAttributeFloatValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    double getAttributeDoubleValue()
    {
        try {
            return baseReader.getAttributeDoubleValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    String getAttributeAnyUriValue()
    {
        try {
            return baseReader.getAttributeStringValue(currentAttributeIndex,
                                                      XMLStreamReaderExt.WS_COLLAPSE);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    InputStream getAttributeHexBinaryValue()
    {
        try {
            return baseReader.getAttributeHexBinaryValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    InputStream getAttributeBase64Value()
    {
        try {
            return baseReader.getAttributeBase64Value(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    XmlCalendar getAttributeCalendarValue()
    {
        try {
            return baseReader.getAttributeCalendarValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    Date getAttributeDateValue()
    {
        try {
            GDate val = baseReader.getAttributeGDateValue(currentAttributeIndex);
            return val == null ? null : val.getDate();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    GDate getAttributeGDateValue()
    {
        try {
            return baseReader.getAttributeGDateValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    GDuration getAttributeGDurationValue()
    {
        try {
            return baseReader.getAttributeGDurationValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    QName getAttributeQNameValue()
    {
        try {
            return baseReader.getAttributeQNameValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }


    /**
     * return the QName value found for xsi:type
     * or null if neither one was found
     */
    QName getXsiType()
    {
        if (!gotXsiAttributes) {
            getXsiAttributes();
        }
        assert gotXsiAttributes;
        return xsiAttributeHolder.xsiType;
    }

    boolean hasXsiNil()
    {
        if (!gotXsiAttributes) {
            getXsiAttributes();
        }
        assert gotXsiAttributes;
        return xsiAttributeHolder.hasXsiNil;
    }

    private void getXsiAttributes()
    {
        try {
            MarshalStreamUtils.getXsiAttributes(xsiAttributeHolder,
                                                baseReader, errors);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
        gotXsiAttributes = true;
    }

    /**
     *
     * @return  false if we hit an end element (any end element at all)
     */
    boolean advanceToNextStartElement()
    {
        boolean ret = MarshalStreamUtils.advanceToNextStartElement(baseReader);
        updateAttributeState();
        return ret;
    }


    void advanceToFirstItemOfInterest()
    {
        assert baseReader != null;
        MarshalStreamUtils.advanceToFirstItemOfInterest(baseReader);
    }

    int next()
    {
        try {
            final int new_state = baseReader.next();
            updateAttributeState();
            return new_state;
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    boolean hasNext()
    {
        try {
            return baseReader.hasNext();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }


    void updateAttributeState()
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

    int getAttributeCount()
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

    public void attributePresent(int att_idx)
    {
        if (defaultAttributeBits == null) {
            int bits_size = getAttributeCount();
            defaultAttributeBits = new BitSet(bits_size);
        }

        defaultAttributeBits.set(att_idx);
    }

    public boolean isAttributePresent(int att_idx)
    {
        if (defaultAttributeBits == null)
            return false;

        return defaultAttributeBits.get(att_idx);
    }

}

