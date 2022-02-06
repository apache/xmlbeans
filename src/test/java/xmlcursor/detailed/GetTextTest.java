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


package xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.*;

public class GetTextTest {
    @Test
    void testGetTextFromEND() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toEndDoc();
            m_xc.toPrevToken();
            assertEquals(TokenType.END, m_xc.currentTokenType());
            //assertEquals(null, m_xc.getTextValue());

            assertThrows(IllegalStateException.class, m_xc::getTextValue);
        }
    }

    @Test
    void testGetTextFromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            assertEquals("type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromCOMMENT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            assertEquals(" comment text ", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            //assertEquals(null, m_xc.getTextValue());

            //modifying test: behavior OK as of Sept 04
            //filed bug on API
            String text = m_xc.getTextValue();
            assertEquals("http://www.foo.org", text);
        }
    }

    @Test
    void testGetTextFromENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO)) {
            toNextTokenOfType(m_xc, TokenType.ENDDOC);
            //assertEquals(null, m_xc.getTextValue());
            assertThrows(IllegalStateException.class, m_xc::getTextValue);
        }
    }


    @Test
    void testGetTextFromTEXT() throws Exception {
        //  m_xo = XmlObject.Factory.parse(Common.XML_FOO_TEXT);

        try (XmlCursor m_xc = cur("<foo>text</foo>")) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(TokenType.TEXT, m_xc.currentTokenType());
            assertEquals("text", m_xc.getChars());
            assertEquals("text", m_xc.getTextValue());

            m_xc.toNextChar(2);
            assertEquals(TokenType.TEXT, m_xc.currentTokenType());
            assertEquals("xt", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromSTART_NotNested() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//FleetID");
            m_xc.toNextSelection();
            assertEquals("FLEETNAME", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromSTART_Nested() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//EventStatus/EquipmentStructure");
            m_xc.toNextSelection();
            assertEquals("\n\t\t\tGATX\n\t\t\t123456\n\t\t\tL\n\t\t", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromSTART_TextAferEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT_EXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            assertEquals("text", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromSTART_TextAferEND_WS() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_WS_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            assertEquals(" text ", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromATTR_Nested() throws Exception {
        String preface = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            m_xc.selectPath(preface + " .//po:billTo");
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("US", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT_EXT)) {
            assertEquals("textextended", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextEmptyElementSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR)) {
            m_xc.selectPath("$this//bar");
            assertEquals("", m_xc.getTextValue());
        }
    }

    @Test
    void testGetTextWhitespaceOnlyFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_WS_ONLY)) {
            m_xc.toFirstChild();
            assertEquals("   ", m_xc.getTextValue());
        }
    }
}

