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

import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
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
        assertEquals(123, XsTypeConverter.lexInt("00123"));
        assertEquals(-123, XsTypeConverter.lexInt("-123"));
        assertEquals(123, XsTypeConverter.lexInt("+123"));
        assertEquals(Integer.MAX_VALUE, XsTypeConverter.lexInt(Integer.toString(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, XsTypeConverter.lexInt(Integer.toString(Integer.MIN_VALUE)));
    }

    @Test
    void lexLongAcceptsAscii() {
        assertEquals(123L, XsTypeConverter.lexLong("123"));
        assertEquals(123L, XsTypeConverter.lexLong("00123"));
        assertEquals(-123L, XsTypeConverter.lexLong("-123"));
        assertEquals(123L, XsTypeConverter.lexLong("+123"));
        assertEquals(Long.MAX_VALUE, XsTypeConverter.lexLong(Long.toString(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, XsTypeConverter.lexLong(Long.toString(Long.MIN_VALUE)));
    }

    @Test
    void lexShortAcceptsAscii() {
        assertEquals(123, XsTypeConverter.lexShort("123"));
        assertEquals(123, XsTypeConverter.lexShort("00123"));
        assertEquals(-123, XsTypeConverter.lexShort("-123"));
        assertEquals(123, XsTypeConverter.lexShort("+123"));
        assertEquals(Short.MAX_VALUE, XsTypeConverter.lexShort(Short.toString(Short.MAX_VALUE)));
        assertEquals(Short.MIN_VALUE, XsTypeConverter.lexShort(Short.toString(Short.MIN_VALUE)));
    }

    @Test
    void lexIntRejectsNonAsciiDigits() {
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(FULLWIDTH_123));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(ARABIC_123));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(DEVANAGARI_123));
    }

    @Test
    void lexIntRejectsEmptyOrNull() {
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(null));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInt(""));
    }

    @Test
    void lexShortRejectsNonAsciiDigits() {
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
    void lexFloatLenientAcceptsJavaForms() {
        // the default stays lenient: hex floats and the java "Infinity" spelling
        // are accepted by Float.parseFloat, so lexFloat keeps accepting them
        assertEquals(16.0f, XsTypeConverter.lexFloat("0x1p4"));
        assertEquals(Float.POSITIVE_INFINITY, XsTypeConverter.lexFloat("Infinity"));
    }

    @Test
    void lexFloatStrictRejectsNonXsdLexicalForms() {
        final int maxNums = XmlOptions.DEFAULT_MAX_NUMBER_CHARS;
        // hex floats, the java "Infinity" spelling and the f/F/d/D suffix are
        // accepted by Float.parseFloat but are outside the xsd:float lexical space
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("0x1p4", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("Infinity", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("-Infinity", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1.0d", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1D", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexFloat("1.0f", true, maxNums));
    }

    @Test
    void lexFloatStrictAcceptsValidValues() {
        final int maxNums = XmlOptions.DEFAULT_MAX_NUMBER_CHARS;
        assertEquals(1.0f, XsTypeConverter.lexFloat("1.0", true, maxNums));
        assertEquals(1.0f, XsTypeConverter.lexFloat("01.0", true, maxNums));
        assertEquals(1500.0f, XsTypeConverter.lexFloat("1.5e3", true, maxNums));
        assertEquals(Float.POSITIVE_INFINITY, XsTypeConverter.lexFloat("INF", true, maxNums));
        assertEquals(Float.NEGATIVE_INFINITY, XsTypeConverter.lexFloat("-INF", true, maxNums));
        assertEquals(Float.NaN, XsTypeConverter.lexFloat("NaN", true, maxNums));
    }

    @Test
    void lexDoubleAcceptsValidValues() {
        assertEquals(1.0, XsTypeConverter.lexDouble("1.0"));
        assertEquals(1.0, XsTypeConverter.lexDouble("01.0"));
        assertEquals(Double.POSITIVE_INFINITY, XsTypeConverter.lexDouble("INF"));
        assertEquals(Double.NEGATIVE_INFINITY, XsTypeConverter.lexDouble("-INF"));
        assertEquals(1500.0, XsTypeConverter.lexDouble("1.5e3"));
    }

    @Test
    void lexDoubleLenientAcceptsJavaForms() {
        assertEquals(16.0, XsTypeConverter.lexDouble("0x1p4"));
        assertEquals(Double.POSITIVE_INFINITY, XsTypeConverter.lexDouble("Infinity"));
    }

    @Test
    void lexDoubleStrictRejectsNonXsdLexicalForms() {
        final int maxNums = XmlOptions.DEFAULT_MAX_NUMBER_CHARS;
        // hex floats, the java "Infinity" spelling and the f/F/d/D suffix are
        // accepted by Double.parseDouble but are outside the xsd:double lexical space
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("0x1p4", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("Infinity", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("-Infinity", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("1.0f", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("1F", true, maxNums));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDouble("1.0d", true, maxNums));
    }

    @Test
    void lexDoubleStrictAcceptsValidValues() {
        final int maxNums = XmlOptions.DEFAULT_MAX_NUMBER_CHARS;
        assertEquals(1.0, XsTypeConverter.lexDouble("1.0", true, maxNums));
        assertEquals(1.0, XsTypeConverter.lexDouble("01.0", true, maxNums));
        assertEquals(1500.0, XsTypeConverter.lexDouble("1.5e3", true, maxNums));
        assertEquals(Double.POSITIVE_INFINITY, XsTypeConverter.lexDouble("INF", true, maxNums));
        assertEquals(Double.NEGATIVE_INFINITY, XsTypeConverter.lexDouble("-INF", true, maxNums));
        assertEquals(Double.NaN, XsTypeConverter.lexDouble("NaN", true, maxNums));
    }

    @Test
    void loadStrictFloatingPointOptionGatesFloatParsing() throws Exception {
        // default load is lenient: the hex float parses as it always has
        assertEquals(16.0f, XmlFloat.Factory.parse("<xml-fragment>0x1p4</xml-fragment>").getFloatValue());

        // with the option set, the value is out of the xsd:float lexical space
        XmlOptions strict = new XmlOptions().setLoadStrictFloatingPoint();
        assertThrows(XmlValueOutOfRangeException.class, () ->
            XmlFloat.Factory.parse("<xml-fragment>0x1p4</xml-fragment>", strict).getFloatValue());
    }

    @Test
    void loadStrictFloatingPointOptionGatesDoubleParsing() throws Exception {
        assertEquals(16.0, XmlDouble.Factory.parse("<xml-fragment>0x1p4</xml-fragment>").getDoubleValue());

        XmlOptions strict = new XmlOptions().setLoadStrictFloatingPoint();
        assertThrows(XmlValueOutOfRangeException.class, () ->
            XmlDouble.Factory.parse("<xml-fragment>0x1p4</xml-fragment>", strict).getDoubleValue());
    }

    @Test
    void loadAllowDecimalExponentOptionGatesDecimalParsing() throws Exception {
        // an exponent is outside the xsd:decimal lexical space, so validation
        // rejects "1E5" by default
        XmlOptions validate = new XmlOptions().setValidateOnSet();
        assertThrows(XmlValueOutOfRangeException.class, () ->
            XmlDecimal.Factory.parse("<xml-fragment>1E5</xml-fragment>", validate).getBigDecimalValue());

        // setLoadAllowDecimalExponent restores the lenient behaviour
        XmlOptions lenient = new XmlOptions().setValidateOnSet().setLoadAllowDecimalExponent();
        assertEquals(0, new java.math.BigDecimal("100000").compareTo(
            XmlDecimal.Factory.parse("<xml-fragment>1E5</xml-fragment>", lenient).getBigDecimalValue()));
    }

    @Test
    void lexLongRejectsDoubleSign() {
        // trimInitialPlus drops the leading '+', then Long.parseLong accepts its
        // own sign, so "++5"/"+-5" used to parse as 5/-5 instead of being rejected.
        assertEquals(5L, XsTypeConverter.lexLong("+5"));
        assertEquals(-5L, XsTypeConverter.lexLong("-5"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexLong("++5"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexLong("+-5"));
    }

    @Test
    void lexLongRejectsEmptyOrNull() {
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexLong(null));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexLong(""));
    }

    @Test
    void lexIntegerRejectsDoubleSign() {
        // "+-5" was already caught; "++5" leaked through as 5.
        assertEquals(java.math.BigInteger.valueOf(5), XsTypeConverter.lexInteger("+5"));
        assertEquals(java.math.BigInteger.valueOf(-5), XsTypeConverter.lexInteger("-5"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInteger("++5"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInteger("+-5"));
    }

    @Test
    void lexDecimalRejectsEmptyString() {
        // an empty value used to read charAt(-1) in trimTrailingZeros and
        // throw StringIndexOutOfBoundsException instead of NumberFormatException.
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal(""));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal(null));
    }

    @Test
    void lexDecimalTrimsTrailingZeros() {
        assertEquals(0, new java.math.BigDecimal("1.5").compareTo(XsTypeConverter.lexDecimal("1.500")));
        assertEquals(0, new java.math.BigDecimal("12").compareTo(XsTypeConverter.lexDecimal("12.000")));
    }

    @Test
    void lexDecimalRejectsExponent() {
        // BigDecimal accepts scientific notation, but the xsd:decimal lexical
        // space has no exponent - "1E5" used to parse to 100000 instead of
        // being rejected as invalid. This is the default (allowExponent = false).
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal("1E5"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal("1.5e3"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal("-2E-3"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexDecimal("1E5", false));
        // plain decimals stay valid
        assertEquals(0, new java.math.BigDecimal("1.5").compareTo(XsTypeConverter.lexDecimal("1.5")));
    }

    @Test
    void lexDecimalAllowsExponentWhenRequested() {
        // XmlOptions.setLoadAllowDecimalExponent restores the lenient behaviour:
        // the exponent form is accepted again (e.g. "1E5" -> 100000).
        assertEquals(0, new java.math.BigDecimal("100000").compareTo(XsTypeConverter.lexDecimal("1E5", true)));
        assertEquals(0, new java.math.BigDecimal("1500").compareTo(XsTypeConverter.lexDecimal("1.5e3", true)));
        // plain decimals are unaffected by the flag
        assertEquals(0, new java.math.BigDecimal("1.5").compareTo(XsTypeConverter.lexDecimal("1.5", true)));
    }

    @Test
    void lexIntegerRejectsExponent() {
        // xs:integer is parsed with BigInteger, which never accepts an exponent,
        // so the exponent form is already rejected for integer types.
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInteger("1E5"));
        assertThrows(NumberFormatException.class, () -> XsTypeConverter.lexInteger("1e5"));
    }

    @Test
    void lexQNameCollectsErrorForColonlessInvalidValue() {
        // The error-collecting overload must record a lexical error and return a
        // best-effort QName, never throw. A colon-less value that is not a valid
        // NCName ("a b", "") has no ':' so the recovery indexOf returns -1.
        javax.xml.namespace.NamespaceContext ctx = new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) { return ""; }
            public String getPrefix(String uri) { return ""; }
            public java.util.Iterator<String> getPrefixes(String uri) {
                return java.util.Collections.<String>emptyList().iterator();
            }
        };

        java.util.List<org.apache.xmlbeans.XmlError> errors = new java.util.ArrayList<>();
        javax.xml.namespace.QName q = XsTypeConverter.lexQName("a b", errors, ctx);
        assertEquals("a b", q.getLocalPart());
        assertEquals(1, errors.size());

        errors.clear();
        javax.xml.namespace.QName empty = XsTypeConverter.lexQName("", errors, ctx);
        assertEquals("", empty.getLocalPart());
        assertEquals(1, errors.size());
    }
}
