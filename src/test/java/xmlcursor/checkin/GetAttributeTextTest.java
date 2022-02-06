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
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xmlcursor.common.BasicCursorTestCase.*;


public class GetAttributeTextTest {

    @Test
    void testGetAttributeTextFromSTARTwith2ATTR() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            m_xc.toFirstChild();
            assertEquals("val1", m_xc.getAttributeText(new QName("attr1")));
        }
    }

    @Test
    void testGetAttributeTextFromSTARTwithInvalid() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            m_xc.toFirstChild();
            assertNull(m_xc.getAttributeText(new QName("invalid")));
        }
    }

    @Test
    void testGetAttributeTextFromSTARTChildHasAttr() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            m_xc.selectPath("$this//items");
            assertNull(m_xc.getAttributeText(new QName("partNum")));
        }
    }

    @Test
    void testGetAttributeTextFromSTARTDOCChildHasAttr() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            assertNull(m_xc.getAttributeText(new QName("partNum")));
        }
    }

    @Test
    void testGetAttributeTextFromATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertNull(m_xc.getAttributeText(new QName("attr1")));
        }
    }
}

