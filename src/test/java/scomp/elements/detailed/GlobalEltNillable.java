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

package scomp.elements.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;
import org.junit.jupiter.api.Test;
import xbean.scomp.element.globalEltNillable.*;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class GlobalEltNillable {

    //xsi:nil illegal in instance if the elt is not nillable
    @Test
    void testNillableFalse() throws XmlException {
        String input =
            "<GlobalEltNotNillable" +
            "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:nil=\"false\"/>";
        GlobalEltNotNillableDocument testElt = GlobalEltNotNillableDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(testElt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$NOT_NILLABLE};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * Try to set a non-nillable elt. to nill
     * CR CR192914:
     * Regardless of Schema definition,
     * setXXX(null) will clear the value of the
     * XXX attribute/element and if the container is an
     * element, will also add the "xsi:nil" attribute.
     */
    @Test
    void testNotNillable() {

        // XmlValueNotNillableException should be thrown only when validateOnSet property is set
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        GlobalEltNotNillableDocument testElt1 = GlobalEltNotNillableDocument.Factory.newInstance(options);
        assertThrows(XmlValueNotNillableException.class, testElt1::setNil);
        assertThrows(XmlValueNotNillableException.class, () -> testElt1.set(null));

        options.setValidateOnSet(false);
        GlobalEltNotNillableDocument testElt = GlobalEltNotNillableDocument.Factory.newInstance(options);
        testElt.setGlobalEltNotNillable(null);

        //assert that value is cleared
        assertEquals("<glob:GlobalEltNotNillable " +
                                "xsi:nil=\"true\" " +
                                "xmlns:glob=\"http://xbean/scomp/element/GlobalEltNillable\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", testElt.xmlText());

        XmlOptions validateOptions = createOptions();
        assertFalse(testElt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$NOT_NILLABLE};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //for nillable, fixed value cannot be specified (instance error) :
    // Walmsley p.137 footnote
    @Test
    void testNillableFixed() throws XmlException {
        String input =
            "<GlobalEltNillableFixed" +
            "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            "   xsi:nil=\"true\"" +
            "/>";
        GlobalEltNillableFixedDocument testElt = GlobalEltNillableFixedDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(testElt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$NIL_WITH_FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    @Test
    void testNillableInt() throws XmlException {
        String input =
            "<GlobalEltNillableInt" +
            "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:nil=\"true\"/>";
        GlobalEltNillableIntDocument testElt = GlobalEltNillableIntDocument.Factory.parse(input);

        XmlOptions validateOptions = createOptions();
        assertTrue(testElt.validate(validateOptions));
        assertTrue(testElt.isNilGlobalEltNillableInt());
        assertEquals(0, testElt.getGlobalEltNillableInt());

        //after setting the value, the nil attribute should be gone
        testElt.setGlobalEltNillableInt(3);
        assertEquals("<GlobalEltNillableInt" +
                            " xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
                            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                            ">3</GlobalEltNillableInt>", testElt.xmlText());
    }


    //default value not filled in for nillable elts when xsi:nil=true
    // $TODO: check w/ Kevin--what is the value of a nillable attr if it's a primitive type????
    @Test
    void testNillableDefault() throws Exception {
        String input =
            "<GlobalEltNillableDefault" +
            "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:nil=\"true\"/>";
        GlobalEltNillableDefaultDocument testElt = GlobalEltNillableDefaultDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertTrue(testElt.validate(validateOptions));

        assertEquals(0, testElt.getGlobalEltNillableDefault());
    }

    // An element with xsi:nil="true" may not have any element content but it
    //  may still carry attributes.
    @Test
    void testComplexNillable() throws XmlException {
        String input =
            "<GlobalEltComplex" +
            "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:nil=\"true\"><nestedElt/></GlobalEltComplex>";
        GlobalEltComplexDocument testElt = GlobalEltComplexDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(testElt.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_LOCALLY_VALID$NIL_WITH_CONTENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        input =
            "<GlobalEltComplex" +
            "   xmlns=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " xsi:nil=\"true\" testattribute=\"foobar\"/>";
        testElt = GlobalEltComplexDocument.Factory.parse(input);
        assertTrue(testElt.validate(validateOptions));
    }

    /**
     * calling setNil should inserts
     * attr and delete value
     */
    @Test
    void testDelete() throws XmlException {
        String input =
            "<pre:GlobalEltComplex" +
            "   xmlns:pre=\"http://xbean/scomp/element/GlobalEltNillable\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            " testattribute=\"foobar\">" +
            "<nestedElt>" +
            "foo</nestedElt></pre:GlobalEltComplex>";
        GlobalEltComplexDocument testElt = GlobalEltComplexDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertTrue(testElt.validate(validateOptions));
        testElt.getGlobalEltComplex().setNil();
        assertEquals("<pre:GlobalEltComplex " +
                                "testattribute=\"foobar\" " +
                                "xsi:nil=\"true\" " +
                                "xmlns:pre=\"http://xbean/scomp/element/GlobalEltNillable\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", testElt.xmlText());
        assertTrue(testElt.validate(validateOptions));
    }

}
