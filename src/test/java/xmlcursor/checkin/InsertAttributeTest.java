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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class InsertAttributeTest {

    @Test
    void testInsertAttributeAtSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertAttributeWithValue("name", "uri", "value");
            m_xc.toStartDoc();
            assertEquals("<foo uri:name=\"value\" xmlns:uri=\"uri\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAtATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            m_xc.insertAttributeWithValue("name", null, "value");
            m_xc.toStartDoc();
            assertEquals("<foo name=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAt2ndATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            toNextTokenOfType(m_xc, TokenType.ATTR);
            m_xc.insertAttributeWithValue("name", null, "value");
            m_xc.toStartDoc();
            assertEquals("<foo attr0=\"val0\" name=\"value\" attr1=\"val1\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAtPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            m_xc.toNextToken();
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttributeWithValue("name", null, "value"));
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithEmptyStringName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttributeWithValue("", "uri", "value"));
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithNullName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttributeWithValue(null, "uri", "value"));
        }
    }

    @Test
    void testInsertAttributeWithNullQName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttribute((QName)null));
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithEmptyStringUri() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertAttributeWithValue("name", "", "value");
            m_xc.toStartDoc();
            assertEquals("<foo name=\"value\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithNameXml() throws Exception {
        try (XmlCursor m_xc = cur("<foo>text</foo>")) {
            assertThrows(Exception.class, () ->
                m_xc.insertAttributeWithValue("xml", null, "value"));
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithValueXml() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertAttributeWithValue("name", null, "xml");
            m_xc.toStartDoc();
            assertEquals("<foo name=\"xml\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithLTcharInName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttributeWithValue("<b", null, "value"));
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithLTcharInValue() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertAttributeWithValue("name", null, "<value");
            m_xc.toStartDoc();
            assertEquals("<foo name=\"&lt;value\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithAmpCharInValue() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertAttributeWithValue("name", null, "&value");
            m_xc.toStartDoc();
            assertEquals("<foo name=\"&amp;value\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeAtSTARTwithAmpCharInName() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttributeWithValue("&bar", null, "value"));
        }
    }

    // tests below use the XMLName form of the parameter signature
    @Test
    void testInsertAttributeType2AtATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            QName name = new QName("name");
            m_xc.insertAttributeWithValue(name, "value");
            m_xc.toStartDoc();
            assertEquals("<foo name=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertAttributeType2AfterSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            QName name = new QName("name");
            m_xc.insertAttributeWithValue(name, null);
            m_xc.toStartDoc();
            assertEquals("<foo attr0=\"val0\" attr1=\"val1\" name=\"\">text</foo>", m_xc.xmlText());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // WithXMLinName
        "<xml>",
        // WithLeadingSpaceinName
        " any",
        // ContainingSpaceinName
        "any any",
        // WithTrailingSpaceinName
        "any ",
        // WithXMLinNameCase
        "<xMlzorro>"
    })
    void testInsertAttributeTypeInvalid(String localPart) throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            QName name = new QName(localPart);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertAttributeWithValue(name, "value"));
        }
    }
}

