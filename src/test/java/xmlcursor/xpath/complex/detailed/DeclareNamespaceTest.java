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
package xmlcursor.xpath.complex.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.obj;

public class DeclareNamespaceTest {

    @Test
    void testDeclareNSPath() throws Exception {
        XmlObject s = obj("<a xmlns:ack='abc' ack:attr='val1'>foo<b>bar</b></a>");
        /*
        res=s.selectPath("./a");
        assertTrue(s.selectChildren("","a")[0] == res[0] );
        assertEquals( res[0].xmlText(),"<xml-fragment ack:attr=\"val1\" xmlns:ack=\"abc\">foo<b>bar</b></xml-fragment>");
        //"for $e in ./a return <doc>{ $e } </doc>"
        */
        String query = "declare namespace ack='abc'; .//@ack:attr";
        try (XmlCursor s1 = s.newCursor()) {
            s1.selectPath(query);
            assertEquals(1, s1.getSelectionCount());
            s1.toNextSelection();
            assertEquals("<xml-fragment ack:attr=\"val1\" xmlns:ack=\"abc\"/>", s1.xmlText());
        }

        XmlObject[] res = s.execQuery(query);
        XmlObject o;
        try (XmlCursor c1 = s.newCursor()) {
            c1.toFirstContentToken();
            o = c1.getObject();
        }

        assertNotSame(o, res[0]);
        assertEquals("<xml-fragment ack:attr=\"val1\" xmlns:ack=\"abc\"/>", res[0].xmlText());
    }

    @Test
    void testDefaultNSPath() throws Exception {
        XmlObject s = obj("<a xmlns='abc'>foo<b>bar</b></a>");
        String query = "declare default element namespace 'abc'; .//b[position()=last()]";
        /*
        XmlCursor s1=s.newCursor();
        s1.selectPath(query);
        assertEquals(1,s1.getSelectionCount());
        s1.toNextSelection();
        assertEquals( s1.xmlText(),"<b xmlns=\"abc\">bar</b>");
        */
        XmlObject[] res = s.execQuery(query);
        try (XmlCursor c1 = s.newCursor()) {
            c1.toFirstContentToken();
            XmlObject o = c1.getObject();
            assertNotSame(o, res[0]);
            assertEquals(res[0].xmlText(), "<abc:b xmlns:abc=\"abc\">bar</abc:b>");
        }
    }

    @Test
    void testSequence() throws Exception {
        XmlObject o = obj("<a xmlns='abc'>foo<b>bar</b></a>");
        XmlObject[] res = o.selectPath("count(//*:a), count(//*:b)");
        assertEquals(2, res.length);
        XmlLong a = ((XmlLong) res[0]);
        String expXml = "<xml-fragment>1</xml-fragment>";
        assertEquals(expXml, a.xmlText());
        a = ((XmlLong) res[1]);
        assertEquals(expXml, a.xmlText());

        //Should evaluate to the sequence:
        // 10, 1, 2, 3, 4

        res = o.selectPath("(10, 1 to 4)");
        assertEquals(5, res.length);
        a = ((XmlLong) res[0]);
        expXml = "<xml-fragment>10</xml-fragment>";
        assertEquals(expXml, a.xmlText());
        for (int i = 1; i < 5; i++) {
            a = ((XmlLong) res[i]);
            assertEquals(Common.wrapInXmlFrag(i + ""), a.xmlText());
        }
    }

    @Test
    void testSequenceUnion() throws Exception {
        XmlObject o = obj("<a><b>1</b>1</a>");
        XmlObject[] res = o.selectPath("//a union //b");
        assertEquals(2, res.length);
        //node a
        assertEquals("<xml-fragment><b>1</b>1</xml-fragment>", res[0].xmlText());
        //node b
        assertEquals("<xml-fragment>1</xml-fragment>", res[1].xmlText());
    }

    @Test
    void testSequenceIntersect() throws Exception {
        try (XmlCursor o = cur("<a><b>1</b>1</a>")) {
            o.selectPath("//b intersect //b");
            assertEquals(1, o.getSelectionCount());
            o.toNextSelection();
            assertEquals("<b>1</b>", o.xmlText());
        }
    }

    @Test
    void testSequenceExcept() throws Exception {
        try (XmlCursor o = cur("<a><b>1</b>1</a>")) {
            o.selectPath("/a except /a");
            assertEquals(0, o.getSelectionCount());
            o.selectPath("//* except //b");
            assertEquals(1, o.getSelectionCount());
            o.toNextSelection();
            assertEquals("<a><b>1</b>1</a>", o.xmlText());
        }
    }

    //If an operand of union, intersect, or except
    // contains an item that is not a node, a type error is raised.
    @Test
    void testSequenceTypeError() throws XmlException {
        try (XmlCursor o = obj("<a/>").newCursor()) {
            o.selectPath("(0 to 4) except (0 to 4)");
            assertThrows(RuntimeException.class, o::toNextSelection);
        }
    }
}
