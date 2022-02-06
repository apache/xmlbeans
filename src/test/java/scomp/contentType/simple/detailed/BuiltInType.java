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

package scomp.contentType.simple.detailed;

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.builtIn.date.*;
import xbean.scomp.contentType.builtIn.number.*;
import xbean.scomp.contentType.builtIn.string.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class BuiltInType {
    /**
     * testing types String, normalizedString and token
     */
    @Test
    void testStringBasedTypes1() throws Throwable {
        String[] exp = new String[]{"\tLead tab,A string on\n 2 lines with 2  spaces", "  2 Lead spaces,A string on\n 2 lines with 2  spaces", " Lead tab,A string on  2 lines with 2  spaces", "  2 Lead spaces,A string on  2 lines with 2  spaces", "Lead tab,A string on 2 lines with 2 spaces", "2 Lead spaces,A string on 2 lines with 2 spaces"};
        StringEltDocument doc = StringEltDocument.Factory.parse(buildString("StringElt", false));
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        assertEquals("<StringElt xmlns=\"http://xbean/scomp/contentType/builtIn/String\">" + exp[0] + "</StringElt>", doc.xmlText());

        doc = StringEltDocument.Factory.parse(buildString("StringElt", true));
        assertTrue(doc.validate(validateOptions));
        assertEquals(exp[1], doc.getStringElt());

        NormalizedStringEltDocument doc1 = NormalizedStringEltDocument.Factory.parse(buildString("NormalizedStringElt", false));
        assertTrue(doc.validate(validateOptions));
        assertEquals(exp[2], doc1.getNormalizedStringElt());
        doc1 = NormalizedStringEltDocument.Factory.parse(buildString("NormalizedStringElt", true));
        assertTrue(doc1.validate(validateOptions));
        assertEquals(exp[3], doc1.getNormalizedStringElt());

        TokenEltDocument doc2 = TokenEltDocument.Factory.parse(buildString("TokenElt", false));
        assertTrue(doc2.validate(validateOptions));
        assertEquals(exp[4], doc2.getTokenElt());
        doc2 = TokenEltDocument.Factory.parse(buildString("TokenElt", true));
        assertTrue(doc2.validate(validateOptions));
        assertEquals(exp[5], doc2.getTokenElt());
    }

    /**
     * testing types Name, NCName, Language
     */
    @Test
    void testStringBasedTypes2() {
        XmlOptions validateOptions = createOptions();
        NameEltDocument nameDoc = NameEltDocument.Factory.newInstance();
        nameDoc.setNameElt("_eltName");
        assertTrue(nameDoc.validate(validateOptions));
        nameDoc.setNameElt(":eltName");
        assertTrue(nameDoc.validate(validateOptions));
        XmlName str = XmlName.Factory.newInstance();
        str.setStringValue("-eltName");
        nameDoc.xsetNameElt(str);
        assertFalse(nameDoc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));


        NCNameEltDocument ncNameDoc = NCNameEltDocument.Factory.newInstance();
        ncNameDoc.setNCNameElt(":eltName");
        validateOptions.getErrorListener().clear();
        assertFalse(ncNameDoc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        XmlNCName ncn = XmlNCName.Factory.newInstance();
        ncn.setStringValue("_elt.Name");
        ncNameDoc.xsetNCNameElt(ncn);
        assertTrue(ncNameDoc.validate(validateOptions));

        LanguageEltDocument langDoc = LanguageEltDocument.Factory.newInstance();
        langDoc.setLanguageElt("de");
        assertTrue(langDoc.validate(validateOptions));

        langDoc.setLanguageElt("en-US");
        assertTrue(langDoc.validate(validateOptions));
        validateOptions.getErrorListener().clear();

        langDoc.setLanguageElt("bulgarian");
        assertFalse(langDoc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    private String buildString(String Elt, boolean leadSpace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<" + Elt);
        sb.append(" xmlns=\"http://xbean/scomp/contentType/builtIn/String\">");
        if (leadSpace) {
            sb.append("  2 Lead spaces,A string on\n 2 lines with 2  spaces");
        } else {
            sb.append("\tLead tab,A string on\n 2 lines with 2  spaces");
        }
        sb.append("</" + Elt + ">");
        return sb.toString();
    }

    @Test
    void testNumericypes() throws Throwable {
        String input = "<FloatElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">12.34e+5</FloatElt>";
        FloatEltDocument flDoc = FloatEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertTrue(flDoc.validate(validateOptions));
        flDoc.setFloatElt(13.5f);
        assertEquals(13.5f, flDoc.getFloatElt(), 0.0);

        DoubleEltDocument doubDoc = DoubleEltDocument.Factory.newInstance();
        assertEquals(0, doubDoc.getDoubleElt(), 0.0);
        XmlDouble val = XmlDouble.Factory.newInstance();
        val.setDoubleValue(13.4d);
        doubDoc.xsetDoubleElt(val);
        assertTrue(doubDoc.validate(validateOptions));

        input = "<DecimalElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">12.34</DecimalElt>";
        DecimalEltDocument decDoc = DecimalEltDocument.Factory.parse(input);
        assertTrue(decDoc.validate(validateOptions));
        BigDecimal bdval = new BigDecimal(new BigInteger("10"));
        decDoc.setDecimalElt(bdval);
        Assertions.assertSame(bdval, decDoc.getDecimalElt());

        input = "<IntegerElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">124353</IntegerElt>";
        IntegerEltDocument integerDoc = IntegerEltDocument.Factory.parse(input);
        assertTrue(decDoc.validate(validateOptions));
        integerDoc.setIntegerElt(BigInteger.ONE);
        Assertions.assertSame(BigInteger.ONE, integerDoc.getIntegerElt());

        LongEltDocument longDoc = LongEltDocument.Factory.newInstance();
        longDoc.setLongElt(2459871);
        assertTrue(longDoc.validate(validateOptions));

        input = "<IntElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\"> -2147483648 </IntElt>";
        IntEltDocument intDoc = IntEltDocument.Factory.parse(input);
        assertTrue(intDoc.validate(validateOptions));
        intDoc.setIntElt(2147483647);
        assertTrue(intDoc.validate(validateOptions));
        // short is derived from int by
        // setting the value of maxInclusive
        // to be 32767 and minInclusive to be -32768.
        input = "<ShortElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">-32768</ShortElt>";
        ShortEltDocument shDoc = ShortEltDocument.Factory.parse(input);
        assertTrue(shDoc.validate(validateOptions));
        assertEquals(-32768, shDoc.xgetShortElt().getShortValue());
        //largest short is 32767. Don't use set--it would wrap around
        input = "<ShortElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">32768</ShortElt>";
        shDoc = ShortEltDocument.Factory.parse(input);
        assertFalse(shDoc.validate(validateOptions));
        String[] errExpected = new String[]{XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));


        ByteEltDocument byteDoc = ByteEltDocument.Factory.newInstance();
        byteDoc.setByteElt((byte) -128);
        assertTrue(byteDoc.validate(validateOptions));
        input = "<ByteElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">128</ByteElt>";
        byteDoc = ByteEltDocument.Factory.parse(input);

        validateOptions.getErrorListener().clear();
        assertFalse(byteDoc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        input = "<NonPosIntElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">-0000000</NonPosIntElt>";
        NonPosIntEltDocument nonposIntDoc = NonPosIntEltDocument.Factory.parse(input);
        assertEquals(0, nonposIntDoc.getNonPosIntElt().intValue());
        assertTrue(nonposIntDoc.validate(validateOptions));
        //should be valid but javac complains is setter is called
        input = "<NonPosIntElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">-12678967543233</NonPosIntElt>";
        nonposIntDoc = NonPosIntEltDocument.Factory.parse(input);
        assertTrue(nonposIntDoc.validate(validateOptions));

        input = "<NegativeIntElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">-12678967543233</NegativeIntElt>";
        NegativeIntEltDocument negIntDoc = NegativeIntEltDocument.Factory.parse(input);
        assertTrue(negIntDoc.validate(validateOptions));

        input = "<NonNegIntElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">12678967543233</NonNegIntElt>";
        NonNegIntEltDocument nonnegIntDoc = NonNegIntEltDocument.Factory.parse(input);
        assertTrue(nonnegIntDoc.validate(validateOptions));

        input = "<UnsignedLongElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">18446744073709551615</UnsignedLongElt>";
        UnsignedLongEltDocument uLongDoc = UnsignedLongEltDocument.Factory.parse(input);
        assertTrue(uLongDoc.validate(validateOptions));

        input = "<UnsignedIntElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">4294967295</UnsignedIntElt>";
        UnsignedIntEltDocument uInt = UnsignedIntEltDocument.Factory.parse(input);
        assertTrue(uInt.validate(validateOptions));

        input = "<UnsignedShortElt  xmlns=\"http://xbean/scomp/contentType/builtIn/Number\">65535</UnsignedShortElt>";
        UnsignedShortEltDocument uShort = UnsignedShortEltDocument.Factory.parse(input);

        assertTrue(uShort.validate(validateOptions));

        UnsignedByteEltDocument uByte = UnsignedByteEltDocument.Factory.newInstance();
        uByte.setUnsignedByteElt((short) 255);
        assertTrue(uByte.validate(validateOptions));
    }

    @Test
    void testDateTime() throws Throwable {
        DateEltDocument date = DateEltDocument.Factory.newInstance();

        date.setDateElt(getCalendar());
        XmlOptions validateOptions = createOptions();
        assertTrue(date.validate(validateOptions));

        String input = "<TimeElt xmlns=\"http://xbean/scomp/contentType/builtIn/Date\">23:56:00</TimeElt>";
        TimeEltDocument time = TimeEltDocument.Factory.parse(input);
        assertTrue(time.validate(validateOptions));

        DateTimeEltDocument dateTime = DateTimeEltDocument.Factory.newInstance();
        dateTime.setDateTimeElt(getCalendar());
        assertTrue(date.validate(validateOptions));

        input = "<gYearElt xmlns=\"http://xbean/scomp/contentType/builtIn/Date\">2004</gYearElt>";
        GYearEltDocument year = GYearEltDocument.Factory.parse(input);
        assertTrue(year.validate(validateOptions));

        GYearMonthEltDocument yrmo = GYearMonthEltDocument.Factory.newInstance();
        XmlGYearMonth val = XmlGYearMonth.Factory.newInstance();
        GDate dt = new GDate(getCalendar());
        val.setGDateValue(dt);
        yrmo.xsetGYearMonthElt(val);
        assertTrue(yrmo.validate(validateOptions));

        GMonthEltDocument mo = GMonthEltDocument.Factory.newInstance();
        Calendar c = getCalendar();
        c.set(1997, Calendar.NOVEMBER, 6);
        mo.setGMonthElt(c);
        assertTrue(mo.validate(validateOptions));

        GMonthDayEltDocument moday = GMonthDayEltDocument.Factory.newInstance();
        moday.setGMonthDayElt(getCalendar());
        assertTrue(moday.validate(validateOptions));

        input = "<gDayElt xmlns=\"http://xbean/scomp/contentType/builtIn/Date\">32</gDayElt>";
        GDayEltDocument day = GDayEltDocument.Factory.parse(input);
        assertFalse(day.validate(validateOptions));

        String[] errExpected = {XmlErrorCodes.DATE};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        day.setGDayElt(c);
        assertTrue(day.validate(validateOptions));

        DurationEltDocument duration = DurationEltDocument.Factory.newInstance();
        GDurationBuilder gdb = new GDurationBuilder();
        gdb.setDay(11);
        gdb.setMonth(5);
        gdb.setYear(2004);
        duration.setDurationElt(new GDuration(gdb));
        assertTrue(duration.validate(validateOptions));
    }

    private static Calendar getCalendar() {
        // get the supported ids for GMT-08:00 (Pacific Standard Time)
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        // if no ids were returned, something is wrong. get out.
        if (ids.length == 0) {
            return null;
        }

        // create a Pacific Standard Time time zone
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);

        // set up rules for daylight savings time
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time zone
        // and the current date and time
        Calendar calendar = new GregorianCalendar(pdt);
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        return calendar;
    }
}
