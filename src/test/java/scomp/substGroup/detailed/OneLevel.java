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

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.substGroup.oneLevel.*;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class OneLevel {

    @Test
    void testValidSubstParse() throws Throwable {
        String input =
            "<items xmlns=\"http://xbean/scomp/substGroup/OneLevel\">" +
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
            "<hat>" +
            " <number>SKU35</number>" +
            " <name>Sombrero</name>" +
            " <size>4</size>" +
            "</hat>" +
            "<umbrella>" +
            " <number>SKU15</number>" +
            "   <name>Umbrella</name>" +
            "</umbrella>" +
            "</items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * Test error message. 1 product too many
     */
    @Test
    void testValidSubstParseInvalid() throws Throwable {
        String input =
            "<items xmlns=\"http://xbean/scomp/substGroup/OneLevel\">" +
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
            "<hat>" +
            " <number>SKU35</number>" +
            " <name>Sombrero</name>" +
            " <size>4</size>" +
            "</hat>" +
            "<umbrella>" +
            " <number>SKU15</number>" +
            "   <name>Umbrella</name>" +
            "</umbrella>" +
            "<hat>" +
            " <number>SKU35</number>" +
            " <name>Sombrero</name>" +
            " <size>4</size>" +
            "</hat>" +
            "</items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testValidSubstBuild() throws Throwable {
        ItemsDocument doc = ItemsDocument.Factory.newInstance();
        ItemType items = doc.addNewItems();
        HatType hat = HatType.Factory.newInstance();
        hat.setName("Funny Hat");
        hat.setNumber("SKU84");
        hat.setSize(new BigInteger("10"));

        //   This doesn't work
        // ProductType hat = elt.addNewProduct();
        // ((HatType)hat).setNumber(3);
        // ShirtType shirt = (ShirtType) elt.addNewProduct();

        ShirtType shirt = ShirtType.Factory.newInstance();
        shirt.setName("Funny Shirt");
        shirt.setNumber("SKU54");
        shirt.setColor("blue");
        shirt.setSize(new BigInteger("10"));
        ProductType genericProd = ProductType.Factory.newInstance();
        genericProd.setName("Pants");
        genericProd.setNumber("32");

        items.setProductArray(new ProductType[]{hat, shirt, genericProd});
        assertTrue(doc.validate(createOptions()));
    }
}
