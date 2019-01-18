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


public class ToLastAttributeTest extends BasicCursorTestCase {

    @Test
    public void testToLastAttrSTARTDOC() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo>text</foo>").newCursor();
        m_xc.toLastChild();
        m_xc.insertAttributeWithValue("attr0", "val0");
        m_xc.insertAttributeWithValue("attr1", "val1");
        m_xc.toStartDoc();
        assertTrue(m_xc.toLastAttribute());
        assertEquals("val1", m_xc.getTextValue());
    }

    @Test
    public void testToLastAttrSTARTmoreThan1ATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertTrue(m_xc.toLastAttribute());
        assertEquals("val1", m_xc.getTextValue());
    }

    @Test
    public void testToLastAttrFrom1stATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertFalse(m_xc.toLastAttribute());
    }

    @Test
    public void testToLastAttrZeroATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertFalse(m_xc.toLastAttribute());
    }

    @Test
    public void testToLastAttrFromTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertFalse(m_xc.toLastAttribute());
    }

    @Test
    public void testToLastAttrWithXMLNS() throws Exception {
        m_xc = XmlObject.Factory.parse("<foo xmlns=\"http://www.foo.org\">text</foo>").newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertFalse(m_xc.toLastAttribute());
    }
}

