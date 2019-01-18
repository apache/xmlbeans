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


package xmlcursor.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.*;


public class ToNextBookmarkTest extends BasicCursorTestCase {
    private SimpleBookmark _theBookmark = new SimpleBookmark("value");
    private SimpleBookmark _theBookmark1 = new SimpleBookmark("value1");
    private DifferentBookmark _difBookmark = new DifferentBookmark("diff");

    @Test
    public void testToNextBookmarkSameKey() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_theBookmark1);
        XmlCursor xc1 = m_xc.newCursor();
        m_xc.toStartDoc();
        try {
            assertEquals(_theBookmark, m_xc.toNextBookmark(SimpleBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc0));
            assertEquals(_theBookmark1, m_xc.toNextBookmark(SimpleBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc1));
            assertNull(m_xc.toNextBookmark(SimpleBookmark.class));
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    @Test
    public void testToNextBookmarkInvalidKey() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_theBookmark1);
        m_xc.toStartDoc();
        assertNull(m_xc.toNextBookmark(Object.class));
    }

    @Test
    public void testToNextBookmarkDifferentKeys() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_difBookmark);
        XmlCursor xc1 = m_xc.newCursor();
        m_xc.toStartDoc();
        try {
            assertEquals(_theBookmark, m_xc.toNextBookmark(SimpleBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc0));
            assertNull(m_xc.toNextBookmark(SimpleBookmark.class));
            assertEquals(_difBookmark, m_xc.toNextBookmark(DifferentBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc1));
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    @Test
    public void testToNextBookmarkPostRemoveChars() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("234", m_xc.getChars());
        m_xc.setBookmark(_theBookmark);  // set bookmark at '2'
        m_xc.toPrevChar(2);
        assertEquals(3, m_xc.removeChars(3));  // '2' should be deleted, along w/ bookmark
        assertEquals("34", m_xc.getChars());
        XmlCursor xc1 = m_xc.newCursor();
        xc1.toStartDoc();
        try {
            assertNull(xc1.toNextBookmark(SimpleBookmark.class));
            assertEquals(TokenType.STARTDOC, xc1.currentTokenType());
        } finally {
            xc1.dispose();
        }
    }

    public class SimpleBookmark extends XmlCursor.XmlBookmark {
        public String text;

        SimpleBookmark(String text) {
            this.text = text;
        }
    }

    public class DifferentBookmark extends XmlCursor.XmlBookmark {
        public String text;

        DifferentBookmark(String text) {
            this.text = text;
        }
    }

}

