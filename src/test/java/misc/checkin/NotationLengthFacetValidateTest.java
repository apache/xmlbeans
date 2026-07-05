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

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotationLengthFacetValidateTest {

    // A maxLength facet is applied to a NOTATION type whose inherited enumeration
    // still admits a longer member, so the enumeration check alone does not catch
    // the length violation.
    private static final String XSD =
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' " +
        "xmlns:t='urn:t' targetNamespace='urn:t' elementFormDefault='qualified'>" +
        "  <xs:notation name='a' public='PA'/>" +
        "  <xs:notation name='bcdefgh' public='PB'/>" +
        "  <xs:simpleType name='baseN'>" +
        "    <xs:restriction base='xs:NOTATION'>" +
        "      <xs:enumeration value='t:a'/>" +
        "      <xs:enumeration value='t:bcdefgh'/>" +
        "    </xs:restriction>" +
        "  </xs:simpleType>" +
        "  <xs:element name='root'>" +
        "    <xs:simpleType>" +
        "      <xs:restriction base='t:baseN'>" +
        "        <xs:maxLength value='3'/>" +
        "      </xs:restriction>" +
        "    </xs:simpleType>" +
        "  </xs:element>" +
        "</xs:schema>";

    private static boolean validate(String notationValue) throws Exception {
        SchemaTypeLoader loader = XmlBeans.loadXsd(new XmlObject[]{SchemaDocument.Factory.parse(XSD)});
        XmlObject doc = loader.parse(
            "<t:root xmlns:t='urn:t'>" + notationValue + "</t:root>", null, null);
        List<XmlError> errors = new ArrayList<>();
        return doc.validate(new XmlOptions().setErrorListener(errors));
    }

    @Test
    void notationWithinMaxLengthValidates() throws Exception {
        // 't:a' is in the enumeration and its lexical length (3) matches maxLength
        assertTrue(validate("t:a"));
    }

    @Test
    void notationViolatingMaxLengthIsInvalid() throws Exception {
        // 't:bcdefgh' is in the inherited enumeration but its lexical length (9)
        // exceeds maxLength=3, so document validation must reject it
        assertFalse(validate("t:bcdefgh"));
    }
}
