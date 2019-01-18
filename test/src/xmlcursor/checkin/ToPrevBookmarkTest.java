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


public class ToPrevBookmarkTest extends BasicCursorTestCase {
    private SimpleBookmark _theBookmark = new SimpleBookmark("value");
    private SimpleBookmark _theBookmark1 = new SimpleBookmark("value1");
    private DifferentBookmark _difBookmark = new DifferentBookmark("diff");

    @Test
    public void testToPrevBookmarkSameKey() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_theBookmark1);
        XmlCursor xc1 = m_xc.newCursor();
        m_xc.toEndDoc();
        try {
            assertEquals(_theBookmark1, m_xc.toPrevBookmark(SimpleBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc1));
            assertEquals(_theBookmark, m_xc.toPrevBookmark(SimpleBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc0));
            assertNull(m_xc.toPrevBookmark(SimpleBookmark.class));
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    @Test
    public void testToPrevBookmarkInvalidKey() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_theBookmark1);
        m_xc.toEndDoc();
        assertNull(m_xc.toPrevBookmark(Object.class));
    }

    @Test
    public void testToPrevBookmarkDifferentKeys() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_difBookmark);
        XmlCursor xc1 = m_xc.newCursor();
        m_xc.toEndDoc();
        try {
            assertEquals(_difBookmark, m_xc.toPrevBookmark(DifferentBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc1));
            assertNull(m_xc.toPrevBookmark(DifferentBookmark.class));
            assertEquals(_theBookmark, m_xc.toPrevBookmark(SimpleBookmark.class));
            assertTrue(m_xc.isAtSamePositionAs(xc0));
        } finally {
            xc0.dispose();
            xc1.dispose();
        }
    }

    @Test
    public void testToPrevBookmarkPostSetTextValue() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(2);
        assertEquals("xt", m_xc.getChars());
        m_xc.setBookmark(_theBookmark);   // set bm in middle of TEXT
        XmlCursor xc1 = m_xc.newCursor();
        xc1.toEndDoc();
        m_xc.toPrevToken();
        m_xc.setTextValue("changed");  // changes text, should destroy bm
        m_xc.toEndDoc();
        try {
            assertNull(xc1.toPrevBookmark(SimpleBookmark.class));
            assertEquals(TokenType.ENDDOC, xc1.currentTokenType());
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

