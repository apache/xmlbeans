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

package xmlobject.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;

public class ValidateTest {
    @Test
    void testValidateTrue() throws Exception {
        //m_xo = XmlObject.Factory.parse(Common.XML_PURCHASEORDER);
        XmlObject m_xo = jobj("xbean/xmlcursor/po.xml");
        assertTrue(m_xo.validate());
    }

    @Test
    void testValidateTrueWithOptionDiscardDocElement() throws Exception {
        XmlOptions map = new XmlOptions();
        map.setLoadReplaceDocumentElement(null);
        XmlObject m_xo = jobj(Common.TRANXML_FILE_XMLCURSOR_PO, map);
        assertFalse(m_xo.validate(map));
    }

    @Test
    void testValidateFalseFixedAttr() throws Exception {
        String ns = "declare namespace po=\"http://xbean.test/xmlcursor/PurchaseOrder\";";
        String exp_ns = "xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\"";

        XmlObject m_xo = jobj("xbean/xmlcursor/po.xml");
        assertTrue(m_xo instanceof PurchaseOrderDocument);

        try (XmlCursor m_xc = m_xo.newCursor()) {
            m_xc.selectPath(ns + " $this//po:shipTo");
            QName name = new QName("country");
            m_xc.setAttributeText(name, "UK");
            XmlObject xo = m_xc.getObject();
            assertEquals("UK", m_xc.getAttributeText(name));

            assertFalse(xo.validate());
            assertFalse(m_xo.validate());
        }
    }
}

