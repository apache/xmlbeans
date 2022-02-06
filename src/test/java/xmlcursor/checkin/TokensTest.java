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

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class TokensTest {

    @Test
    void testHasNextToken() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertTrue(m_xc.hasNextToken());
        }
    }

    @Test
    void testHasNextTokenENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toEndDoc();
            assertFalse(m_xc.hasNextToken());
        }
    }

    @Test
    void testHasPrevToken() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertTrue(m_xc.hasPrevToken());
        }
    }

    @Test
    void testHasPrevTokenSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            assertFalse(m_xc.hasPrevToken());
        }
    }

    @Test
    void testToEndTokenFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            assertEquals(TokenType.ENDDOC, m_xc.toEndToken());
        }
    }

    @Test
    void testToEndTokenFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals(TokenType.END, m_xc.toEndToken());
        }
    }

    @Test
    void testToEndTokenFromTEXTmiddle() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(1);
            assertEquals(TokenType.NONE, m_xc.toEndToken());
        }
    }

    @Test
    void testToFirstContentTokenFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toFirstContentToken();
            assertEquals(TokenType.START, m_xc.currentTokenType());
        }
    }

    @Test
    void testToFirstContentTokenFromATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
            assertEquals(TokenType.ATTR, m_xc.currentTokenType());
        }
    }

    @Test
    void testToFirstContentTokenFromSTARTwithContent() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals(TokenType.TEXT, m_xc.toFirstContentToken());
        }
    }

    @Test
    void testToFirstContentTokenFromSTARTwithoutContent() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals(TokenType.END, m_xc.toFirstContentToken());
        }
    }

    @Test
    void testToNextTokenFromENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toEndDoc();
            assertEquals(TokenType.NONE, m_xc.toNextToken());
        }
    }

    @Test
    void testToNextTokenNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals(TokenType.NAMESPACE, m_xc.toNextToken());
        }
    }

    @Test
    void testToPrevTokenSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            assertEquals(TokenType.NONE, m_xc.toPrevToken());
            assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
        }
    }

    @Test
    void testToPrevTokenENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toEndDoc();
            assertEquals(TokenType.END, m_xc.toPrevToken());
        }
    }
}

