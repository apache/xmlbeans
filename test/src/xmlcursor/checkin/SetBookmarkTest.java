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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SetBookmarkTest extends BasicCursorTestCase {
    private SimpleBookmark _theBookmark = new SimpleBookmark("value");
    private SimpleBookmark _theBookmark1 = new SimpleBookmark("value1");

    @Test
    public void testSetBookmarkAtSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.setBookmark(_theBookmark);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkDuplicateKey() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.setBookmark(_theBookmark);
        m_xc.setBookmark(_theBookmark1);
        // should overwrite the Bookmark of same key analogous to hashtable
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value1", sa.text);
    }

    @Test
    public void testSetBookmarkDuplicateKeyDifferentLocation() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.setBookmark(_theBookmark);
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.setBookmark(_theBookmark1);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value1", sa.text);
        m_xc.toStartDoc();
        sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkAtSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        m_xc.setBookmark(_theBookmark);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkAtATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        m_xc.setBookmark(_theBookmark);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkAtPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        m_xc.setBookmark(_theBookmark);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkInMiddleOfTEXT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(3);
        m_xc.setBookmark(_theBookmark);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkAtENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(_theBookmark);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertEquals("value", sa.text);
    }

    @Test
    public void testSetBookmarkNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        m_xc.setBookmark(null);
        SimpleBookmark sa = (SimpleBookmark) m_xc.getBookmark(SimpleBookmark.class);
        assertNull(sa);
    }

    @Test
    public void testXmlDocumentProperties() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR);
        m_xo.documentProperties().put("fredkey", "fredvalue");
        m_xc = m_xo.newCursor();
        assertEquals("fredvalue", m_xo.documentProperties().get("fredkey"));
    }


    public class SimpleBookmark extends XmlCursor.XmlBookmark {
        public String text;

        SimpleBookmark(String text) {
            this.text = text;
        }
    }

}

