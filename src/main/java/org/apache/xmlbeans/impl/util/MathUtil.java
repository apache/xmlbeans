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

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.apache.xmlbeans.XmlOptions.DEFAULT_MAX_NUMBER_CHARS;

/**
 * Internal Use Only. Utility methods for dealing with conversions
 */
public class MathUtil {
    private MathUtil() {}

    public static int safeFloatToInt(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("Cannot convert NaN to int");
        }
        if (Float.isInfinite(f)) {
            throw new IllegalArgumentException("Cannot convert infinity to int");
        }
        if (f > Integer.MAX_VALUE || f < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value out of range: " + f);
        }
        return (int) f;
    }

    public static int safeDoubleToInt(double d) {
        if (Double.isNaN(d)) {
            throw new IllegalArgumentException("Cannot convert NaN to int");
        }
        if (Double.isInfinite(d)) {
            throw new IllegalArgumentException("Cannot convert infinity to int");
        }
        if (d > Integer.MAX_VALUE || d < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value out of range: " + d);
        }
        return (int) d;
    }

    /**
     * @param s string to parse
     * @return valid BigDecimal
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static BigDecimal parseAsBigDecimal(String s) {
        return parseAsBigDecimal(s, DEFAULT_MAX_NUMBER_CHARS);
    }

    /**
     * @param s string to parse
     * @param maxNumberOfChars maximum number of characters allowed in the string
     * @return valid BigDecimal
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static BigDecimal parseAsBigDecimal(String s, int maxNumberOfChars) {
        if (s == null) {
            throw new NullPointerException("Cannot parse null as BigDecimal");
        }
        if (s.length() > maxNumberOfChars) {
            throw new IllegalArgumentException("Number has more than " + maxNumberOfChars + " characters");
        }
        return new BigDecimal(s);
    }

    /**
     * @param s string to parse
     * @return valid BigInteger
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static BigInteger parseAsBigInteger(String s) {
        if (s == null) {
            throw new NullPointerException("Cannot parse null as BigInteger");
        }
        if (s.length() > DEFAULT_MAX_NUMBER_CHARS) {
            throw new IllegalArgumentException("Number has more than " + DEFAULT_MAX_NUMBER_CHARS + " characters");
        }
        return new BigInteger(s);
    }

    /**
     * @param s string to parse
     * @return valid Float
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static float parseAsFloat(String s) {
        return parseAsFloat(s, DEFAULT_MAX_NUMBER_CHARS);
    }

    /**
     * @param s string to parse
     * @param maxNumberOfChars maximum number of characters allowed in the string
     * @return valid float
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static float parseAsFloat(String s, int maxNumberOfChars) {
        if (s == null) {
            throw new NullPointerException("Cannot parse null as Float");
        }
        if (s.length() > maxNumberOfChars) {
            throw new IllegalArgumentException("Number has more than " + maxNumberOfChars + " characters");
        }
        return Float.parseFloat(s);
    }

    /**
     * @param s string to parse
     * @return valid float
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static double parseAsDouble(String s) {
        return parseAsDouble(s, DEFAULT_MAX_NUMBER_CHARS);
    }

    /**
     * @param s string to parse
     * @param maxNumberOfChars maximum number of characters allowed in the string
     * @return valid float
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static double parseAsDouble(String s, int maxNumberOfChars) {
        if (s == null) {
            throw new NullPointerException("Cannot parse null as Double");
        }
        if (s.length() > maxNumberOfChars) {
            throw new IllegalArgumentException("Number has more than " + maxNumberOfChars + " characters");
        }
        return Double.parseDouble(s);
    }

    /**
     * @param s string to parse
     * @return valid long
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static long parseAsLong(String s) {
        if (s == null) {
            throw new NullPointerException("Cannot parse null as Long");
        }
        if (s.length() > DEFAULT_MAX_NUMBER_CHARS) {
            throw new IllegalArgumentException("Number has more than " + DEFAULT_MAX_NUMBER_CHARS + " characters");
        }
        return Long.parseLong(s);
    }

    /**
     * @param s string to parse
     * @return valid int
     * @throws NumberFormatException if parse fails
     * @throws IllegalArgumentException if string is too long
     * @throws NullPointerException if string is null
     */
    public static int parseAsInt(String s) {
        if (s == null) {
            throw new NullPointerException("Cannot parse null as Integer");
        }
        if (s.length() > DEFAULT_MAX_NUMBER_CHARS) {
            throw new IllegalArgumentException("Number has more than " + DEFAULT_MAX_NUMBER_CHARS + " characters");
        }
        return Integer.parseInt(s);
    }

    /**
     * @param value BigDecimal to convert
     * @return valid BigInteger
     * @throws IllegalArgumentException if the input has an absolute exponent that is too large to safely convert
     * @throws NullPointerException if value is null
     */
    public static BigInteger toBigInteger(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        int integerDigits = normalized.precision() - normalized.scale();
        if (integerDigits > DEFAULT_MAX_NUMBER_CHARS || normalized.scale() < -DEFAULT_MAX_NUMBER_CHARS) {
            throw new IllegalArgumentException(
                    "BigDecimal magnitude too large to convert safely: approx "
                            + integerDigits + " integer digits (limit " + DEFAULT_MAX_NUMBER_CHARS + ")");
        }
        return normalized.toBigInteger();
    }

    /**
     * @param value BigDecimal to convert
     * @return valid long
     * @throws IllegalArgumentException if the input has an absolute exponent that is too large to safely convert
     * or if the value is out of long range
     * @throws NullPointerException if value is null
     */
    public static long toLong(BigDecimal value) {
        final BigInteger bigInt = toBigInteger(value);
        final long lv = bigInt.longValue();
        if (BigInteger.valueOf(lv).equals(bigInt)) {
            return lv;
        } else {
            throw new IllegalArgumentException("Value can't be converted to long");
        }
    }

    /**
     * @param value BigDecimal to convert
     * @return valid int
     * @throws IllegalArgumentException if the input has an absolute exponent that is too large to safely convert
     * or if the value is out of int range
     * @throws NullPointerException if value is null
     */
    public static int toInt(BigDecimal value) {
        BigInteger bigInt = toBigInteger(value);
        final int iv = bigInt.intValue();
        if (BigInteger.valueOf(iv).equals(bigInt)) {
            return iv;
        } else {
            throw new IllegalArgumentException("Value can't be converted to int");
        }
    }

}
