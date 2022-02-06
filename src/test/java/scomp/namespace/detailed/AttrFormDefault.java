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

package scomp.namespace.detailed;

import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xbean.scomp.namespace.attributeFormDefault.AttributeFormDefaultEltDocument;
import xbean.scomp.namespace.attributeFormDefault.ElementT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;

/**
 *
 */
public class AttrFormDefault {
    @Test
    void testValid() throws Throwable {
        String input =
            "<ns:AttributeFormDefaultElt xmlns:ns=\"http://xbean/scomp/namespace/AttributeFormDefault\" ns:localAttr=\"foobar\"/>";
        AttributeFormDefaultEltDocument doc = AttributeFormDefaultEltDocument.Factory.parse(input);
        XmlOptions validateOptions = createOptions();
        doc.validate(validateOptions);
    }

    @Test
    void testInvalid() throws Throwable {
        AttributeFormDefaultEltDocument doc = AttributeFormDefaultEltDocument.Factory.newInstance();
        ElementT elt = doc.addNewAttributeFormDefaultElt();
        XmlAnySimpleType val = XmlAnySimpleType.Factory.newInstance();
        val.setStringValue("345");
        elt.setLocalAttr(val);
        XmlOptions validateOptions = createOptions();
        assertTrue(doc.validate(validateOptions));

        String input =
            "<ns:AttributeFormDefaultElt xmlns:ns=\"http://xbean/scomp/namespace/AttributeFormDefault\" localAttr=\"foobar\"/>";
        doc = AttributeFormDefaultEltDocument.Factory.parse(input);
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$NO_WILDCARD};
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));
    }
}
