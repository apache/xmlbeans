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
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xmlcursor.common.BasicCursorTestCase.*;


public class GetNameTest {
    private static final String STARTDOC_XML =
        "<?xml version=\"1.0\"?>\n" +
        "<po:purchaseOrder xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" orderDate=\"1999-10-20\">\n" +
        "    <po:shipTo country=\"US\">\n" +
        "        <po:name>Alice Smith</po:name>\n" +
        "        <po:street>123 Maple Street</po:street>\n" +
        "        <po:city>Mill Valley</po:city>\n" +
        "        <po:state>CA</po:state>\n" +
        "        <po:zip>90952</po:zip>\n" +
        "    </po:shipTo>\n" +
        "    <po:billTo country=\"US\">\n" +
        "        <po:name>Robert Smith</po:name>\n" +
        "        <po:street>8 Oak Avenue</po:street>\n" +
        "        <po:city>Old Town</po:city>\n" +
        "        <po:state>PA</po:state>\n" +
        "        <po:zip>95819</po:zip>\n" +
        "    </po:billTo>\n" +
        "    <po:comment>Hurry, my lawn is going wild!</po:comment>\n" +
        "    <po:items>\n" +
        "        <po:item partNum=\"872-AA\">\n" +
        "            <po:productName>Lawnmower</po:productName>\n" +
        "            <po:quantity>1</po:quantity>\n" +
        "            <po:USPrice>148.95</po:USPrice>\n" +
        "            <po:comment>Confirm this is electric</po:comment>\n" +
        "        </po:item>\n" +
        "        <po:item partNum=\"926-AA\">\n" +
        "            <po:productName>Baby Monitor</po:productName>\n" +
        "            <po:quantity>1</po:quantity>\n" +
        "            <po:USPrice>39.98</po:USPrice>\n" +
        "            <po:shipDate>1999-05-21</po:shipDate>\n" +
        "        </po:item>\n" +
        "    </po:items>\n" +
        "</po:purchaseOrder>";

    @Test
    void testGetNameFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(STARTDOC_XML)) {
            assertNull(m_xc.getName());
        }
    }

    @Test
    void testGetNameFromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            assertEquals("xml-stylesheet", m_xc.getName().getLocalPart());
        }
    }

    @Test
    void testGetNameFromSTART() throws Exception {
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            m_xc.selectPath(ns + " .//po:shipTo/po:city");
            m_xc.toNextSelection();
            assertEquals("city", m_xc.getName().getLocalPart());
        }
    }

    @Test
    void testGetNameFromEND() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>text</bar></foo>")) {
            m_xc.selectPath(".//bar");
            toNextTokenOfType(m_xc, TokenType.END);
            assertNull(m_xc.getName());
        }
    }

    @Test
    void testGetNameFromATTR() throws Exception {
        String ns="declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"; ";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            m_xc.selectPath(ns + " .//po:shipTo");
            m_xc.toNextSelection();
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("country", m_xc.getName().getLocalPart());
        }
    }

    @Test
    void testGetNameFromCOMMENT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            assertNull(m_xc.getName());
        }
    }

    @Test
    void testGetNameElementWithDefaultNamespace() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + ".//ETA");
            m_xc.toNextSelection();
            assertEquals("ETA", m_xc.getName().getLocalPart());
            assertEquals(Common.CLM_NS, m_xc.getName().getNamespaceURI());
        }
    }

    @Test
    void testGetNameAttrWithDefaultNamespace() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            String sDefaultElemNS = "declare default element namespace \"http://ecommerce.org/schema\"; ";
            m_xc.selectPath(sDefaultElemNS + ".//price");
            m_xc.toNextSelection();
            m_xc.toFirstAttribute();
            assertEquals("units", m_xc.getName().getLocalPart());
            // note: default namespace does not apply to attribute names, hence should be null
            assertEquals("", m_xc.getName().getNamespaceURI());
        }
    }
}

