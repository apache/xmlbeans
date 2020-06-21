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
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;


public class SetAttributeTextTest extends BasicCursorTestCase {
    @Test
    public void testSetAttributeTextFromSTARTOn2ndATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("attr2");
        assertTrue(m_xc.setAttributeText(name, "newval2"));
        assertEquals("newval2", m_xc.getAttributeText(name));
    }

    @Test
    public void testSetAttributeTextNewName() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        QName name = new QName("newname");
        assertTrue(m_xc.setAttributeText(name, "newval2"));
        assertEquals("newval2", m_xc.getAttributeText(name));
    }

    @Test
    public void testSetAttributeTextFromSTARTChildHasATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//purchaseOrder");
        QName name = new QName("country");
        assertTrue(m_xc.setAttributeText(name, "Finland"));
        assertEquals("Finland", m_xc.getAttributeText(name));
    }

    @Test
    public void testSetAttributeTextFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(
                 JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        QName name = new QName("orderDate");
        assertFalse(m_xc.setAttributeText(name, "2003-01-10"));
    }
}

