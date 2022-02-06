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
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.*;


public class CopyTest {

    @Test
    void testCopyToNull() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.copyXml(null));
        }
    }

    @Test
    void testCopyDifferentStoresLoadedByParse() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
             XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            m_xc.copyXml(xc1);
            xc1.toParent();
            // verify xc1
            assertEquals("01234text", xc1.getTextValue());
            // verify m_xc
            assertEquals("01234", m_xc.getChars());
        }
    }

    /**
     * Method testCopyDifferentStoresLoadedFromFile
     * Tests copy from document w/ namespaces to doc w/o
     */
    @Test
    void testCopyDifferentStoresLoadedFromFile() throws XmlException, IOException {
        try (XmlCursor xc0 = jcur(Common.TRANXML_FILE_CLM);
            XmlCursor xc1 = jcur("xbean/xmlcursor/po.xml")) {
            xc0.selectPath(Common.CLM_NS_XQUERY_DEFAULT + " .//Initial");
            xc0.toNextSelection();
             String sQuery=
                     "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; "+
                     ".//po:zip";
            xc1.selectPath( sQuery );
            xc1.toNextSelection();

            xc0.copyXml(xc1); // should copy the <Initial>GATX</Initial> element plus the default namespace
            xc1.toPrevSibling();
            // verify xc1
            String sExpected = "<ver:Initial " +
                    "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" " +
                    "xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">" +
                    "GATX</ver:Initial>";
            assertEquals(sExpected, xc1.xmlText());
            // verify xc0
            // should contain all the namespaces for the document
            assertEquals(
                    "<Initial xmlns=\"" + Common.CLM_NS + "\" " +
                    Common.CLM_XSI_NS +
                    ">GATX</Initial>",
                    xc0.xmlText());
        }

    }

    /**
     * Method testCopyDifferentStoresLoadedFromFile2
     *
     * Tests copy from document w/o namespaces to document with namespaces
     */
    @Test
    void testCopyDifferentStoresLoadedFromFile2() throws Exception {
        // load the documents and obtain a cursor
        try (XmlCursor xc0 = jcur(Common.TRANXML_FILE_CLM);
            XmlCursor xc1 = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            xc0.selectPath(Common.CLM_NS_XQUERY_DEFAULT + " .//Initial");
            xc0.toNextSelection();

            String sQuery=
                     "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; "+
                     ".//po:zip";
            xc1.selectPath( sQuery );
            xc1.selectPath( sQuery );
            xc1.toNextSelection();

            xc1.copyXml(xc0); // should copy the <zip>90952</zip> element
            // verify xc1
            assertEquals(
                    "<po:zip xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\">90952</po:zip>",
                    xc1.xmlText());
            // verify xc0
            // should contain all the namespaces for the document
            xc0.toPrevSibling();
            // assertEquals("<zip xmlns=\"" + Common.CLM_NS + "\" " + Common.CLM_XSI_NS + ">90952</zip>", xc0.xmlText());
            String sExpected = "<po:zip " +
                    "xmlns=\"http://www.tranxml.org/TranXML/Version4.0\" " +
                    "xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" " +
                    "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\">" +
                    "90952</po:zip>";

            assertEquals(sExpected, xc0.xmlText());
        }
    }

    @Test
    void testCopySameLocation() throws Exception {
        XmlObject m_xo = obj(Common.XML_FOO_DIGITS);
        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            m_xc.copyXml(xc1);
            m_xc.toParent();
            assertEquals("0123401234", m_xc.getTextValue());
        }
    }

    @Test
    void testCopyNewLocation() throws Exception {
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        XmlObject m_xo = jobj(Common.TRANXML_FILE_XMLCURSOR_PO);

        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            m_xc.selectPath(ns+" .//po:shipTo/po:city");
            m_xc.toNextSelection();
            xc1.selectPath(ns +" .//po:billTo/po:city");
            xc1.toNextSelection();
            m_xc.copyXml(xc1);
            xc1.toPrevToken();
            xc1.toPrevToken();
            // verify xc1
            assertEquals("Mill Valley", xc1.getChars());
            // verify m_xc
            assertEquals("Mill Valley", m_xc.getTextValue());
        }
    }

    @Test
    void testCopyElementToMiddleOfTEXT() throws Exception {
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        String exp_ns="xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        XmlObject m_xo = jobj(Common.TRANXML_FILE_XMLCURSOR_PO);

        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            m_xc.selectPath(ns+" .//po:shipTo/po:city");
            m_xc.toNextSelection();
            xc1.selectPath(ns+" .//po:billTo/po:city");
            xc1.toNextSelection();
            xc1.toNextToken();
            xc1.toNextChar(4);  // should be at 'T' in "Old Town"
            m_xc.copyXml(xc1);     // should be "Old <city>Mill Valley</city>Town"
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
            assertEquals("<po:city "+exp_ns+">Mill Valley</po:city>", m_xc.xmlText());
        }
    }
}
