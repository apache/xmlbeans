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
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xbean.scomp.element.globalEltDefault.GlobalEltDefaultIntDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static xmlcursor.common.BasicCursorTestCase.obj;

public class NodeCopyTest {

    @Test
    void testNS() throws Exception {
        XmlObject s = obj("<a xmlns:ack='abc' ack:attr='val1'>foo<b>bar</b></a>");
        /*
        res=s.selectPath("./a");
        assertTrue(s.selectChildren("","a")[0] == res[0] );
        assertEquals( res[0].xmlText(),"<xml-fragment ack:attr=\"val1\" xmlns:ack=\"abc\">foo<b>bar</b></xml-fragment>");
        //"for $e in ./a return <doc>{ $e } </doc>"
        */
        try (XmlCursor c = s.newCursor();
            XmlCursor s1 = c.execQuery("./a")) {
            assertEquals("<a ack:attr=\"val1\" xmlns:ack=\"abc\">foo<b>bar</b></a>", s1.xmlText());
        }

        XmlObject[] res = s.execQuery("./a");
        XmlObject o;
        try (XmlCursor c1 = s.newCursor()) {
            c1.toFirstContentToken();
            o = c1.getObject();
        }

        assertNotSame(o, res[0]);
        assertEquals("<a ack:attr=\"val1\" xmlns:ack=\"abc\">foo<b>bar</b></a>", res[0].xmlText());
    }

    @Test
    void testText() throws Exception {
        XmlObject s = obj("<a><b>bar</b><c>foo</c></a>");
        XmlObject[] res = s.selectPath(".//text()");
        assertEquals(2, res.length);
        assertEquals("<xml-fragment>bar</xml-fragment>", res[0].xmlText());
        assertEquals("<xml-fragment>foo</xml-fragment>", res[1].xmlText());
    }

    @Test
    void testCount() throws Exception {
        XmlObject s = obj("<a><b>bar</b>foo</a>");
        XmlObject[] res;
        res = s.selectPath("count(.//b)");
        System.out.println(res[0].xmlText());
        XmlLong i = (XmlLong) res[0];
        assertEquals((long) 1, i.getLongValue());
        // res= s.selectPath("//b");
    }

    @Test
    @Disabled
    public void testInt() throws Exception {
        GlobalEltDefaultIntDocument d =
            GlobalEltDefaultIntDocument.Factory
                .parse("<GlobalEltDefaultInt xmlns='http://xbean/scomp/element/GlobalEltDefault'>" +
                       "3" +
                       "</GlobalEltDefaultInt>");
        d.getGlobalEltDefaultInt();
    }

    @Test
    void testXmlObjectSelectPath() {

    }

    @Test
    void testDeleteMe() throws Exception {
        XmlObject t = obj("<a><b/><b/></a>");
        try (XmlCursor cursor = t.newCursor()) {
            // use xpath to select elements
            cursor.selectPath("*/*");

            System.out.println("cnt " + cursor.getSelectionCount());
            // iterate over the selection
            while (cursor.toNextSelection()) {
                // two views of the same data:
                // move back and forth between XmlObject <-> XmlCursor
                XmlObject trans = cursor.getObject();

                System.out.println("Trans " + trans.xmlText());
                System.out.println("xmlText " + cursor.xmlText());
            }
        }
    }
}
