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
package misc.checkin;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * XmlOptions.setMaxNumberOfCharsForNumbers has to apply to values materialised from the
 * store, not just to the explicit XmlObject.validate(options) path.
 */
public class MaxNumberOfCharsTest {
    /** a number of exactly {@code n} characters */
    private static String digits(int n) {
        StringBuilder sb = new StringBuilder(n);
        sb.append('1');
        for (int i = 1; i < n; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    private static String frag(String value) {
        return "<xml-fragment>" + value + "</xml-fragment>";
    }

    private static XmlOptions maxChars(int max) {
        XmlOptions options = new XmlOptions();
        options.setMaxNumberOfCharsForNumbers(max);
        return options;
    }

    @Test
    public void testDecimalHonoursRaisedLimit() throws XmlException {
        XmlDecimal value = XmlDecimal.Factory.parse(frag(digits(2000)), maxChars(4096));
        assertEquals(2000, value.getBigDecimalValue().precision());
    }

    @Test
    public void testDecimalHonoursLoweredLimit() throws XmlException {
        XmlDecimal value = XmlDecimal.Factory.parse(frag("123456789012"), maxChars(8));
        assertThrows(XmlValueOutOfRangeException.class, value::getBigDecimalValue);
    }

    @Test
    public void testDecimalUsesDefaultLimitWhenUnset() throws XmlException {
        XmlDecimal withinDefault = XmlDecimal.Factory.parse(frag(digits(1000)));
        assertEquals(1000, withinDefault.getBigDecimalValue().precision());

        XmlDecimal overDefault = XmlDecimal.Factory.parse(frag(digits(2000)));
        assertThrows(XmlValueOutOfRangeException.class, overDefault::getBigDecimalValue);
    }

    @Test
    public void testIntegerHonoursRaisedLimit() throws XmlException {
        XmlInteger value = XmlInteger.Factory.parse(frag(digits(2000)), maxChars(4096));
        assertEquals(new java.math.BigInteger(digits(2000)), value.getBigIntegerValue());
    }

    @Test
    public void testIntegerUsesDefaultLimitWhenUnset() throws XmlException {
        XmlInteger overDefault = XmlInteger.Factory.parse(frag(digits(2000)));
        assertThrows(XmlValueOutOfRangeException.class, overDefault::getBigIntegerValue);
    }

    @Test
    public void testDecimalToBigIntegerHonoursRaisedLimit() throws XmlException {
        // getBigIntegerValue() over a decimal goes through MathUtil.toBigInteger
        SimpleValue value = (SimpleValue) XmlDecimal.Factory.parse(frag(digits(2000)), maxChars(4096));
        assertEquals(new java.math.BigInteger(digits(2000)), value.getBigIntegerValue());
    }

    @Test
    public void testDecimalToBigIntegerUsesDefaultLimitWhenUnset() throws XmlException {
        SimpleValue value = (SimpleValue) XmlDecimal.Factory.parse(frag(digits(2000)));
        assertThrows(XmlValueOutOfRangeException.class, value::getBigIntegerValue);
    }

    @Test
    public void testDecimalToBigIntegerReportsOutOfRangeWhenSetProgrammatically() {
        // setBigDecimalValue() bypasses the lexical path, so the limit is only applied
        // on the way out - and has to be reported the same way it is on the way in
        XmlDecimal value = XmlDecimal.Factory.newInstance();
        value.setBigDecimalValue(new BigDecimal(digits(2000)));
        assertThrows(XmlValueOutOfRangeException.class,
            () -> ((SimpleValue) value).getBigIntegerValue());
    }

    @Test
    public void testIntegralSettersReportOutOfRange() {
        BigDecimal oversized = new BigDecimal(digits(2000));

        assertThrows(XmlValueOutOfRangeException.class,
            () -> XmlInt.Factory.newInstance().setBigDecimalValue(oversized));
        assertThrows(XmlValueOutOfRangeException.class,
            () -> XmlLong.Factory.newInstance().setBigDecimalValue(oversized));
        assertThrows(XmlValueOutOfRangeException.class,
            () -> XmlInteger.Factory.newInstance().setBigDecimalValue(oversized));

        // a value that merely overflows the java type is unaffected
        assertThrows(XmlValueOutOfRangeException.class,
            () -> XmlInt.Factory.newInstance().setBigDecimalValue(new BigDecimal("1E+20")));
    }

    @Test
    public void testDecimalHashCodeIgnoresLimit() {
        // hashCode() must not throw, whatever the limit is, and must stay aligned with
        // the hash of the same value held as an xsd:integer
        XmlDecimal decimal = XmlDecimal.Factory.newInstance();
        decimal.setBigDecimalValue(new BigDecimal(digits(2000)));
        XmlInteger integer = XmlInteger.Factory.newInstance();
        integer.setBigIntegerValue(new BigInteger(digits(2000)));

        assertEquals(integer.valueHashCode(), decimal.valueHashCode());
    }

    @Test
    public void testDecimalHashCodeIsIndependentOfScale() {
        // 1E+200000 and the same value written out in full are equal, and are on either
        // side of the threshold at which hashing stops expanding the value
        XmlDecimal exponent = XmlDecimal.Factory.newInstance();
        exponent.setBigDecimalValue(new BigDecimal("1E+200000"));
        XmlDecimal expanded = XmlDecimal.Factory.newInstance();
        expanded.setBigDecimalValue(new BigDecimal(digits(200001)));

        assertEquals(true, exponent.valueEquals(expanded));
        assertEquals(expanded.valueHashCode(), exponent.valueHashCode());
    }

    @Test
    public void testDecimalHashCodeDoesNotExpandHugeExponent() {
        // expanding 1E+2000000000 would need gigabytes; hashing must not attempt it
        XmlDecimal value = XmlDecimal.Factory.newInstance();
        value.setBigDecimalValue(new BigDecimal("1E+2000000000"));
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(10), value::valueHashCode);
    }
}
