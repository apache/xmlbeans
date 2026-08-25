/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.xmlbeans.impl.util;

import org.junit.jupiter.api.Test;

import org.apache.xmlbeans.XmlOptions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class TestMathUtil {
    @Test
    public void testToBigInteger() {
        BigInteger expected = new BigInteger("1234567890");
        assertEquals(expected, MathUtil.toBigInteger(new BigDecimal(expected)));
    }

    @Test
    public void testToBigIntegerBigExponent() {
        BigDecimal expected = new BigDecimal("1E+2000");
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toBigInteger(expected));
    }

    @Test
    public void testToIntWithValueOutOfRange() {
        BigDecimal expected = BigDecimal.valueOf(Long.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toInt(expected));
        BigDecimal expected2 = BigDecimal.valueOf(Long.MIN_VALUE);
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toInt(expected2));
    }

    @Test
    public void testToLongWithValueOutOfRange() {
        BigDecimal expected = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toLong(expected));
        BigDecimal expected2 = BigDecimal.valueOf(Long.MIN_VALUE).subtract(BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toLong(expected2));
    }

    @Test
    public void testParseAsBigIntegerWithMaxNumberOfChars() {
        assertEquals(new BigInteger("12345"), MathUtil.parseAsBigInteger("12345", 5));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.parseAsBigInteger("12345", 4));
        // the default overload still applies DEFAULT_MAX_NUMBER_CHARS
        String tooLong = repeat('1', XmlOptions.DEFAULT_MAX_NUMBER_CHARS + 1);
        assertThrows(IllegalArgumentException.class, () -> MathUtil.parseAsBigInteger(tooLong));
        assertEquals(new BigInteger(tooLong), MathUtil.parseAsBigInteger(tooLong, tooLong.length()));
    }

    @Test
    public void testParseAsLongWithMaxNumberOfChars() {
        assertEquals(12345L, MathUtil.parseAsLong("12345", 5));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.parseAsLong("12345", 4));
    }

    @Test
    public void testToBigIntegerWithMaxNumberOfChars() {
        BigDecimal value = new BigDecimal("1E+2000");
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toBigInteger(value));
        assertEquals(new BigDecimal("1E+2000").toBigInteger(), MathUtil.toBigInteger(value, 4096));
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Test
    public void testToBigIntegerNull() {
        assertThrows(NullPointerException.class, () -> MathUtil.toBigInteger(null));
        assertThrows(NullPointerException.class, () -> MathUtil.toLong(null));
        assertThrows(NullPointerException.class, () -> MathUtil.toInt(null));
    }

    @Test
    public void testToBigIntegerSmallValuesTruncateToZero() {
        assertEquals(BigInteger.ZERO, MathUtil.toBigInteger(new BigDecimal("0.5")));
        assertEquals(BigInteger.ZERO, MathUtil.toBigInteger(new BigDecimal("-0.999")));
        assertEquals(BigInteger.ZERO, MathUtil.toBigInteger(BigDecimal.ZERO));
        assertEquals(BigInteger.ONE, MathUtil.toBigInteger(new BigDecimal("1.5")));
    }

    @Test
    public void testToBigIntegerSmallExponent() {
        // BigDecimal.toBigInteger() would compute 10^10000000 here, taking seconds
        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                assertEquals(BigInteger.ZERO, MathUtil.toBigInteger(new BigDecimal("1E-10000000"))));
    }

    @Test
    public void testToBigIntegerMaxNegativeScale() {
        BigDecimal value = new BigDecimal("1E+2147483647");
        assertThrows(IllegalArgumentException.class, () -> MathUtil.toBigInteger(value));
    }

    @Test
    public void testSafeFloatToInt() {
        assertEquals(1, MathUtil.safeFloatToInt(1.75f));
        assertEquals(-1, MathUtil.safeFloatToInt(-1.75f));
        assertEquals(Integer.MIN_VALUE, MathUtil.safeFloatToInt(Integer.MIN_VALUE));
    }

    @Test
    public void testSafeFloatToIntWithValueOutOfRange() {
        // 2^31 is the nearest float above Integer.MAX_VALUE and must not be accepted
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeFloatToInt(2147483648f));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeFloatToInt(-2147483904f));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeFloatToInt(Float.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeFloatToInt(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeFloatToInt(Float.POSITIVE_INFINITY));
    }

    @Test
    public void testSafeDoubleToInt() {
        assertEquals(1, MathUtil.safeDoubleToInt(1.75));
        assertEquals(Integer.MAX_VALUE, MathUtil.safeDoubleToInt(Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, MathUtil.safeDoubleToInt(Integer.MIN_VALUE));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeDoubleToInt(2147483648d));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeDoubleToInt(-2147483649d));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeDoubleToInt(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> MathUtil.safeDoubleToInt(Double.NEGATIVE_INFINITY));
    }
}
