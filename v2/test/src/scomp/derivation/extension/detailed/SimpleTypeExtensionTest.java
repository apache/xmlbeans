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

import xbean.scomp.derivation.simpleExtension.SimpleExtensionEltDocument;
import xbean.scomp.derivation.simpleExtension.SimpleExtensionT;
import scomp.common.BaseCase;

/**
 * @owner: ykadiysk
 * Date: Jul 21, 2004
 * Time: 10:14:25 AM
 */
public class SimpleTypeExtensionTest extends BaseCase {

    public void testExtension() throws Throwable {
        SimpleExtensionEltDocument doc = SimpleExtensionEltDocument.Factory.newInstance();
        SimpleExtensionT elt = doc.addNewSimpleExtensionElt();

        assertTrue(!doc.validate(validateOptions));

        String[] errExpected = new String[]{"cvc-attribute"};
//        assertTrue(compareErrorCodes(errExpected));

        elt.setStringValue("1");
        assertTrue(elt.validate());
        elt.setAttribute("ATTR_VAL");
        try{
        assertTrue(doc.validate());
        }catch(Throwable t){
            showErrors();
            throw t;
        }
        assertEquals("ATTR_VAL", elt.getAttribute());
        elt.unsetAttribute();
        assertEquals(null, elt.getAttribute());
        assertTrue(!elt.isSetAttribute());

        //why does type mismatch show up as XmlValueOutOfRangeException 
        elt.setStringValue("foobar");
        assertTrue(!elt.validate(validateOptions));

         errExpected = new String[]{"cvc-attribute"};
        assertTrue(compareErrorCodes(errExpected));


    }

}
