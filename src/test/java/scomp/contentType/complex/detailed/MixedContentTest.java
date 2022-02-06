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

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.complexTypeTest.MixedFixedEltDocument;
import xbean.scomp.contentType.complexTypeTest.MixedT;
import xbean.scomp.contentType.complexTypeTest.MixedTypeDocument;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class MixedContentTest {

    @Test
    void testElementsOnly() throws Throwable {
        MixedTypeDocument doc = MixedTypeDocument.Factory.newInstance();
        MixedT testElt = doc.addNewMixedType();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));
        assertEquals("<xml-fragment><child1>0</child1><child2>5</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());

        testElt.xsetChild2(
            XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        assertEquals("<xml-fragment><child1>0</child1><child2>3</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());
        assertTrue(testElt.validate(createOptions()));
    }

    /**
     * Note that the mixed model in XML Schema differs fundamentally from the
     * mixed model in XML 1.0. Under the XML Schema mixed model, the order and
     * number of child elements appearing in an instance must agree with the
     * order and number of child elements specified in the model
     */
    @Test
    void testTextOnly() {
        MixedTypeDocument doc = MixedTypeDocument.Factory.newInstance();
        MixedT testElt = doc.addNewMixedType();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        try (XmlCursor cur = testElt.newCursor()) {
            cur.insertChars("Random mixed content");
        }
        XmlOptions validateOptions = createOptions();
        assertFalse(testElt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testMixed() {
        MixedTypeDocument doc = MixedTypeDocument.Factory.newInstance();
        MixedT testElt = doc.addNewMixedType();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));
        assertTrue(testElt.validate());
        try (XmlCursor cur = testElt.newCursor()) {
            cur.toFirstContentToken();
            cur.insertChars("Random mixed content");
            //move past child1
            cur.toNextToken();
            cur.toNextToken();
            cur.toNextToken();
            cur.insertChars("Random mixed content1");

            assertTrue(testElt.validate());
        }
        assertEquals("<xml-fragment>Random mixed content" +
                                "<child1>0</child1>Random mixed content1<child2>5</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());
    }

    @Test
    void testInsertDelete() {
        MixedTypeDocument doc = MixedTypeDocument.Factory.newInstance();
        MixedT testElt = doc.addNewMixedType();
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));
        try (XmlCursor cur = testElt.newCursor()) {
            cur.toFirstContentToken();
            cur.insertChars("Random mixed content");
            //move past child1
            cur.toNextToken();
            cur.toNextToken();
            cur.toNextToken();
            cur.insertChars("Random mixed content1");
            assertTrue(testElt.validate(createOptions()));
            assertEquals("<xml-fragment>Random mixed content" +
                                    "<child1>0</child1>Random mixed content1<child2>5</child2>" +
                                    "<child3>1</child3></xml-fragment>", testElt.xmlText());
            //to child1
            cur.toPrevToken();
            cur.toPrevToken();
            cur.toPrevToken();
            cur.toPrevToken();
            assertEquals(XmlCursor.TokenType.START, cur.currentTokenType());
            assertTrue(cur.removeXml());
            assertNull(testElt.getChild1());

            assertEquals("<xml-fragment>Random mixed content" +
                                    "Random mixed content1<child2>5</child2>" +
                                    "<child3>1</child3></xml-fragment>", testElt.xmlText());
        }
    }

    /**
     * see CR related to CR194159:
     * <p>
     * clause 5.2.2.1 of
     * "Validation Rule: Element Locally Valid (Element)" says
     * if there is a fixed value constraint, the element may not have element children.
     */
    @Test
    void testMixedFixed() throws XmlException {
        String input =
            "<pre:MixedFixedElt " +
            " xmlns:pre=\"http://xbean/scomp/contentType/ComplexTypeTest\">" +
            "<a/>abc</pre:MixedFixedElt>";

        MixedFixedEltDocument doc = MixedFixedEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] expected = {XmlErrorCodes.ELEM_LOCALLY_VALID$FIXED_WITH_CONTENT};
        assertArrayEquals(expected, getErrorCodes(validateOptions));
    }

}
