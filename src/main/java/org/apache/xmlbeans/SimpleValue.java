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

package org.apache.xmlbeans;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
public interface SimpleValue extends XmlObject {
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

    /**
     * Returns the value as a {@link String}.
     */
    String getStringValue();

    /**
     * Returns the value as a boolean.
     */
    boolean getBooleanValue();

    /**
     * Returns the value as a byte.
     */
    byte getByteValue();

    /**
     * Returns the value as a short.
     */
    short getShortValue();

    /**
     * Returns the value as an int.
     */
    int getIntValue();

    /**
     * Returns the value as a long.
     */
    long getLongValue();

    /**
     * Returns the value as a {@link BigInteger}.
     */
    BigInteger getBigIntegerValue();

    /**
     * Returns the value as a {@link BigDecimal}.
     */
    BigDecimal getBigDecimalValue();

    /**
     * Returns the value as a float.
     */
    float getFloatValue();

    /**
     * Returns the value as a double.
     */
    double getDoubleValue();

    /**
     * Returns the value as a byte array.
     */
    byte[] getByteArrayValue();

    /**
     * Returns the value as a {@link StringEnumAbstractBase}.
     */
    StringEnumAbstractBase getEnumValue();

    /**
     * Returns the value as a {@link Calendar}.
     */
    Calendar getCalendarValue();

    /**
     * Returns the value as a {@link Date}.
     */
    Date getDateValue();

    /**
     * Returns the value as a {@link GDate}.
     */
    GDate getGDateValue();

    /**
     * Returns the value as a {@link GDuration}.
     */
    GDuration getGDurationValue();

    /**
     * Returns the value as a {@link QName}.
     */
    QName getQNameValue();

    /**
     * Returns the value as a {@link List} of friendly Java objects (String, Integer, Byte, Short, Long, BigInteger, Decimal, Float, Double, byte[], Calendar, GDuration).
     */
    List<?> getListValue();

    /**
     * Returns the value as a {@link List} of XmlAnySimpleType objects.
     */
    List<? extends XmlAnySimpleType> xgetListValue();

    /**
     * Returns a union value as a its natural friendly Java object (String, Integer, Byte, Short, Long, BigInteger, Decimal, Float, Double, byte[], Calendar, GDuration).
     */
    Object getObjectValue();

    // following are simple type value setters

    /**
     * Sets the value as a {@link String}.
     */
    void setStringValue(String obj);

    /**
     * Sets the value as a boolean.
     */
    void setBooleanValue(boolean v);

    /**
     * Sets the value as a byte.
     */
    void setByteValue(byte v);

    /**
     * Sets the value as a short.
     */
    void setShortValue(short v);

    /**
     * Sets the value as an int.
     */
    void setIntValue(int v);

    /**
     * Sets the value as a long.
     */
    void setLongValue(long v);

    /**
     * Sets the value as a {@link BigInteger}.
     */
    void setBigIntegerValue(BigInteger obj);

    /**
     * Sets the value as a {@link BigDecimal}.
     */
    void setBigDecimalValue(BigDecimal obj);

    /**
     * Sets the value as a float.
     */
    void setFloatValue(float v);

    /**
     * Sets the value as a double.
     */
    void setDoubleValue(double v);

    /**
     * Sets the value as a byte array.
     */
    void setByteArrayValue(byte[] obj);

    /**
     * Sets the value as a {@link StringEnumAbstractBase}.
     */
    void setEnumValue(StringEnumAbstractBase obj);

    /**
     * Sets the value as a {@link Calendar}.
     */
    void setCalendarValue(Calendar obj);

    /**
     * Sets the value as a {@link Date}.
     */
    void setDateValue(Date obj);

    /**
     * Sets the value as a {@link GDate}.
     */
    void setGDateValue(GDate obj);

    /**
     * Sets the value as a {@link GDuration}.
     */
    void setGDurationValue(GDuration obj);

    /**
     * Sets the value as a {@link QName}.
     */
    void setQNameValue(QName obj);

    /**
     * Sets the value as a {@link List}.
     */
    void setListValue(List<?> obj);

    /**
     * Sets the value as an arbitrary {@link Object}.
     */
    void setObjectValue(Object obj);
}
