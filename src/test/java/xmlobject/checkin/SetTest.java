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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Test;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import test.xbean.xmlcursor.purchaseOrder.USAddress;
import xmlcursor.common.Common;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;


public class SetTest {
    @Test
    void testSetFromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.START);
            XmlObject xo = m_xc.getObject();
            xo.set(XmlString.Factory.newValue("newtext"));
            assertEquals("newtext", m_xc.getTextValue());
        }
    }

    @Test
    void testSetFromATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            XmlObject xo = m_xc.getObject();
            xo.set(XmlString.Factory.newValue(" new attr text "));
            assertEquals(" new attr text ", m_xc.getTextValue());
        }
    }

    @Test
    void testSetFromSTARTstronglyTyped() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) jobj("xbean/xmlcursor/po.xml");
        XmlString xcomment = pod.getPurchaseOrder().xgetComment();
        xcomment.setStringValue("new comment text");
        assertEquals("new comment text", pod.getPurchaseOrder().getComment());
    }

    @Test
    void testSetFromATTRstronglyTyped() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) jobj("xbean/xmlcursor/po.xml");
        XmlDate xorderDate = pod.getPurchaseOrder().xgetOrderDate();

        assertNotNull(xorderDate);

        Calendar d = new XmlCalendar(new java.util.Date());
        xorderDate.setCalendarValue(d);

        // compare year, month, day of the xsd:date type
        assertEquals(d.get(Calendar.YEAR), pod.getPurchaseOrder().getOrderDate().get(Calendar.YEAR));
        assertEquals(d.get(Calendar.MONTH), pod.getPurchaseOrder().getOrderDate().get(Calendar.MONTH));
        assertEquals(d.get(Calendar.DAY_OF_MONTH), pod.getPurchaseOrder().getOrderDate().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testSetFromFixedATTR() throws Exception {

        PurchaseOrderDocument pod = (PurchaseOrderDocument) jobj("xbean/xmlcursor/po.xml");
        USAddress usa = pod.getPurchaseOrder().getShipTo();
        assertNotNull(usa);

        XmlString xcountry = usa.xgetCountry();

        xcountry.setStringValue("UK");

        assertFalse(pod.validate());
    }

    @Test
    void testSetFromComplexType() throws Exception {

        PurchaseOrderDocument pod = (PurchaseOrderDocument) jobj("xbean/xmlcursor/po.xml");
        USAddress usa = pod.getPurchaseOrder().getShipTo();
        assertNotNull(usa);
        usa.set(USAddress.Factory.parse(
            "<shipTo country=\"UK\"><name>Fred</name><street>paved</street><city>town</city><state>AK</state><zip>00000</zip></shipTo>"));

        // assertTrue(true);
        assertFalse(pod.validate());
    }
}

