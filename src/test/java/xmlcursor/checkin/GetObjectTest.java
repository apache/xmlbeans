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
import org.apache.xmlbeans.XmlNMTOKEN;
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xmlcursor.common.BasicCursorTestCase.*;


public class GetObjectTest {

    @Test
    void testGetObjectFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            assertTrue(m_xc.getObject() instanceof CarLocationMessageDocument);
        }
    }

    @Test
    void testGetObjectFromSTART() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toFirstChild();
            assertTrue(m_xc.getObject() instanceof CarLocationMessageDocument.CarLocationMessage);
        }
    }

    @Test
    void testGetObjectFromATTR() throws Exception {
        String sQuery = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";  $this//po:shipTo";
        try (XmlCursor m_xc = jcur("xbean/xmlcursor/po.xml")) {
            m_xc.selectPath(sQuery);
            m_xc.toNextSelection();
            m_xc.toFirstAttribute();
            assertTrue(m_xc.getObject() instanceof XmlNMTOKEN);
        }
    }

    @Test
    void testGetObjectFromEND() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            toNextTokenOfType(m_xc, TokenType.END);
            assertNull(m_xc.getObject());
        }
    }

    @Test
    void testGetObjectFromENDDOC() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toEndDoc();
            assertNull(m_xc.getObject());
        }
    }

    @Test
    void testGetObjectFromNAMESPACE() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            assertNull(m_xc.getObject());
        }
    }

    @Test
    void testGetObjectFromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            assertNull(m_xc.getObject());
        }
    }

    @Test
    void testGetObjectFromCOMMENT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            assertNull(m_xc.getObject());
        }
    }

    @Test
    void testGetObjectFromTEXT() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertNull(m_xc.getObject());
        }
    }
}

