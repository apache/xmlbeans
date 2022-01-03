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
 package scomp.contentType.complex.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlString;
import org.junit.Test;
import scomp.common.BaseCase;
import xbean.scomp.contentType.complexTypeTest.EmptyT;
import xbean.scomp.contentType.complexTypeTest.EmptyTypeDocument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmptyContentTest extends BaseCase {
    @Test
    public void testIllegalContent() {
        EmptyTypeDocument doc = EmptyTypeDocument.Factory.newInstance();
        EmptyT elt = doc.addNewEmptyType();
        assertTrue(!elt.isSetEmptyAttr());
        elt.setEmptyAttr("foobar");
        assertTrue(elt.validate());
        try (XmlCursor cur = elt.newCursor()) {
            cur.toFirstContentToken();
            cur.beginElement("foobarElt");
        }
        assertTrue(!elt.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$ELEMENT_NOT_ALLOWED
        };
        assertTrue(compareErrorCodes(errExpected));

    }

    @Test
    public void testLegalContent() {
        EmptyTypeDocument doc = EmptyTypeDocument.Factory.newInstance();
        EmptyT elt = doc.addNewEmptyType();
        assertTrue(!elt.isSetEmptyAttr());
        elt.setEmptyAttr("foobar");
        assertTrue(elt.isSetEmptyAttr());
        assertEquals("foobar", elt.getEmptyAttr());

        XmlString expected=XmlString.Factory.newInstance();
        expected.setStringValue("foobar");

        XmlString expected1=XmlString.Factory.newInstance();
        expected1.setStringValue("foobar");

        System.out.println( expected.equals(expected1));
       assertTrue( expected.valueEquals(elt.xgetEmptyAttr()) );

        elt.unsetEmptyAttr();
        assertTrue(!elt.isSetEmptyAttr());
        assertTrue(elt.validate());
    }
}
