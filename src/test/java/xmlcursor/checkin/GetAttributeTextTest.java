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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class GetAttributeTextTest extends BasicCursorTestCase {
    @Test
    public void testGetAttributeTextFromSTARTwith2ATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals("val1", m_xc.getAttributeText(new QName("attr1")));
    }

    @Test
    public void testGetAttributeTextFromSTARTwithInvalid() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertNull(m_xc.getAttributeText(new QName("invalid")));
    }

    @Test
    public void testGetAttributeTextFromSTARTChildHasAttr() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//items");
        assertNull(m_xc.getAttributeText(new QName("partNum")));
    }

    @Test
    public void testGetAttributeTextFromSTARTDOCChildHasAttr() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_XMLCURSOR_PO));
        m_xc = m_xo.newCursor();
        assertNull(m_xc.getAttributeText(new QName("partNum")));
    }

    @Test
    public void testGetAttributeTextFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertNull(m_xc.getAttributeText(new QName("attr1")));
    }
}

