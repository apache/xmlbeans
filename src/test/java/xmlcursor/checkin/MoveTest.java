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
import org.junit.jupiter.api.Test;
import tools.util.Util;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;


public class MoveTest {

    @Test
    void testMoveToNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.moveXml(null));
        }
    }

    @Test
    void testMoveDifferentStoresLoadedByParse() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
             XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            m_xc.moveXml(xc1);
            xc1.toParent();
            // verify xc1
            assertEquals("01234text", xc1.getTextValue());
            // verify m_xc
            assertEquals(TokenType.END, m_xc.currentTokenType());
        }
    }

    @Test
    void testMoveDifferentStoresLoadedFromFile() throws Exception {
        String sQuery = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; .//po:zip";
        String sExpected = "<ver:Initial " +
                           "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" " +
                           "xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">" +
                           "GATX</ver:Initial>";

        // load the documents and obtain a cursor
        try (XmlCursor xc0 = jcur(Common.TRANXML_FILE_CLM);
             XmlCursor xc1 = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            xc0.selectPath(Common.CLM_NS_XQUERY_DEFAULT + ".//Initial");
            xc0.toNextSelection();

            xc1.selectPath(sQuery);
            assertTrue(0 < xc1.getSelectionCount());
            xc1.toNextSelection();

            // should move the <Initial>GATX</Initial> element plus the namespace
            xc0.moveXml(xc1);

            xc1.toPrevSibling();
            // verify xc1
            assertEquals(sExpected, xc1.xmlText());
            // verify xc0
            xc0.toNextToken();  // skip the whitespace token
            assertEquals("123456", xc0.getTextValue());
        }
    }

    @Test
    void testMoveSameLocation() throws Exception {
        XmlObject m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            m_xc.moveXml(xc1);
            assertEquals("01234", m_xc.getChars());
        }
    }

    @Test
    void testMoveNewLocation() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        XmlObject m_xo = jobj(Common.TRANXML_FILE_XMLCURSOR_PO);

        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            m_xc.selectPath(ns + " .//po:shipTo/po:city");
            m_xc.toNextSelection();
            xc1.selectPath(ns + " .//po:billTo/po:city");
            xc1.toNextSelection();
            m_xc.moveXml(xc1);
            xc1.toPrevToken();
            xc1.toPrevToken();
            // verify xc1
            assertEquals("Mill Valley", xc1.getChars());
            // verify m_xc
            m_xc.toNextToken(); // skip the whitespace token
            assertEquals("CA", m_xc.getTextValue());
        }
    }

    @Test
    void testMoveElementToMiddleOfTEXT() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        XmlObject m_xo = jobj(Common.TRANXML_FILE_XMLCURSOR_PO);

        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            m_xc.selectPath(ns + " .//po:shipTo/po:city");
            m_xc.toNextSelection();
            xc1.selectPath(ns + " .//po:billTo/po:city");
            xc1.toNextSelection();
            xc1.toNextToken();
            // should be at 'T' in "Old Town"
            xc1.toNextChar(4);
            // should be "Old <city>Mill Valley</city>Town"
            m_xc.moveXml(xc1);
            // verify xc1
            xc1.toPrevToken();
            assertEquals(TokenType.END, xc1.currentTokenType());
            xc1.toPrevToken();
            assertEquals("Mill Valley", xc1.getChars());
            xc1.toPrevToken();
            assertEquals(TokenType.START, xc1.currentTokenType());
            assertEquals(new QName("city").getLocalPart(), xc1.getName().getLocalPart());
            xc1.toPrevToken();
            assertEquals("Old ", xc1.getChars());
            // verify m_xc
            // skip the whitespace token
            m_xc.toNextToken();
            assertEquals("CA", m_xc.getTextValue());
        }
    }

    /**
     * Method testMoveFromSTARTDOC
     * Also used to verify radar bug 16160
     */
    @Test
    void testMoveFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO)) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> m_xc.moveXml(m_xc));
            // verify 16160
            String sTrace = Util.getStackTrace(e);
            assertFalse(sTrace.contains("splay.bitch"));
        }
    }
}

