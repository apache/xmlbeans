/*   Copyright 2026 The Apache Software Foundation
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

import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XsTypeConverterTest {

    // fullwidth "123", arabic-indic "123" and devanagari "123" are unicode
    // decimal digits but outside the xsd lexical space.
    private static final String FULLWIDTH_123 = "１２３";
    private static final String ARABIC_123 = "١٢٣";
    private static final String DEVANAGARI_123 = "१२३";

    @Test
    void lexIntAcceptsAscii() {
        assertEquals(123, XsTypeConverter.lexInt("123"));
        assertEquals(-123, XsTypeConverter.lexInt("-123"));
        assertEquals(123, XsTypeConverter.lexInt("+123"));
    }

    @Test
    void lexIntRejectsNonAsciiDigits() {
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(FULLWIDTH_123));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(ARABIC_123));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(DEVANAGARI_123));
    }

    @Test
    void lexShortRejectsNonAsciiDigits() {
        assertEquals(123, XsTypeConverter.lexShort("123"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexShort(FULLWIDTH_123));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexShort(ARABIC_123));
    }

    @Test
    void lexByteRejectsNonAsciiDigits() {
        assertEquals(123, XsTypeConverter.lexByte("123"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexByte(FULLWIDTH_123));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexByte(ARABIC_123));
    }

    @Test
    void lexFloatRejectsSingleCharSuffix() {
        // a lone "f"/"F" used to read charAt(-1) and throw
        // StringIndexOutOfBoundsException instead of NumberFormatException.
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("f"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("F"));
    }

    @Test
    void lexFloatRejectsJavaFloatSuffix() {
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1.0f"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1.0F"));
    }

    @Test
    void lexFloatAcceptsValidValues() {
        assertEquals(1.0f, XsTypeConverter.lexFloat("1.0"));
        assertEquals(Float.POSITIVE_INFINITY, XsTypeConverter.lexFloat("INF"));
        assertEquals(Float.NEGATIVE_INFINITY, XsTypeConverter.lexFloat("-INF"));
        assertEquals(1500.0f, XsTypeConverter.lexFloat("1.5e3"));
    }

    @Test
    void lexFloatRejectsNonXsdLexicalForms() {
        // hex floats, the java "Infinity" spelling and the double suffix are
        // accepted by Float.parseFloat but are outside the xsd:float lexical space
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("0x1p4"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("Infinity"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("-Infinity"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1.0d"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1D"));
    }

    @Test
    void lexDoubleAcceptsValidValues() {
        assertEquals(1.0, XsTypeConverter.lexDouble("1.0"));
        assertEquals(Double.POSITIVE_INFINITY, XsTypeConverter.lexDouble("INF"));
        assertEquals(Double.NEGATIVE_INFINITY, XsTypeConverter.lexDouble("-INF"));
        assertEquals(1500.0, XsTypeConverter.lexDouble("1.5e3"));
    }

    @Test
    void lexDoubleRejectsNonXsdLexicalForms() {
        // hex floats, the java "Infinity" spelling and the float suffix are
        // accepted by Double.parseDouble but are outside the xsd:double lexical space
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("0x1p4"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("Infinity"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("-Infinity"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("1.0f"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("1F"));
    }

    @Test
    void lexDecimalRejectsEmptyString() {
        // an empty value used to read charAt(-1) in trimTrailingZeros and
        // throw StringIndexOutOfBoundsException instead of NumberFormatException.
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal(""));
    }

    @Test
    void lexDecimalTrimsTrailingZeros() {
        assertEquals(0, new java.math.BigDecimal("1.5").compareTo(XsTypeConverter.lexDecimal("1.500")));
        assertEquals(0, new java.math.BigDecimal("12").compareTo(XsTypeConverter.lexDecimal("12.000")));
    }
}
