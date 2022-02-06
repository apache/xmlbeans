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

package scomp.contentType.simple.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.jupiter.api.Test;
import xbean.scomp.contentType.union.UnionEltDocument;
import xbean.scomp.contentType.union.UnionOfListsDocument;
import xbean.scomp.contentType.union.UnionOfUnionsDocument;
import xbean.scomp.contentType.union.UnionOfUnionsT;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;


public class UnionType {
    /**
     * should be a bunch of negative cases at compile time
     */
    @Test
    void testUnionType() throws Throwable {
        UnionEltDocument doc = UnionEltDocument.Factory.newInstance();
        assertNull(doc.getUnionElt());
        doc.setUnionElt("small");

        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        doc.setUnionElt(2);
        assertTrue(doc.validate(validateOptions));
        doc.setUnionElt(-2);
        assertTrue(doc.validate(validateOptions));
        doc.setUnionElt(5);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_VALID$UNION};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * valid instance w/ xsi:type hint
     */
    @Test
    void testParseInstanceValid() throws Throwable {
        String input =
            "<UnionElt xmlns=\"http://xbean/scomp/contentType/Union\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            " xsi:type=\"GlobalSimpleT2\">" +
            "-2" +
            "</UnionElt>";
        UnionEltDocument doc = UnionEltDocument.Factory.parse(input);
        assertTrue(doc.validate(createOptions()));
    }

    /**
     * invalid instance w/ xsi:type hint
     */
    @Test
    void testParseInstanceInvalid() throws Throwable {
        String input =
            "<UnionElt xmlns=\"http://xbean/scomp/contentType/Union\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            " xsi:type=\"GlobalSimpleT1\">" +
            "-2" +
            "</UnionElt>";
        UnionEltDocument doc = UnionEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    /**
     * Specifiying value for a union that is not part of the consitituent types. The constituent types in this schema
     * are enumerations and not basic XmlSchema types and hence get translated into enum types in the XmlObjects
     */
    @Test
    void testUnionOfUnions() throws Throwable {
        UnionOfUnionsDocument doc = UnionOfUnionsDocument.Factory.newInstance();
        doc.setUnionOfUnions("large");
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));
        UnionOfUnionsT elt = UnionOfUnionsT.Factory.newInstance();
        elt.setObjectValue(-3);
        doc.xsetUnionOfUnions(elt);
        assertTrue(doc.validate(validateOptions));
        doc.setUnionOfUnions("addVal1");
        assertTrue(doc.validate(validateOptions));
        doc.setUnionOfUnions("addVal2");
        assertTrue(doc.validate(validateOptions));
        doc.setUnionOfUnions("addVal4");
        assertTrue(doc.validate(validateOptions));
        // setting a value outside of the union should throw an exception as
        // type inside the Xmlobject is an enumeration and has a fixed number of constants in the type
        // This will fail irrespective of the setValidateOnSet() option
        assertThrows(XmlValueOutOfRangeException.class, () -> doc.setUnionOfUnions("foobar"));

        assertTrue(doc.validate(validateOptions));
    }

    // for the above test (testUnionOfUnions), if the value set for the union type is AnyType (in the schema)
    // but the Java type defined as say Integer or Date then an Exception should be thrown only if
    // validateOnSet XmlOption is set and not otherwise.
    @Test
    void UnionOfUnions2() {
        UnionOfUnionsDocument doc = UnionOfUnionsDocument.Factory.newInstance();
        doc.setUnionOfUnions("4");

        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));

        // now validate with setValidateOnSetoption
        XmlOptions optWithValidateOnSet = new XmlOptions();
        optWithValidateOnSet.setValidateOnSet();

        UnionOfUnionsDocument doc2 = UnionOfUnionsDocument.Factory.newInstance(optWithValidateOnSet);
        assertThrows(XmlValueOutOfRangeException.class, () -> doc2.setUnionOfUnions("4"));
    }

    /**
     * values allolwed here are either a list of (small, med, large, 1-3,-1,-2,-3}
     * or     (lstsmall, lstmed, lstlarge)
     */
    @Test
    void testUnionOfLists() throws Throwable {
        UnionOfListsDocument doc = UnionOfListsDocument.Factory.newInstance();
        List<Object> vals = Arrays.asList("small", -1, -2, -3, 3, "medium");

        doc.setUnionOfLists(vals);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        vals = Arrays.asList("lstsmall","lstlarge");

        doc.setUnionOfLists(vals);
        assertTrue(doc.validate(validateOptions));

        //mixing and matching should not be allowed
        //the list shoudl have exactly one of the 2 union types
        List<Object> vals2 = Arrays.asList("lstsmall", -1);

        // if the type in a union and cannot be converted into any of the union types, and in this case
        // since the list have enumerations, an exception is expected irrespective of validateOnSet XmlOption
        // being set. Refer testUnionOfUnions comment also
        assertThrows(XmlValueOutOfRangeException.class, () -> doc.setUnionOfLists(vals2));
    }
}

