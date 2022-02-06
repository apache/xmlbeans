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

package xmlcursor.xquery.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;


//Used to be a checkin
public class StoreTestsXqrl {
    @ParameterizedTest
    @ValueSource(strings = {
        "<foo xmlns=\"foo.com\"><bar>1</bar></foo>",
        "<foo><!--comment--><?target foo?></foo>",
        "<foo>a<bar>b</bar>c<bar>d</bar>e</foo>",
        "<foo xmlns:x=\"y\"><bar xmlns:x=\"z\"/></foo>",
        "<foo x=\"y\" p=\"r\"/>",
        "<bar>xxxxsssssssssssssss</bar>"
    })
    void doSaveTest(String xml) throws Exception {
        if (xml.startsWith("<bar>")) xml = xml.replace("s", "<foo>aaa</foo>bbb");

        try (XmlCursor c = XmlObject.Factory.parse(xml).newCursor();
             XmlCursor cq = c.execQuery(".")) {
            String s = cq.xmlText();
            assertEquals(s, xml);
        }
    }

    @Test
    void testSaving() throws Exception {
        XmlObject x = XmlObject.Factory.parse("<foo xmlns:a='a.com'><bar xmlns:a='b.com'/></foo>");

        try (XmlCursor c = x.newCursor()) {
            c.toFirstChild();
            c.toFirstChild();

            assertEquals("<bar xmlns:a=\"b.com\"/>", c.xmlText());
        }

        x = XmlObject.Factory.parse("<foo xmlns:a='a.com'><bar/></foo>");

        try (XmlCursor c = x.newCursor()) {
            c.toFirstChild();
            c.toFirstChild();

            assertEquals("<bar xmlns:a=\"a.com\"/>", c.xmlText());
        }
    }

    //
    // Make sure XQuery works (tests the saver too)
    //
    @Test
    void testXQuery() throws Exception {
        String pre = "<xml-fragment>";
        String post = "</xml-fragment>";
        try (XmlCursor c = XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar></foo>").newCursor();
            XmlCursor cq = c.execQuery("for $b in //bar order by ($b) descending return $b")) {
            String actual = cq.xmlText();

            assertTrue(actual.startsWith(pre));
            assertTrue(actual.endsWith(post));
            assertEquals("<bar>2</bar><bar>1</bar>", actual.substring(pre.length(), actual.length() - post.length()));
        }

        try (XmlCursor c = XmlObject.Factory.parse("<foo></foo>").newCursor()) {
            c.toNextToken();
            c.toNextToken();
            c.insertElement("boo", "boo.com");
            c.toStartDoc();

            try (XmlCursor cq = c.execQuery(".")) {
                String s = cq.xmlText();
                assertEquals("<foo><boo:boo xmlns:boo=\"boo.com\"/></foo>", s);
            }
        }
    }

    @Test
    void testPathing() throws Exception {
        XmlObject x =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>");

        try (XmlCursor c = x.newCursor()) {
            c.selectPath("//bar");

            assertTrue(c.toNextSelection());
            assertEquals("<bar>1</bar>", c.xmlText());

            assertTrue(c.toNextSelection());
            assertEquals("<bar>2</bar>", c.xmlText());

            assertTrue(c.toNextSelection());
            assertEquals("<bar>3</bar>", c.xmlText());

            assertFalse(c.toNextSelection());

            x = XmlObject.Factory.parse("<foo><bar x='1'/><bar x='2'/><bar x='3'/></foo>");
        }

        try (XmlCursor c = x.newCursor()) {
            //c.selectPath( "$this//@x" );
            c.selectPath(".//@x");

            assertTrue(c.toNextSelection());
            assertTrue(c.currentTokenType().isAttr());
            assertEquals("1", c.getTextValue());

            assertTrue(c.toNextSelection());
            assertTrue(c.currentTokenType().isAttr());
            assertEquals("2", c.getTextValue());

            assertTrue(c.toNextSelection());
            assertTrue(c.currentTokenType().isAttr());
            assertEquals("3", c.getTextValue());

            assertFalse(c.toNextSelection());

            x = XmlObject.Factory.parse("<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>");
        }

        try (XmlCursor c = x.newCursor()) {
            c.selectPath("//text()");

            assertTrue(c.toNextSelection());
            assertTrue(c.currentTokenType().isText());
            assertEquals("1", c.getChars());

            assertTrue(c.toNextSelection());
            assertTrue(c.currentTokenType().isText());
            assertEquals("2", c.getChars());

            assertTrue(c.toNextSelection());
            assertTrue(c.currentTokenType().isText());
            assertEquals("3", c.getChars());

            assertFalse(c.toNextSelection());
        }
    }
}
