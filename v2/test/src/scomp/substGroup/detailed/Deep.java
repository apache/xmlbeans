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
package scomp.substGroup.detailed;

import xbean.scomp.substGroup.deep.*;

import java.math.BigInteger;

import scomp.common.BaseCase;
import org.apache.xmlbeans.XmlException;

/**
 * @owner: ykadiysk
 * Date: Jul 29, 2004
 * Time: 1:27:27 PM
 */
public class Deep extends BaseCase {

    /**
     * QName for casualBShirt is incorrect: err. message seems bad to me
     */
    public void testErrorMessage() throws XmlException {
        String input =
                "<pre:items xmlns:pre=\"http://xbean/scomp/substGroup/Deep\">" +
                "<casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</casualBShirt>" +
                "</pre:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
    }


    public void testValidSubstParse() throws Throwable {
        String input =
                "<pre:items xmlns:pre=\"http://xbean/scomp/substGroup/Deep\">" +
                "<pre:casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</pre:casualBShirt>" +
                "<pre:product>" +
                " <number>SKU45</number>" +
                "   <name>Accessory</name>" +
                "</pre:product>" +
                "<pre:umbrella>" +
                " <number>SKU15</number>" +
                "   <name>Umbrella</name>" +
                "</pre:umbrella>" +
                "</pre:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

    }

    /**
     * Test error message. 1 product too many
     *
     * @throws Throwable
     */
    public void testValidSubstParseInvalid() throws Throwable {
        String input =
                "<items xmlns=\"http://xbean/scomp/substGroup/Deep\">" +
                "<shirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "</shirt>" +
                "<product>" +
                " <number>SKU45</number>" +
                "   <name>Accessory</name>" +
                "</product>" +
                "<umbrella>" +
                " <number>SKU15</number>" +
                "   <name>Umbrella</name>" +
                "</umbrella>" +
                "<casualBShirt>" +
                " <number>SKU25</number>" +
                " <name>Oxford Shirt</name>" +
                " <size>12</size>" +
                " <color>blue</color>" +
                "<pokadotColor>yellow</pokadotColor>" +
                "</casualBShirt>" +
                "</items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        try {
            assertTrue(!doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testValidSubstBuild() throws Throwable {
        ItemsDocument doc = ItemsDocument.Factory.newInstance();
        ItemType items = doc.addNewItems();
        BusinessCasualShirtType bShirt = BusinessCasualShirtType.Factory.newInstance();
        bShirt.setName("Funny Shirt");
        bShirt.setNumber("SKU84");
        bShirt.setSize(BigInteger.TEN);
        bShirt.setPokadotColor("yellow");
        bShirt.setColor("blue");
/*   This doesn't work
ProductType hat = elt.addNewProduct();
((HatType)hat).setNumber(3);
ShirtType shirt = (ShirtType) elt.addNewProduct();
*/
        ShirtType shirt = ShirtType.Factory.newInstance();
        shirt.setName("Funny Shirt");
        shirt.setNumber("SKU54");
        shirt.setColor("green");
        shirt.setSize(BigInteger.TEN);
        ProductType genericProd = ProductType.Factory.newInstance();
        genericProd.setName("Pants");
        genericProd.setNumber("32");

        items.setProductArray(new ProductType[]{bShirt, shirt, genericProd});
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

//TODO: what to do with the umbrella here???
    }
}
