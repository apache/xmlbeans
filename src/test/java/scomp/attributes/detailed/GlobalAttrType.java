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
package scomp.attributes.detailed;

import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlString;
import org.junit.jupiter.api.Test;
import xbean.scomp.attribute.globalAttrType.GlobalAttrTypeDocDocument;
import xbean.scomp.attribute.globalAttrType.GlobalAttrTypeT;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;

public class GlobalAttrType {
    @Test
    void testAllValid() throws Throwable {
        String input =
            "<pre:GlobalAttrTypeDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrType\" " +
            "pre:attSimple=\"XBeanAttrStr\" " +
            "pre:attAnyType=\" 1 \" " +
            "pre:attAnonymous=\" 1 \" />";

        GlobalAttrTypeT testDoc = GlobalAttrTypeDocDocument.Factory.parse(input).getGlobalAttrTypeDoc();
        assertTrue(testDoc.validate(createOptions()));

        assertTrue(testDoc.isSetAttSimple());
        assertEquals("XBeanAttrStr", testDoc.getAttSimple());
        assertEquals(" 1 ", testDoc.getAttAnyType().getStringValue());
        assertEquals(1, testDoc.getAttAnonymous().intValue());
    }

    /**
     * This should awlays be valid
     */
    @Test
    void testAnyType() throws Throwable {
        String input =
            "<pre:GlobalAttrTypeDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrType\" " +
            " pre:attAnyType=\" 1 \" " +
            " />";

        GlobalAttrTypeT testDoc = GlobalAttrTypeDocDocument.Factory.parse(input).getGlobalAttrTypeDoc();
        assertTrue(testDoc.validate(createOptions()));

        assertEquals(" 1 ", testDoc.getAttAnyType().getStringValue());
        assertTrue(testDoc.validate(createOptions()));
        XmlInteger ival = XmlInteger.Factory.newInstance();
        ival.setBigIntegerValue(BigInteger.ZERO);

        testDoc.setAttAnyType(ival);

        // assertEquals(BigInteger.ZERO,testDoc.getAttAnyType().changeType(XmlInteger.type));
        assertEquals(BigInteger.ZERO.toString(), testDoc.getAttAnyType().getStringValue());

        assertTrue(testDoc.validate(createOptions()));
        XmlString sval = XmlString.Factory.newInstance();
        sval.setStringValue("foobar");
        testDoc.setAttAnyType(sval);
        assertEquals("foobar", testDoc.getAttAnyType().getStringValue());
        assertTrue(testDoc.validate(createOptions()));

        XmlDouble fval = XmlDouble.Factory.newInstance();
        fval.setDoubleValue(-0.01);
        testDoc.setAttAnyType(fval);
        assertEquals("-0.01", testDoc.getAttAnyType().getStringValue());
        assertTrue(testDoc.validate(createOptions()));
    }

    @Test
    void testAnonType() throws Throwable {
        String input =
            "<pre:GlobalAttrTypeDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrType\" " +
            "pre:attAnonymous=\" 1 \" " +
            " />";
        GlobalAttrTypeT testDoc = GlobalAttrTypeDocDocument.Factory.parse(input).getGlobalAttrTypeDoc();
        assertTrue(testDoc.validate(createOptions()));

        assertEquals(1, testDoc.getAttAnonymous().intValue());

        testDoc.setAttAnonymous( BigInteger.ZERO );
        assertEquals(0, testDoc.getAttAnonymous().intValue());
        assertTrue(testDoc.validate(createOptions()));
    }
}
