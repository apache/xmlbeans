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


package xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;


public class ToBookmarkTest extends BasicCursorTestCase {
    private SimpleBookmark _theBookmark = new SimpleBookmark("value");
    private SimpleBookmark _theBookmark1 = new SimpleBookmark("value1");

    @Test
    public void testToBookmarkPrior() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toEndDoc();
            assertTrue(xc1.toBookmark(_theBookmark));

            assertTrue(m_xc.isAtSamePositionAs(xc1));
            SimpleBookmark sa = (SimpleBookmark) xc1.getBookmark(_theBookmark.getClass());
            assertEquals("value", sa.text);
        }
    }

    @Test
    public void testToBookmarkPost() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toStartDoc();
            assertTrue(xc1.toBookmark(_theBookmark));

            assertTrue(m_xc.isAtSamePositionAs(xc1));
            SimpleBookmark sa = (SimpleBookmark) xc1.getBookmark(_theBookmark.getClass());
            assertEquals("value", sa.text);
        }
    }

    @Test
    public void testToBookmarkNULL() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toEndDoc();
            assertFalse(xc1.toBookmark(null));

            assertFalse(m_xc.isAtSamePositionAs(xc1));
            assertEquals(TokenType.ENDDOC, xc1.currentTokenType());
        }
    }

    @Test
    public void testToBookmarkDifferentDoc() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        XmlObject xo = XmlObject.Factory.parse(Common.XML_FOO);
        try (XmlCursor xc1 = xo.newCursor()) {
            assertFalse(m_xc.isInSameDocument(xc1));
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.setBookmark(_theBookmark);

            assertFalse(xc1.toBookmark(_theBookmark));
            assertFalse(m_xc.isInSameDocument(xc1));
        }
    }

    @Test
    public void testPostMoveBookmarkInsideMove() throws Exception {
        m_xo = XmlObject.Factory.parse(JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        String exp_ns = "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";

        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = m_xo.newCursor()) {
            m_xc.selectPath(ns + " $this//po:shipTo/po:city");
            while (m_xc.toNextSelection()) {
                m_xc.setBookmark(_theBookmark);
                xc1.selectPath(ns + " $this//po:billTo/po:city");
                while (xc1.toNextSelection()) {
                    m_xc.moveXml(xc1);
                    try {
                        assertTrue(xc1.toBookmark(_theBookmark));
                        assertEquals("<po:city " + exp_ns + ">Mill Valley</po:city>", xc1.xmlText());
                        xc1.toNextSibling();
                        assertEquals("<po:city " + exp_ns + ">Old Town</po:city>", xc1.xmlText());
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Test
    public void testPostMoveBookmarkToRightOfMove() throws Exception {
        m_xo = XmlObject.Factory.parse(JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        String exp_ns = "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";

        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = m_xo.newCursor()) {
            m_xc.selectPath(ns + " $this//po:shipTo/po:city");
            while (m_xc.toNextSelection()) {
                m_xc.setBookmark(_theBookmark);
                toNextTokenOfType(m_xc, TokenType.TEXT);
                m_xc.toNextToken();
                m_xc.toNextToken();  // move to behind the <city>Mill Valley</city> element
                assertEquals(TokenType.TEXT, m_xc.currentTokenType());
                m_xc.setBookmark(_theBookmark1);
                m_xc.toBookmark(_theBookmark);
                xc1.selectPath(ns + " $this//po:billTo/po:city");
                while (xc1.toNextSelection()) {
                    m_xc.moveXml(xc1);
                    m_xc.toStartDoc();
                    try {
                        assertTrue(xc1.toBookmark(_theBookmark1));
                        xc1.toPrevSibling();
                        assertEquals("<po:street " + exp_ns + ">123 Maple Street</po:street>", xc1.xmlText());
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Test
    public void testToBookmarkPostCopy() throws Exception {
        m_xo = XmlObject.Factory.parse(JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = m_xo.newCursor()) {
            String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
            String exp_ns = "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";

            m_xc.selectPath(ns + " $this//po:shipTo/po:city");
            while (m_xc.toNextSelection()) {
                m_xc.setBookmark(_theBookmark);
                xc1.selectPath(ns + "$this//po:billTo/po:city");
                while (xc1.toNextSelection()) {
                    m_xc.copyXml(xc1);
                    try {
                        assertTrue(xc1.toBookmark(_theBookmark));
                        assertEquals("<po:city " + exp_ns + ">Mill Valley</po:city>", xc1.xmlText());
                        xc1.toNextSibling();
                        assertEquals("<po:state " + exp_ns + ">CA</po:state>", xc1.xmlText());
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Test
    public void testToBookmarkPostMoveChars() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = m_xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            xc1.toCursor(m_xc);
            xc1.toNextChar(1);
            xc1.setBookmark(_theBookmark);  // set an Bookmark at the '1'
            xc1.toNextChar(2);  // move xc1 to the '3'

            assertEquals("34", xc1.getTextValue());
            assertEquals(2, m_xc.moveChars(2, xc1));
            assertEquals("20134", m_xc.getTextValue());
            assertEquals("34", xc1.getTextValue());
            xc1.toBookmark(_theBookmark);
            assertEquals("134", xc1.getTextValue());
        }
    }

    /**
     * Purpose of the test:
     * start w/ 01234, copy the first two characters b/n 3 and 4
     * result should be 0123*01*4  where * shows the new insert
     */
    @Test
    public void testToBookmarkPostCopyChars() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        // XmlCursor xc1 = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try (XmlCursor xc1 = m_xc.newCursor()) {
            //xc1.toCursor(m_xc);
            xc1.toNextChar(1);
            // set a Bookmark at the '1', text is 1234
            xc1.setBookmark(_theBookmark);
            // move xc1 to the '3' , text post cursor is 34
            xc1.toNextChar(2);

            assertEquals("34", xc1.getTextValue());
            //text at m_xc is 01234, should get 0123*01*4
            assertEquals(2, m_xc.copyChars(2, xc1));
            assertEquals("0120134", m_xc.getTextValue());
            assertEquals("34", xc1.getTextValue());
            xc1.toBookmark(_theBookmark);
            assertEquals("120134", xc1.getTextValue());
        }
    }

    @Test
    public void testDumb() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo>01234</foo>");
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            xc1.toNextChar(2);
            assertEquals(2, m_xc.copyChars(2, xc1));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDumbDelete() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo>01234</foo>");
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            m_xc.toNextChar(2);
            m_xc.setBookmark(_theBookmark);
            m_xc.toStartDoc();
            //remove the text , bookmark goes bye bye too
            xc1.removeXml();
            xc1.toCursor(m_xc);
            //both at start of original doc
            assertEquals(m_xc.currentTokenType(),
                    XmlCursor.TokenType.STARTDOC);
            assertTrue(m_xc.isAtSamePositionAs(xc1));
            //move xc1 to outer space
            xc1.toBookmark(_theBookmark);
            assertTrue(!m_xc.isInSameDocument(xc1));
            assertTrue(!m_xc.isLeftOf(xc1));
        }
    }

    @Test
    public void testToBookmarkPostRemove() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath(".//bar");
        while (m_xc.toNextSelection())
            assertEquals("<bar>text</bar>", m_xc.xmlText());
        m_xc.toNextToken();
        m_xc.setBookmark(_theBookmark);  // set annot. at 'text'

        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) xc1.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
            m_xc.toStartDoc();
            xc1.toPrevToken();
            xc1.removeXml();
            xc1.toStartDoc();
            assertTrue(m_xc.isAtSamePositionAs(xc1));
            assertEquals("<foo/>", m_xc.xmlText());
            //test modified, the two cursors are not in the same
            //tree anymore
            assertTrue(xc1.toBookmark(_theBookmark));
            assertTrue(!xc1.isInSameDocument(m_xc));
//            assertTrue(!xc1.isLeftOf(m_xc));

            sa = (SimpleBookmark) xc1.getBookmark(SimpleBookmark.class);
            assertNotNull(sa);
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
        }
    }

    @Test
    public void testToBookmarkPostRemoveAttribute() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        m_xc.setBookmark(_theBookmark);  // set annot. at attribute
        m_xc.toStartDoc();
        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) xc1.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
            xc1.toEndDoc();

            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.removeAttribute(new QName("attr0"));
            m_xc.toStartDoc();

            assertEquals("<foo>text</foo>", m_xc.xmlText());
            assertTrue(xc1.toBookmark(_theBookmark));
            assertTrue(!xc1.isInSameDocument(m_xc));
        }
    }

    @Test
    public void testToBookmarkPostRemoveChars() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("234", m_xc.getTextValue());
        m_xc.setBookmark(_theBookmark);  // set annot. at '2'
        m_xc.toPrevChar(2);
        assertEquals(3, m_xc.removeChars(3));  // '2' should be deleted
        assertEquals("34", m_xc.getTextValue());
        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toEndDoc();

            assertTrue(xc1.toBookmark(_theBookmark));
            assertTrue(!xc1.isInSameDocument(m_xc));
            SimpleBookmark sa =
                    (SimpleBookmark) xc1.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
        }
    }

    @Test
    public void testToBookmarkPostSetTextValue() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getTextValue());
        m_xc.setBookmark(_theBookmark);   // set annot. in middle of TEXT
        try (XmlCursor xc1 = m_xc.newCursor()) {
            xc1.toEndDoc();
            m_xc.toPrevToken();
            m_xc.setTextValue("changed");
            m_xc.toStartDoc();
            assertEquals("<foo>changed</foo>", m_xc.xmlText());

            assertTrue(xc1.toBookmark(_theBookmark));
            assertTrue(!xc1.isInSameDocument(m_xc));
            SimpleBookmark sa = (SimpleBookmark) xc1.getBookmark(SimpleBookmark.class);
               assertEquals("value", sa.text);
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
        }

    }

    public class SimpleBookmark extends XmlCursor.XmlBookmark {
        public String text;

        SimpleBookmark(String text) {
            this.text = text;
        }
    }

}

