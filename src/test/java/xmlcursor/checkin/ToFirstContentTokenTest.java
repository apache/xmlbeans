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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;

public class ToFirstContentTokenTest {

    @Test
    void testToFirstContentTokenFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur("<foo>early<bar>text</bar></foo>")) {
            m_xc.toFirstChild();
            m_xc.insertAttributeWithValue("attr0", "val0");
            m_xc.insertNamespace("nsx", "valx");
            m_xc.toStartDoc();
            assertEquals(TokenType.START, m_xc.toFirstContentToken());
            assertEquals("earlytext", m_xc.getTextValue());
        }
    }

    @Test
    void testToFirstContentTokenFromATTR() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"nsuri\">early<bar>text</bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
            assertEquals(TokenType.ATTR, m_xc.currentTokenType());
            assertEquals("val0", m_xc.getTextValue());
        }
    }

    @Test
    void testToFirstContentTokenFromNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"nsuri\">early<bar>text</bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
            assertEquals(TokenType.NAMESPACE, m_xc.currentTokenType());

            assertEquals(m_xc.getTextValue(), "nsuri");
        }
    }

    @Test
    void testToFirstContentTokenFromSTARTwithContent() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"nsuri\">early<bar>text</bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals(TokenType.TEXT, m_xc.toFirstContentToken());
            assertEquals("early", m_xc.getChars());
        }
    }

    @Test
    void testToFirstContentTokenFromSTARTnoContent() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"nsuri\"></foo>")) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals(TokenType.END, m_xc.toFirstContentToken());
            m_xc.toNextToken();
            assertEquals(TokenType.ENDDOC, m_xc.currentTokenType());
        }
    }

    @Test
    void testToFirstContentTokenEmptyDocument() throws Exception {
        try (XmlCursor m_xc = XmlObject.Factory.newInstance().newCursor()) {
            assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
            assertEquals(TokenType.ENDDOC, m_xc.toFirstContentToken());
        }
    }

    @Test
    void testToFirstContentTokenFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"nsuri\"><bar>text</bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
            assertEquals("text", m_xc.getChars());
        }
    }

    @Test
    void testToFirstContentTokenFromTEXTmiddle() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"nsuri\"><bar>text</bar></foo>")) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
            assertEquals("xt", m_xc.getChars());
        }
    }
}

