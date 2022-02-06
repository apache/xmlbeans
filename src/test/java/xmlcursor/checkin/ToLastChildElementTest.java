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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class ToLastChildElementTest {
    @Test
    void testToLastChildElemSTARTnestedSiblings() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early<bar>text</bar><char>zap</char></foo>")) {
            assertTrue(m_xc.toFirstChild());
            assertTrue(m_xc.toLastChild());
            assertEquals("zap", m_xc.getTextValue());
        }
    }

    @Test
    void testToLastChildElemSTARTnestedSiblingsTwice() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>")) {
            assertTrue(m_xc.toFirstChild());
            assertTrue(m_xc.toLastChild());
            assertTrue(m_xc.toLastChild());
            assertEquals("yap", m_xc.getTextValue());
        }
    }

    @Test
    void testToLastChildElemFromTEXTnested() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early<bar>text<char>zap</char><dar>yap</dar></bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals("early", m_xc.getChars());
            try (XmlCursor xc0 = m_xc.newCursor()) {
                xc0.toNextSibling();

                assertEquals("textzapyap", xc0.getTextValue());
                xc0.toLastChild();
                assertEquals("yap", xc0.getTextValue());
                assertTrue(m_xc.toLastChild());
                assertEquals("yap", m_xc.getTextValue());
            }
        }
    }

    @Test
    void testToLastChildElemFromATTRnested() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\">early<bar>text<char>zap</char><dar>yap</dar></bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("val0", m_xc.getTextValue());
            assertTrue(m_xc.toLastChild());
        }
    }

    @Test
    void testToLastChildElemFromSTARTnoChild() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early</foo>")) {
            assertTrue(m_xc.toFirstChild());
            assertFalse(m_xc.toLastChild());
        }
    }

    @Test
    void testToLastChildElemFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early</foo>")) {
            assertTrue(m_xc.toLastChild());
            assertEquals(TokenType.START, m_xc.currentTokenType());
        }
    }
}

