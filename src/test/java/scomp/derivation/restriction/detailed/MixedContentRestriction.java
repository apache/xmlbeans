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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.mixedContentRestriction.*;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class MixedContentRestriction {
    @Test
    void testRestrictedMixed() {
        MixedEltDocument doc = MixedEltDocument.Factory.newInstance();
        RestrictedMixedT elt = doc.addNewMixedElt();
        assertFalse(elt.isSetChild1());
        elt.setChild1(new BigInteger("10"));
        elt.setChild2(BigInteger.ZERO);
        //insert text b/n the 2 elements
        try (XmlCursor cur = elt.newCursor()) {
            cur.toFirstContentToken();
            assertTrue(cur.toNextSibling());
            cur.insertChars("My chars");
        }
        assertTrue(doc.validate(createOptions()));

        assertEquals("<xml-fragment><child1>10</child1>My chars<child2>0</child2></xml-fragment>", elt.xmlText());
    }

    @Test
    void testRestrictedEltOnly() throws Throwable {
        XmlOptions validateOptions = createOptions();
        ElementOnlyEltDocument doc = ElementOnlyEltDocument.Factory.newInstance();
        RestrictedEltT elt = doc.addNewElementOnlyElt();
        assertFalse(elt.isSetChild1());
        elt.setChild1(new BigInteger("10"));
        elt.setChild2(BigInteger.ZERO);
        //insert text b/n the 2 elements
        try (XmlCursor cur = elt.newCursor()) {
            cur.toFirstContentToken();
            assertTrue(cur.toNextSibling());
            cur.insertChars("My chars");
            assertFalse(doc.validate(validateOptions));
            String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_ONLY_WITH_TEXT};
            assertArrayEquals(errExpected, getErrorCodes(validateOptions));

            //should be valid w/o the Text there
            cur.toPrevToken();
            assertEquals("<xml-fragment><child1>10</child1>My chars<child2>0</child2></xml-fragment>", elt.xmlText());
            assertTrue(cur.removeXml());
        }
        assertTrue(doc.validate(validateOptions));
        assertEquals("<xml-fragment><child1>10</child1><child2>0</child2></xml-fragment>", elt.xmlText());
    }

    //seems that this is not a valid example p.329 top
    @Test
    void testRestrictedMixedToEmpty() throws Throwable {
        Mixed2EmptyEltDocument doc = Mixed2EmptyEltDocument.Factory.newInstance();
        Mixed2EmptyT elt = doc.addNewMixed2EmptyElt();
        assertNull(elt.xgetChild1());

        // ok this gets a little tricky. Due to the restriction extension, the setter method is now
        // 'removed'. So the schema is actually an XmlAnyType while the method sets it to a BigInteger.
        // This will fail irrespective of the setValidateOnset XmlOption
        assertThrows(XmlValueNotSupportedException.class, () -> elt.setChild1(new BigInteger("10")));

        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
