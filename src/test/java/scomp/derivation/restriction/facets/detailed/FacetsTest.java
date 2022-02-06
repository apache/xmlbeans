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
package scomp.derivation.restriction.facets.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.facets.facets.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 *
 */
public class FacetsTest {
    @Test
    void testMinMaxInclusiveElt() {
        MinMaxInclusiveEltDocument doc = MinMaxInclusiveEltDocument.Factory.newInstance();
        doc.setMinMaxInclusiveElt(3);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID};

        doc.setMinMaxInclusiveElt(1);
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        validateOptions.getErrorListener().clear();
        errExpected = new String[]{XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        doc.setMinMaxInclusiveElt(11);
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testMinMaxInclusiveDateElt() {
        MinMaxInclusiveDateEltDocument doc = MinMaxInclusiveDateEltDocument.Factory.newInstance();
        TimeZone tz = TimeZone.getDefault();
        Calendar c = new GregorianCalendar(tz);
        c.set(2003, Calendar.DECEMBER, 24);
        doc.setMinMaxInclusiveDateElt(c);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        c = new GregorianCalendar(2003, 11, 28);
        doc.setMinMaxInclusiveDateElt(c);
        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //valid range should be 3-9
    @Test
    void testMinMaxExclusiveElt() {
        MinMaxExclusiveEltDocument doc = MinMaxExclusiveEltDocument.Factory.newInstance();
        String[] errExpected = {XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID};

        doc.setMinMaxExclusiveElt(2);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
        doc.setMinMaxExclusiveElt(3);

        assertTrue(doc.validate(validateOptions));
        doc.setMinMaxExclusiveElt(9);
        assertTrue(doc.validate(validateOptions));
    }

    //valid range is 12-11 12-24-2003
    @Test
    void testMinMaxExclusiveDateElt() {
        MinMaxExclusiveDateEltDocument doc = MinMaxExclusiveDateEltDocument.Factory.newInstance();
        Calendar c = new GregorianCalendar(2003, Calendar.DECEMBER, 25);
        doc.setMinMaxExclusiveDateElt(c);
        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID};
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        c = new GregorianCalendar(2003, Calendar.DECEMBER, 11);
        doc.setMinMaxExclusiveDateElt(c);
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testLengthElt() {
        LengthEltDocument doc = LengthEltDocument.Factory.newInstance();
        doc.setLengthElt("foobar");
        String[] errExpected = new String[]{XmlErrorCodes.DATATYPE_LENGTH_VALID$STRING};

        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setLengthElt("f");
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setLengthElt("fo");
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testMinMaxLengthElt() {
        MinMaxLengthEltDocument doc = MinMaxLengthEltDocument.Factory.newInstance();
        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$STRING};

        doc.setMinMaxLengthElt("foobar");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setMinMaxLengthElt("f");
        errExpected = new String[]{XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$STRING};
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setMinMaxLengthElt("fo");
        assertTrue(doc.validate(validateOptions));
        doc.setMinMaxLengthElt("fooba");
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testDigitsElt() {
        DigitsEltDocument doc = DigitsEltDocument.Factory.newInstance();
        String[] errExpected = {XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID};

        doc.setDigitsElt(new BigDecimal("234.25"));
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setDigitsElt(new BigDecimal("12.13"));
        assertTrue(doc.validate(validateOptions));
        validateOptions.getErrorListener().clear();
        errExpected = new String[]{XmlErrorCodes.DATATYPE_FRACTION_DIGITS_VALID};
        doc.setDigitsElt(new BigDecimal(".145"));
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testWSElt() throws XmlException {
        String input =
            "<WSPreserveElt xmlns=\"http://xbean/scomp/derivation/facets/Facets\">" +
            "This is a\ttest.\nThe resulting string should preserve all whitespace     tabs and carriage returns as is\n" +
            "</WSPreserveElt>";
        WSPreserveEltDocument doc = WSPreserveEltDocument.Factory.parse(input);

        assertTrue(doc.validate(createOptions()));

        String expected = "This is a\ttest.\nThe resulting string should preserve all whitespace     tabs and carriage returns as is\n";
        assertEquals(expected, doc.getWSPreserveElt());
    }

    @Test
    void testEnumElt() throws XmlException {
        EnumEltDocument doc = EnumEltDocument.Factory.newInstance();
        doc.setEnumElt(EnumT.A);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        doc = EnumEltDocument.Factory.parse(
            "<EnumElt xmlns=\"http://xbean/scomp/derivation/facets/Facets\">foo</EnumElt>");
        String[] errExpected = {XmlErrorCodes.DATATYPE_ENUM_VALID};

        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testPatternElt() {
        PatternEltDocument doc = PatternEltDocument.Factory.newInstance();
        doc.setPatternElt("aedaedaed");
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        String[] errExpected = new String[]{XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};

        doc.setPatternElt("abdadad");
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
