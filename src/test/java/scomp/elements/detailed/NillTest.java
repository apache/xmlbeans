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

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlValueNotNillableException;
import org.junit.jupiter.api.Test;
import xbean.scomp.element.nillTest.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * this test illustrates somewhat inconsistent behavior
 * of nillable:
 */
public class NillTest {
    /**
     * Tests exceptions when setting values to
     * null for non-nillable elems
     * * CR CR192914:
     * Regardless of Schema definition,
     * setXXX(null) will clear the value of the
     * XXX attribute/element and if the container is an
     * element, will also add the "xsi:nil" attribute.
     */
    // for all nillable tests, the validation falls thro only if the ValidateOnSet option is turned on
    @Test
    void testNotNillableLocalElem() {

        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // local element, not nillable. If setXXX is set to null & validateOnSet is true, it should throw XmlValueNotNillableException
        Contact contact = Contact.Factory.newInstance(options);
        assertThrows(XmlValueNotNillableException.class, () -> contact.setFirstName(null));

        // with validate turned off, this should to thro
        Contact contactWithValidateOff = Contact.Factory.newInstance();
        contactWithValidateOff.setFirstName(null);
        assertEquals("<firstName " +
                                "xsi:nil=\"true\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", contactWithValidateOff.xmlText());
    }

    @Test
    void testNotNillableGlobalElem() {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // global element, not nillable. If setXXX is set to null & validateOnSet is true, it should throw XmlValueNotNillableException
        CityNameDocument cityName = CityNameDocument.Factory.newInstance(options);
        assertThrows(XmlValueNotNillableException.class, () -> cityName.setCityName(null));

        // with validate turned off, this should to thro
        CityNameDocument cityNameWithValidateOff = CityNameDocument.Factory.newInstance();
        cityNameWithValidateOff.setCityName(null);

        assertEquals("<nil:cityName " +
                                "xsi:nil=\"true\" " +
                                "xmlns:nil=\"http://xbean/scomp/element/NillTest\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", cityNameWithValidateOff.xmlText());
    }

    @Test
    void testNillableGlobalElement() {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // global element, nillable. If setXXX is set to null & validateOnSet is true, it should NOT throw XmlValueNotNillableException
        GlobalEltNillableDocument testElt = GlobalEltNillableDocument.Factory.newInstance(options);
        testElt.setGlobalEltNillable(null);
        assertEquals("<nil:GlobalEltNillable " +
                                "xsi:nil=\"true\" " +
                                "xmlns:nil=\"http://xbean/scomp/element/NillTest\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", testElt.xmlText());

        // without the validateOnSet - no exception in this case either
        GlobalEltNillableDocument testEltWithValidateOff = GlobalEltNillableDocument.Factory.newInstance();
        testEltWithValidateOff.setGlobalEltNillable(null);
        assertEquals("<nil:GlobalEltNillable " +
                                "xsi:nil=\"true\" " +
                                "xmlns:nil=\"http://xbean/scomp/element/NillTest\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", testElt.xmlText());
    }

    @Test
    void testNillableLocalElement() {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // global element, nillable. If setXXX is set to null & validateOnSet is true, it should NOT throw XmlValueNotNillableException
        Contact contact = Contact.Factory.newInstance(options);
        contact.setLocalNillableElem(null);
        assertEquals("<LocalNillableElem " +
                                "xsi:nil=\"true\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", contact.xmlText());

        // without the validateOnSet - no exception in this case either
        Contact contactWithValidationOff = Contact.Factory.newInstance();
        contactWithValidationOff.setLocalNillableElem(null);
        assertEquals("<LocalNillableElem " +
                                "xsi:nil=\"true\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>", contactWithValidationOff.xmlText());
    }

    @Test
    void testDefaultValElement() {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // default value element, not nillable. If setXXX is set to null & validateOnSet is true, it should
        // throw XmlValueNotNillableException and validation should fail
        GlobalEltDefaultDocument elt = GlobalEltDefaultDocument.Factory.newInstance(options);
        assertThrows(XmlValueNotNillableException.class, () -> elt.setGlobalEltDefault(null));
    }

    @Test
    void testNotNillableFixedValueElement() {
        XmlOptions options = new XmlOptions();
        options.setValidateOnSet();

        // fixed value element, not nillable. If setXXX is set to null & validateOnSet is true, it should
        // throw XmlValueNotNillableException and validation should fail
        GlobalEltFixedDocument elt = GlobalEltFixedDocument.Factory.newInstance(options);
        assertThrows(XmlValueNotNillableException.class, () -> elt.setGlobalEltFixed(null));
    }
}
