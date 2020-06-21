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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import static org.junit.Assert.*;

public class ToNextElementTest extends BasicCursorTestCase {
    @Test
    public void testToNextElementFromSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar></foo>").newCursor();
        assertFalse(m_xc.toNextSibling());
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
    }

    @Test
    public void testToNextElementSiblings() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        assertTrue(m_xc.toFirstChild());
        assertTrue(m_xc.toFirstChild());
        assertEquals("text", m_xc.getTextValue());
        assertTrue(m_xc.toNextSibling());
        assertEquals("zapwapyap", m_xc.getTextValue());
        assertFalse(m_xc.toNextSibling());
    }

    @Test
    public void testToNextElementFromATTR() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\">early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val0", m_xc.getTextValue());
        assertTrue(m_xc.toNextSibling());
    }

    @Test
    public void testToNextElementFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\">early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals("early", m_xc.getChars());
        assertTrue(m_xc.toNextSibling());
        assertEquals("text", m_xc.getTextValue());
    }
}

