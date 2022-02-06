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
import static xmlcursor.common.BasicCursorTestCase.*;


public class InsertProcInstTest {

    @Test
    void testInsertProcInstWithNullTarget() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertProcInst(null, "value"));
        }
    }

    @Test
    void testInsertProcInstWithEmptyStringTarget() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertProcInst("", "value"));
        }
    }

    @Test
    void testInsertProcInstWithLTcharInTarget() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertProcInst("<target", " value "));
        }
    }

    @Test
    void testInsertProcInstWithNullText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertProcInst("target", null);
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<bar>te<?target?>xt</bar>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertProcInstWithEmptyStringText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertProcInst("target", "");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<bar>te<?target?>xt</bar>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertProcInstWithLTcharInText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            m_xc.insertProcInst("target", "< value ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo><?target < value ?><bar>text</bar></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertProcInstInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertProcInst("target", " value ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<bar>te<?target  value ?>xt</bar>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertProcInstAfterSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            m_xc.insertProcInst("target", " value ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo><?target  value ?><bar>text</bar></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertProcInstAtEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            toNextTokenOfType(m_xc, TokenType.END);
            m_xc.insertProcInst("target", " value ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo xmlns=\"http://www.foo.org\"><?target  value ?></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertProcInstBeforeATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertProcInst("target", " value "));
        }
    }
}

