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
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jcur;


public class GetDocChangeStampTest {
    @Test
    void testGetDocChangeStampHasChanged() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";

            m_xc.selectPath(ns + " $this//po:city");
            m_xc.toNextSelection();
            assertEquals("Mill Valley", m_xc.getTextValue());
            XmlCursor.ChangeStamp cs0 = m_xc.getDocChangeStamp();
            m_xc.setTextValue("Mowed Valley");
            assertEquals("Mowed Valley", m_xc.getTextValue());
            assertTrue(cs0.hasChanged());
        }
    }

    @Test
    void testGetDocChangeStampNotChanged() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_XMLCURSOR_PO)) {
            String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
            m_xc.selectPath(ns + " $this//po:city");
            XmlCursor.ChangeStamp cs0 = m_xc.getDocChangeStamp();
            m_xc.toEndDoc();
            assertFalse(cs0.hasChanged());
        }
    }
}

