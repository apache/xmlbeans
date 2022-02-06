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

package scomp.contentType.complex.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.complexTypeTest.ElementT;
import xbean.scomp.contentType.complexTypeTest.EltTypeDocument;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class ElementOnlyContentTest {

    /**
     * Element only content
     */
    @Test
    void testElementOnly() throws Throwable {
        EltTypeDocument doc = EltTypeDocument.Factory.newInstance();
        ElementT testElt
                = doc.getEltType();
        assertNull(testElt);
        testElt = doc.addNewEltType();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild1(new BigInteger("10"));
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        assertEquals("<xml-fragment><child1>10</child1><child2>5</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());

        testElt.xsetChild2(XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        assertEquals("<xml-fragment><child1>10</child1><child2>3</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * Mixed content is invalid for element only types
     */
    @Test
    void testInvalidContent() {
        EltTypeDocument doc = EltTypeDocument.Factory.newInstance();
        ElementT testElt = doc.getEltType();
        assertNull(testElt);
        testElt = doc.addNewEltType();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild1(new BigInteger("10"));
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        assertTrue(testElt.validate());
        try (XmlCursor cur = testElt.newCursor()) {
            cur.toFirstContentToken();
            cur.insertChars("Random mixed content");
        }
        XmlOptions validateOptions = createOptions();
        assertFalse(testElt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_ONLY_WITH_TEXT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

}
