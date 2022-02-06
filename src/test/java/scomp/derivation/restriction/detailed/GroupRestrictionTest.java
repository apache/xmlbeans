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
package scomp.derivation.restriction.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.groupRestriction.*;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 *
 */
public class GroupRestrictionTest {

    @Test
    void testRestrictSequence() {
        RestrictedSequenceEltDocument doc = RestrictedSequenceEltDocument.Factory.newInstance();
        RestrictedSequenceT elt = doc.addNewRestrictedSequenceElt();
        elt.setChild1(BigInteger.ONE);

        elt.addChild3(new BigInteger("10"));
        elt.addChild3(new BigInteger("10"));
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        elt.addChild2("foobar");
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testRestrictChoice() {
        RestrictedChoiceEltDocument doc = RestrictedChoiceEltDocument.Factory.newInstance();
        RestrictedChoiceT elt = doc.addNewRestrictedChoiceElt();
        elt.addChild2("foobar");
        elt.addChild3(BigInteger.ZERO);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        elt.removeChild2(0);
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testRestrictAll() {
        RestrictedAllEltDocument doc = RestrictedAllEltDocument.Factory.newInstance();
        RestrictedAllT elt = doc.addNewRestrictedAllElt();
        elt.setChild2("foobar");
        //child3 can't be missing
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        elt.setChild3(new BigInteger("10"));
        assertTrue(doc.validate(validateOptions));
    }

    @Test
    void testAllToSequence() {
        All2SeqEltDocument doc = All2SeqEltDocument.Factory.newInstance();
        All2SequenceT elt = doc.addNewAll2SeqElt();
        elt.setA("foo");
        elt.setC(3);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        //b not part of restricted type
        elt.setB("bar");
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testChoiceToSequence() throws Throwable {
        Choice2SeqEltDocument doc = Choice2SeqEltDocument.Factory.newInstance();
        Choice2SequenceT elt = doc.addNewChoice2SeqElt();
        elt.addA("foo");
        elt.addC(3);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        //b not part of restricted type
        elt.addB("bar");
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
