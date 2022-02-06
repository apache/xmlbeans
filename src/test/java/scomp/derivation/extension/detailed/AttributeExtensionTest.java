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
import xbean.scomp.derivation.attributeExtension.ExtendedElementDocument;
import xbean.scomp.derivation.attributeExtension.ExtendedT;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;

public class AttributeExtensionTest {
    /**
     * Attribute w/ same LN but diff NS in base type
     * Other scenarious are compile time errors
     */
    @Test
    void testAttribute() throws Throwable {
        ExtendedElementDocument doc = ExtendedElementDocument.Factory.newInstance();
        ExtendedT elt = doc.addNewExtendedElement();
        assertTrue(doc.validate(createOptions()));

        elt.setTestattribute("foo");
        elt.setTestattribute2("bar");
        elt.setTestattributeInt(new BigInteger("10"));
        assertTrue(doc.validate(createOptions()));

        //make sure attr w/ value foo is in the imported NS
        String expected =
            "<att:ExtendedElement glob:testattribute=\"foo\" " +
            "testattribute=\"bar\" glob:testattributeInt=\"10\" " +
            "xmlns:att=\"http://xbean/scomp/derivation/AttributeExtension\" " +
            "xmlns:glob=\"http://xbean/scomp/attribute/GlobalAttrDefault\"/>";
        assertEquals(expected, doc.xmlText());
    }
}
