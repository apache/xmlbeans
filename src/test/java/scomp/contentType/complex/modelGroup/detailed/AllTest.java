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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.modelGroup.AllEltDocument;
import xbean.scomp.contentType.modelGroup.AllT;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class AllTest {

    /**
     * Instance should be valid w/ child1 missing
     */
    @Test
    void testChild1Optional() {
        AllEltDocument doc = AllEltDocument.Factory.newInstance();
        AllT elt = doc.addNewAllElt();
        elt.setChild2("doo");
        elt.setChild3(BigInteger.ONE);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * All group doesn't care about field order
     */
    @Test
    void testOrder() throws XmlException {
        String input =
            "<pre:AllElt xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
            "<child3>5</child3><child1>2</child1><child2>bar</child2>" +
            "</pre:AllElt>";
        AllEltDocument doc = AllEltDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * maxOccurs is always 1
     */
    @Test
    void testIllegal() throws XmlException {
        String input =
            "<pre:AllElt xmlns:pre=\"http://xbean/scomp/contentType/ModelGroup\">" +
            "<child3>5</child3><child3>2</child3><child2>bar</child2>" +
            "</pre:AllElt>";
        AllEltDocument doc = AllEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        //$TODO: QUESTIONABLE ERRORS: if <child3>2</child3> is replaced by <child1>2</child1>
        //all will be good: why 2 errors?
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
//            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
//            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
