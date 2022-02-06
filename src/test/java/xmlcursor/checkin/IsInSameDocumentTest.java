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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class IsInSameDocumentTest {

    @Test
    void testSameDocSTARTDOCandENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
            XmlCursor xc0 = m_xc.newCursor()) {
            xc0.toEndDoc();
            assertTrue(m_xc.isInSameDocument(xc0));
            assertTrue(xc0.isInSameDocument(m_xc));
        }
    }

    @Test
    void testSameDocNAMESPACEandATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
             XmlCursor xc0 = m_xc.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            toNextTokenOfType(xc0, TokenType.ATTR);
            assertTrue(m_xc.isInSameDocument(xc0));
            assertTrue(xc0.isInSameDocument(m_xc));
        }
    }

    @Test
    void testSameDocNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            assertFalse(m_xc.isInSameDocument(null));
        }
    }

    @Test
    void testSameDocDifferentDocs() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.TEXT);

            assertFalse(m_xc.isInSameDocument(xc0));
            assertFalse(xc0.isInSameDocument(m_xc));
        }
    }

    @Test
    void testSameDocTEXTpositional() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = m_xc.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.TEXT);
            xc0.toNextChar(2);

            assertTrue(m_xc.isInSameDocument(xc0));
            assertTrue(xc0.isInSameDocument(m_xc));
        }
    }
}
