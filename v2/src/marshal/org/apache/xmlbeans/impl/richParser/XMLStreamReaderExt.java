package org.apache.xmlbeans.impl.richParser;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlCalendar;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;

/**
 * This interface extends the simple XMLStreamReader interface adding
 * methods to get java objects asociated with schemas simple types like:
 * ints, long, BigIntegers, Dates etc
 *
 * Note: Given the fact that an implemetation of this interface will not
 * run validation in the stream, we will not include support for lists,
 * enumeration and unions. They might be possible to introduce but the
 * user has to push the corect schema type. Because usualy the backends
 * are not list/union aware, there would be not performance win from this.
 * If interest rises for this, support should be added.
 *
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 14, 2003
 */
public interface XMLStreamReaderExt
    extends XMLStreamReader
{
    /** Returns the value as a {@link String}. */
    String getStringValue() throws XMLStreamException;

    /** Returns the value as a boolean. */
    boolean getBooleanValue() throws XMLStreamException;

    /** Returns the value as a byte. */
    public byte getByteValue() throws XMLStreamException;

    /** Returns the value as a short. */
    public short getShortValue() throws XMLStreamException;

    /** Returns the value as an int. */
    public int getIntValue() throws XMLStreamException;

    /** Returns the value as a long. */
    public long getLongValue() throws XMLStreamException;

    /** Returns the value as a {@link java.math.BigInteger}. */
    public BigInteger getBigIntegerValue() throws XMLStreamException;

    /** Returns the value as a {@link java.math.BigDecimal}. */
    public BigDecimal getBigDecimalValue() throws XMLStreamException;

    /** Returns the value as a float. */
    public float getFloatValue() throws XMLStreamException;

    /** Returns the value as a double. */
    public double getDoubleValue() throws XMLStreamException;

    /** Returns the decoded hexbinary value as an InputStream. */
    InputStream getHexBinaryValue() throws XMLStreamException;

    /** Returns the decoded base64 value as anInputStream. */
    InputStream getBase64Value() throws XMLStreamException;

    /** Returns the value as an XmlCalendar which extends {@link java.util.Calendar}. */
    XmlCalendar getCalendarValue() throws XMLStreamException;

    /** Returns the value as a {@link java.util.Date}. */
    Date getDateValue() throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDate}. */
    GDate getGDateValue() throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDuration}. */
    GDuration getGDurationValue() throws XMLStreamException;

    /** Returns the value as a {@link javax.xml.namespace.QName}. */
    QName getQNameValue() throws XMLStreamException;

    /** Returns the value as a {@link String}. */
    String getAttributeStringValue(int index) throws XMLStreamException;

    /** Returns the value as a boolean. */
    boolean getAttributeBooleanValue(int index) throws XMLStreamException;

    /** Returns the value as a byte. */
    public byte getAttributeByteValue(int index) throws XMLStreamException;

    /** Returns the value as a short. */
    public short getAttributeShortValue(int index) throws XMLStreamException;

    /** Returns the value as an int. */
    public int getAttributeIntValue(int index) throws XMLStreamException;

    /** Returns the value as a long. */
    public long getAttributeLongValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link java.math.BigInteger}. */
    public BigInteger getAttributeBigIntegerValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link java.math.BigDecimal}. */
    public BigDecimal getAttributeBigDecimalValue(int index) throws XMLStreamException;

    /** Returns the value as a float. */
    public float getAttributeFloatValue(int index) throws XMLStreamException;

    /** Returns the value as a double. */
    public double getAttributeDoubleValue(int index) throws XMLStreamException;

    /** Returns the decoded hexbinary value as an InputStream. */
    InputStream getAttributeHexBinaryValue(int index) throws XMLStreamException;

    /** Returns the decoded base64 value as anInputStream. */
    InputStream getAttributeBase64Value(int index) throws XMLStreamException;

    /** Returns the value as an XmlCalendar which extends {@link java.util.Calendar}. */
    XmlCalendar getAttributeCalendarValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link java.util.Date}. */
    Date getAttributeDateValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDate}. */
    GDate getAttributeGDateValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDuration}. */
    GDuration getAttributeGDurationValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link javax.xml.namespace.QName}. */
    QName getAttributeQNameValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link String}. */
    String getAttributeStringValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a boolean. */
    boolean getAttributeBooleanValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a byte. */
    public byte getAttributeByteValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a short. */
    public short getAttributeShortValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as an int. */
    public int getAttributeIntValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a long. */
    public long getAttributeLongValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link java.math.BigInteger}. */
    public BigInteger getAttributeBigIntegerValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link java.math.BigDecimal}. */
    public BigDecimal getAttributeBigDecimalValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a float. */
    public float getAttributeFloatValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a double. */
    public double getAttributeDoubleValue(String uri, String local) throws XMLStreamException;

    /** Returns the decoded hexbinary value as an InputStream. */
    InputStream getAttributeHexBinaryValue(String uri, String local) throws XMLStreamException;

    /** Returns the decoded base64 value as anInputStream. */
    InputStream getAttributeBase64Value(String uri, String local) throws XMLStreamException;

    /** Returns the value as an XmlCalendar which extends {@link java.util.Calendar}. */
    XmlCalendar getAttributeCalendarValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link java.util.Date}. */
    Date getAttributeDateValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDate}. */
    GDate getAttributeGDateValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDuration}. */
    GDuration getAttributeGDurationValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link javax.xml.namespace.QName}. */
    QName getAttributeQNameValue(String uri, String local) throws XMLStreamException;
}
