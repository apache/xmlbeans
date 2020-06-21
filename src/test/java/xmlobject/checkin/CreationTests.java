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

import com.easypo.XmlLineItemBean;
import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import com.easypo.XmlShipperBean;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CreationTests {
    @Test
    public void testCreatePo() {
        XmlPurchaseOrderDocumentBean doc =
            XmlPurchaseOrderDocumentBean.Factory.newInstance();

        PurchaseOrder order = doc.addNewPurchaseOrder();
        order.addNewCustomer().setName("David Bau");
        order.getCustomer().setAddress("Gladwyne, PA");
        XmlLineItemBean li;
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 1");
        li.setPrice(new BigDecimal("21.79"));
        li.setQuantity(BigInteger.valueOf(2));
        li.setPerUnitOunces(new BigDecimal("5"));
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 2");
        li.setPrice(new BigDecimal("19.89"));
        li.setQuantity(BigInteger.valueOf(2));
        li.setPerUnitOunces(new BigDecimal("5"));
        li = order.addNewLineItem();
        li.setDescription("Burnham's Celestial Handbook, Vol 3");
        li.setPrice(new BigDecimal("19.89"));
        li.setQuantity(BigInteger.valueOf(1));
        li.setPerUnitOunces(new BigDecimal("5"));
        XmlShipperBean sh = order.addNewShipper();
        sh.setName("UPS");
        sh.setPerOunceRate(new BigDecimal("0.74"));

        assertEquals("David Bau", order.getCustomer().getName());
        assertEquals("Gladwyne, PA", order.getCustomer().getAddress());
        assertEquals(3, order.sizeOfLineItemArray());

        assertEquals("Burnham's Celestial Handbook, Vol 1", order.getLineItemArray(0).getDescription());
        assertEquals(new BigDecimal("21.79"), order.getLineItemArray(0).getPrice());
        assertEquals(new BigInteger("2"), order.getLineItemArray(0).getQuantity());
        assertEquals(new BigDecimal("5"), order.getLineItemArray(0).getPerUnitOunces());

        assertEquals("Burnham's Celestial Handbook, Vol 2", order.getLineItemArray(1).getDescription());
        assertEquals(new BigDecimal("19.89"), order.getLineItemArray(1).getPrice());
        assertEquals(new BigInteger("2"), order.getLineItemArray(1).getQuantity());
        assertEquals(new BigDecimal("5"), order.getLineItemArray(1).getPerUnitOunces());

        assertEquals("Burnham's Celestial Handbook, Vol 3", order.getLineItemArray(2).getDescription());
        assertEquals(new BigDecimal("19.89"), order.getLineItemArray(2).getPrice());
        assertEquals(new BigInteger("1"), order.getLineItemArray(2).getQuantity());
        assertEquals(new BigDecimal("5"), order.getLineItemArray(2).getPerUnitOunces());

        assertTrue(order.isSetShipper());
        assertEquals("UPS", order.getShipper().getName());
        assertEquals(new BigDecimal("0.74"), order.getShipper().getPerOunceRate());
    }
}
