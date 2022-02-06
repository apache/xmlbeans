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

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.junit.jupiter.api.Test;
import tools.xml.XmlComparator;
import xmlobject.substgroup.*;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubstGroupTests {
    /**
     * TODO: Determine what the proper Return value is
     */
    @Test
    void test_invalidSubstitute() {
        OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("ItemType");
        item.setSku(new BigInteger("42"));

        XmlObject xm = item.substitute(FootstoolDocument.type.getDocumentElementName(), FootstoolDocument.type);
        assertFalse(xm instanceof FootstoolDocument);

        List<XmlError> err = new ArrayList<>();
        XmlOptions xOpts = new XmlOptions().setErrorListener(err);
        // no way this should happen ... TODO: ... but as of now it's validated ok
        // assertFalse(xm.validate(xOpts), "Invalid substitute validated");

        //invalid substitute should leave good state
        String exp = "<xml-fragment><sku>42</sku><name>ItemType</name></xml-fragment>";

        assertEquals(exp, xm.xmlText(), "text values should be the same");
    }

    @Test
    void test_validSubstitute() {
        String URI = "http://xmlobject/substgroup";
        QName name = new QName(URI, "beanBag");
        // get an item
        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("ItemForTest");
        item.setSku(new BigInteger("12"));

        XmlObject xObj = item.substitute(name, BeanBagType.type);
        assertNotSame(xObj.getClass().getName(), item.getClass().getName(),
            "Invalid Substitution. Xobj Types after substitution are the same.");

        // invoke some operation on the original XmlObject, it should thrown an XmlValueDisconnectedException
        assertThrows(XmlValueDisconnectedException.class, item::xmlText);
    }

    /**
     * Tests substition upcase, from item to Document, then ensure validation
     */
    @Test
    void test_valid_sub() throws Exception {
        String expectedXML =
            "<sub:beanBag xmlns:sub=\"http://xmlobject/substgroup\">" +
            "  <sku>12</sku>" +
            "  <name>BeanBagType</name>" +
            "  <size color=\"Blue\">Blue</size>" +
            "</sub:beanBag>";
        XmlObject xm = XmlObject.Factory.parse(expectedXML);
        String itemName = "item";
        BigInteger bInt = new BigInteger("12");

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName(itemName);
        item.setSku(bInt);

        BeanBagType b2Type = (BeanBagType) item.substitute(BeanBagDocument.type.getDocumentElementName(), BeanBagType.type);

        assertEquals(0, b2Type.getName().compareTo(itemName),
            "Name Value was not as expected\nactual: " + b2Type.getName() + " exp: " + itemName);
        assertEquals(0, b2Type.getSku().compareTo(bInt), "Integer Value was not as Excepted");

        BeanBagSizeType bbSize = b2Type.addNewSize();
        bbSize.setColor("Blue");
        bbSize.setStringValue("Blue");
        b2Type.setSize(bbSize);
        b2Type.setName("BeanBagType");

        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();

        assertTrue(XmlComparator.lenientlyCompareTwoXmlStrings(order.xmlText(), xm.xmlText(), diag));
    }

    @Test
    void test_item_disconnect() {
        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("item");
        item.setSku(BigInteger.valueOf(12));

        XmlObject b2Type = item.substitute(BeanBagDocument.type.getDocumentElementName(), BeanBagType.type);
        assertTrue(b2Type instanceof BeanBagType);

        assertThrows(XmlValueDisconnectedException.class, item::xmlText);
    }

    @Test
    void test_item_downcasts_valid() throws Exception {
        BigInteger bInt = new BigInteger("12");
        List<XmlError> err = new ArrayList<>();
        XmlOptions opts = new XmlOptions(new XmlOptions().setErrorListener(err));

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();

        BeanBagType b2Type = (BeanBagType) item.substitute(BeanBagDocument.type.getDocumentElementName(), BeanBagType.type);

        BeanBagSizeType bbSize = b2Type.addNewSize();
        bbSize.setColor("Blue");
        bbSize.setStringValue("Blue");
        b2Type.setSku(bInt);
        b2Type.setSize(bbSize);
        b2Type.setName("BeanBagType");

        ItemType nItem = order.getItem();
        assertTrue(nItem.validate(opts), "nItem - Downcasting Failed Validation");
        err.clear();

        item = (ItemType) nItem.substitute(ItemDocument.type.getDocumentElementName(), ItemType.type);
        // TODO: downcasting shouldn't be allowed
        // assertTrue(item.validate(opts), "Item - Downcasting Failed Validation:\n");
        item.validate(opts);

        XmlError[] xErr = getXmlErrors(err);
        assertEquals(1, xErr.length, "Length of xm_errors was greater than expected");
        assertEquals("cvc-complex-type.2.4b", xErr[0].getErrorCode(), "Error Code was not as Expected");
        err.clear();

        String nName = "ItemType";
        item.setName(nName);

        assertFalse(order.validate(opts), "Order - Downcasting Failed Validation");

        //Check value was set
        assertEquals(nName, order.getItem().getName(), "Name Value was not changed");

        //Check Error message
        String expText = "Element not allowed: size in element item@http://xmlobject/substgroup";
        XmlError[] xErr2 = getXmlErrors(err);
        assertEquals(1, xErr2.length, "Length of xm_errors was greater than expected");
        assertEquals("cvc-complex-type.2.4b", xErr2[0].getErrorCode(), "Error Code was not as Expected");
        assertEquals(expText, xErr2[0].getMessage(), "Error Message was not as expected");
    }

    private XmlError[] getXmlErrors(List<XmlError> c) {
        return c.toArray(new XmlError[0]);
    }

    @Test
    void test_null_newName() {
        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        assertThrows(IllegalArgumentException.class, () -> order.substitute(null, OrderItem.type));
    }

    @Test
    void test_null_newType() {
        OrderItem order = OrderItem.Factory.newInstance();
        assertThrows(IllegalArgumentException.class, () -> order.substitute(OrderItem.type.getDocumentElementName(), null));
    }

    @Test
    void test_unknownQName() {
        QName exp = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
        OrderItem order = OrderItem.Factory.newInstance();
        XmlObject xm = order.substitute(new QName("http://baz", "baz"), OrderItem.type);

        //Verify that the invalid substitution results in an anyType
        assertEquals(0, exp.getNamespaceURI().compareTo(
            xm.type.getName().getNamespaceURI()), "Namespace URIs were not the same");
        assertEquals(0, xm.type.getName().getLocalPart().compareTo(
            exp.getLocalPart()), "Local Part was not as Expected");
    }

    @Test
    void test_null_Params() {
        XmlObject xml = XmlObject.Factory.newInstance();
        assertThrows(IllegalArgumentException.class, () -> xml.substitute(null, null));
    }
}
