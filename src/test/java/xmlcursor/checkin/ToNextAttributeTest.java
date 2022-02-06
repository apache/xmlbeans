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


public class ToNextAttributeTest {
    @Test
    void testToNextAttrFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertFalse(m_xc.toNextAttribute());
        }
    }

    @Test
    void testToNextAttrFromSingleATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertFalse(m_xc.toNextAttribute());
            assertEquals("val0", m_xc.getTextValue());
        }
    }

    @Test
    void testToNextAttrWithSiblings() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertTrue(m_xc.toNextAttribute());
            assertEquals("val1", m_xc.getTextValue());
        }
    }

    @Test
    void testToNextAttrFromLastSibling() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertFalse(m_xc.toNextAttribute());
            assertEquals("val1", m_xc.getTextValue());
        }
    }

    @Test
    void testToNextAttrWithXMLNS() throws Exception {
        try (XmlCursor m_xc = cur("<foo attr0=\"val0\" xmlns=\"uri\" attr1=\"val1\">text</foo>")) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("val0", m_xc.getTextValue());
            assertTrue(m_xc.toNextAttribute());
            assertEquals("val1", m_xc.getTextValue());
            assertFalse(m_xc.toNextAttribute());
        }
    }

    @Test
    void testToNextAttrFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertFalse(m_xc.toNextAttribute());
        }
    }

}

