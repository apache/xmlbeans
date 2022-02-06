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

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Test;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;


public class TypesTest {
    @Test
    void testSchemaTypeFromStronglyTypedBuiltIn() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//EventStatus/Date");
            m_xc.toNextSelection();
            XmlObject xo = m_xc.getObject();
            SchemaType st = xo.schemaType();
            assertTrue(st.isBuiltinType());
            QName q = st.getName();
            assertEquals("{" + Common.XML_SCHEMA_TYPE_SUFFIX + "}date", q.toString());
        }
    }

    @Test
    void testSchemaTypeFromStronglyTyped() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//EventStatus");
            m_xc.toNextSelection();
            XmlObject xo = m_xc.getObject();
            SchemaType st = xo.schemaType();
            assertFalse(st.isBuiltinType());
            assertEquals("E=EventStatus|D=EventStatus@" + Common.TRANXML_SCHEMA_TYPE_SUFFIX, st.toString());
        }
    }

    @Test
    void testSchemaTypeFromNonTyped() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            XmlObject xo = m_xc.getObject();
            SchemaType st = xo.schemaType();
            assertTrue(st.isNoType());
            //assertEquals("TanyType@" + Common.XML_SCHEMA_TYPE_SUFFIX, st.toString());
        }
    }

    @Test
    void testInstanceTypeNotNillable() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//EventStatus");
            XmlObject xo = m_xc.getObject();
            assertEquals(xo.schemaType(), ((SimpleValue) xo).instanceType());
        }
    }

    @Test
    void testInstanceTypeNil() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) jobj("xbean/xmlcursor/po.xml");
        XmlString m_xo = pod.getPurchaseOrder().getShipTo().xgetName();
        m_xo.setNil();
        assertTrue(m_xo.isNil());
        assertNotEquals(m_xo.schemaType(), ((SimpleValue) m_xo).instanceType(),
            "Nil object's instanceType should not be equal to schemaType");
    }
}

