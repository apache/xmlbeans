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


public class ToFirstAttributeTest extends BasicCursorTestCase {
    @Test
    public void testToFirstAttrSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>text</foo>").newCursor();
        m_xc.toFirstChild();
        m_xc.insertAttributeWithValue("attr0", "val0");
        m_xc.toStartDoc();
        assertTrue(m_xc.toFirstAttribute());
        assertEquals("val0", m_xc.getTextValue());
    }

    @Test
    public void testToFirstAttrSTARTmoreThan1ATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertTrue(m_xc.toFirstAttribute());
        assertEquals("val0", m_xc.getTextValue());
    }

    @Test
    public void testToFirstAttrFrom2ndATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertFalse(m_xc.toFirstAttribute());
    }

    @Test
    public void testToFirstAttrZeroATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertFalse(m_xc.toFirstAttribute());
    }

    @Test
    public void testToFirstAttrFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertEquals(TokenType.TEXT, m_xc.currentTokenType());
        assertEquals("text", m_xc.getChars());
        assertFalse(m_xc.toFirstAttribute());
    }

    @Test
    public void testToFirstAttrWithXMLNS() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo xmlns=\"http://www.foo.org\">text</foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertFalse(m_xc.toFirstAttribute());
    }
}

