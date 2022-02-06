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

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class RemoveAttributeTest {
    @Test
    void testRemoveAttributeValidAttrFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            QName name = new QName("attr1");
            assertTrue(m_xc.removeAttribute(name));
            assertNull(m_xc.getAttributeText(name));
        }
    }

    @Test
    void testRemoveAttributeInvalidAttrFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            QName name = new QName("invalid");
            assertFalse(m_xc.removeAttribute(name));
        }
    }

    @Test
    void testRemoveAttributeNullAttrFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            QName name = new QName("dummy");
            assertThrows(IllegalArgumentException.class, () -> m_xc.removeAttribute(null));
        }
    }

    @Test
    void testRemoveAttributeFromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            QName name = new QName("type");
            assertFalse(m_xc.removeAttribute(name));
        }
    }

    @Test
    void testRemoveAttributeXMLNS() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            QName name = new QName("xmlns");
            assertFalse(m_xc.removeAttribute(name));
        }
    }

    @Test
    void testRemoveAttributeFromEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.END);
            QName name = new QName("attr1");
            assertFalse(m_xc.removeAttribute(name));
        }
    }
}

