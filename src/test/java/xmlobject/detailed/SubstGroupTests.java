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
import org.junit.Assert;
import org.junit.Test;
import tools.xml.XmlComparator;
import xmlobject.substgroup.*;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SubstGroupTests {
    /**
     * TODO: Determine what the proper Return value is
     */
    @Test
    public void test_invalidSubstitute() {
        OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("ItemType");
        item.setSku(new BigInteger("42"));

        //FootstoolDocument fsd;
        try {

            //on invalid substitute orignal value is returned.
            FootstoolDocument fsd = (FootstoolDocument) item.substitute(
                FootstoolDocument.type.getDocumentElementName(),
                FootstoolDocument.type);
            fail("Class Cast Exception was thrown on invalid substitute ");
        } catch (ClassCastException ccEx) {
        }

        XmlObject xm = item.substitute(
            FootstoolDocument.type.getDocumentElementName(),
            FootstoolDocument.type);

        System.out.println("XM: " + xm.xmlText());
        ArrayList err = new ArrayList();
        XmlOptions xOpts = new XmlOptions().setErrorListener(err);
        //no way this should happen
        if (xm.validate(xOpts)) {
            System.err.println("Invalid substitute validated");

            for (Iterator iterator = err.iterator(); iterator.hasNext(); ) {
                System.err.println("Error: " + iterator.next());
            }
        }

        //invalid substitute should leave good state
        System.out.println("Item: " + item.xmlText());

        String exp = "<xml-fragment><sku>42</sku><name>ItemType</name></xml-fragment>";

        assertEquals("text values should be the same", 0, exp.compareTo(xm.xmlText()));
    }

    @Test(expected = XmlValueDisconnectedException.class)
    public void test_validSubstitute() {
        String URI = "http://xmlobject/substgroup";
        QName name = new QName(URI, "beanBag");
        // get an item
        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName("ItemForTest");
        item.setSku(new BigInteger("12"));

        // types and content before substitution
        System.out.println("Before Substitution :\nQNAme Item doc    :" + ItemDocument.type.getName());
        System.out.println("QNAme beanBag elem:" + name);
        System.out.println("item type:" + item.getClass().getName());
        System.out.println("item XMLText      : " + item.xmlText());

        try {
            XmlObject xObj = item.substitute(name, BeanBagType.type);
            System.out.println("After Substitution :\nSubstituted XObj text: " + xObj.xmlText());
            System.out.println("Substituted XObj type: " + xObj.getClass().getName());
            Assert.assertNotSame("Invalid Substitution. Xobj Types after substitution are the same.", xObj.getClass().getName(), item.getClass().getName());

        } catch (NullPointerException npe) {
            System.out.println("NPE Thrown: " + npe.getMessage());
            npe.printStackTrace();
        }

        // invoke some operation on the original XmlObject, it should thrown an XmlValueDisconnectedException
        item.xmlText();
    }

    /**
     * Tests substition upcase, from item to Document, then ensure validation
     */
    @Test
    public void test_valid_sub() throws Exception {
        String expectedXML = "<sub:beanBag xmlns:sub=\"http://xmlobject/substgroup\">" +
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

        System.out.println("Order: " +
            order.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("valid: " + order.validate());

        BeanBagType b2Type = (BeanBagType) item.substitute(
            BeanBagDocument.type.getDocumentElementName(),
            BeanBagType.type);

        assertEquals("Name Value was not as expected\nactual: " +
            b2Type.getName() +
            " exp: " +
            itemName, 0, b2Type.getName().compareTo(itemName));
        assertEquals("Integer Value was not as Excepted", 0, b2Type.getSku().compareTo(bInt));

        BeanBagSizeType bbSize = b2Type.addNewSize();
        bbSize.setColor("Blue");
        bbSize.setStringValue("Blue");
        b2Type.setSize(bbSize);
        b2Type.setName("BeanBagType");

        System.out.println("b2Type: " +
            b2Type.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("b2Type: " + b2Type.validate());

        System.out.println("Order: " +
            order.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("ovalid: " + order.validate());

        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();

        if (!XmlComparator.lenientlyCompareTwoXmlStrings(order.xmlText(),
            xm.xmlText(), diag))
            throw new Exception("Compare Values Fails\n" + diag.toString());
    }

    @Test(expected = XmlValueDisconnectedException.class)
    public void test_item_disconnect() {
        String itemName = "item";
        BigInteger bInt = new BigInteger("12");
        boolean exThrown = false;

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();
        item.setName(itemName);
        item.setSku(bInt);

        System.out.println("Order: " +
            order.xmlText(new XmlOptions().setSavePrettyPrint()));
        System.out.println("valid: " + order.validate());

        BeanBagType b2Type = (BeanBagType) item.substitute(
            BeanBagDocument.type.getDocumentElementName(),
            BeanBagType.type);

        item.xmlText();
    }

    @Test
    public void test_item_downcasts_valid() throws Exception {
        BigInteger bInt = new BigInteger("12");
        ArrayList err = new ArrayList();
        XmlOptions opts = new XmlOptions(
            new XmlOptions().setErrorListener(err));

        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        ItemType item = order.addNewItem();

        BeanBagType b2Type = (BeanBagType) item.substitute(
            BeanBagDocument.type.getDocumentElementName(),
            BeanBagType.type);

        BeanBagSizeType bbSize = b2Type.addNewSize();
        bbSize.setColor("Blue");
        bbSize.setStringValue("Blue");
        b2Type.setSku(bInt);
        b2Type.setSize(bbSize);
        b2Type.setName("BeanBagType");

        ItemType nItem = order.getItem();

        //nItem.validate(opts);
        if (!nItem.validate(opts))
            System.out.println(
                "nItem - Downcasting Failed Validation:\n" + err);
        err.clear();

        item = (ItemType) nItem.substitute(
            ItemDocument.type.getDocumentElementName(),
            ItemType.type);

        //System.out.println("Item1: " + item.xmlText());

        if (!item.validate(opts))
            System.out.println("Item - Downcasting Failed Validation:\n" + err);

        XmlError[] xErr = getXmlErrors(err);
        assertEquals("Length of xm_errors was greater than expected", 1, xErr.length);
        assertEquals("Error Code was not as Expected", 0, xErr[0].getErrorCode().compareTo("cvc-complex-type.2.4b"));
        err.clear();

        String nName = "ItemType";
        item.setName(nName);
        System.out.println("Item2: " + item.xmlText());

        if (!order.validate(opts))
            System.out.println(
                "Order - Downcasting Failed Validation:\n" + err);

        //Check value was set
        if (!(nName.compareTo(order.getItem().getName()) == 0))
            throw new Exception("Name Value was not changed");

        //Check Error message
        String expText = "Element not allowed: size in element item@http://xmlobject/substgroup";
        XmlError[] xErr2 = getXmlErrors(err);
        assertEquals("Length of xm_errors was greater than expected", 1, xErr2.length);
        assertEquals("Error Code was not as Expected", 0, xErr2[0].getErrorCode().compareTo("cvc-complex-type.2.4b"));
        assertEquals("Error Message was not as expected", 0, xErr2[0].getMessage().compareTo(expText));

        err.clear();
    }

    private XmlError[] getXmlErrors(ArrayList c) {
        XmlError[] errs = new XmlError[c.size()];
        for (int i = 0; i < errs.length; i++) {
            errs[i] = (XmlError) c.get(i);
        }
        return errs;
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_newName() {
        xmlobject.substgroup.OrderItem order = OrderItem.Factory.newInstance();
        order.substitute(null, OrderItem.type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_newType() {
        OrderItem order = OrderItem.Factory.newInstance();
        order.substitute(OrderItem.type.getDocumentElementName(), null);
    }

    @Test
    public void test_unknownQName() {
        QName exp = new QName("http://www.w3.org/2001/XMLSchema", "anyType");
        OrderItem order = OrderItem.Factory.newInstance();
        XmlObject xm = order.substitute(new QName("http://baz", "baz"),
            OrderItem.type);

        //Verify that the invalid substitution results in an anyType
        assertEquals("Namespace URIs were not the same", 0, exp.getNamespaceURI().compareTo(
            xm.type.getName().getNamespaceURI()));
        assertEquals("Local Part was not as Expected", 0, xm.type.getName().getLocalPart().compareTo(
            exp.getLocalPart()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_Params() {
        XmlObject xml = XmlObject.Factory.newInstance();
        xml.substitute(null, null);
    }
}
