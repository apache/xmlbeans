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
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;


public class TypesTest extends BasicCursorTestCase {
    @Test
    public void testSchemaTypeFromStronglyTypedBuiltIn() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                "$this//EventStatus/Date");
        m_xc.toNextSelection();
        XmlObject xo = m_xc.getObject();
        SchemaType st = xo.schemaType();
        assertEquals(true, st.isBuiltinType());
        QName q = st.getName();
        assertEquals("{" + Common.XML_SCHEMA_TYPE_SUFFIX + "}date",
                q.toString());
    }

    @Test
    public void testSchemaTypeFromStronglyTyped() throws Exception {
        m_xo = XmlObject.Factory.parse(
                   JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                "$this//EventStatus");
        m_xc.toNextSelection();
        XmlObject xo = m_xc.getObject();
        SchemaType st = xo.schemaType();
        assertEquals(false, st.isBuiltinType());
        assertEquals(
                "E=EventStatus|D=EventStatus@" +
                Common.TRANXML_SCHEMA_TYPE_SUFFIX,
                st.toString());

    }

    @Test
    public void testSchemaTypeFromNonTyped() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_BAR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.selectPath("$this//bar");
        m_xc.toNextSelection();
        XmlObject xo = m_xc.getObject();
        SchemaType st = xo.schemaType();
        assertEquals(true, st.isNoType());
        //assertEquals("TanyType@" + Common.XML_SCHEMA_TYPE_SUFFIX, st.toString());
    }

    @Test
    public void testInstanceTypeNotNillable() throws Exception {
        m_xo = XmlObject.Factory.parse(
                  JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        m_xc = m_xo.newCursor();
        m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                "$this//EventStatus");
        XmlObject xo = m_xc.getObject();
        assertEquals(xo.schemaType(), ((SimpleValue) xo).instanceType());
    }

    @Test
    public void testInstanceTypeNil() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        m_xo = pod.getPurchaseOrder().getShipTo().xgetName();
        m_xo.setNil();
        assertEquals(true, m_xo.isNil());
        if (m_xo.schemaType() == ((SimpleValue) m_xo).instanceType()) {
            fail("Nil object's instanceType should not be equal to schemaType");
        }
        assertTrue(true);
    }

    /*
    public void testInstanceTypeUnion() throws Exception
    {
        // tbd
    }
    */


}

