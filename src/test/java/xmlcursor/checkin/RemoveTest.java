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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.junit.jupiter.api.Test;
import test.xbean.xmlcursor.purchaseOrder.USAddress;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;

public class RemoveTest {
    @Test
    void testRemoveFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            assertThrows(IllegalStateException.class,  m_xc::removeXml);
        }
    }

    @Test
    void testRemoveFromFirstChild() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            m_xc.toFirstChild();
            m_xc.removeXml();
            assertEquals(TokenType.ENDDOC, m_xc.currentTokenType());
        }
    }

    @Test
    void testRemoveAllText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.removeXml();
            assertEquals(TokenType.END, m_xc.currentTokenType());
            m_xc.toStartDoc();
            assertEquals("<foo><bar/></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testRemovePartialText() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.removeXml();
            assertEquals(TokenType.END, m_xc.currentTokenType());
            m_xc.toStartDoc();
            assertEquals("<foo><bar>te</bar></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testRemoveFromATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            m_xc.removeXml();
            assertEquals(TokenType.ATTR, m_xc.currentTokenType());
            m_xc.toStartDoc();
            assertEquals("<foo attr1=\"val1\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testRemoveAffectOnXmlObjectGetXXX() throws Exception {
        String sQuery = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";$this//po:shipTo";
        try (XmlCursor m_xc = jcur("xbean/xmlcursor/po.xml")) {
            m_xc.selectPath(sQuery);
            m_xc.toNextSelection();
            XmlObject xo = m_xc.getObject();
            USAddress usa = (USAddress) xo;
            m_xc.removeXml();
            assertThrows(XmlValueDisconnectedException.class, usa::getCity);
        }
    }

    @Test
    void testRemoveAffectOnXmlObjectNewCursor() throws Exception {
        String sQuery = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";$this//po:shipTo";
        try (XmlCursor m_xc = jcur("xbean/xmlcursor/po.xml")) {
            m_xc.selectPath(sQuery);
            m_xc.toNextSelection();
            XmlObject xo = m_xc.getObject();
            USAddress usa = (USAddress) xo;
            m_xc.removeXml();
            assertNotNull(usa, "USAddress object expected non-null, but is null");
            assertThrows(XmlValueDisconnectedException.class, usa::newCursor);
        }
    }
}

