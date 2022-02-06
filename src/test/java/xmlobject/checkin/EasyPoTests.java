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

import com.easypo.XmlPurchaseOrderDocumentBean;
import com.easypo.XmlPurchaseOrderDocumentBean.PurchaseOrder;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;

public class EasyPoTests {
    @Test
    void testEasyPo() throws Exception {
        XmlPurchaseOrderDocumentBean doc = (XmlPurchaseOrderDocumentBean) jobj("xbean/xmlobject/easypo1.xml");
        assertFalse(doc.isNil());
        PurchaseOrder order = doc.getPurchaseOrder();
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

        assertEquals(3, order.sizeOfLineItemArray());
    }

    @Test
    void testAutoValidationOk() {
        Assertions.assertDoesNotThrow(() -> XmlPurchaseOrderDocumentBean.Factory.parse(
            "<purchase-order xmlns='http://openuri.org/easypo'/>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<purchase-orde xmlns='http://openuri.org/easypo'/>",
        "<purchase-order xmlns='http://openuri.org/easyp'/>",
        "<f:fragment xmlns:f='http://www.openuri.org/fragment'/>",
        "<f:fragment xmlns:f='http://www.openuri.org/fragment'><a/></f:fragment>",
        "<f:fragment xmlns:f='http://www.openuri.org/fragment'><a/><a/></f:fragment>"
    })
    void testSimpleAutoValidationNok(String xml) {
        assertThrows(XmlException.class, () -> XmlPurchaseOrderDocumentBean.Factory.parse(xml));
    }
}
