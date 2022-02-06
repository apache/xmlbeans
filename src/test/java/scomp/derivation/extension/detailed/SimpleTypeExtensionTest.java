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

import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.simpleExtension.SimpleExtensionEltDocument;
import xbean.scomp.derivation.simpleExtension.SimpleExtensionT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;

public class SimpleTypeExtensionTest {

    @Test
    void testExtension() throws Throwable {
        SimpleExtensionEltDocument doc = SimpleExtensionEltDocument.Factory.newInstance();
        SimpleExtensionT elt = doc.addNewSimpleExtensionElt();

        assertFalse(doc.validate(createOptions()));

        elt.setStringValue("1");
        assertTrue(elt.validate());
        elt.setAttribute("ATTR_VAL");
        assertTrue(doc.validate());
        assertEquals("ATTR_VAL", elt.getAttribute());
        elt.unsetAttribute();
        assertNull(elt.getAttribute());
        assertFalse(elt.isSetAttribute());

        // why does type mismatch show up as XmlValueOutOfRangeException ?
        // updated: ok, since a setStringValue is used for an integer, this is a case where set value cannot be converted
        // into any of the possible valid types. Hence an exception is
        // throw irrespective of the setValidateOnSet XmlOption
        assertThrows(XmlValueOutOfRangeException.class, () -> elt.setStringValue("foobar"));
    }
}
