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

package scomp.substGroup.restriction.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.junit.jupiter.api.Test;
import xbean.scomp.substGroup.deep.BusinessShirtType;
import xbean.scomp.substGroup.deep.ItemType;
import xbean.scomp.substGroup.deep.ItemsDocument;
import xbean.scomp.substGroup.deep.ProductType;
import xbean.scomp.substGroup.userAbstract.AbstractFoo;
import xbean.scomp.substGroup.userAbstract.Bar;
import xbean.scomp.substGroup.userAbstract.GenericFoo;
import xbean.scomp.substGroup.userAbstract.RootDocument;
import xbean.scomp.substGroup.xabstract.BeachUmbrellaT;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 *
 */
public class Abstract {
    @Test
    void testAbstractInvalid() throws Throwable {
        //umbrella not OK here
        String input =
            "<base:items xmlns:pre=\"http://xbean/scomp/substGroup/Abstract\"" +
            " xmlns:base=\"http://xbean/scomp/substGroup/Deep\">" +
            "<base:product>" +
            " <number>SKU45</number>" +
            "   <name>Accessory</name>" +
            "</base:product>" +
            "<pre:umbrella>" +
            " <number>SKU15</number>" +
            "   <name>Umbrella</name>" +
            "</pre:umbrella>" +
            "<base:casualBShirt>" +
            " <number>SKU25</number>" +
            " <name>Oxford Shirt</name>" +
            " <size>12</size>" +
            " <color>blue</color>" +
            "<pokadotColor>yellow</pokadotColor>" +
            "</base:casualBShirt>" +
            "</base:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        assertFalse(doc.validate(createOptions()));
    }

    @Test
    void testParseValid() throws Throwable {
        String input =
            "<base:items xmlns:pre=\"http://xbean/scomp/substGroup/Abstract\"" +
            " xmlns:base=\"http://xbean/scomp/substGroup/Deep\">" +
            "<base:product>" +
            " <number>SKU45</number>" +
            "   <name>Accessory</name>" +
            "</base:product>" +
            "<pre:beachumbrella diameter=\"19.5\">" +
            " <number>SKU15</number>" +
            "   <name>Umbrella</name>" +
            "</pre:beachumbrella>" +
            "<base:casualBShirt>" +
            " <number>SKU25</number>" +
            " <name>Oxford Shirt</name>" +
            " <size>12</size>" +
            " <color>blue</color>" +
            "<pokadotColor>yellow</pokadotColor>" +
            "</base:casualBShirt>" +
            "</base:items>";
        ItemsDocument doc = ItemsDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testBuildValid() throws Throwable {
        ItemsDocument doc = ItemsDocument.Factory.newInstance();
        BusinessShirtType bs = BusinessShirtType.Factory.newInstance();
        bs.setName("Oxford Shirt");
        bs.setNumber("SKU35");
        bs.setColor("blue");
        bs.setSize(new BigInteger("10"));
        ItemType it = doc.addNewItems();
        BeachUmbrellaT um = BeachUmbrellaT.Factory.newInstance();
        um.setDiameter(1.5f);
        um.xsetName(null);
        um.setNumber("SKU_Umbrella");
        it.setProductArray(new ProductType[]{bs, um});
        assertFalse(doc.validate());
        XmlString name = XmlString.Factory.newInstance();
        name.setStringValue("foobar");
        um.xsetName(name);
        it.setProductArray(new ProductType[]{bs, um});
        assertTrue(doc.validate(createOptions()));
    }

    //user described problem
    @Test
    void testUserAbstract() {

        RootDocument doc = RootDocument.Factory.newInstance();
        Bar barElt = doc.addNewRoot();
        barElt.insertNewFoo(0);

        AbstractFoo elt = barElt.getFooArray()[0];
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$ABSTRACT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
        elt.changeType(GenericFoo.type);
        assertTrue(doc.validate(validateOptions));
    }
}
