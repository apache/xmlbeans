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

import javax.xml.namespace.QName;

import static org.junit.Assert.*;


public class RemoveAttributeTest extends BasicCursorTestCase {
    @Test
    public void testRemoveAttributeValidAttrFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("attr1");
        assertTrue(m_xc.removeAttribute(name));
        assertNull(m_xc.getAttributeText(name));
    }

    @Test
    public void testRemoveAttributeInvalidAttrFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("invalid");
        assertFalse(m_xc.removeAttribute(name));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributeNullAttrFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("dummy");
        m_xc.removeAttribute(null);
    }

    @Test
    public void testRemoveAttributeFromPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.PROCINST);
        QName name = new QName("type");
        assertFalse(m_xc.removeAttribute(name));
    }

    @Test
    public void testRemoveAttributeXMLNS() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("xmlns");
        assertFalse(m_xc.removeAttribute(name));
    }

    @Test
    public void testRemoveAttributeFromEND() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.END);
        QName name = new QName("attr1");
        assertFalse(m_xc.removeAttribute(name));
    }
}

