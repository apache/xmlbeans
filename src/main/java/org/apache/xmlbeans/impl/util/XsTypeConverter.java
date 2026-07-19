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

package org.apache.xmlbeans.impl.util;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.common.XMLChar;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public final class XsTypeConverter {
    private static final String POS_INF_LEX = "INF";
    private static final String NEG_INF_LEX = "-INF";
    private static final String NAN_LEX = "NaN";

    private static final char NAMESPACE_SEP = ':';
    private static final String EMPTY_PREFIX = "";

    // See Section 2.4.3 of FRC2396  http://www.ietf.org/rfc/rfc2396.txt
    private static final String[] URI_CHARS_TO_BE_REPLACED = {" ", "{", "}", "|", "\\", "^", "[", "]", "`"};
    private static final String[] URI_CHARS_REPLACED_WITH = {"%20", "%7b", "%7d", "%7c", "%5c", "%5e", "%5b", "%5d", "%60"};

    // Float.parseFloat / Double.parseDouble accept lexical forms that are not
    // in the XSD float/double value space: hexadecimal floats (0x1p4), the Java
    // "Infinity" token, and a trailing type suffix (f/F/d/D). XSD only allows a
    // decimal number with an optional exponent, or the special values INF, -INF
    // and NaN. This is only applied when strict floating point parsing is
    // requested (XmlOptions.setLoadStrictFloatingPoint); the default stays lenient.
    private static void checkFloatingPointLexical(CharSequence cs) {
        final int len = cs.length();
        for (int i = 0; i < len; i++) {
            switch (cs.charAt(i)) {
                case 'x':
                case 'X':
                case 'p':
                case 'P':
                case 'i':
                case 't':
                case 'y':
                    throw new NumberFormatException("invalid char '" + cs.charAt(i) + "' in floating point value");
                default:
                    break;
            }
        }
        if (len > 0) {
            final char last = cs.charAt(len - 1);
            // a trailing 'F' is only valid as the last char of "INF"
            if (last == 'd' || last == 'D' ||
                ((last == 'f' || last == 'F') && (len < 2 || cs.charAt(len - 2) != 'N'))) {
                throw new NumberFormatException("invalid trailing char '" + last + "' in floating point value");
            }
        }
    }

    // ======================== float ========================
    public static float lexFloat(CharSequence cs)
        throws NumberFormatException {
        return lexFloat(cs, false, XmlOptions.DEFAULT_MAX_NUMBER_CHARS);
    }

    /**
     * Parses an xsd:float lexical value.
     *
     * @param cs     the lexical value
     * @param strict when {@code true}, lexical forms that {@link Float#parseFloat} accepts
     *               but XSD does not are rejected: hexadecimal floats ({@code 0x1p4}), the
     *               Java {@code Infinity} token, and a trailing type suffix
     *               ({@code f}/{@code F}/{@code d}/{@code D}). When {@code false} the
     *               long-standing lenient behaviour applies. Driven by
     *               {@link org.apache.xmlbeans.XmlOptions#setLoadStrictFloatingPoint()}.
     * @param maxNumberOfChars the maximum number of characters allowed in the string
     * @return the parsed float
     * @throws NumberFormatException if the value is not a valid xsd:float
     * @since 5.4.0
     */
    public static float lexFloat(CharSequence cs, boolean strict,
                                 int maxNumberOfChars)
        throws NumberFormatException {
        rejectInvalidNumber(cs);
        final String v = cs.toString();
        switch (v) {
            case POS_INF_LEX:
                return Float.POSITIVE_INFINITY;
            case NEG_INF_LEX:
                return Float.NEGATIVE_INFINITY;
            case NAN_LEX:
                return Float.NaN;
        }
        //current jdk impl of parseFloat calls trim() on the string.
        //Any other space is illegal anyway, whether there are one or more spaces.
        //so no need to do a collapse pass through the string.
        if (strict) {
            checkFloatingPointLexical(cs);
        } else if (cs.length() > 1) {
            char ch = cs.charAt(cs.length() - 1);
            if ((ch == 'f' || ch == 'F') && cs.charAt(cs.length() - 2) != 'N') {
                throw new NumberFormatException("Invalid char '" + ch + "' in float.");
            }
        }
        return MathUtil.parseAsFloat(v, maxNumberOfChars);
    }

    public static float lexFloat(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexFloat(cs, false, XmlOptions.DEFAULT_MAX_NUMBER_CHARS);
        } catch (NumberFormatException e) {
            String msg = "invalid float: " + cs;
            errors.add(XmlError.forMessage(msg));

            return Float.NaN;
        }
    }

    public static String printFloat(float value) {
        if (value == Float.POSITIVE_INFINITY) {
            return POS_INF_LEX;
        } else if (value == Float.NEGATIVE_INFINITY) {
            return NEG_INF_LEX;
        } else if (Float.isNaN(value)) {
            return NAN_LEX;
        } else {
            return Float.toString(value);
        }
    }


    // ======================== double ========================
    public static double lexDouble(CharSequence cs)
        throws NumberFormatException {
        return lexDouble(cs, false,  XmlOptions.DEFAULT_MAX_NUMBER_CHARS);
    }

    /**
     * Parses an xsd:double lexical value.
     *
     * @param cs     the lexical value
     * @param strict when {@code true}, lexical forms that {@link Double#parseDouble} accepts
     *               but XSD does not are rejected: hexadecimal floats ({@code 0x1p4}), the
     *               Java {@code Infinity} token, and a trailing type suffix
     *               ({@code f}/{@code F}/{@code d}/{@code D}). When {@code false} the
     *               long-standing lenient behaviour applies. Driven by
     *               {@link org.apache.xmlbeans.XmlOptions#setLoadStrictFloatingPoint()}.
     * @param maxNumberOfChars the maximum number of characters allowed in the string
     * @return the parsed double
     * @throws NumberFormatException if the value is not a valid xsd:double
     * @since 5.4.0
     */
    public static double lexDouble(CharSequence cs, boolean strict,
                                   int maxNumberOfChars)
        throws NumberFormatException {
        rejectInvalidNumber(cs);
        final String v = cs.toString();
        switch (v) {
            case POS_INF_LEX:
                return Double.POSITIVE_INFINITY;
            case NEG_INF_LEX:
                return Double.NEGATIVE_INFINITY;
            case NAN_LEX:
                return Double.NaN;
        }
        //current jdk impl of parseDouble calls trim() on the string.
        //Any other space is illegal anyway, whether there are one or more spaces.
        //so no need to do a collapse pass through the string.
        if (strict) {
            checkFloatingPointLexical(cs);
        } else if (cs.length() > 0) {
            char ch = cs.charAt(cs.length() - 1);
            if (ch == 'd' || ch == 'D') {
                throw new NumberFormatException("Invalid char '" + ch + "' in double.");
            }
        }
        return MathUtil.parseAsDouble(v, maxNumberOfChars);
    }

    public static double lexDouble(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexDouble(cs, false, XmlOptions.DEFAULT_MAX_NUMBER_CHARS);
        } catch (NumberFormatException e) {
            String msg = "invalid double: " + cs;
            errors.add(XmlError.forMessage(msg));

            return Double.NaN;
        }
    }

    public static String printDouble(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return POS_INF_LEX;
        } else if (value == Double.NEGATIVE_INFINITY) {
            return NEG_INF_LEX;
        } else if (Double.isNaN(value)) {
            return NAN_LEX;
        } else {
            return Double.toString(value);
        }
    }


    // ======================== decimal ========================
    public static BigDecimal lexDecimal(CharSequence cs)
        throws NumberFormatException {
        return lexDecimal(cs, false);
    }

    /**
     * Parses an xsd:decimal lexical value.
     *
     * @param cs            the lexical value
     * @param allowExponent when {@code false} (the default) scientific/exponent notation
     *                      such as {@code 1E5} is rejected: it is outside the xsd:decimal
     *                      lexical space (that form belongs to xsd:double/xsd:float) and
     *                      {@link BigDecimal} would otherwise parse it to a wrong value
     *                      ({@code 1E5 -> 100000}). When {@code true} the long-standing
     *                      lenient behaviour applies and an exponent is accepted. Driven by
     *                      {@link org.apache.xmlbeans.XmlOptions#setLoadAllowDecimalExponent()}.
     * @return the parsed decimal
     * @throws NumberFormatException if the value is not a valid xsd:decimal
     * @since 5.4.0
     */
    public static BigDecimal lexDecimal(CharSequence cs, boolean allowExponent)
        throws NumberFormatException {
        rejectInvalidNumber(cs);
        if (!allowExponent) {
            rejectExponent(cs);
        }
        final String v = cs.toString();

        //TODO: review this
        //NOTE: we trim unneeded zeros from the string because
        //java.math.BigDecimal considers them significant for its
        //equals() method, but the xml value
        //space does not consider them significant.
        //See http://www.w3.org/2001/05/xmlschema-errata#e2-44
        return MathUtil.parseAsBigDecimal(trimTrailingZeros(v));
    }

    private static final char[] CH_ZEROS = new char[]{'0', '0', '0', '0', '0', '0', '0', '0',
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};

    public static String printDecimal(BigDecimal value) {
        // We can't simply use value.toString() here, because in JDK1.5 that returns an
        // exponent String and exponents are not allowed in XMLSchema decimal values
        // The following code comes from Apache Harmony
        String intStr = value.unscaledValue().toString();
        int scale = value.scale();
        if (scale == 0 || (MathUtil.toLong(value) == 0 && scale < 0)) {
            return intStr;
        }

        int begin = (value.signum() < 0) ? 1 : 0;
        int delta = scale;
        // We take space for all digits, plus a possible decimal point, plus 'scale'
        StringBuilder result = new StringBuilder(intStr.length() + 1 + Math.abs(scale));

        if (begin == 1) {
            // If the number is negative, we insert a '-' character at front
            result.append('-');
        }
        if (scale > 0) {
            delta -= (intStr.length() - begin);
            if (delta >= 0) {
                result.append("0."); //$NON-NLS-1$
                // To append zeros after the decimal point
                for (; delta > CH_ZEROS.length; delta -= CH_ZEROS.length) {
                    result.append(CH_ZEROS);
                }
                result.append(CH_ZEROS, 0, delta);
                result.append(intStr.substring(begin));
            } else {
                delta = begin - delta;
                result.append(intStr.substring(begin, delta));
                result.append('.');
                result.append(intStr.substring(delta));
            }
        } else {// (scale <= 0)
            result.append(intStr.substring(begin));
            // To append trailing zeros
            for (; delta < -CH_ZEROS.length; delta += CH_ZEROS.length) {
                result.append(CH_ZEROS);
            }
            result.append(CH_ZEROS, 0, -delta);
        }
        return result.toString();
    }

    // ======================== integer ========================
    public static BigInteger lexInteger(CharSequence cs)
        throws NumberFormatException {
        rejectSignAfterPlus(cs);
        final String v = cs.toString();

        //TODO: consider special casing zero and one to return static values
        //from BigInteger to avoid object creation.
        return MathUtil.parseAsBigInteger(trimInitialPlus(v));
    }

    public static BigInteger lexInteger(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexInteger(cs);
        } catch (NumberFormatException e) {
            String msg = "invalid long: " + cs;
            errors.add(XmlError.forMessage(msg));
            return BigInteger.ZERO;
        }
    }

    public static String printInteger(BigInteger value) {
        return value.toString();
    }

    // ======================== long ========================
    public static long lexLong(CharSequence cs)
        throws NumberFormatException {
        rejectInvalidNumber(cs);
        rejectSignAfterPlus(cs);
        final String v = cs.toString();
        return MathUtil.parseAsLong(trimInitialPlus(v));
    }

    // trimInitialPlus drops a single leading '+', then Long.parseLong /
    // new BigInteger accept their own leading sign, so "++5" and "+-5" slip
    // through as 5 and -5. Neither is in the xsd integer lexical space
    // ([\-+]?[0-9]+ allows one sign). lexInt/lexShort/lexByte already reject
    // the second sign in parseIntXsdNumber.
    private static void rejectSignAfterPlus(CharSequence cs) {
        if (cs.length() > 1 && cs.charAt(0) == '+') {
            final char c = cs.charAt(1);
            if (c == '+' || c == '-') {
                throw new NumberFormatException("Illegal char sequence '+" + c + "'");
            }
        }
    }

    public static long lexLong(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexLong(cs);
        } catch (NumberFormatException e) {
            String msg = "invalid long: " + cs;
            errors.add(XmlError.forMessage(msg));
            return 0L;
        }
    }

    public static String printLong(long value) {
        return Long.toString(value);
    }


    // ======================== short ========================
    public static short lexShort(CharSequence cs)
        throws NumberFormatException {
        return parseShort(cs);
    }

    public static short lexShort(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexShort(cs);
        } catch (NumberFormatException e) {
            String msg = "invalid short: " + cs;
            errors.add(XmlError.forMessage(msg));
            return 0;
        }
    }

    public static String printShort(short value) {
        return Short.toString(value);
    }


    // ======================== int ========================
    public static int lexInt(CharSequence cs)
        throws NumberFormatException {
        return parseIntXsdNumber(cs, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int lexInt(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexInt(cs);
        } catch (NumberFormatException e) {
            String msg = "invalid int:" + cs;
            errors.add(XmlError.forMessage(msg));
            return 0;
        }
    }

    public static String printInt(int value) {
        return Integer.toString(value);
    }


    // ======================== byte ========================
    public static byte lexByte(CharSequence cs)
        throws NumberFormatException {
        return parseByte(cs);
    }

    public static byte lexByte(CharSequence cs, Collection<XmlError> errors) {
        try {
            return lexByte(cs);
        } catch (NumberFormatException e) {
            String msg = "invalid byte: " + cs;
            errors.add(XmlError.forMessage(msg));
            return 0;
        }
    }

    public static String printByte(byte value) {
        return Byte.toString(value);
    }


    // ======================== boolean ========================
    public static boolean lexBoolean(CharSequence v) {
        switch (v.length()) {
            case 1:  // "0" or "1"
                final char c = v.charAt(0);
                if ('0' == c) {
                    return false;
                }
                if ('1' == c) {
                    return true;
                }
                break;
            case 4:  //"true"
                if ('t' == v.charAt(0) &&
                    'r' == v.charAt(1) &&
                    'u' == v.charAt(2) &&
                    'e' == v.charAt(3)) {
                    return true;
                }
                break;
            case 5:  //"false"
                if ('f' == v.charAt(0) &&
                    'a' == v.charAt(1) &&
                    'l' == v.charAt(2) &&
                    's' == v.charAt(3) &&
                    'e' == v.charAt(4)) {
                    return false;
                }
                break;
        }

        //reaching here means an invalid boolean lexical
        String msg = "invalid boolean: " + v;
        throw new InvalidLexicalValueException(msg);
    }

    public static boolean lexBoolean(CharSequence value, Collection<XmlError> errors) {
        try {
            return lexBoolean(value);
        } catch (InvalidLexicalValueException e) {
            errors.add(XmlError.forMessage(e.getMessage()));
            return false;
        }
    }

    public static String printBoolean(boolean value) {
        return Boolean.toString(value);
    }


    // ======================== string ========================
    public static String printString(String value) {
        return value;
    }


    // ======================== QName ========================
    public static QName lexQName(CharSequence charSeq, NamespaceContext nscontext) {
        String prefix, localname;

        int firstcolon;
        boolean hasFirstCollon = false;
        for (firstcolon = 0; firstcolon < charSeq.length(); firstcolon++) {
            if (charSeq.charAt(firstcolon) == NAMESPACE_SEP) {
                hasFirstCollon = true;
                break;
            }
        }

        if (hasFirstCollon) {
            prefix = charSeq.subSequence(0, firstcolon).toString();
            localname = charSeq.subSequence(firstcolon + 1, charSeq.length()).toString();
            if (firstcolon == 0) {
                throw new InvalidLexicalValueException("invalid xsd:QName '" + charSeq + "'");
            }
        } else {
            prefix = EMPTY_PREFIX;
            localname = charSeq.toString();
        }

        if (!prefix.isEmpty() && !XMLChar.isValidNCName(prefix)) {
            throw new InvalidLexicalValueException("invalid xsd:QName '" + charSeq + "'");
        }
        if (!XMLChar.isValidNCName(localname)) {
            throw new InvalidLexicalValueException("invalid xsd:QName '" + charSeq + "'");
        }

        String uri = nscontext.getNamespaceURI(prefix);

        if (uri == null) {
            if (prefix != null && !prefix.isEmpty()) {
                throw new InvalidLexicalValueException("Can't resolve prefix: " + prefix);
            }

            uri = "";
        }

        return new QName(uri, localname);
    }

    public static QName lexQName(String xsd_qname, Collection<XmlError> errors,
                                 NamespaceContext nscontext) {
        try {
            return lexQName(xsd_qname, nscontext);
        } catch (InvalidLexicalValueException e) {
            errors.add(XmlError.forMessage(e.getMessage()));
            final int idx = xsd_qname.indexOf(NAMESPACE_SEP);
            return idx < 0 ? new QName(xsd_qname) : new QName(null, xsd_qname.substring(idx));
        }
    }

    public static String printQName(QName qname, NamespaceContext nsContext,
                                    Collection<XmlError> errors) {
        final String uri = qname.getNamespaceURI();
        assert uri != null; //qname is not allowed to have null uri values
        final String prefix;
        if (!uri.isEmpty()) {
            prefix = nsContext.getPrefix(uri);
            if (prefix == null) {
                String msg = "NamespaceContext does not provide" +
                             " prefix for namespaceURI " + uri;
                errors.add(XmlError.forMessage(msg));
            }
        } else {
            prefix = null;
        }
        return getQNameString(uri, qname.getLocalPart(), prefix);

    }

    public static String getQNameString(String uri,
                                        String localpart,
                                        String prefix) {
        if (prefix != null &&
                uri != null &&
                !uri.isEmpty() &&
                !prefix.isEmpty()) {
            return (prefix + NAMESPACE_SEP + localpart);
        } else {
            return localpart;
        }
    }

    // ======================== GDate ========================
    public static GDate lexGDate(CharSequence charSeq) {
        return new GDate(charSeq);
    }

    public static GDate lexGDate(String xsd_gdate, Collection<XmlError> errors) {
        try {
            return lexGDate(xsd_gdate);
        } catch (IllegalArgumentException e) {
            errors.add(XmlError.forMessage(e.getMessage()));
            return new GDateBuilder().toGDate();
        }
    }

    public static String printGDate(GDate gdate, Collection<XmlError> errors) {
        return gdate.toString();
    }


    // ======================== dateTime ========================
    public static XmlCalendar lexDateTime(CharSequence v) {
        GDateSpecification value = getGDateValue(v, SchemaType.BTC_DATE_TIME);
        return value.getCalendar();
    }


    public static String printDateTime(Calendar c) {
        return printDateTime(c, SchemaType.BTC_DATE_TIME);
    }

    public static String printTime(Calendar c) {
        return printDateTime(c, SchemaType.BTC_TIME);
    }

    public static String printDate(Calendar c) {
        return printDateTime(c, SchemaType.BTC_DATE);
    }

    public static String printDate(Date d) {
        GDateSpecification value = getGDateValue(d, SchemaType.BTC_DATE);
        return value.toString();
    }

    public static String printDateTime(Calendar c, int type_code) {
        GDateSpecification value = getGDateValue(c, type_code);
        return value.toString();
    }

    public static String printDateTime(Date c) {
        GDateSpecification value = getGDateValue(c, SchemaType.BTC_DATE_TIME);
        return value.toString();
    }


    // ======================== hexBinary ========================
    public static CharSequence printHexBinary(byte[] val) {
        return HexBin.bytesToString(val);
    }


    // date utils
    public static GDateSpecification getGDateValue(Date d,
                                                   int builtin_type_code) {
        GDateBuilder gDateBuilder = new GDateBuilder(d);
        gDateBuilder.setBuiltinTypeCode(builtin_type_code);
        return gDateBuilder.toGDate();
    }


    public static GDateSpecification getGDateValue(Calendar c,
                                                   int builtin_type_code) {
        GDateBuilder gDateBuilder = new GDateBuilder(c);
        gDateBuilder.setBuiltinTypeCode(builtin_type_code);
        return gDateBuilder.toGDate();
    }

    public static GDateSpecification getGDateValue(CharSequence v,
                                                   int builtin_type_code) {
        GDateBuilder gDateBuilder = new GDateBuilder(v);
        gDateBuilder.setBuiltinTypeCode(builtin_type_code);
        return gDateBuilder.toGDate();
    }

    private static String trimInitialPlus(String xml) {
        if (!xml.isEmpty() && xml.charAt(0) == '+') {
            return xml.substring(1);
        } else {
            return xml;
        }
    }

    private static String trimTrailingZeros(String xsd_decimal) {
        final int last_char_idx = xsd_decimal.length() - 1;
        if (last_char_idx >= 0 && xsd_decimal.charAt(last_char_idx) == '0') {
            final int last_point = xsd_decimal.lastIndexOf('.');
            if (last_point >= 0) {
                //find last trailing zero
                for (int idx = last_char_idx; idx > last_point; idx--) {
                    if (xsd_decimal.charAt(idx) != '0') {
                        return xsd_decimal.substring(0, idx + 1);
                    }
                }
                //reaching here means the string matched xxx.0*
                return xsd_decimal.substring(0, last_point);
            }
        }
        return xsd_decimal;
    }

    private static short parseShort(CharSequence cs) {
        return (short) parseIntXsdNumber(cs, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    private static byte parseByte(CharSequence cs) {
        return (byte) parseIntXsdNumber(cs, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    private static int parseIntXsdNumber(CharSequence ch, int min_value, int max_value) {
        rejectInvalidNumber(ch);

        final int len = ch.length();
        int i = 0;
        boolean negative = false;

        // Sign
        char first = ch.charAt(0);
        if (first == '-') {
            negative = true;
            i = 1;
        } else if (first == '+') {
            i = 1;
        }

        if (i == len) {
            throw new NumberFormatException("For input string: \"" + ch + "\""); // just "+" or "-"
        }

        long result = 0;           // Use long to avoid intermediate overflow

        while (i < len) {
            char c = ch.charAt(i++);
            int digit = c - '0';
            if (digit < 0 || digit > 9) {
                throw new NumberFormatException("For input string: \"" + ch + "\"");
            }

            // Early overflow detection
            if (result > (Long.MAX_VALUE / 10)) {
                throw new NumberFormatException("For input string: \"" + ch + "\"");
            }
            result = result * 10 + digit;
        }

        if (negative) {
            result = -result;
        }

        if (result < min_value || result > max_value) {
            throw new NumberFormatException(String.format(
                    Locale.ROOT,
                    "For input string: \"%s\"; min-allowed=%d, max-allowed=%d",
                    ch, min_value, max_value));
        }

        return Math.toIntExact(result);
    }

    // ======================== anyURI ========================

    /**
     * Checks the regular expression of URI, defined by RFC2369 http://www.ietf.org/rfc/rfc2396.txt Appendix B.
     * Note: The whitespace normalization rule collapse must be applied prior to calling this method.
     *
     * @param lexical_value the lexical value
     * @return same input value if input value is in the lexical space
     */
    public static CharSequence lexAnyURI(CharSequence lexical_value) {
        /*  // Reg exp from RFC2396, but it's too forgiving for XQTS
        Pattern p = Pattern.compile("^([^:/?#]+:)?(//[^/?#]*)?([^?#]*)(\\?[^#]*)?(#.*)?");
        Matcher m = p.matcher(lexical_value);
        if ( !m.matches() )
            throw new InvalidLexicalValueException("invalid anyURI value");
        else
        {
            for ( int i = 0; i<= m.groupCount(); i++ )
            {
                System.out.print("  " + i + ": " + m.group(i));
            }
            System.out.println("");
            return lexical_value;
        } */

        // Per XMLSchema spec allow spaces inside URIs
        StringBuilder s = new StringBuilder(lexical_value.toString());
        for (int ic = 0; ic < URI_CHARS_TO_BE_REPLACED.length; ic++) {
            int i = 0;
            while ((i = s.indexOf(URI_CHARS_TO_BE_REPLACED[ic], i)) >= 0) {
                s.replace(i, i + 1, URI_CHARS_REPLACED_WITH[ic]);
                i += 3;
            }
        }

        try {
            URI.create(s.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidLexicalValueException("invalid anyURI value: " + lexical_value, e);
        }

        return lexical_value;
    }

    private static void rejectInvalidNumber(CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            throw new NumberFormatException("For input string: \"" + cs + "\"");
        }
    }

    // BigDecimal accepts scientific notation such as "1E5", but the xsd:decimal
    // lexical space does not allow an exponent - that form belongs to xsd:double
    // and xsd:float. Without this check an exponent value reaching lexDecimal via
    // the rich parser parses to a wrong value (e.g. "1E5" -> 100000) instead of
    // being reported as invalid.
    private static void rejectExponent(CharSequence cs) {
        for (int i = 0, len = cs.length(); i < len; i++) {
            final char c = cs.charAt(i);
            if (c == 'e' || c == 'E') {
                throw new NumberFormatException("invalid char '" + c + "' in decimal value");
            }
        }
    }
}
