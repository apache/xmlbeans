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

package dom.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import xbean.dom.complexTypeTest.ElementT;
import xbean.dom.complexTypeTest.EltTypeDocument;
import xbean.dom.complexTypeTest.MixedT;
import xbean.dom.complexTypeTest.MixedTypeDocument;
import xbean.dom.dumbNS.RootDocument;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DirtyCacheTests {
    @Test
    void testDirtyValue() throws Exception {
        EltTypeDocument o = EltTypeDocument.Factory.newInstance();
        ElementT t = o.addNewEltType();
        t.setChild1(new BigInteger("30"));
        Node n = o.getDomNode();
        n = n.getFirstChild();
        n = n.getFirstChild();
        n = n.getFirstChild();
        assertEquals("#text", n.getNodeName());
        assertEquals("30", n.getNodeValue());
        t.setChild1(new BigInteger("5"));
        assertEquals("#text", n.getNodeName());
        assertEquals("5", n.getNodeValue());
    }

    @Test
    void testDirtyValue1() throws Exception {
        MixedTypeDocument o = MixedTypeDocument.Factory.newInstance();
        MixedT testElt = o.addNewMixedType();
        assertNull(testElt.getChild1());
        assertNull(testElt.xgetChild1());
        testElt.setChild2(new BigInteger("5"));
        testElt.setChild3(new BigInteger("1"));
        testElt.setChild1(new BigInteger("0"));

        try (XmlCursor cur = testElt.newCursor()) {
            cur.toFirstContentToken();
            cur.insertChars("Random mixed content");
        }
        Node n = o.getDomNode();
        n = n.getFirstChild();
        n = n.getFirstChild();

        assertEquals("#text", n.getNodeName());
        assertEquals("Random mixed content", n.getNodeValue());
        n = n.getNextSibling();
        n = n.getFirstChild();

        assertEquals("#text", n.getNodeName());
        assertEquals("0", n.getNodeValue());
    }

    @Test
    void testDirtyValue2() throws Exception {
        RootDocument o = RootDocument.Factory.newInstance();
        RootDocument.Root testElt = o.addNewRoot();
        testElt.setB(new BigInteger("5"));
        Node n = o.getDomNode();
        n = n.getFirstChild();
        n = n.getAttributes().getNamedItem("b");

        assertEquals("b", n.getNodeName());
        assertEquals("5", n.getNodeValue());
    }
}
