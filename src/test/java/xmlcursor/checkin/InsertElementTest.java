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

public class InsertElementTest {

    @Test
    void testInsertElementNullName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertElementWithText(null, "uri", "value"));
        }
    }

    @Test
    void testInsertElementEmptyStringName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertElementWithText("", "uri", "value"));
        }
    }

    @Test
    void testInsertElementNullUri() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.insertElementWithText("name", null, "value");
            m_xc.toPrevSibling();
            assertEquals("<name>value</name>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertElementNullText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.insertElementWithText("name", "uri", null);
            m_xc.toPrevSibling();
            assertEquals("<uri:name xmlns:uri=\"uri\"/>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertElementEmptyStringText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.insertElementWithText("name", null, "");
            m_xc.toPrevSibling();
            assertEquals("<name/>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertElementInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertElementWithText("name", null, "value");
            m_xc.toStartDoc();
            assertEquals("<foo>te<name>value</name>xt</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertElementAtEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.END);
            m_xc.insertElementWithText("name", null, "value");
            m_xc.toStartDoc();
            assertEquals("<foo>text<name>value</name></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertElementAtSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertElementWithText("name", null, "value"));
        }
    }

    @Test
    void testInsertElementAtENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ENDDOC);
            m_xc.insertElementWithText("name", null, "value");
            m_xc.toStartDoc();
            assertEquals(Common.wrapInXmlFrag("<foo>text</foo><name>value</name>"), m_xc.xmlText());
        }
    }

    @Test
    void testInsertElementInStoreWithNamespace() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + ".//FleetID");
            m_xc.toNextSelection();
            m_xc.insertElementWithText("name", "uri", "value");
            m_xc.toPrevSibling();
            assertEquals("<uri:name xmlns=\"" +
                                    Common.CLM_NS + "\" " +
                                    Common.CLM_XSI_NS + " " +
                                    "xmlns:uri=\"uri\">value</uri:name>", m_xc.xmlText());
        }
    }
}

