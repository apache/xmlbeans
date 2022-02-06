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
import org.junit.jupiter.api.Test;
import xbean.scomp.element.localEltMinMaxOccurs.MinMaxOccursDocDocument;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 *
 */
public class LocalEltMinMaxOccurs {

    @Test
    void testMinOccursZero() throws XmlException {
        String input =
            "<MinMaxOccursDoc" +
            " xmlns=\"http://xbean/scomp/element/LocalEltMinMaxOccurs\">" +
            "<minOccursOne>1</minOccursOne>" +
            "<maxOccursOne>1</maxOccursOne>" +
            "<twoToFour>1</twoToFour>" +
            "<twoToFour>1</twoToFour>" +
            "</MinMaxOccursDoc>";
        MinMaxOccursDocDocument testDoc = MinMaxOccursDocDocument.Factory.parse(input);
        assertTrue(testDoc.validate());
    }


//    @Test
//    public void testMinGTMaxOccurs() {
//        //compile time error raised correctly. Same for neg values
//    }

    // twoToFour occurs only once
    @Test
    void testInstanceLTMinOccurs() throws XmlException {
        String input =
            "<MinMaxOccursDoc" +
            " xmlns=\"http://xbean/scomp/element/LocalEltMinMaxOccurs\">" +
            "<minOccursOne>1</minOccursOne>" +
            "<maxOccursOne>1</maxOccursOne>" +
            "<twoToFour>1</twoToFour>" +
            "</MinMaxOccursDoc>";
        MinMaxOccursDocDocument testDoc = MinMaxOccursDocDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

    // maxOccursOne occurs 2ce
    @Test
    void testInstanceGTMaxOccurs() throws Exception {
        String input =
            "<MinMaxOccursDoc" +
            " xmlns=\"http://xbean/scomp/element/LocalEltMinMaxOccurs\">" +
            "<minOccursOne>1</minOccursOne>" +
            "<maxOccursOne>1</maxOccursOne>" +
            "<maxOccursOne>1</maxOccursOne>" +
            "<twoToFour>1</twoToFour>" +
            "<twoToFour>1</twoToFour>" +
            "</MinMaxOccursDoc>";
        MinMaxOccursDocDocument testDoc = MinMaxOccursDocDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        assertFalse(testDoc.validate(validateOptions));
        //TODO: why is this not element not allowed?

        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }

}
