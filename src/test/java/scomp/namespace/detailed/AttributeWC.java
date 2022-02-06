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

package scomp.namespace.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xbean.scomp.namespace.attributeWC.*;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class AttributeWC {
    @Test
    void testAnyLaxLegal() throws XmlException {
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse(
            "<AnyLax xmlns=\"http://xbean/scomp/namespace/AttributeWC\" attr1=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testAnyLaxIllegal() throws XmlException {
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse(
            "<AnyLax xmlns=\"http://xbean/scomp/namespace/AttributeWC\" attr1=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testAnySkipLegal() throws XmlException {
        AnySkipDocument doc = AnySkipDocument.Factory.parse(
            "<AnySkip xmlns=\"http://xbean/scomp/namespace/AttributeWC\" attr1=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * Everything is legal here
     * public void testAnySkipIllegal() throws XmlException {
     * }
     */
    //no NS is legal too
    @Test
    void testAnyStrictLegal() throws XmlException {
        AnyStrictDocument doc = AnyStrictDocument.Factory.parse(
            "<ns:AnyStrict" +
            " xmlns:ns=\"http://xbean/scomp/namespace/AttributeWC\" " +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
            "at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testAnyStrictIllegal() throws XmlException {
        AnyStrictDocument doc = AnyStrictDocument.Factory.parse(
            "<AnyStrict xmlns=\"http://xbean/scomp/namespace/AttributeWC\" attr1=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testOtherLaxLegal() throws XmlException {
        OtherLaxDocument doc = OtherLaxDocument.Factory.parse(
            "<foo:OtherLax " +
            "xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
            "xmlns:foobar=\"http:apache.org\" " +
            "foobar:attr1=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    //can not be in target NS
    //cannot be in noNS
    @Test
    void testOtherLaxIllegal() throws XmlException {
        OtherLaxDocument doc = OtherLaxDocument.Factory.parse(
            "<foo:OtherLax xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"  foo:attr1=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        doc = OtherLaxDocument.Factory.parse(
            "<foo:OtherLax xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" attr1=\"val1\"/>");
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testOtherSkipLegal() throws XmlException {
        OtherSkipDocument doc = OtherSkipDocument.Factory.parse(
            "<foo:OtherSkip xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" " +
            "xmlns:foobar=\"http:apache.org\" " +
            "foobar:attr1=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    //ns not allowed by the wc
    @Test
    void testOtherSkipIllegal() throws XmlException {
        OtherSkipDocument doc = OtherSkipDocument.Factory.parse(
            "<foo:OtherSkip xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"  foo:attr1=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testOtherStrictLegal() throws XmlException {
        OtherStrictDocument doc = OtherStrictDocument.Factory.parse(
            "<foo:OtherStrict xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));

    }

    @Test
    void testOtherStrictIllegal() throws XmlException {
        OtherStrictDocument doc = OtherStrictDocument.Factory.parse(
            "<foo:OtherStrict xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:test=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //no declaration for this attr, no error on Lax
    @Test
    void testListLaxLegal() throws XmlException {
        ListLaxDocument doc = ListLaxDocument.Factory.parse(
            "<foo:ListLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://apache.org\"" +
            " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testListLaxIllegal() throws XmlException {
        ListLaxDocument doc = ListLaxDocument.Factory.parse(
            "<foo:ListLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:test=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }


    @Test
    void testListSkipLegal() throws XmlException {
        ListSkipDocument doc = ListSkipDocument.Factory.parse(
            "<foo:ListSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://apache.org\"" +
            " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testListSkipIllegal() throws XmlException {
        ListSkipDocument doc = ListSkipDocument.Factory.parse(
            "<foo:ListSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://apache_org.org\"" +
            " at:testattribute=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //  " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrDefault\"
    @Test
    void testListStrictLegal() throws XmlException {
        ListStrictDocument doc = ListStrictDocument.Factory.parse(
            "<foo:ListStrict " +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:testattribute=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testListStrictIllegal() throws XmlException {
        ListStrictDocument doc = ListStrictDocument.Factory.parse(
            "<foo:ListStrict " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://apache.org\"" +
            " at:testattribute=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testTargetLaxLegal() throws XmlException {
        TargetLaxDocument doc = TargetLaxDocument.Factory.parse(
            "<foo:TargetLax  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" foo:testattribute=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testTargetLaxIllegal() throws XmlException {
        TargetLaxDocument doc = TargetLaxDocument.Factory.parse(
            "<foo:TargetLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:testattributeInt=\"foo\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testTargetSkipLegal() throws XmlException {
        TargetSkipDocument doc = TargetSkipDocument.Factory.parse(
            "<foo:TargetSkip  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" foo:undeclAttr=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * can a test ever be illegal here?
     */
    @Disabled
    @Test
    void testTargetSkipIllegal() throws XmlException {
        TargetSkipDocument doc = TargetSkipDocument.Factory.parse(
            "<foo:TargetSkip  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" foo:undeclAttr=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testTargetStrictLegal() throws XmlException {
        TargetStrictDocument doc = TargetStrictDocument.Factory.parse(
            "<foo:TargetStrict  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" foo:LocalAttr=\"3\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testTargetStrictIllegal() throws XmlException {
        TargetStrictDocument doc = TargetStrictDocument.Factory.parse(
            "<foo:TargetStrict  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" foo:LocalAttr=\"foo\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DECIMAL};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalLaxLegal() throws XmlException {
        LocalLaxDocument doc = LocalLaxDocument.Factory.parse(
            "<foo:LocalLax  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" undeclAttr=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testLocalLaxIllegal() throws XmlException {
        LocalLaxDocument doc = LocalLaxDocument.Factory.parse(
            "<foo:LocalLax  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" foo:undeclAttr=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NOT_WILDCARD_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalSkipLegal() throws XmlException {
        LocalSkipDocument doc = LocalSkipDocument.Factory.parse(
            "<foo:LocalSkip  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" undeclAttr=\"val1\"/>");
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * can a test ever be illegal here?
     */
    @Disabled
    @Test
    void testLocalSkipIllegal() throws XmlException {
        LocalSkipDocument doc = LocalSkipDocument.Factory.parse(
            "<foo:LocalSkip  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" undeclAttr=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalStrictIllegal() throws XmlException {
        LocalStrictDocument doc = LocalStrictDocument.Factory.parse(
            "<foo:LocalStrict  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" undeclAttr=\"val1\"/>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ATTR_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalStrictLegal() throws XmlException {
        LocalStrictDocument doc = LocalStrictDocument.Factory.parse(
            "<foo:LocalStrict  xmlns:foo=\"http://xbean/scomp/namespace/AttributeWC\" NoNSAttr=\"2\"/>");
        assertTrue(doc.validate(createOptions()));
    }
}
