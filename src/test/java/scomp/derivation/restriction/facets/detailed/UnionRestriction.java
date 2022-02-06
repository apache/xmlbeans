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
import xbean.scomp.derivation.facets.union.SmallEnumUnion;
import xbean.scomp.derivation.facets.union.SmallPatternUnion;
import xbean.scomp.derivation.facets.union.UnionEnumEltDocument;
import xbean.scomp.derivation.facets.union.UnionPatternEltDocument;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 * Only pattern and enumeration restrictions possible
 * Compile time tests for the rest
 */
public class UnionRestriction {

    @Test
    void testPatternRestriction() {
        UnionPatternEltDocument doc = UnionPatternEltDocument.Factory.newInstance();
        doc.setUnionPatternElt("small");
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        doc.setUnionPatternElt(1);
        assertTrue(doc.validate(validateOptions));
        SmallPatternUnion elt = SmallPatternUnion.Factory.newInstance();
        elt.setObjectValue(2);
        doc.xsetUnionPatternElt(elt);
        assertTrue(doc.validate(validateOptions));
        doc.setUnionPatternElt(-1);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testEnumRestriction() {
        UnionEnumEltDocument doc = UnionEnumEltDocument.Factory.newInstance();
        doc.setUnionEnumElt("small");
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        doc.setUnionEnumElt(1);
        assertTrue(doc.validate(validateOptions));
        SmallEnumUnion elt = SmallEnumUnion.Factory.newInstance();
        elt.setObjectValue(-1);
        doc.xsetUnionEnumElt(elt);
        assertTrue(doc.validate(validateOptions));
        doc.setUnionEnumElt(2);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_ENUM_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
