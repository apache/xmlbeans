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

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
