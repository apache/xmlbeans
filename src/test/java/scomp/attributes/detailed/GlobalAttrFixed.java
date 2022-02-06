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


import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.attribute.globalAttrFixed.GlobalAttrFixedDocDocument;
import xbean.scomp.attribute.globalAttrFixed.GlobalAttrFixedT;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class GlobalAttrFixed {

    /**
     * Missing OK
     */
    @Test
    void testValidMissing() throws Exception {
        String input = "<pre:GlobalAttrFixedDoc xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" />";
        GlobalAttrFixedT testDoc = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();
        assertTrue(testDoc.validate());
    }

    @Test
    void testBothValid() throws Throwable {
        String input =
            "<pre:GlobalAttrFixedDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
            "pre:testattributeStr=\"XBeanAttrStr\" " +
            "pre:testattributeInt=\" 1 \"/>";
        GlobalAttrFixedT testDoc = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();
        assertTrue(testDoc.validate(createOptions()));
        assertEquals("XBeanAttrStr", testDoc.getTestattributeStr());
        assertEquals(1, testDoc.getTestattributeInt().intValue());
    }

    /**
     * value does not match fixed
     */
    @Test
    void testStringInvalidSpace() throws Exception {
        String input =
            "<pre:GlobalAttrFixedDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
            "pre:testattributeStr=\" XBeanAttrStr \"/>";
        GlobalAttrFixedT testAtt = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();
        XmlOptions validateOptions = createOptions();
        assertFalse(testAtt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));


        //catch XmlExceptionHere;
    }

    @Test
    void testStringInvalidValue() throws Exception {
        String input =
            "<pre:GlobalAttrFixedDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
            "pre:testattributeStr=\" foobar \" />";
        GlobalAttrFixedT testAtt = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();
        XmlOptions validateOptions = createOptions();
        assertFalse(testAtt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * Test empty string: should be preserved
     */
    @Test
    void testIntInvalidType() throws XmlException {
        String input =
            "<pre:GlobalAttrFixedDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
            "pre:testattributeInt=\" foo \"/>";
        GlobalAttrFixedT testAtt = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();

        XmlOptions validateOptions = createOptions();
        assertFalse(testAtt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DECIMAL};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testIntInvalidValue() throws XmlException {
        String input =
            "<pre:GlobalAttrFixedDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
            "pre:testattributeInt=\" 4 \"/>";
        GlobalAttrFixedT testAtt = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();
        XmlOptions validateOptions = createOptions();
        assertFalse(testAtt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testIntValidValue() throws Throwable {
        String input =
            "<pre:GlobalAttrFixedDoc" +
            " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrFixed\" " +
            "pre:testattributeInt=\" +01 \"/>";
        GlobalAttrFixedT testAtt = GlobalAttrFixedDocDocument.Factory.parse(input).getGlobalAttrFixedDoc();
        assertTrue(testAtt.validate(createOptions()));
    }

    @Test
    void testSetValue() {
        GlobalAttrFixedDocDocument testAtt = GlobalAttrFixedDocDocument.Factory.newInstance();
        GlobalAttrFixedT testDoc = testAtt.addNewGlobalAttrFixedDoc();
        testDoc.setTestattributeInt(new BigInteger("5"));
        //shouldn't this fail?
        assertEquals(5, testDoc.getTestattributeInt().intValue());
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
