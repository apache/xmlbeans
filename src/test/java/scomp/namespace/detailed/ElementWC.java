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

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xbean.scomp.namespace.elementWC.*;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

//TODO: no test on minOccurs maxOccurs here
public class ElementWC {
    @Test
    void testAnyLaxLegal() throws XmlException {
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse(
            "<AnyLax " +
            "xmlns=\"http://xbean/scomp/namespace/ElementWC\" " +
            "xmlns:foobar=\"http://foo\">" +
            "<foobar:child/></AnyLax>");
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * Is it possible to have an illegal LAX/Any--think not
     */
    @Disabled
    @Test
    void testAnyLaxIllegal() throws XmlException {
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse(
            "<AnyLax " +
            "xmlns=\"http://xbean/scomp/namespace/ElementWC\" >" +
            "<child/></AnyLax>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = new String[]{XmlErrorCodes.ATTR_LOCALLY_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testAnySkipLegal() throws XmlException {
        AnySkipDocument doc = AnySkipDocument.Factory.parse(
            "<AnySkip " +
            "xmlns=\"http://xbean/scomp/namespace/ElementWC\" " +
            "xmlns:foobar=\"http://foo\">" +
            "<foobar:child/></AnySkip>");
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
            " xmlns:ns=\"http://xbean/scomp/namespace/ElementWC\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\"" +
            // " xsi:schemaLocation=\"http://xbean/scomp/element/GlobalEltDefault " +
            //  "GlobalEltDefault.xsd\"
            "> " +
            "<elt:GlobalEltDefaultStr/></ns:AnyStrict>");
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        XmlObject[] arr = doc.getAnyStrict().selectChildren(
            new QName("http://xbean/scomp/element/GlobalEltDefault", "GlobalEltDefaultStr", "elt"));
        assertEquals(XmlString.type, arr[0].schemaType());
    }

    @Test
    void testAnyStrictIllegal() throws XmlException {
        AnyStrictDocument doc = AnyStrictDocument.Factory.parse(
            "<AnyStrict " +
            "xmlns=\"http://xbean/scomp/namespace/ElementWC\" " +
            "xmlns:foobar=\"http://foo\">" +
            "<foobar:child/></AnyStrict>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testOtherLaxLegal() throws XmlException {
        OtherLaxDocument doc = OtherLaxDocument.Factory.parse(
            "<foo:OtherLax " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\" " +
            "xmlns:foobar=\"http:apache.org\" >" +
            "<foobar:child/></foo:OtherLax>");
        assertTrue(doc.validate(createOptions()));
    }

    //can not be in target NS
    //cannot be in noNS
    @Test
    void testOtherLaxIllegal() throws XmlException {
        OtherLaxDocument doc = OtherLaxDocument.Factory.parse(
            "<foo:OtherLax " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"> " +
            "<foo:child/></foo:OtherLax>");
        assertFalse(doc.validate(createOptions()));
        doc = OtherLaxDocument.Factory.parse(
            "<foo:OtherLax " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"> " +
            "<child/></foo:OtherLax>");
        assertFalse(doc.validate(createOptions()));
    }

    @Test
    void testOtherSkipLegal() throws XmlException {
        OtherSkipDocument doc = OtherSkipDocument.Factory.parse(
            "<foo:OtherSkip " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:elt=\"http://xbean/scomp/attribute/GlobalEltDefault\">" +
            "<elt:child/></foo:OtherSkip>");
        assertTrue(doc.validate(createOptions()));
    }

    //no ns not allowed by the wc
    @Test
    void testOtherSkipIllegal() throws XmlException {
        OtherSkipDocument doc = OtherSkipDocument.Factory.parse(
            "<foo:OtherSkip " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\" >" +
            "<child/></foo:OtherSkip>");
        assertFalse(doc.validate(createOptions()));
    }

    //"http://xbean/scomp/element/GlobalEltDefault"
    @Test
    void testOtherStrictLegal() throws XmlException {
        OtherStrictDocument doc = OtherStrictDocument.Factory.parse(
            "<foo:OtherStrict xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
            "<elt:GlobalEltDefaultStr/></foo:OtherStrict>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testOtherStrictIllegal() throws XmlException {
        OtherStrictDocument doc = OtherStrictDocument.Factory.parse(
            "<foo:OtherStrict " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\"" +
            " xsi:schemaLocation=\"http://xbean/scomp/element/GlobalEltDefault.xsd\"> " +
            "<elt:GlobalEltDefaultInt> foo " +
            "</elt:GlobalEltDefaultInt>" +
            "</foo:OtherStrict>");
        assertFalse(doc.validate(createOptions()));
    }

    //no declaration for this attr, no error on Lax
    @Test
    void testListLaxLegal() throws XmlException {
        ListLaxDocument doc = ListLaxDocument.Factory.parse(
            "<foo:ListLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:at=\"http://apache.org\">" +
            " <at:child/></foo:ListLax>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testListLaxIllegal() throws XmlException {
        ListLaxDocument doc = ListLaxDocument.Factory.parse(
            "<foo:ListLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\">" +
            " <at:child/></foo:ListLax>");
        assertFalse(doc.validate(createOptions()));
    }

    @Test
    void testListSkipLegal() throws XmlException {
        ListSkipDocument doc = ListSkipDocument.Factory.parse(
            "<foo:ListSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:at=\"http://apache.org\">" +
            " <at:child/></foo:ListSkip>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testListSkipIllegal() throws XmlException {
        ListSkipDocument doc = ListSkipDocument.Factory.parse(
            "<foo:ListSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:at=\"http://apache_org.org\">" +
            " <at:child/></foo:ListSkip>");
        assertFalse(doc.validate(createOptions()));
    }

    @Test
    void testListStrictLegal() throws XmlException {
        ListStrictDocument doc = ListStrictDocument.Factory.parse(
            "<foo:ListStrict " +
            "xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
            " <elt:GlobalEltDefaultInt/>" +
            "</foo:ListStrict>");
        assertTrue(doc.validate(createOptions()));
    }

    //element will not be found
    @Test
    void testListStrictIllegal() throws XmlException {
        ListStrictDocument doc = ListStrictDocument.Factory.parse(
            "<foo:ListStrict " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:at=\"http://apache.org\">" +
            " <at:child/></foo:ListStrict>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //replacement elements MUST be in the
    //  current target NS
    @Test
    void testTargetLaxLegal() throws XmlException {
        TargetLaxDocument doc = TargetLaxDocument.Factory.parse(
            "<foo:TargetLax" +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            "<foo:LocalElt>2</foo:LocalElt></foo:TargetLax>");
        assertTrue(doc.validate(createOptions()));
        XmlObject[] arr = doc.getTargetLax().selectChildren("http://xbean/scomp/namespace/ElementWC", "LocalElt");
        assertEquals(XmlInt.type, arr[0].schemaType());
    }

    //no such element in the NS
    @Test
    void testTargetLaxIllegal() throws XmlException {
        TargetLaxDocument doc = TargetLaxDocument.Factory.parse(
            "<foo:TargetLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\"" +
            "  xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\">" +
            " <at:child/></foo:TargetLax>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testTargetSkipLegal() throws XmlException {
        TargetSkipDocument doc = TargetSkipDocument.Factory.parse(
            "<foo:TargetSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <foo:child/></foo:TargetSkip>");
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * can a test ever be illegal here?
     */
    @Test
    void testTargetSkipIllegal() throws XmlException {
        TargetSkipDocument doc = TargetSkipDocument.Factory.parse(
            "<foo:TargetSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <child/></foo:TargetSkip>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

    }

    @Test
    void testTargetStrictLegal() throws XmlException {
        TargetStrictDocument doc = TargetStrictDocument.Factory.parse(
            "<foo:TargetStrict " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            "<foo:LocalElt>2</foo:LocalElt></foo:TargetStrict>");
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testTargetStrictIllegal() throws XmlException {
        TargetStrictDocument doc = TargetStrictDocument.Factory.parse(
            "<foo:TargetStrict " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <foo:child/></foo:TargetStrict>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalLaxLegal() throws XmlException {
        LocalLaxDocument doc = LocalLaxDocument.Factory.parse(
            "<foo:LocalLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <child/></foo:LocalLax>");
        assertTrue(doc.validate(createOptions()));
    }

    //no such child in current NS
    @Test
    void testLocalLaxIllegal() throws XmlException {
        LocalLaxDocument doc = LocalLaxDocument.Factory.parse(
            "<foo:LocalLax " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <foo:child/></foo:LocalLax>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalSkipLegal() throws XmlException {
        LocalSkipDocument doc = LocalSkipDocument.Factory.parse(
            "<foo:LocalSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <child/></foo:LocalSkip>");
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * can a test ever be illegal here?
     */
    @Disabled
    @Test
    void testLocalSkipIllegal() throws XmlException {
        LocalSkipDocument doc = LocalSkipDocument.Factory.parse(
            "<foo:LocalSkip " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <child/></foo:LocalSkip>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalStrictIllegal() throws XmlException {
        LocalStrictDocument doc = LocalStrictDocument.Factory.parse(
            "<foo:LocalStrict " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            " <child/></foo:LocalStrict>");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ASSESS_ELEM_SCHEMA_VALID$NOT_RESOLVED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testLocalStrictLegal() throws XmlException {
        LocalStrictDocument doc = LocalStrictDocument.Factory.parse(
            "<foo:LocalStrict " +
            " xmlns:foo=\"http://xbean/scomp/namespace/ElementWC\">" +
            "<NoNSElt>2</NoNSElt></foo:LocalStrict>");
        assertTrue(doc.validate(createOptions()));
    }
}

