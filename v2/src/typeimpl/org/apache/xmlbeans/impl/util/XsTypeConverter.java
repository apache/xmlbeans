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

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;

public final class XsTypeConverter
{
    private static final String POS_INF_LEX = "INF";
    private static final String NEG_INF_LEX = "-INF";
    private static final String NAN_LEX = "NaN";

    private static final char NAMESPACE_SEP = ':';
    private static final String EMPTY_PREFIX = "";
    private static final BigDecimal DECIMAL__ZERO = new BigDecimal(0.0);

    // ======================== float ========================
    public static float lexFloat(CharSequence cs)
        throws NumberFormatException
    {
        final String v = cs.toString();
        try {
            //current jdk impl of parseFloat calls trim() on the string.
            //Any other space is illegal anyway, whether there are one or more spaces.
            //so no need to do a collapse pass through the string.
            if (cs.length()>0)
            {
                char ch = cs.charAt(cs.length()-1);
                if (ch=='f' || ch=='F')
                {
                    if (cs.charAt(cs.length()-2)!='N')
                        throw new NumberFormatException("Invalid char '" + ch + "' in float.");
                }
            }
            return Float.parseFloat(v);
        }
        catch (NumberFormatException e) {
            if (v.equals(POS_INF_LEX)) return Float.POSITIVE_INFINITY;
            if (v.equals(NEG_INF_LEX)) return Float.NEGATIVE_INFINITY;
            if (v.equals(NAN_LEX)) return Float.NaN;

            throw e;
        }
    }

    public static float lexFloat(CharSequence cs, Collection errors)
    {
        try {
            return lexFloat(cs);
        }
        catch (NumberFormatException e) {
            String msg = "invalid float: " + cs;
            errors.add(XmlError.forMessage(msg));

            return Float.NaN;
        }
    }

    public static String printFloat(float value)
    {
        if (value == Float.POSITIVE_INFINITY)
            return POS_INF_LEX;
        else if (value == Float.NEGATIVE_INFINITY)
            return NEG_INF_LEX;
        else if (value == Float.NaN)
            return NAN_LEX;
        else
            return Float.toString(value);
    }


    // ======================== double ========================
    public static double lexDouble(CharSequence cs)
        throws NumberFormatException
    {
        final String v = cs.toString();

        try {
            //current jdk impl of parseDouble calls trim() on the string.
            //Any other space is illegal anyway, whether there are one or more spaces.
            //so no need to do a collapse pass through the string.
            if (cs.length()>0)
            {
                char ch = cs.charAt(cs.length()-1);
                if (ch=='d' || ch=='D')
                    throw new NumberFormatException("Invalid char '" + ch + "' in double.");
            }
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e) {
            if (v.equals(POS_INF_LEX)) return Double.POSITIVE_INFINITY;
            if (v.equals(NEG_INF_LEX)) return Double.NEGATIVE_INFINITY;
            if (v.equals(NAN_LEX)) return Double.NaN;

            throw e;
        }
    }

    public static double lexDouble(CharSequence cs, Collection errors)
    {
        try {
            return lexDouble(cs);
        }
        catch (NumberFormatException e) {
            String msg = "invalid double: " + cs;
            errors.add(XmlError.forMessage(msg));

            return Double.NaN;
        }
    }

    public static String printDouble(double value)
    {
        if (value == Double.POSITIVE_INFINITY)
            return POS_INF_LEX;
        else if (value == Double.NEGATIVE_INFINITY)
            return NEG_INF_LEX;
        else if (value == Double.NaN)
            return NAN_LEX;
        else
            return Double.toString(value);
    }


    // ======================== decimal ========================
    public static BigDecimal lexDecimal(CharSequence cs)
        throws NumberFormatException
    {
        final String v = cs.toString();

        //TODO: review this
        //NOTE: we trim unneeded zeros from the string because
        //java.math.BigDecimal considers them significant for its
        //equals() method, but the xml value
        //space does not consider them significant.
        //See http://www.w3.org/2001/05/xmlschema-errata#e2-44
        return new BigDecimal(trimTrailingZeros(v));
    }

    public static BigDecimal lexDecimal(CharSequence cs, Collection errors)
    {
        try {
            return lexDecimal(cs);
        }
        catch (NumberFormatException e) {
            String msg = "invalid long: " + cs;
            errors.add(XmlError.forMessage(msg));
            return DECIMAL__ZERO;
        }
    }

    public static String printDecimal(BigDecimal value)
    {
        return value.toString();
    }

    // ======================== integer ========================
    public static BigInteger lexInteger(CharSequence cs)
        throws NumberFormatException
    {
        if (cs.length()>1)
        {
            if (cs.charAt(0)=='+' && cs.charAt(1)=='-')
                throw new NumberFormatException("Illegal char sequence '+-'");
        }
        final String v = cs.toString();

        //TODO: consider special casing zero and one to return static values
        //from BigInteger to avoid object creation.
        return new BigInteger(trimInitialPlus(v));
    }

    public static BigInteger lexInteger(CharSequence cs, Collection errors)
    {
        try {
            return lexInteger(cs);
        }
        catch (NumberFormatException e) {
            String msg = "invalid long: " + cs;
            errors.add(XmlError.forMessage(msg));
            return BigInteger.ZERO;
        }
    }

    public static String printInteger(BigInteger value)
    {
        return value.toString();
    }

    // ======================== long ========================
    public static long lexLong(CharSequence cs)
        throws NumberFormatException
    {
        final String v = cs.toString();
        return Long.parseLong(trimInitialPlus(v));
    }

    public static long lexLong(CharSequence cs, Collection errors)
    {
        try {
            return lexLong(cs);
        }
        catch (NumberFormatException e) {
            String msg = "invalid long: " + cs;
            errors.add(XmlError.forMessage(msg));
            return 0L;
        }
    }

    public static String printLong(long value)
    {
        return Long.toString(value);
    }


    // ======================== short ========================
    public static short lexShort(CharSequence cs)
        throws NumberFormatException
    {
        return parseShort(cs);
    }

    public static short lexShort(CharSequence cs, Collection errors)
    {
        try {
            return lexShort(cs);
        }
        catch (NumberFormatException e) {
            String msg = "invalid short: " + cs;
            errors.add(XmlError.forMessage(msg));
            return 0;
        }
    }

    public static String printShort(short value)
    {
        return Short.toString(value);
    }


    // ======================== int ========================
    public static int lexInt(CharSequence cs)
        throws NumberFormatException
    {
        return parseInt(cs);
    }

    public static int lexInt(CharSequence cs, Collection errors)
    {
        try
        {
            return lexInt(cs);
        }
        catch(NumberFormatException e)
        {
            String msg = "invalid int:" + cs;
            errors.add(XmlError.forMessage(msg));
            return 0;
        }
    }

    public static String printInt(int value)
    {
        return Integer.toString(value);
    }


    // ======================== byte ========================
    public static byte lexByte(CharSequence cs)
        throws NumberFormatException
    {
        return parseByte(cs);
    }

    public static byte lexByte(CharSequence cs, Collection errors)
    {
        try
        {
            return lexByte(cs);
        }
        catch (NumberFormatException e)
        {
            String msg = "invalid byte: " + cs;
            errors.add(XmlError.forMessage(msg));
            return 0;
        }
    }

    public static String printByte(byte value)
    {
        return Byte.toString(value);
    }


    // ======================== boolean ========================
    public static boolean lexBoolean(CharSequence v)
    {
        switch (v.length()) {
            case 1:  // "0" or "1"
                final char c = v.charAt(0);
                if ('0' == c) return false;
                if ('1' == c) return true;
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
        throw new IllegalArgumentException(msg);
    }

    public static boolean lexBoolean(CharSequence value, Collection errors)
    {
        try
        {
            return lexBoolean(value);
        }
        catch(IllegalArgumentException e)
        {
            errors.add(XmlError.forMessage(e.getMessage()));
            return false;
        }
    }

    public static String printBoolean(boolean value)
    {
        return (value ? "true" : "false");
    }


    // ======================== string ========================
    public static String lexString(CharSequence cs, Collection errors)
    {
        final String v = cs.toString();

        return v;
    }

    public static String printString(String value)
    {
        return value;
    }


    // ======================== QName ========================
    public static QName lexQName(CharSequence charSeq, NamespaceContext nscontext)
    {
        String prefix, localname;

        int firstcolon;
        boolean hasFirstCollon = false;
        for (firstcolon = 0; firstcolon < charSeq.length(); firstcolon ++)
            if (charSeq.charAt(firstcolon)==NAMESPACE_SEP)
            {
                hasFirstCollon = true;
                break;
            }

        if (hasFirstCollon)
        {
            prefix = charSeq.subSequence(0, firstcolon).toString();
            localname = charSeq.subSequence(firstcolon + 1, charSeq.length()).toString();
            if (firstcolon==0)
            {
                throw new InvalidLexicalValueException("invalid xsd:QName '" + charSeq.toString() + "'");
            }
        }
        else
        {
            prefix = EMPTY_PREFIX;
            localname = charSeq.toString();
        }

        String uri = nscontext.getNamespaceURI(prefix);

        if (uri == null)
        {
            if (prefix!=null && prefix.length() > 0)
                throw new InvalidLexicalValueException("Can't resolve prefix: " + prefix);

            uri = "";
        }

        return new QName( uri, localname );
    }

    public static QName lexQName(String xsd_qname, Collection errors,
                                 NamespaceContext nscontext)
    {
        try
        {
            return lexQName(xsd_qname, nscontext);
        }
        catch( InvalidLexicalValueException e)
        {
            errors.add(XmlError.forMessage(e.getMessage()));
            final int idx = xsd_qname.indexOf(NAMESPACE_SEP);
            return new QName(null, xsd_qname.substring(idx));
        }
    }

    public static String printQName(QName qname, NamespaceContext nsContext,
                                    Collection errors)
    {
        final String uri = qname.getNamespaceURI();
        String prefix = nsContext.getPrefix(uri);
        if (prefix == null) {
            String msg = "NamespaceContext does not provide" +
                " prefix for namespaceURI " + uri;
            errors.add(XmlError.forMessage(msg));
        }
        return getQNameString(uri, qname.getLocalPart(), prefix);
    }

    public static String getQNameString(String uri,
                                        String localpart,
                                        String prefix)
    {
        if (uri != null &&
            prefix != null &&
            prefix.length() > 0) {
            return (prefix + NAMESPACE_SEP + localpart);
        } else {
            return localpart;
        }
    }

    // ======================== GDate ========================
    public static GDate lexGDate(CharSequence charSeq)
    {
        return new GDate(charSeq);
    }

    public static GDate lexGDate(String xsd_gdate, Collection errors)
    {
        try
        {
            return lexGDate(xsd_gdate);
        }
        catch( IllegalArgumentException e)
        {
            errors.add(XmlError.forMessage(e.getMessage()));
            return new GDateBuilder().toGDate();
        }
    }

    public static String printGDate(GDate gdate, Collection errors)
    {
        return gdate.toString();
    }



    // ======================== dateTime ========================
    public static XmlCalendar lexDateTime(CharSequence v)
    {
        GDate value = getGDateValue(v, SchemaType.BTC_DATE_TIME);
        return value.getCalendar();
    }


    public static String printDateTime(Calendar c)
    {
        GDate value = getGDateValue(c, SchemaType.BTC_DATE_TIME);
        return value.canonicalString();
    }

    public static String printDateTime(Date c)
    {
        GDate value = getGDateValue(c, SchemaType.BTC_DATE_TIME);
        return value.canonicalString();
    }


    // ======================== hexBinary ========================
    public static CharSequence printHexBinary(byte[] val)
    {
        return HexBin.bytesToString(val);
    }


    // ======================== base64binary ========================
    public static CharSequence printBase64Binary(byte[] val)
    {
        final byte[] bytes = Base64.encode(val);
        return new String(bytes);
    }



    // private utils
    private static GDate getGDateValue(Calendar c, int builtin_type_code)
    {
        GDateBuilder gDateBuilder = new GDateBuilder(c);
        gDateBuilder.setBuiltinTypeCode(builtin_type_code);
        GDate value = gDateBuilder.toGDate();
        return value;
    }

    private static GDate getGDateValue(Date d, int builtin_type_code)
    {
        GDateBuilder gDateBuilder = new GDateBuilder(d);
        gDateBuilder.setBuiltinTypeCode(builtin_type_code);
        GDate value = gDateBuilder.toGDate();
        return value;
    }

    private static GDate getGDateValue(CharSequence v, int builtin_type_code)
    {
        GDateBuilder gDateBuilder = new GDateBuilder(v);
        gDateBuilder.setBuiltinTypeCode(builtin_type_code);
        GDate value = gDateBuilder.toGDate();
        return value;
    }

    private static String trimInitialPlus(String xml)
    {
        if (xml.charAt(0) == '+') {
            return xml.substring(1);
        } else {
            return xml;
        }
    }

    private static String trimTrailingZeros(String xsd_decimal)
    {
        final int last_char_idx = xsd_decimal.length() - 1;
        if (xsd_decimal.charAt(last_char_idx) == '0') {
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

        private static int parseInt(CharSequence cs)
    {
        return parseIntXsdNumber(cs, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static short parseShort(CharSequence cs)
    {
        return (short)parseIntXsdNumber(cs, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    private static byte parseByte(CharSequence cs)
    {
        return (byte)parseIntXsdNumber(cs, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    private static int parseIntXsdNumber(CharSequence ch, int min_value, int max_value)
    {
        // int parser on a CharSequence
        int length = ch.length();
        if (length<1)
            throw new NumberFormatException("For input string: \"" + ch.toString() +"\"");

        int sign = 1;
        int result = 0;
        int start = 0;
        int limit;
        int limit2;

        char c = ch.charAt(0);
        if (c=='-')
        {
            start++;
            limit  = (min_value/10);
            limit2 = -(min_value%10);
        }
        else if (c=='+')
        {
            start++;
            sign = -1;
            limit  = -(max_value/10);
            limit2 = (max_value%10);
        }
        else
        {
            sign = -1;
            limit  = -(max_value/10);
            limit2 = (max_value%10);
        }

        for (int i = 0; i < length-start; i++)
        {
            c = ch.charAt(i + start);
            int v = Character.digit(c, 10);

            if (v<0)
                throw new NumberFormatException("For input string: \"" + ch.toString() +"\"");

            if ( result <= limit && v > limit2)
                throw new NumberFormatException("For input string: \"" + ch.toString() + "\"");

            result = result*10 - v;
        }

        return sign*result;
    }
}
