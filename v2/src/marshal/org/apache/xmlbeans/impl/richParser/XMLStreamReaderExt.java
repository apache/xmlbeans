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

package org.apache.xmlbeans.impl.richParser;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.math.BigDecimal;
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
     public static final int WS_PRESERVE = XmlWhitespace.WS_PRESERVE;
     public static final int WS_REPLACE = XmlWhitespace.WS_REPLACE;
     public static final int WS_COLLAPSE = XmlWhitespace.WS_COLLAPSE;

    /** Returns the value as a {@link String}. */
    public String getStringValue() throws XMLStreamException;

    /** Returns the value as a {@link String}, with wsStyle applied. */
    public String getStringValue(int wsStyle) throws XMLStreamException;

    /** Returns the value as a boolean. */
    public boolean getBooleanValue() throws XMLStreamException;

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
    public InputStream getHexBinaryValue() throws XMLStreamException;

    /** Returns the decoded base64 value as anInputStream. */
    public InputStream getBase64Value() throws XMLStreamException;

    /** Returns the value as an XmlCalendar which extends {@link java.util.Calendar}. */
    public XmlCalendar getCalendarValue() throws XMLStreamException;

    /** Returns the value as a {@link java.util.Date}. */
    public Date getDateValue() throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDate}. */
    public GDate getGDateValue() throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDuration}. */
    public GDuration getGDurationValue() throws XMLStreamException;

    /** Returns the value as a {@link javax.xml.namespace.QName}. */
    public QName getQNameValue() throws XMLStreamException;

    /** Returns the value as a {@link String}. */
    public String getAttributeStringValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link String}, with wsStyle applied. */
    public String getAttributeStringValue(int index, int wsStyle) throws XMLStreamException;

    /** Returns the value as a boolean. */
    public boolean getAttributeBooleanValue(int index) throws XMLStreamException;

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
    public InputStream getAttributeHexBinaryValue(int index) throws XMLStreamException;

    /** Returns the decoded base64 value as anInputStream. */
    public InputStream getAttributeBase64Value(int index) throws XMLStreamException;

    /** Returns the value as an XmlCalendar which extends {@link java.util.Calendar}. */
    public XmlCalendar getAttributeCalendarValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link java.util.Date}. */
    public Date getAttributeDateValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDate}. */
    public GDate getAttributeGDateValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDuration}. */
    public GDuration getAttributeGDurationValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link javax.xml.namespace.QName}. */
    public QName getAttributeQNameValue(int index) throws XMLStreamException;

    /** Returns the value as a {@link String}. */
    public String getAttributeStringValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link String}, with wsStyle applied. */
    public String getAttributeStringValue(String uri, String local, int wsStyle) throws XMLStreamException;

    /** Returns the value as a boolean. */
    public boolean getAttributeBooleanValue(String uri, String local) throws XMLStreamException;

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
    public InputStream getAttributeHexBinaryValue(String uri, String local) throws XMLStreamException;

    /** Returns the decoded base64 value as anInputStream. */
    public InputStream getAttributeBase64Value(String uri, String local) throws XMLStreamException;

    /** Returns the value as an XmlCalendar which extends {@link java.util.Calendar}. */
    public XmlCalendar getAttributeCalendarValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link java.util.Date}. */
    public Date getAttributeDateValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDate}. */
    public GDate getAttributeGDateValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link org.apache.xmlbeans.GDuration}. */
    public GDuration getAttributeGDurationValue(String uri, String local) throws XMLStreamException;

    /** Returns the value as a {@link javax.xml.namespace.QName}. */
    public QName getAttributeQNameValue(String uri, String local) throws XMLStreamException;
}
