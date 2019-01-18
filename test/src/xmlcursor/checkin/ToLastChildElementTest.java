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


public class ToLastChildElementTest extends BasicCursorTestCase {
    @Test
    public void testToLastChildElemSTARTnestedSiblings() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap</char></foo>").newCursor();
        assertTrue(m_xc.toFirstChild());
        assertTrue(m_xc.toLastChild());
        assertEquals("zap", m_xc.getTextValue());
    }

    @Test
    public void testToLastChildElemSTARTnestedSiblingsTwice() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        assertTrue(m_xc.toFirstChild());
        assertTrue(m_xc.toLastChild());
        assertTrue(m_xc.toLastChild());
        assertEquals("yap", m_xc.getTextValue());
    }

    @Test
    public void testToLastChildElemFromTEXTnested() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text<char>zap</char><dar>yap</dar></bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals("early", m_xc.getChars());
        XmlCursor xc0 = m_xc.newCursor();
        xc0.toNextSibling();
        try {
            assertEquals("textzapyap", xc0.getTextValue());
            xc0.toLastChild();
            assertEquals("yap", xc0.getTextValue());
            assertTrue(m_xc.toLastChild());
            assertEquals("yap", m_xc.getTextValue());
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testToLastChildElemFromATTRnested() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\">early<bar>text<char>zap</char><dar>yap</dar></bar></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        assertTrue(m_xc.toLastChild());
    }

    @Test
    public void testToLastChildElemFromSTARTnoChild() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early</foo>").newCursor();
        assertTrue(m_xc.toFirstChild());
        assertFalse(m_xc.toLastChild());
    }

    @Test
    public void testToLastChildElemFromSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early</foo>").newCursor();
        assertTrue(m_xc.toLastChild());
        assertEquals(TokenType.START, m_xc.currentTokenType());
    }
}

