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

import scomp.common.BaseCase;
import xbean.scomp.contentType.union.UnionEltDocument;
import xbean.scomp.contentType.union.UnionOfUnionsDocument;
import xbean.scomp.contentType.union.UnionOfUnionsT;
import xbean.scomp.contentType.union.UnionOfListsDocument;

import java.util.List;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class UnionType extends BaseCase {
    /**
     * should be a bunch of negative cases at compile time
     */
    public void testUnionType() throws Throwable {
        UnionEltDocument doc = UnionEltDocument.Factory.newInstance();
        assertEquals(null, doc.getUnionElt());
        doc.setUnionElt("small");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionElt(new Integer(2));
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionElt(new Integer(-2));
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionElt(new Integer(5));
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$UNION};
                    assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * valid instance w/ xsi:type hint
     *
     * @throws Throwable
     */
    public void testParseInstanceValid() throws Throwable {
        String input =
                "<UnionElt xmlns=\"http://xbean/scomp/contentType/Union\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xsi:type=\"GlobalSimpleT2\">" +
                "-2" +
                "</UnionElt>";
        UnionEltDocument doc = UnionEltDocument.Factory.parse(input);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    /**
     * invalid instance w/ xsi:type hint
     *
     * @throws Throwable
     */
    public void testParseInstanceInvalid() throws Throwable {
        String input =
                "<UnionElt xmlns=\"http://xbean/scomp/contentType/Union\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                " xsi:type=\"GlobalSimpleT1\">" +
                "-2" +
                "</UnionElt>";
        UnionEltDocument doc = UnionEltDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID};
                    assertTrue(compareErrorCodes(errExpected));

    }

    /**
     * All values in the union should be legal. All others-not
     *
     * @throws Throwable
     */
    public void testUnionOfUnions() throws Throwable {
        UnionOfUnionsDocument doc = UnionOfUnionsDocument.Factory.newInstance();
        doc.setUnionOfUnions("large");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        UnionOfUnionsT elt = UnionOfUnionsT.Factory.newInstance();
        elt.setObjectValue(new Integer(-3));
        doc.xsetUnionOfUnions(elt);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("addVal1");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("addVal2");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("addVal4");
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        doc.setUnionOfUnions("foobar");

        assertTrue(!doc.validate(validateOptions));

        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
                    assertTrue(compareErrorCodes(errExpected));


    }

    /**
     * values allolwed here are either a list of (small, med, large, 1-3,-1,-2,-3}
     * or     (lstsmall, lstmed, lstlarge)
     */

    public void testUnionOfLists() throws Throwable {
        UnionOfListsDocument doc = UnionOfListsDocument.Factory.newInstance();
        List vals = new ArrayList();
        vals.add("small");
        vals.add(new Integer(-1));
        vals.add(new Integer(-2));
        vals.add(new Integer(-3));
        vals.add(new Integer(3));
        vals.add("medium");
        doc.setUnionOfLists(vals);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        vals.clear();
        vals.add("lstsmall");
        vals.add("lstlarge");

        doc.setUnionOfLists(vals);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        vals.clear();

        //mixing and matching should not be allowed
        //the list shoudl have exactly one of the 2 union types
        vals.add("lstsmall");
        vals.add(new Integer(-1));
        doc.setUnionOfLists( vals );
        assertTrue(! doc.validate(validateOptions) );
        showErrors();
        String[] errExpected = new String[]{"cvc-attribute"};
                    assertTrue(compareErrorCodes(errExpected));


    }
}

