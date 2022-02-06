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
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class RemoveCharsTest {
    @Test
    void testRemoveCharsLTLengthFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(3, m_xc.removeChars(3));
            assertEquals("34", m_xc.getChars());
        }
    }

    @Test
    void testRemoveCharsGTLengthFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(5, m_xc.removeChars(10));
            assertEquals(TokenType.END, m_xc.currentTokenType());
        }
    }

    @Test
    void testRemoveCharsNegativeFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(5, m_xc.removeChars(-1));
            assertEquals(TokenType.END, m_xc.currentTokenType());
        }
    }

    @Test
    void testRemoveCharsZeroFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(0, m_xc.removeChars(0));
            assertEquals("01234", m_xc.getChars());
        }
    }

    @Test
    void testRemoveCharsFromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            assertEquals(0, m_xc.removeChars(3));
        }
    }

    @Test
    void testRemoveCharsFromNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            assertEquals(0, m_xc.removeChars(3));
        }
    }
}

