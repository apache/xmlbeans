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
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.facets.facetRestriction.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class FacetRestrictionTest {
    @Test
    void testMinMaxInclusiveElt() {
        MinMaxInclusiveEltDocument doc = MinMaxInclusiveEltDocument.Factory.newInstance();
        doc.setMinMaxInclusiveElt(3);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID};

        doc.setMinMaxInclusiveElt(2);
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        validateOptions.getErrorListener().clear();
        errExpected = new String[]{XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        doc.setMinMaxInclusiveElt(10);
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testMinMaxInclusiveDateElt() {
        MinMaxInclusiveDateEltDocument doc = MinMaxInclusiveDateEltDocument.Factory.newInstance();
        TimeZone tz = TimeZone.getDefault();
        Calendar c = new GregorianCalendar(tz);
        c.set(2003, Calendar.DECEMBER, 22);
        doc.setMinMaxInclusiveDateElt(c);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        c = new GregorianCalendar(2003, Calendar.DECEMBER, 24);
        doc.setMinMaxInclusiveDateElt(c);
        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testMinMaxExclusiveElt() {
        MinMaxExclusiveEltDocument doc = MinMaxExclusiveEltDocument.Factory.newInstance();
        String[] errExpected = {XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID};

        doc.setMinMaxExclusiveElt(3);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
        validateOptions.getErrorListener().clear();

        doc.setMinMaxExclusiveElt(4);
        assertTrue(doc.validate(validateOptions));
        doc.setMinMaxExclusiveElt(8);
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testMinMaxExclusiveDateElt() {
        MinMaxExclusiveDateEltDocument doc = MinMaxExclusiveDateEltDocument.Factory.newInstance();
        Calendar c = new GregorianCalendar(2003, Calendar.DECEMBER, 24);
        doc.setMinMaxExclusiveDateElt(c);
        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID};
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
        validateOptions.getErrorListener().clear();
        c = new GregorianCalendar(2003, Calendar.DECEMBER, 23);
        doc.setMinMaxExclusiveDateElt(c);
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testMinMaxLengthElt() {
        MinMaxLengthEltDocument doc = MinMaxLengthEltDocument.Factory.newInstance();
        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$STRING};
        XmlOptions validateOptions = createOptions();

        doc.setMinMaxLengthElt("fooba");
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setMinMaxLengthElt("fo");
        errExpected = new String[]{XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$STRING};
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setMinMaxLengthElt("foo");
        assertTrue(doc.validate(validateOptions));
        doc.setMinMaxLengthElt("foob");
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testDigitsElt() {
        DigitsEltDocument doc = DigitsEltDocument.Factory.newInstance();
        String[] errExpected = {XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID};
        XmlOptions validateOptions = createOptions();

        doc.setDigitsElt(new BigDecimal("122.2"));
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc.setDigitsElt(new BigDecimal("12.3"));
        assertTrue(doc.validate(validateOptions));
        validateOptions.getErrorListener().clear();
        errExpected = new String[]{XmlErrorCodes.DATATYPE_FRACTION_DIGITS_VALID};
        doc.setDigitsElt(new BigDecimal("2.45"));
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testWSElt() throws Throwable {
        String inputReplace =
            "<WSReplaceElt xmlns=\"http://xbean/scomp/derivation/facets/FacetRestriction\">" +
            " This is a    test.\nThe resulting string should convert tabs \t, line feeds \n  and carriage returns into a single space \n" +
            "</WSReplaceElt>";

        String inputCollapse =
            "<WSCollapseElt xmlns=\"http://xbean/scomp/derivation/facets/FacetRestriction\">" +
            " This is a    test.\nThe resulting string should convert tabs \t, line feeds \n  and carriage returns into a single space \n" +
            "</WSCollapseElt>";

        // whiteSpace="replace" case
        WSReplaceEltDocument doc = WSReplaceEltDocument.Factory.parse(inputReplace);

        // whiteSpace="collapse " case
        WSCollapseEltDocument doc2 = WSCollapseEltDocument.Factory.parse(inputCollapse);
        XmlOptions validateOptions = createOptions();

        assertTrue(doc.validate(validateOptions));
        assertTrue(doc2.validate(validateOptions));

        String replaceExpected = " This is a    test. The resulting string should convert tabs  , line feeds    and carriage returns into a single space  ";
        assertEquals(replaceExpected, doc.getWSReplaceElt());

        String collapseExpected = "This is a test. The resulting string should convert tabs , line feeds and carriage returns into a single space";
        assertEquals(collapseExpected, doc2.getWSCollapseElt());
    }

    @Test
    void testEnumElt() throws Throwable {
        EnumEltDocument doc = EnumEltDocument.Factory.newInstance();
        doc.setEnumElt(EnumT.A);

        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        doc = EnumEltDocument.Factory.parse(
            "<EnumElt xmlns=\"http://xbean/scomp/derivation/facets/FacetRestriction\">b</EnumElt>");
        String[] errExpected = {XmlErrorCodes.DATATYPE_ENUM_VALID};

        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testPatternElt() throws Throwable {
        // base pattern is (a[^bc]d){3}, derived pattern is (a[^ef]d){3}
        PatternEltDocument doc = PatternEltDocument.Factory.newInstance();
        doc.setPatternElt("axdaydazd");

        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};

        doc.setPatternElt("aedafdagd");
        assertFalse(doc.validate(validateOptions));
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
