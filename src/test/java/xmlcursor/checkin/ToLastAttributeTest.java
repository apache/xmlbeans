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


public class ToLastAttributeTest {

    @Test
    void testToLastAttrSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur("<foo>text</foo>")) {
            m_xc.toLastChild();
            m_xc.insertAttributeWithValue("attr0", "val0");
            m_xc.insertAttributeWithValue("attr1", "val1");
            m_xc.toStartDoc();
            assertTrue(m_xc.toLastAttribute());
            assertEquals("val1", m_xc.getTextValue());
        }
    }

    @Test
    void testToLastAttrSTARTmoreThan1ATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertTrue(m_xc.toLastAttribute());
            assertEquals("val1", m_xc.getTextValue());
        }
    }

    @Test
    void testToLastAttrFrom1stATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertFalse(m_xc.toLastAttribute());
        }
    }

    @Test
    void testToLastAttrZeroATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertFalse(m_xc.toLastAttribute());
        }
    }

    @Test
    void testToLastAttrFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertFalse(m_xc.toLastAttribute());
        }
    }

    @Test
    void testToLastAttrWithXMLNS() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"http://www.foo.org\">text</foo>")) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertFalse(m_xc.toLastAttribute());
        }
    }
}

