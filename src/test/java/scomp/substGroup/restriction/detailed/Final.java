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
package scomp.substGroup.restriction.detailed;

import org.apache.xmlbeans.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scomp.common.BaseCase.createOptions;


public class Final {

    private static final String XSD =
        "    <xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
        "\n" +
        "    <xsd:element name=\"Product\" type=\"BaseProductType\" final=\"sFinalAttributeValue\"/>\n" +
        "\n" +
        "    <xsd:complexType name=\"BaseProductType\">\n" +
        "        <xsd:sequence>\n" +
        "            <xsd:element name=\"number\" type=\"xsd:integer\" />\n" +
        "            <xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\" />\n" +
        "            <xsd:element name=\"size\" type=\"xsd:integer\" minOccurs=\"0\" />\n" +
        "        </xsd:sequence>\n" +
        "    </xsd:complexType>\n" +
        "\n" +
        "    <xsd:element name=\"Shirt\" type=\"ShirtType\" substitutionGroup=\"Product\" />\n" +
        "\n" +
        "    <xsd:complexType name=\"ShirtType\">\n" +
        "        <xsd:complexContent>\n" +
        "            <xsd:extension base=\"BaseProductType\">\n" +
        "                <xsd:sequence>\n" +
        "                    <xsd:element name=\"color\" type=\"xsd:string\"/>\n" +
        "                </xsd:sequence>\n" +
        "            </xsd:extension>\n" +
        "        </xsd:complexContent>\n" +
        "    </xsd:complexType>\n" +
        "\n" +
        "        <xsd:element name=\"Hat\" type=\"HatType\" substitutionGroup=\"Product\" />\n" +
        "\n" +
        "        <xsd:complexType name=\"HatType\">\n" +
        "            <xsd:complexContent>\n" +
        "                <xsd:restriction base=\"BaseProductType\">\n" +
        "                    <xsd:sequence>\n" +
        "                        <xsd:element name=\"number\" type=\"xsd:integer\"/>\n" +
        "                    </xsd:sequence>\n" +
        "                </xsd:restriction>\n" +
        "            </xsd:complexContent>\n" +
        "        </xsd:complexType>\n" +
        "\n" +
        "    </xsd:schema>";

    /**
     * The follwing are test for the 'final' attribute used in a base in substitution groups
     * They are negative tests and test for #all, restriction, extenstion and 'extenstion restriction' values
     */
    @ParameterizedTest
    @ValueSource(strings = {"#all", "extension restriction", "restriction", "extension"})
    void parseXsdDoc(String inputXsd) throws XmlException {
        XmlObject xobj = XmlObject.Factory.parse(XSD.replace("sFinalAttributeValue",inputXsd));
        XmlObject[] compInput = new XmlObject[]{xobj};
        XmlOptions validateOptions = createOptions();

        assertThrows(XmlException.class, () ->
            XmlBeans.compileXmlBeans(null, null, compInput, null, XmlBeans.getBuiltinTypeSystem(), null, validateOptions));

        boolean errMatch = validateOptions.getErrorListener().stream().map(XmlError::getErrorCode)
            .allMatch(XmlErrorCodes.ELEM_PROPERTIES$SUBSTITUTION_FINAL::equals);
        assertTrue(errMatch);
    }
}

