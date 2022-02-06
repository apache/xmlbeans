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

package scomp.derivation.restriction.detailed;

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.elementWCRestriction.ConcreteEltDocument;
import xbean.scomp.derivation.elementWCRestriction.OtherLaxDocument;
import xbean.scomp.derivation.elementWCRestriction.UriSkipDocument;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

//TODO compile time tests w/ occurence contstraints
public class ElementWCRestriction {
    // max occurs is now 2, not 3
    //NS restricted from any to other
    @Test
    void testMaxOccurs() throws XmlException {
        XmlOptions validateOptions = createOptions();
        String input =
            "<OtherLax " +
            " xmlns=\"http://xbean/scomp/derivation/ElementWCRestriction\"" +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "</OtherLax>";
        OtherLaxDocument doc = OtherLaxDocument.Factory.parse(input);
        assertTrue(doc.validate(validateOptions));

        //more than 2 elts not OK anymore
        //TODO: how do you add more than one elt here? only a
        //  setXXX method...no array
        input =
            "<OtherLax " +
            " xmlns=\"http://xbean/scomp/derivation/ElementWCRestriction\"" +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "<elt:GlobalEltDefaultInt>3</elt:GlobalEltDefaultInt>" +
            "</OtherLax>";
        doc = OtherLaxDocument.Factory.parse(input);

        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        //Only valid NS should be Other
        input =
            "<OtherLax " +
            " xmlns=\"http://xbean/scomp/derivation/ElementWCRestriction\">" +
            "<testElt>foo</testElt></OtherLax>";
        doc = OtherLaxDocument.Factory.parse(input);
        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //elt needs to occur exactly 2x
    //only URI allowed is GlobalElt
    @Test
    void testMinOccurs() throws XmlException {
        XmlOptions validateOptions = createOptions();
        String input =
            "<UriSkip " +
            " xmlns=\"http://xbean/scomp/derivation/ElementWCRestriction\"" +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "</UriSkip>";
        UriSkipDocument doc = UriSkipDocument.Factory.parse(input);
        assertTrue(doc.validate(validateOptions));

        //MinOccurs is 2
        input =
            "<UriSkip " +
            " xmlns=\"http://xbean/scomp/derivation/ElementWCRestriction\"" +
            " xmlns:elt=\"http://xbean/scomp/element/GlobalEltDefault\">" +
            "<elt:GlobalEltDefaultStr>foo</elt:GlobalEltDefaultStr>" +
            "</UriSkip>";
        doc = UriSkipDocument.Factory.parse(input);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    //WC replaced by elt
    //maxOccurs is 1
    @Test
    void testConcrete() throws Throwable {
        XmlOptions validateOptions = createOptions();
        String input =
            "<foo:ConcreteElt " +
            "xmlns:foo=\"http://xbean/scomp/derivation/ElementWCRestriction\">" +
            "<concreteChild>foo</concreteChild>" +
            "</foo:ConcreteElt>";
        ConcreteEltDocument doc = ConcreteEltDocument.Factory.parse(input);
        assertTrue(doc.validate(validateOptions));

        //Max Occurs is 1
        input =
            "<foo:ConcreteElt " +
            "xmlns:foo=\"http://xbean/scomp/derivation/ElementWCRestriction\">" +
            "<concreteChild>foo</concreteChild>" +
            "<concreteChild>2</concreteChild>" +
            "</foo:ConcreteElt>";
        doc = ConcreteEltDocument.Factory.parse(input);
        assertFalse(doc.validate(validateOptions));

        //child other than that elt
        input =
            "<ConcreteElt " +
            "xmlns=\"http://xbean/scomp/derivation/ElementWCRestriction\">" +
            "<testElt>foo</testElt></ConcreteElt>";
        validateOptions.getErrorListener().clear();
        doc = ConcreteEltDocument.Factory.parse(input);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}

