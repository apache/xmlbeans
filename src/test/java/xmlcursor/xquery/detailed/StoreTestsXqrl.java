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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


//Used to be a checkin
public class StoreTestsXqrl {
    private void doTokenTest(String xml) throws Exception {
        XmlCursor c = XmlObject.Factory.parse(xml).newCursor();
        //String s = c.execQuery( "$this" ).xmlText();
        String s = c.execQuery(".").xmlText();
        assertEquals(s, xml);
    }

    private void doSaveTest(String xml) throws Exception {
        doTokenTest(xml);
    }

    @Test
    public void testSaving() throws Exception {
        doSaveTest("<foo xmlns=\"foo.com\"><bar>1</bar></foo>");
        doSaveTest("<foo><!--comment--><?target foo?></foo>");
        doSaveTest("<foo>a<bar>b</bar>c<bar>d</bar>e</foo>");
        doSaveTest("<foo xmlns:x=\"y\"><bar xmlns:x=\"z\"/></foo>");
        doSaveTest("<foo x=\"y\" p=\"r\"/>");

        String s = "<foo>aaa</foo>bbb";
        s = s + s + s + s + s + s + s + s + s + s + s + s + s + s + s;
        s = "<bar>xxxx" + s + "</bar>";

        doSaveTest(s);

        XmlObject x =
            XmlObject.Factory.parse("<foo xmlns:a='a.com'><bar xmlns:a='b.com'/></foo>");

        XmlCursor c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        assertEquals("<bar xmlns:a=\"b.com\"/>", c.xmlText());

        x = XmlObject.Factory.parse("<foo xmlns:a='a.com'><bar/></foo>");

        c = x.newCursor();

        c.toFirstChild();
        c.toFirstChild();

        assertEquals("<bar xmlns:a=\"a.com\"/>", c.xmlText());
    }

    @Test
    private void testTextFrag(String actual, String expected) {
        String pre = "<xml-fragment>";

        String post = "</xml-fragment>";

        assertTrue(actual.startsWith(pre));
        assertTrue(actual.endsWith(post));

        assertEquals(expected, actual.substring(
            pre.length(), actual.length() - post.length()));
    }

    //
    // Make sure XQuery works (tests the saver too)
    //
    @Test
    public void testXQuery()
        throws Exception {
        XmlCursor c =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar></foo>").newCursor();

        String s =
            c.execQuery("for $b in //bar order by ($b) " +
                        "descending return $b").xmlText();

        testTextFrag(s, "<bar>2</bar><bar>1</bar>");

        c = XmlObject.Factory.parse("<foo></foo>").newCursor();
        c.toNextToken();
        c.toNextToken();
        c.insertElement("boo", "boo.com");
        c.toStartDoc();

        assertEquals("<foo><boo:boo xmlns:boo=\"boo.com\"/></foo>",
            c.execQuery(".").xmlText());
    }

    @Test
    public void testPathing() throws Exception {
        XmlObject x =
            XmlObject.Factory.parse(
                "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>");

        XmlCursor c = x.newCursor();

        c.selectPath("//bar");

        assertTrue(c.toNextSelection());
        assertEquals("<bar>1</bar>", c.xmlText());

        assertTrue(c.toNextSelection());
        assertEquals("<bar>2</bar>", c.xmlText());

        assertTrue(c.toNextSelection());
        assertEquals("<bar>3</bar>", c.xmlText());

        assertTrue(!c.toNextSelection());

        x =
            XmlObject.Factory.parse(
                "<foo><bar x='1'/><bar x='2'/><bar x='3'/></foo>");

        c = x.newCursor();

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

        assertTrue(!c.toNextSelection());

        x = XmlObject.Factory.parse(
            "<foo><bar>1</bar><bar>2</bar><bar>3</bar></foo>");

        c = x.newCursor();

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

        assertTrue(!c.toNextSelection());
    }
}
