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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CityNameDocument.CityName;
import org.tranxml.tranXML.version40.ETADocument.ETA;
import org.tranxml.tranXML.version40.EventStatusDocument.EventStatus;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument;
import tools.util.JarUtil;
import xmlcursor.common.Common;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompareToTest {
    @Test(expected = ClassCastException.class)
    public void testCompareToEquals() throws Exception {
        CarLocationMessageDocument clmDoc = (CarLocationMessageDocument) XmlObject.Factory.parse(
            JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        EventStatus[] aEventStatus = clmDoc.getCarLocationMessage().getEventStatusArray();

        assertTrue("Unexpected: Missing EventStatus element.  Test harness failure.", aEventStatus.length > 0);

        GeographicLocation gl = aEventStatus[0].getGeographicLocation();
        CityName cname0 = gl.getCityName();
        ETA eta = aEventStatus[0].getETA();
        CityName cname1 = eta.getGeographicLocation().getCityName();
        assertTrue(cname0.valueEquals(cname1));
        assertEquals(XmlObject.EQUAL, cname0.compareTo(cname1));
    }

    @Test(expected = ClassCastException.class)
    public void testCompareToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        assertEquals(XmlObject.NOT_EQUAL, m_xo.compareTo(null));
    }

    @Test
    public void testCompareToLessThan() throws Exception {
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) XmlObject.Factory.parse(
            JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));

        BigDecimal bdUSPrice0 = poDoc.getPurchaseOrder().getItems()
            .getItemArray(0)
            .getUSPrice();
        BigDecimal bdUSPrice1 = poDoc.getPurchaseOrder().getItems()
            .getItemArray(1)
            .getUSPrice();
        assertEquals(XmlObject.LESS_THAN, bdUSPrice1.compareTo(bdUSPrice0));
    }

    @Test
    public void testCompareToGreaterThan() throws Exception {
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument)
            XmlObject.Factory.parse(
                JarUtil.getResourceFromJar("xbean/xmlcursor/po.xml"));
        BigDecimal bdUSPrice0 = poDoc.getPurchaseOrder().getItems()
            .getItemArray(0)
            .getUSPrice();
        BigDecimal bdUSPrice1 = poDoc.getPurchaseOrder().getItems()
            .getItemArray(1)
            .getUSPrice();
        assertEquals(XmlObject.GREATER_THAN, bdUSPrice0.compareTo(bdUSPrice1));
    }

    @Test(expected = ClassCastException.class)
    public void testCompareToString() throws Exception {
        m_xo = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        assertEquals(0, m_xo.compareTo(""));
    }

    @Test
    public void testCompareValue() throws Exception {
        m_xo = XmlObject.Factory.parse(
            JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        XmlCursor m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        XmlObject xo = m_xc.getObject();
        assertEquals(XmlObject.NOT_EQUAL, m_xo.compareValue(xo));
    }

    private XmlObject m_xo;
}

