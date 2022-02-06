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
package scomp.redefine.detailed;

import org.junit.jupiter.api.Test;
import xbean.scomp.redefine.attrGroupRedefine.AttrGroupEltDocument;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttrGroupRedefine {

    /**
     * test that fields from the old type def are not
     * visible anymore
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testCodeGeneration() throws NoSuchMethodException {
        AttrGroupEltDocument doc = AttrGroupEltDocument.Factory.newInstance();
        AttrGroupEltDocument.AttrGroupElt elt = doc.addNewAttrGroupElt();

        assertThrows( NoSuchFieldException.class, () -> elt.getClass().getDeclaredField("attr2"),
            "field should be redefined");


        elt.getClass().getDeclaredMethod("getAttr1");
        elt.getClass().getDeclaredMethod("getAttr2A");

        Method m = elt.getClass().getDeclaredMethod("getAttr3A");
        assertEquals(Integer.TYPE, m.getReturnType());
    }
}
