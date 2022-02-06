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

public class ToNextElementTest {
    @Test
    void testToNextElementFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early<bar>text</bar></foo>")) {
            assertFalse(m_xc.toNextSibling());
            assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
        }
    }

    @Test
    void testToNextElementSiblings() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>")) {
            assertTrue(m_xc.toFirstChild());
            assertTrue(m_xc.toFirstChild());
            assertEquals("text", m_xc.getTextValue());
            assertTrue(m_xc.toNextSibling());
            assertEquals("zapwapyap", m_xc.getTextValue());
            assertFalse(m_xc.toNextSibling());
        }
    }

    @Test
    void testToNextElementFromATTR() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\">early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>")) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("val0", m_xc.getTextValue());
            assertTrue(m_xc.toNextSibling());
        }
    }

    @Test
    void testToNextElementFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\">early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>")) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals("early", m_xc.getChars());
            assertTrue(m_xc.toNextSibling());
            assertEquals("text", m_xc.getTextValue());
        }
    }
}

