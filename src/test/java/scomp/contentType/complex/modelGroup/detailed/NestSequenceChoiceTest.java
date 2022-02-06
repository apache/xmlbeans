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
package scomp.contentType.complex.modelGroup.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.modelGroup.NestedChoiceInSequenceDocument;
import xbean.scomp.contentType.modelGroup.NestedChoiceInSequenceT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class NestSequenceChoiceTest {
    /**
     * Choice group is optional
     */
    @Test
    void testChoiceMissing() {
        NestedChoiceInSequenceDocument doc = NestedChoiceInSequenceDocument.Factory.newInstance();
        NestedChoiceInSequenceT elt = doc.addNewNestedChoiceInSequence();
        elt.setChildDouble(1.3);
        elt.setChildInt(2);
        elt.setChildStr("foo");

        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testAllPresent() {
        NestedChoiceInSequenceDocument doc = NestedChoiceInSequenceDocument.Factory.newInstance();
        NestedChoiceInSequenceT elt = doc.addNewNestedChoiceInSequence();
        elt.setChildDouble(1.3);
        elt.setChildInt(2);
        elt.setChildStr("foo");

        elt.setOptchildDouble(1.4);

        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        //can't have both set
        elt.setOptchildInt(2);
        elt.setOptchildStr("boo");
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        elt.unsetOptchildDouble();
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * Missing elt. from the sequence in the choice
     */
    @Test
    void testIllegal() throws Throwable {
        NestedChoiceInSequenceDocument doc = NestedChoiceInSequenceDocument.Factory.newInstance();
        NestedChoiceInSequenceT elt = doc.addNewNestedChoiceInSequence();
        elt.setChildDouble(1.3);
        elt.setChildInt(2);
        elt.setChildStr("foo");

        elt.setOptchildInt(2);
        //optChildStr is missing
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));


        elt.setOptchildStr("boo");
        assertTrue(doc.validate(validateOptions));
    }

    /**
     * Incorrect order in inner sequence
     */
    @Test
    void testIllegalOrderInner() throws Throwable {
        String input =
            "<pre:NestedChoiceInSequence  " +
            "xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
            "<childStr>foo</childStr>" +
            "<childInt>3</childInt>" +
            "<optchildInt>0</optchildInt>" +
            "<optchildStr>foo</optchildStr>" +
            "</pre:NestedChoiceInSequence>";
        NestedChoiceInSequenceDocument doc = NestedChoiceInSequenceDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        // TODO: why are there 2 different errors: just the order is swapped
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * Incorrect order in outer sequence
     */
    @Test
    void testIllegalOrderOuter() throws Throwable {
        String input =
            "<pre:NestedChoiceInSequence  " +
            "xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
            "<childInt>3</childInt>" +
            "<childStr>foo</childStr>" +
            "<optchildStr>foo</optchildStr>" +
            "<optchildInt>0</optchildInt>" +
            "</pre:NestedChoiceInSequence>";
        NestedChoiceInSequenceDocument doc = NestedChoiceInSequenceDocument.Factory.parse(input);
        assertFalse(doc.validate(createOptions()));
    }
}
