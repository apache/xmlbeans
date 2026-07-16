/*   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringLengthFacetCodePointTest {

    // U+1D54F MATHEMATICAL DOUBLE-STRUCK CAPITAL X: one character, encoded as a
    // surrogate pair so String.length() is 2 but codePointCount is 1
    private static final String SUPPLEMENTARY = "𝕏";

    private static boolean validates(String base, String facet, String value) throws Exception {
        String xsd =
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:t='urn:t' " +
            "targetNamespace='urn:t' elementFormDefault='qualified'>" +
            "  <xs:element name='root'>" +
            "    <xs:simpleType><xs:restriction base='" + base + "'>" + facet +
            "</xs:restriction></xs:simpleType>" +
            "  </xs:element>" +
            "</xs:schema>";
        SchemaTypeLoader loader = XmlBeans.loadXsd(new XmlObject[]{SchemaDocument.Factory.parse(xsd)});
        XmlObject doc = loader.parse("<t:root xmlns:t='urn:t'>" + value + "</t:root>", null, null);
        return doc.validate();
    }

    @Test
    void stringLengthFacetsCountCharactersNotCodeUnits() throws Exception {
        // one supplementary character is one xsd character, so it meets length/maxLength of 1
        assertTrue(validates("xs:string", "<xs:length value='1'/>", SUPPLEMENTARY));
        assertTrue(validates("xs:string", "<xs:maxLength value='1'/>", SUPPLEMENTARY));
        // it is one character, so it does not meet a length of 2
        assertFalse(validates("xs:string", "<xs:length value='2'/>", SUPPLEMENTARY));
    }

    @Test
    void anyUriLengthFacetsCountCharactersNotCodeUnits() throws Exception {
        assertTrue(validates("xs:anyURI", "<xs:length value='1'/>", SUPPLEMENTARY));
        assertTrue(validates("xs:anyURI", "<xs:maxLength value='1'/>", SUPPLEMENTARY));
    }
}
