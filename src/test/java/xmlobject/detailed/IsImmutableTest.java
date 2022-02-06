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


package xmlobject.detailed;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jcur;
import static xmlcursor.common.BasicCursorTestCase.jobj;


public class IsImmutableTest {
    @Test
    void testIsImmutableFalse() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) jobj(Common.TRANXML_FILE_CLM);
        assertFalse(clmDoc.isImmutable());
    }

    @Test
    void testIsImmutableTrue() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//Initial");
            m_xc.toNextSelection();
            SchemaType st = m_xc.getObject().schemaType();
            XmlObject xoNew = st.newValue("ZZZZ");
            assertTrue(xoNew.isImmutable());
            // verify it's not in main store
            assertEquals("GATX", m_xc.getTextValue());
        }
    }
}
