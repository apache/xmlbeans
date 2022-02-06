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

public class SetTextTest {
    @Test
    void testSetTextFromCOMMENT() throws Exception {

        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            m_xc.setTextValue("fred");
            assertEquals("fred", m_xc.getTextValue());
        }
    }

    @Test
    void testSetTextFromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            m_xc.setTextValue("new procinst text");
            assertEquals("new procinst text", m_xc.getTextValue());
        }
    }

    @Test
    void testSetTextFromPROCINSTInputNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            m_xc.setTextValue(null);
            assertEquals("", m_xc.getTextValue());
        }
    }

    @Test
    void testSetTextFromEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            toNextTokenOfType(m_xc, TokenType.END);
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue("fred"));
        }
    }

    @Test
    void testSetTextFromENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            m_xc.toEndDoc();
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue("fred"));
        }
    }

    @Test
    void testSetTextFromTEXTbegin() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals("01234", m_xc.getChars());
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue("new text"));
        }
    }

    @Test
    void testSetTextFromTEXTmiddle() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("234", m_xc.getChars());
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue("new text"));
        }
    }

    @Test
    void testSetTextFromSTARTnotNested() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals("01234", m_xc.getTextValue());
            m_xc.setTextValue("new text");
            assertEquals("new text", m_xc.getTextValue());
        }
    }

    @Test
    void testSetTextFromSTARTnotNestedInputNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals("01234", m_xc.getTextValue());
            m_xc.setTextValue(null);
            assertEquals("", m_xc.getTextValue());
        }
    }

    @Test
    void testSetTextFromSTARTnested() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_NESTED_SIBLINGS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals("text0nested0text1nested1", m_xc.getTextValue());
            m_xc.setTextValue("new text");
            assertEquals("<foo attr0=\"val0\">new text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testSetTextFromSTARTnestedInputNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_NESTED_SIBLINGS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertEquals("text0nested0text1nested1", m_xc.getTextValue());
            m_xc.setTextValue(null);
            assertEquals("<foo attr0=\"val0\"/>", m_xc.xmlText());
        }
    }

    @Test
    void testSetTextFromATTRnested() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_NESTED_SIBLINGS)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("val0", m_xc.getTextValue());
            m_xc.setTextValue("new text");
            assertEquals("new text", m_xc.getTextValue());
        }
    }

    @Test
    void testSetTextFromSTARTDOCnested() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_NESTED_SIBLINGS)) {
            assertEquals("text0nested0text1nested1", m_xc.getTextValue());
            m_xc.setTextValue("new text");
            assertEquals(Common.wrapInXmlFrag("new text"), m_xc.xmlText());
        }
    }
}

