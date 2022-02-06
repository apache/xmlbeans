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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class InsertCharsTest {

    @Test
    void testInsertCharsAtSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            m_xc.insertChars(" new chars ");
            m_xc.toPrevToken();
            assertEquals(" new chars ", m_xc.getChars());
        }
    }

    @Test
    void testInsertCharsAtSTARTnonEmptyPriorTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_WS_ONLY)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            m_xc.insertChars("new chars ");
            m_xc.toPrevToken();
            assertEquals(" new chars ", m_xc.getChars());
        }
    }

    @Test
    void testInsertCharsAtENDnonEmptyPriorTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_WS_ONLY)) {
            m_xc.selectPath("$this//bar");
            toNextTokenOfType(m_xc, TokenType.END);
            m_xc.insertChars("new chars ");
            m_xc.toPrevToken();
            assertEquals(" new chars ", m_xc.getChars());
        }
    }

    @Test
    void testInsertCharsInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertChars("new chars ");
            assertEquals("xt", m_xc.getChars());
            m_xc.toPrevToken();
            assertEquals("tenew chars xt", m_xc.getTextValue());
        }
    }

    @Test
    void testInsertCharsNullInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertChars(null);
            assertEquals("xt", m_xc.getChars());
            m_xc.toPrevToken();
            assertEquals("text", m_xc.getTextValue());
        }
    }

    @Test
    void testInsertCharsEmptyInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertChars("");
            assertEquals("xt", m_xc.getChars());
            m_xc.toPrevToken();
            assertEquals("text", m_xc.getTextValue());
        }
    }

    @Test
    void testInsertCharsInNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            assertThrows(IllegalStateException.class, () -> m_xc.insertChars("fred"));
        }
    }

}

