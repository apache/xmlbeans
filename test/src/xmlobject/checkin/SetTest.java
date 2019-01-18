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

import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.junit.Test;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import test.xbean.xmlcursor.purchaseOrder.USAddress;
import tools.util.JarUtil;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import java.util.Calendar;

import static org.junit.Assert.*;


public class SetTest extends BasicCursorTestCase {
    @Test
    public void testSetFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        XmlObject xo = m_xc.getObject();
        xo.set(XmlString.Factory.newValue("newtext"));
        assertEquals("newtext", m_xc.getTextValue());
    }

    @Test
    public void testSetFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        XmlObject xo = m_xc.getObject();
        xo.set(XmlString.Factory.newValue(" new attr text "));
        assertEquals(" new attr text ", m_xc.getTextValue());
    }

    @Test
    public void testSetFromSTARTstronglyTyped() throws Exception {
       PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        XmlString xcomment = pod.getPurchaseOrder().xgetComment();
        xcomment.setStringValue("new comment text");
        assertEquals("new comment text", pod.getPurchaseOrder().getComment());
    }

    @Test
    public void testSetFromATTRstronglyTyped() throws Exception {
        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        XmlDate xorderDate = pod.getPurchaseOrder().xgetOrderDate();

        assertNotNull(xorderDate);

        Calendar d = new XmlCalendar(new java.util.Date());
        xorderDate.setCalendarValue(d);

        // compare year, month, day of the xsd:date type
        assertEquals(d.get(Calendar.YEAR),
                pod.getPurchaseOrder().getOrderDate().get(Calendar.YEAR));
        assertEquals(d.get(Calendar.MONTH),
                pod.getPurchaseOrder().getOrderDate().get(Calendar.MONTH));
        assertEquals(d.get(Calendar.DAY_OF_MONTH),
                pod.getPurchaseOrder().getOrderDate().get(
                        Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetFromFixedATTR() throws Exception {

        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        USAddress usa = pod.getPurchaseOrder().getShipTo();
        assertNotNull(usa);

        XmlString xcountry = usa.xgetCountry();

        xcountry.setStringValue("UK");

        assertFalse(pod.validate());
    }

    @Test
    public void testSetFromComplexType() throws Exception {

        PurchaseOrderDocument pod = (PurchaseOrderDocument) XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        USAddress usa = pod.getPurchaseOrder().getShipTo();
        assertNotNull(usa);
        usa.set(
                USAddress.Factory.parse(
                        "<shipTo country=\"UK\"><name>Fred</name><street>paved</street><city>town</city><state>AK</state><zip>00000</zip></shipTo>"));

        // assertTrue(true);
        assertFalse(pod.validate());
    }
}

