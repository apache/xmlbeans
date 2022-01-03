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

import static org.junit.Assert.*;


public class ToPrevElementTest extends BasicCursorTestCase {
    @Test
    public void testToPrevElementFromSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        assertFalse(m_xc.toPrevSibling());
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
    }

    @Test
    public void testToPrevElementFromENDDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        try (XmlCursor xc0 = m_xc.newCursor()) {
            xc0.toFirstChild();
            m_xc.toEndDoc();
            m_xc.toPrevSibling();

            assertTrue(m_xc.isAtSamePositionAs(xc0));
        }
    }

    @Test
    public void testToPrevElementSiblings() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        m_xc.selectPath("$this//ear");
        m_xc.toNextSelection();
        assertEquals("yap", m_xc.getTextValue());
        assertTrue(m_xc.toPrevSibling());
        assertEquals("wap", m_xc.getTextValue());
        assertFalse(m_xc.toPrevSibling());
    }

    @Test
    public void testToPrevElementFromATTR() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear attr0=\"val0\">yap</ear></char></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        assertFalse(m_xc.toPrevSibling());
    }

    @Test
    public void testToPrevElementFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear><a/>yap</ear></char></foo>").newCursor();
        m_xc.selectPath("$this//ear");
        m_xc.toNextSelection();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals("yap", m_xc.getChars());
        assertTrue(m_xc.toPrevSibling());
        assertEquals("", m_xc.getTextValue());
    }
}

