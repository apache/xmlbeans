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
import xbean.scomp.derivation.simpleExtension.SimpleRestrictionEltDocument;
import xbean.scomp.derivation.simpleExtension.SimpleRestrictionT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class SimpleContentRestrictionTest {
    @Test
    void testLegalValues() throws Throwable {
        SimpleRestrictionEltDocument doc = SimpleRestrictionEltDocument.Factory.newInstance();
        SimpleRestrictionT elt = doc.addNewSimpleRestrictionElt();
        elt.setIntValue(3);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testIllegalValues() throws Throwable {
        SimpleRestrictionEltDocument doc = SimpleRestrictionEltDocument.Factory.newInstance();
        SimpleRestrictionT elt = doc.addNewSimpleRestrictionElt();
        elt.setIntValue(5);

        XmlOptions validateOptions = createOptions();

        assertFalse(doc.validate(validateOptions));

        String[] errExpected = {XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
