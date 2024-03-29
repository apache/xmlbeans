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
package scomp.derivation.extension.detailed;

import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.attributeWCExtension.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;

public class AttributeWCExtension {

    /**
     * Strict wildcard ##local
     * Base type didn't have wildcards
     */
    @Test
    void testRestrictBaseWOWC() throws Throwable {
        String input =
            "<pre:BaseNoWC " +
            " xmlns:pre=\"http://xbean/scomp/derivation/AttributeWCExtension\"" +
            " xmlns:base=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
            " base:testattribute=\"val\"" +
            " base:testattributeInt=\"3\"" +
            " pre:testAttr=\"val1\"/>";
        BaseNoWCDocument doc = BaseNoWCDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * Strict validation: attr should be found AND valid
     */
    @Test
    void testAnyStrict() throws Throwable {
        String input =
            "<foo:AnyStrict  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" foo:testAttr=\"val1\"/>";
        AnyStrictDocument doc = AnyStrictDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * Lax validation:IF attr is found, it should be valid
     */
    @Test
    void testAnyLax() throws Throwable {
        String input =
            "<foo:AnyLax  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" testAttr=\"val1\"/>";
        AnyLaxDocument doc = AnyLaxDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testAnySkip() throws Throwable {
        String input =
            "<foo:AnySkip  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" testAttr=\"val1\"/>";
        AnySkipDocument doc = AnySkipDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * target and local here too
     */
    @Test
    void test2ListsLax() throws Throwable {

        String input =
            "<foo:UriListLax  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" testAttr=\"val1\"/>";
        UriListLaxDocument doc = UriListLaxDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * any valid XML should be OK,
     * as long as NS is other or target
     */
    @Test
    void test2ListsSkip() throws Throwable {
        String input =
            "<foo:UriListSkip  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" foo:testAttr=\"val1\"/>";
        UriListSkipDocument doc = UriListSkipDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void test2ListsStrict() throws Throwable {
        String input =
            "<foo:UriListStrict " +
            " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\"" +
            " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrFixed\"" +
            " at:testattributeStr=\"XBeanAttrStr\"/>";
        UriListStrictDocument doc = UriListStrictDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * target and local here too
     */
    @Test
    void testOtherListLax() throws Throwable {
        String input =
            "<foo:OtherListLax  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" testAttr=\"val1\"/>";
        OtherListLaxDocument doc = OtherListLaxDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testOtherListSkip() throws Throwable {
        String input =
            "<foo:OtherListSkip  xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\" foo:undeclAttr=\"val1\"/>";
        OtherListSkipDocument doc = OtherListSkipDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testOtherListStrict() throws Throwable {
        String input =
            "<foo:OtherListStrict " +
            " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\"" +
            " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrFixed\"" +
            " at:testattributeStr=\"XBeanAttrStr\"/>";

        OtherListStrictDocument doc = OtherListStrictDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * target and local here too
     * Other other should be equivalent to ##any
     */
    @Test
    void testOtherOtherLax() throws Throwable {
        String input =
            "<foo:OtherOtherLax " +
            " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\"" +
            " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:testAttr=\"val1\"/>";
        OtherOtherLaxDocument doc = OtherOtherLaxDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testOtherOtherSkip() throws Throwable {
        String input =
            "<foo:OtherOtherSkip " +
            " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\"" +
            " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrDefault\"" +
            " at:foobar=\"val1\"/>";
        OtherOtherSkipDocument doc = OtherOtherSkipDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    @Test
    void testOtherOtherStrict() throws Throwable {
        String input =
            "<foo:OtherOtherStrict " +
            " xmlns:foo=\"http://xbean/scomp/derivation/AttributeWCExtension\"" +
            " xmlns:at=\"http://xbean/scomp/attribute/GlobalAttrFixed\"" +
            " at:testattributeStr=\"XBeanAttrStr\"/>";

        OtherOtherStrictDocument doc = OtherOtherStrictDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }
}
