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
import org.apache.xmlbeans.XmlInteger;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.named.ElementT;
import xbean.scomp.contentType.named.MixedT;
import xbean.scomp.contentType.named.NamedEltDocument;
import xbean.scomp.contentType.named.NamedMixedEltDocument;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;

public class NamedTest {

    @Test
    void testSimpleAnonymous() throws Throwable {
        NamedEltDocument doc = NamedEltDocument.Factory.newInstance();
        ElementT testElt = doc.getNamedElt();
        assertNull(testElt);
        testElt = doc.addNewNamedElt();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        assertEquals("<xml-fragment><child2>5</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());
        testElt.setChild1(BigInteger.ONE);
        testElt.xsetChild2(XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        assertEquals("<xml-fragment><child1>1</child1><child2>3</child2>" +
                                "<child3>1</child3></xml-fragment>", testElt.xmlText());
        assertTrue(testElt.validate(createOptions()));
    }

    @Test
    void testMixedAnonymous() throws Throwable {
        NamedMixedEltDocument doc = NamedMixedEltDocument.Factory.newInstance();

        MixedT testElt = doc.getNamedMixedElt();
        assertNull(testElt);
        testElt = doc.addNewNamedMixedElt();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        assertEquals(5, testElt.getChild2().intValue());
        XmlInteger expected = XmlInteger.Factory.newInstance();
        expected.setBigIntegerValue(new BigInteger("5"));
        assertTrue(expected.valueEquals(testElt.xgetChild2()));

        try (XmlCursor cur = testElt.newCursor()) {
            cur.toFirstContentToken();
            cur.insertChars("Random mixed content");
        }
        testElt.setChild3(new BigInteger("1"));
        assertEquals("<xml-fragment>Random mixed content" +
                                "<child2>5</child2><child3>1</child3></xml-fragment>", testElt.xmlText());

        testElt.xsetChild2(XmlInteger.Factory.parse("<xml-fragment>3</xml-fragment>"));
        assertEquals("<xml-fragment>" +
                                "Random mixed content" +
                                "<child2>3</child2>" +
                                "<child3>1</child3>" +
                                "</xml-fragment>", testElt.xmlText());
        testElt.xsetChild1(XmlInteger.Factory.parse("<xml-fragment>0</xml-fragment>"));

        assertTrue(testElt.validate(createOptions()));
    }
}
