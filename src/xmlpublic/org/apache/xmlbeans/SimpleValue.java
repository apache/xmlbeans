/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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

package org.apache.xmlbeans;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * All XmlObject implementations can be coerced to SimpleValue.
 * For any given schema type, only a subset of the conversion
 * methods will work.  Others will throw an exception.
 * <p>
 * SimpleValue is useful for declaring variables which can hold
 * more than one similar schema type that may not happen to
 * have a common XML base type, for example, two list types,
 * or two unrelated integer restrictions that happen to fit
 * into an int.
 */
public interface SimpleValue extends XmlObject
{
    /**
     * The same as getSchemaType unless this is a union instance
     * or nil value.
     * <p>
     * For unions, this returns the non-union consituent type of
     * this instance. This type may change if setters are called
     * that cause the instance to change to another constituent
     * type of the union.
     * <p>
     * For nil values, this returns null.
     */
    SchemaType instanceType();

    /** Returns the value as a {@link String}. */
    String getStringValue();
    /** Returns the value as a boolean. */
    boolean getBooleanValue();
    /** Returns the value as a byte. */
    public byte getByteValue();
    /** Returns the value as a short. */
    public short getShortValue();
    /** Returns the value as an int. */
    public int getIntValue();
    /** Returns the value as a long. */
    public long getLongValue();
    /** Returns the value as a {@link BigInteger}. */
    public BigInteger getBigIntegerValue();
    /** Returns the value as a {@link BigDecimal}. */
    public BigDecimal getBigDecimalValue();
    /** Returns the value as a float. */
    public float getFloatValue();
    /** Returns the value as a double. */
    public double getDoubleValue();
    /** Returns the value as a byte array. */
    byte[] getByteArrayValue();
    /** Returns the value as a {@link StringEnumAbstractBase}. */
    StringEnumAbstractBase getEnumValue();
    /** Returns the value as a {@link Calendar}. */
    Calendar getCalendarValue();
    /** Returns the value as a {@link Date}. */
    Date getDateValue();
    /** Returns the value as a {@link GDate}. */
    GDate getGDateValue();
    /** Returns the value as a {@link GDuration}. */
    GDuration getGDurationValue();
    /** Returns the value as a {@link QName}. */
    QName getQNameValue();
    /** Returns the value as a {@link List} of friendly Java objects (String, Integer, Byte, Short, Long, BigInteger, Decimal, Float, Double, byte[], Calendar, GDuration). */
    List getListValue();
    /** Returns the value as a {@link List} of XmlAnySimpleType objects. */
    List xgetListValue();

    /** Returns a union value as a its natural friendly Java object (String, Integer, Byte, Short, Long, BigInteger, Decimal, Float, Double, byte[], Calendar, GDuration). */
    Object getObjectValue();

    // following are simple type value setters

    /** Sets the value as a {@link String}. */
    void setStringValue(String obj);
    /** Sets the value as a boolean. */
    void setBooleanValue(boolean v);
    /** Sets the value as a byte. */
    void setByteValue(byte v);
    /** Sets the value as a short. */
    void setShortValue(short v);
    /** Sets the value as an int. */
    void setIntValue(int v);
    /** Sets the value as a long. */
    void setLongValue(long v);
    /** Sets the value as a {@link BigInteger}. */
    void setBigIntegerValue(BigInteger obj);
    /** Sets the value as a {@link BigDecimal}. */
    void setBigDecimalValue(BigDecimal obj);
    /** Sets the value as a float. */
    void setFloatValue(float v);
    /** Sets the value as a double. */
    void setDoubleValue(double v);
    /** Sets the value as a byte array. */
    void setByteArrayValue(byte[] obj);
    /** Sets the value as a {@link StringEnumAbstractBase}. */
    void setEnumValue(StringEnumAbstractBase obj);
    /** Sets the value as a {@link Calendar}. */
    void setCalendarValue(Calendar obj);
    /** Sets the value as a {@link Date}. */
    void setDateValue(Date obj);
    /** Sets the value as a {@link GDate}. */
    void setGDateValue(GDate obj);
    /** Sets the value as a {@link GDuration}. */
    void setGDurationValue(GDuration obj);
    /** Sets the value as a {@link QName}. */
    void setQNameValue(QName obj);
    /** Sets the value as a {@link List}. */
    void setListValue(List obj);
    /** Sets the value as an arbitrary {@link Object}. */
    void setObjectValue(Object obj);

    /**
     * Returns the value as a {@link String}. *
     * @deprecated replaced with {@link #getStringValue}
     */
    String stringValue();
    /**
     * Returns the value as a boolean. *
     * @deprecated replaced with {@link #getBooleanValue}
     */
    boolean booleanValue();
    /**
     * Returns the value as a byte. *
     * @deprecated replaced with {@link #getByteValue}
     */
    public byte byteValue();
    /**
     * Returns the value as a short. *
     * @deprecated replaced with {@link #getShortValue}
     */
    public short shortValue();
    /**
     * Returns the value as an int. *
     * @deprecated replaced with {@link #getIntValue}
     */
    public int intValue();
    /**
     * Returns the value as a long. *
     * @deprecated replaced with {@link #getLongValue}
     */
    public long longValue();
    /**
     * Returns the value as a {@link BigInteger}. *
     * @deprecated replaced with {@link #getBigIntegerValue}
     */
    public BigInteger bigIntegerValue();
    /**
     * Returns the value as a {@link BigDecimal}. *
     * @deprecated replaced with {@link #getBigDecimalValue}
     */
    public BigDecimal bigDecimalValue();
    /**
     * Returns the value as a float. *
     * @deprecated replaced with {@link #getFloatValue}
     */
    public float floatValue();
    /**
     * Returns the value as a double. *
     * @deprecated replaced with {@link #getDoubleValue}
     */
    public double doubleValue();
    /**
     * Returns the value as a byte array. *
     * @deprecated replaced with {@link #getByteArrayValue}
     */
    byte[] byteArrayValue();
    /**
     * Returns the value as a {@link StringEnumAbstractBase}. *
     * @deprecated replaced with {@link #getEnumValue}
     */
    StringEnumAbstractBase enumValue();
    /**
     * Returns the value as a {@link Calendar}. *
     * @deprecated replaced with {@link #getCalendarValue}
     */
    Calendar calendarValue();
    /**
     * Returns the value as a {@link Date}. *
     * @deprecated replaced with {@link #getDateValue}
     */
    Date dateValue();
    /**
     * Returns the value as a {@link GDate}. *
     * @deprecated replaced with {@link #getGDateValue}
     */
    GDate gDateValue();
    /**
     * Returns the value as a {@link GDuration}. *
     * @deprecated replaced with {@link #getGDurationValue}
     */
    GDuration gDurationValue();
    /**
     * Returns the value as a {@link QName}. *
     * @deprecated replaced with {@link #getQNameValue}
     */
    QName qNameValue();
    /**
     * Returns the value as a {@link List} of friendly Java objects (String, Integer, Byte, Short, Long, BigInteger, Decimal, Float, Double, byte[], Calendar, GDuration). *
     * @deprecated replaced with {@link #getListValue}
     */
    List listValue();
    /**
     * Returns the value as a {@link List} of XmlAnySimpleType objects. *
     * @deprecated replaced with {@link #getListValue}
     */
    List xlistValue();
    
    /**
     * Returns a union value as a its natural friendly Java object (String, Integer, Byte, Short, Long, BigInteger, Decimal, Float, Double, byte[], Calendar, GDuration). *
     * @deprecated replaced with {@link #getObjectValue}
     */
    Object objectValue();

    // following are simple type value setters

    /**
     * Sets the value as a {@link String}. *
     * @deprecated replaced with {@link #setStringValue}
     */
    void set(String obj);
    /**
     * Sets the value as a boolean. *
     * @deprecated replaced with {@link #setBooleanValue}
     */
    void set(boolean v);
    /**
     * Sets the value as a byte.
     * @deprecated replaced with {@link #setByteValue}
     **/
    void set(byte v);
    /**
     * Sets the value as a short.
     * @deprecated replaced with {@link #setShortValue}
     **/
    void set(short v);
    /**
     * Sets the value as an int.
     * @deprecated replaced with {@link #setIntValue}
     **/
    void set(int v);
    /**
     * Sets the value as a long.
     * @deprecated replaced with {@link #setLongValue}
     **/
    void set(long v);
    /**
     * Sets the value as a {@link BigInteger}.
     * @deprecated replaced with {@link #setBigIntegerValue}
     **/
    void set(BigInteger obj);
    /**
     * Sets the value as a {@link BigDecimal}
     * @deprecated replaced with {@link #setBigDecimalValue}
     **/
    void set(BigDecimal obj);
    /**
     * Sets the value as a float.
     * @deprecated replaced with {@link #setFloatValue}
     **/
    void set(float v);
    /**
     * Sets the value as a double.
     * @deprecated replaced with {@link #setDoubleValue}
     **/
    void set(double v);
    /**
     * Sets the value as a byte array.
     * @deprecated replaced with {@link #setByteArrayValue}
     **/
    void set(byte[] obj);
    /**
     * Sets the value as a {@link StringEnumAbstractBase}.
     * @deprecated replaced with {@link #setEnumValue}
     **/
    void set(StringEnumAbstractBase obj);
    /**
     * Sets the value as a {@link Calendar}.
     * @deprecated replaced with {@link #setCalendarValue}
     **/
    void set(Calendar obj);
    /**
     * Sets the value as a {@link Date}.
     * @deprecated replaced with {@link #setDateValue}
     **/
    void set(Date obj);
    /**
     * Sets the value as a {@link GDate}.
     * @deprecated replaced with {@link #setGDateValue}
     **/
    void set(GDateSpecification obj);
    /**
     * Sets the value as a {@link GDuration}.
     * @deprecated replaced with {@link #setGDurationValue}
     **/
    void set(GDurationSpecification obj);
    /**
     * Sets the value as a {@link QName}.
     * @deprecated replaced with {@link #setQNameValue}
     **/
    void set(QName obj);
    /**
     * Sets the value as a {@link List}.
     * @deprecated replaced with {@link #setListValue}
     **/
    void set(List obj);
    /**
     * Sets the value as an arbitrary {@link Object}.
     * @deprecated replaced with {@link #setObjectValue}
     **/
    void objectSet(Object obj);
}
