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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.emtpy.RestrictedEmptyEltDocument;
import xbean.scomp.derivation.emtpy.RestrictedEmptyT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

public class EmptyContentRestriction {

    @Test
    void testRestriction() throws Throwable {
        RestrictedEmptyEltDocument doc = RestrictedEmptyEltDocument.Factory.newInstance();

        RestrictedEmptyT elt = doc.addNewRestrictedEmptyElt();
        elt.setEmptyAttr("foobar");
        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ATTR_LOCALLY_VALID$FIXED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        elt.setEmptyAttr("myval");
        assertTrue(doc.validate(validateOptions));

        try (XmlCursor cur = elt.newCursor()) {
            cur.toFirstContentToken();
            cur.toNextToken();
            cur.beginElement("foobar");
        }
        String expXml =
            "<xml-fragment>" +
            "<emt:RestrictedEmptyElt emptyAttr=\"myval\" " +
            "xmlns:emt=\"http://xbean/scomp/derivation/Emtpy\"/>" +
            "<foobar/></xml-fragment>";
        assertEquals(expXml, doc.xmlText());

        validateOptions.getErrorListener().clear();
        assertFalse(doc.validate(validateOptions));
        errExpected = new String[]{XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
