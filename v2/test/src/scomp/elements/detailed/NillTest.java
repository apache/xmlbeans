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

import scomp.common.BaseCase;
import xbean.scomp.element.nillTest.Contact;
import xbean.scomp.element.nillTest.AddressInfo;
import xbean.scomp.element.nillTest.CityNameDocument;
import xbean.scomp.element.nillTest.GlobalEltNotNillableDocument;
import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;

/**
 * @owner: ykadiysk
 * Date: Jul 28, 2004
 * Time: 9:08:41 AM
 */
public class NillTest extends BaseCase {
    /**
     * Tests exceptions when setting values to null for non-nillable elems
     */
    public void testNillable() {
        Contact contact = Contact.Factory.newInstance();

// if the first name is null, xmlbeans doest not thrown any exceptions...
        contact.setFirstName(null);

        GlobalEltNotNillableDocument testElt = GlobalEltNotNillableDocument
                .Factory.newInstance();
        testElt.setGlobalEltNotNillable(null);

        CityNameDocument doc = CityNameDocument.Factory.newInstance();
        try {
            doc.setCityName(null);
            fail("Expected XmlValueNotNillableException");
        }
        catch (XmlValueNotNillableException e) {
        }
        try {
            doc.setCityName(null);
            testElt.setGlobalEltNotNillable(null);
            fail("Expected XmlValueNotNillableException");
        }
        catch (XmlValueNotNillableException e) {
        }

        AddressInfo address = AddressInfo.Factory.newInstance();
        //this can be null
        address.setGlobalEltNotNillable(null);
        // ...whereas this optional field cannot be null
        address.setCityName(null);
    }
}
