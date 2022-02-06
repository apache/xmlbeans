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
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xmlcursor.common.BasicCursorTestCase.*;


public class SetBookmarkTest {
    private static final SimpleBookmark _theBookmark = new SimpleBookmark("value");
    private static final SimpleBookmark _theBookmark1 = new SimpleBookmark("value1");

    @Test
    void testSetBookmarkAtSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            m_xc.setBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkDuplicateKey() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            m_xc.setBookmark(_theBookmark);
            m_xc.setBookmark(_theBookmark1);
            // should overwrite the Bookmark of same key analogous to hashtable
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value1", sa.text);
        }
    }

    @Test
    void testSetBookmarkDuplicateKeyDifferentLocation() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            m_xc.setBookmark(_theBookmark);
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.setBookmark(_theBookmark1);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value1", sa.text);
            m_xc.toStartDoc();
            sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkAtSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.setBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkAtATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            m_xc.setBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkAtPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            m_xc.setBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(3);
            m_xc.setBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkAtENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR)) {
            toNextTokenOfType(m_xc, TokenType.END);
            m_xc.setBookmark(_theBookmark);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertEquals("value", sa.text);
        }
    }

    @Test
    void testSetBookmarkNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR)) {
            toNextTokenOfType(m_xc, TokenType.END);
            m_xc.setBookmark(null);
            SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
            assertNull(sa);
        }
    }

    @Test
    void testXmlDocumentProperties() throws Exception {
        XmlObject m_xo = obj(Common.XML_FOO_1ATTR);
        m_xo.documentProperties().put("fredkey", "fredvalue");
        try (XmlCursor m_xc = m_xo.newCursor()) {
            assertEquals("fredvalue", m_xo.documentProperties().get("fredkey"));
        }
    }


    private static class SimpleBookmark extends XmlCursor.XmlBookmark {
        public String text;

        SimpleBookmark(String text) {
            this.text = text;
        }
    }

}

