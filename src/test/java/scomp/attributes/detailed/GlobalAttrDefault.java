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
import xbean.scomp.attribute.globalAttrDefault.GlobalAttrDefaultDocDocument;
import xbean.scomp.attribute.globalAttrDefault.GlobalAttrDefaultT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class GlobalAttrDefault {
    /**
     * If value is missing default should appear
     */
    @Test
    void testMissing() {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.newInstance()
                .addNewGlobalAttrDefaultDoc();
        assertEquals("XBeanAttr", testDoc.getTestattribute());
    }

    /**
     * Test val preservation
     */
    @Test
    void testPresent() {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.newInstance()
                .addNewGlobalAttrDefaultDoc();
        testDoc.setTestattribute("Existing");
        assertEquals("Existing", testDoc.getTestattribute());
    }

    /**
     * Test empty string: should be preserved
     */
    @Test
    void testPresentEmpty() throws Throwable {
        GlobalAttrDefaultT testDoc =
            GlobalAttrDefaultDocDocument.Factory.parse("<pre:GlobalAttrDefaultDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
                " pre:testattribute=\"\"/>").getGlobalAttrDefaultDoc();
        assertEquals("", testDoc.getTestattribute());
        assertTrue(testDoc.validate(createOptions()));
    }


    /**
     * Type mismatch
     */
    @Test
    void testBadType() throws XmlException {
        GlobalAttrDefaultT testDoc =
                GlobalAttrDefaultDocDocument.Factory.parse("<pre:GlobalAttrDefaultDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrDefault\" " +
                "pre:testattributeInt=\"\"/>").getGlobalAttrDefaultDoc();
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));

        String[] errExpected={XmlErrorCodes.DECIMAL};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }


}
