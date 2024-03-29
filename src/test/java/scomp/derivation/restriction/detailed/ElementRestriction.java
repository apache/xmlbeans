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

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.junit.jupiter.api.Test;
import xbean.scomp.derivation.elementRestriction.ElementDocument;
import xbean.scomp.derivation.elementRestriction.RestrictedEltT;

import static org.junit.jupiter.api.Assertions.*;
import static scomp.common.BaseCase.createOptions;
import static scomp.common.BaseCase.getErrorCodes;


public class ElementRestriction {
    /**
     * <xsd:complexType name="SequenceT">
     * <xsd:sequence>
     * <xsd:element name="child1" type="xsd:integer" minOccurs="1" />
     * <xsd:element name="child2" type="xsd:string" minOccurs="0"/>
     * </xsd:sequence>
     * </xsd:complexType>
     * <p/>
     * <xsd:complexType name="restrictedSequenceT">
     * <xsd:complexContent>
     * <xsd:restriction base="SequenceT">
     * <xsd:sequence>
     * <xsd:element name="child1" type="xsd:integer" minOccurs="2"/>
     * <xsd:element name="newchild" type="xsd:string"/>
     * </xsd:sequence>
     * </xsd:restriction>
     * </xsd:complexContent>
     * </xsd:complexType>
     */
    @Test
    void testRestrictedElement() throws Throwable {
        ElementDocument doc = ElementDocument.Factory.newInstance();
        RestrictedEltT elt = doc.addNewElement();
        XmlString aValue = XmlString.Factory.newInstance();
        aValue.setStringValue("foobar");
        //a can only occur 2ce
        elt.setAArray(new XmlObject[]{aValue, aValue, aValue});
        //b has to be missing or "myval"
        XmlString bValue = XmlString.Factory.newInstance();
        bValue.setStringValue("foobar");
        elt.setB(bValue);
        //c is of type xsd:token
        elt.setC("foobar:123");
        //d is an xsd:integer
        XmlDecimal dValue = XmlDecimal.Factory.newInstance();
        dValue.setBigDecimalValue(new java.math.BigDecimal("3.5"));
        elt.setD(dValue);

        XmlOptions validateOptions = createOptions();
        assertFalse(doc.validate(validateOptions));
        String[] errExpected = {
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
            XmlErrorCodes.ELEM_LOCALLY_VALID$FIXED_VALID_MIXED_CONTENT,
            XmlErrorCodes.INTEGER,
        };
        assertArrayEquals(errExpected, getErrorCodes(validateOptions));

        elt.removeA(2);
        bValue.setStringValue("myval");
        elt.setB(bValue);
        elt.setD(3);
        assertEquals("myval", ((XmlAnyTypeImpl)elt.getB()).getStringValue());
        assertTrue(doc.validate(validateOptions));
    }
}
