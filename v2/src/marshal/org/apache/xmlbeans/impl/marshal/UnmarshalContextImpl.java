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
import org.apache.xmlbeans.UnmarshalContext;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExt;
import org.apache.xmlbeans.impl.richParser.XMLStreamReaderExtImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;

/**
 * An UnmarshalContext holds the mutable state using by an Unmarshaller
 * during unmarshalling.  Example contents are an id -> object table
 * for href processing, and the position in the xml document.
 *
 * The UnmarshalContext is purposefullly unsynchronized.
 * Only one thread should ever be accessing this object, and a new one will be
 * required for each unmarshalling pass.
 */
final class UnmarshalContextImpl
    implements UnmarshalContext
{
    private XMLStreamReaderExt baseReader;
    private final BindingLoader bindingLoader;
    private final RuntimeBindingTypeTable typeTable;
    private final Collection errors;
    private final XsiAttributeHolder xsiAttributeHolder =
        new XsiAttributeHolder();

    private static final int INVALID = -1;

    private boolean gotXsiAttributes;
    private int currentAttributeIndex = INVALID;
    private int currentAttributeCount = INVALID;


    UnmarshalContextImpl(XMLStreamReader reader,
                         BindingLoader bindingLoader,
                         RuntimeBindingTypeTable typeTable,
                         Collection errors)
    {
        this.baseReader = createExtendedReader(reader);
        this.bindingLoader = bindingLoader;
        this.errors = errors;
        this.typeTable = typeTable;

        if (reader != null) {
            updateAttributeState();
        }
    }


    UnmarshalContextImpl(BindingLoader bindingLoader,
                         RuntimeBindingTypeTable typeTable,
                         Collection errors)
    {
        this(null, bindingLoader, typeTable, errors);
    }


    public void setXmlStream(XMLStreamReader reader)
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

    /**
     *
     * @return  true if we have a non null XMLStreamReader
     */
    boolean hasXmlStream()
    {
        return (baseReader != null);
    }

    RuntimeBindingTypeTable getTypeTable()
    {
        return typeTable;
    }


    TypeUnmarshaller getTypeUnmarshaller(QName xsi_type)
    {
        XmlTypeName xname = XmlTypeName.forTypeNamed(xsi_type);
        BindingType binding_type =
            bindingLoader.getBindingType(bindingLoader.lookupPojoFor(xname));
        if (binding_type == null) {
            String msg = "unable to locate binding type for " + xsi_type;
            throw new XmlRuntimeException(msg);
        }
        TypeUnmarshaller um =
            typeTable.getTypeUnmarshaller(binding_type);
        if (um == null) {
            String msg = "unable to locate unmarshaller for " +
                binding_type.getName();
            throw new XmlRuntimeException(msg);
        }
        return um;
    }

    //package only -- get read/write version of error collection

    /**
     * package only -- get read/write version of error collection
     *
     * @return  read/write version of error collection
     */
    Collection getErrorCollection()
    {
        return errors;
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

    boolean getBooleanValue()
    {
        try {
            return baseReader.getBooleanValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public byte getByteValue()
    {
        try {
            return baseReader.getByteValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public short getShortValue()
    {
        try {
            return baseReader.getShortValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public int getIntValue()
    {
        try {
            return baseReader.getIntValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public long getLongValue()
    {
        try {
            return baseReader.getLongValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public BigInteger getBigIntegerValue()
    {
        try {
            return baseReader.getBigIntegerValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public BigDecimal getBigDecimalValue()
    {
        try {
            return baseReader.getBigDecimalValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public float getFloatValue()
    {
        try {
            return baseReader.getFloatValue();
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public double getDoubleValue()
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
        throw new AssertionError("unimp");
    }

    InputStream getBase64Value()
    {
        throw new AssertionError("unimp");
    }

    XmlCalendar getCalendarValue()
    {
        throw new AssertionError("unimp");
    }

    Date getDateValue()
    {
        throw new AssertionError("unimp");
    }

    GDate getGDateValue()
    {
        throw new AssertionError("unimp");
    }

    GDuration getGDurationValue()
    {
        throw new AssertionError("unimp");
    }

    QName getQNameValue()
    {
        throw new AssertionError("unimp");
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

    public byte getAttributeByteValue()
    {
        try {
            return baseReader.getAttributeByteValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public short getAttributeShortValue()
    {
        try {
            return baseReader.getAttributeShortValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public int getAttributeIntValue()
    {
        try {
            return baseReader.getAttributeIntValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public long getAttributeLongValue()
    {
        try {
            return baseReader.getAttributeLongValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public BigInteger getAttributeBigIntegerValue()
    {
        try {
            return baseReader.getAttributeBigIntegerValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public BigDecimal getAttributeBigDecimalValue()
    {
        try {
            return baseReader.getAttributeBigDecimalValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public float getAttributeFloatValue()
    {
        try {
            return baseReader.getAttributeFloatValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    public double getAttributeDoubleValue()
    {
        try {
            return baseReader.getAttributeDoubleValue(currentAttributeIndex);
        }
        catch (XMLStreamException e) {
            throw new XmlRuntimeException(e);
        }
    }

    InputStream getAttributeHexBinaryValue()
    {
        throw new AssertionError("unimp");
    }

    InputStream getAttributeBase64Value()
    {
        throw new AssertionError("unimp");
    }

    XmlCalendar getAttributeCalendarValue()
    {
        throw new AssertionError("unimp");
    }

    Date getAttributeDateValue()
    {
        throw new AssertionError("unimp");
    }

    GDate getAttributeGDateValue()
    {
        throw new AssertionError("unimp");
    }

    GDuration getAttributeGDurationValue()
    {
        throw new AssertionError("unimp");
    }

    QName getAttributeQNameValue()
    {
        throw new AssertionError("unimp");
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
        MarshalStreamUtils.getXsiAttributes(xsiAttributeHolder,
                                            baseReader, errors);
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

}

