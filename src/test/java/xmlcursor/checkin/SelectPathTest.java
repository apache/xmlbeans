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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.jcur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class SelectPathTest {
    /**
     * $BUGBUG: Eric's engine doesn't send to Jaxen appropriately
     */
    @Test
    @Disabled
    public void testSelectPathFromEND() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            toNextTokenOfType(m_xc, XmlCursor.TokenType.END);
            m_xc.selectPath(ns + " $this//city");
            assertEquals(0, m_xc.getSelectionCount());
        }
    }

    @Test
    @Disabled
    public void testSelectPathFromENDDOC() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            toNextTokenOfType(m_xc, XmlCursor.TokenType.ENDDOC);
            m_xc.selectPath(ns + " .//po:city");
            assertEquals(0, m_xc.getSelectionCount());
        }
    }

    @Test
    void testSelectPathNamespace() throws Exception {
        String sLocalPath = ".//FleetID";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(sLocalPath);
            assertEquals(0, m_xc.getSelectionCount());
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + sLocalPath);
            assertEquals(1, m_xc.getSelectionCount());
        }
    }

    @Test
    void testSelectPathCaseSensitive() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            m_xc.selectPath(ns + " .//po:ciTy");
            assertEquals(0, m_xc.getSelectionCount());
            m_xc.selectPath(ns + " .//po:city");
            assertEquals(2, m_xc.getSelectionCount());
        }
    }

    @Test
    void testSelectPathReservedKeyword() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            m_xc.selectPath(ns + " .//po:item");
            assertEquals(2, m_xc.getSelectionCount());
        }
    }

    @Test
    void testSelectPathNull() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            // TODO: surround with appropriate t-c once ericvas creates the exception type
            //  see bugs 18009 and/or 18718
            assertThrows(RuntimeException.class, () -> m_xc.selectPath(null));
        }
    }

    @Test
    void testSelectPathInvalidXPath() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            // TODO: surround with appropriate t-c once ericvas creates the exception type
            // see bugs 18009 and/or 18718
            m_xc.selectPath("&GARBAGE");
            assertThrows(RuntimeException.class, m_xc::getSelectionCount);
        }
    }
}

