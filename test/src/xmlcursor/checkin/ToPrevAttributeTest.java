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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.*;

public class ToPrevAttributeTest extends BasicCursorTestCase {
    @Test
    public void testtoPrevAttrFromSTART() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertFalse(m_xc.toPrevAttribute());
    }

    @Test
    public void testtoPrevAttrFromSingleATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertFalse(m_xc.toPrevAttribute());
        assertEquals("val0", m_xc.getTextValue());
    }

    @Test
    public void testtoPrevAttrWithSiblings() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val1", m_xc.getTextValue());
        assertTrue(m_xc.toPrevAttribute());
        assertEquals("val0", m_xc.getTextValue());
    }

    @Test
    public void testtoPrevAttrFromFirstSibling() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertFalse(m_xc.toPrevAttribute());
        assertEquals("val0", m_xc.getTextValue());
    }

    @Test
    public void testtoPrevAttrWithXMLNS() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo attr0=\"val0\" xmlns=\"uri\" attr1=\"val1\">text</foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("val1", m_xc.getTextValue());
        assertTrue(m_xc.toPrevAttribute());
        assertEquals("val0", m_xc.getTextValue());
        assertFalse(m_xc.toPrevAttribute());
    }

    @Test
    public void testtoPrevAttrFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertFalse(m_xc.toPrevAttribute());
    }
}
