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

package xmlobject.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openuri.sgs.ADocument;
import org.openuri.sgs.BDocument;
import org.openuri.sgs.CDocument;
import org.openuri.sgs.RootDocument;

import static org.junit.jupiter.api.Assertions.*;

public class SubstGroupTests {
    @Test
    void test1() throws Exception {
        String xml1 = "<root xmlns='http://openuri.org/sgs'>" +
                      "<A>\ta\ta\t</A>" +
                      "<B>\tb\tb\t</B>" +
                      "<C>\tc\tc\t</C>" +
                      "</root>";


        RootDocument doc1 = RootDocument.Factory.parse(xml1);
        RootDocument.Root root = doc1.getRoot();
        assertTrue(doc1.validate());

        XmlString a = root.xgetAArray(0);
        assertEquals(a.schemaType(), XmlString.type);
        assertEquals("\ta\ta\t", a.getStringValue());

        XmlString b = root.xgetAArray(1);
        assertEquals(b.schemaType(), XmlNormalizedString.type);
        assertEquals(" b b ", b.getStringValue());

        XmlString c = root.xgetAArray(2);
        assertEquals(c.schemaType(), XmlToken.type);
        assertEquals("c c", c.getStringValue());

        root.insertA(2, "d d");
        assertEquals("d d", root.getAArray(2));
        assertEquals(4, root.sizeOfAArray());
        root.removeA(2);

        root.removeA(1);
        assertEquals("c c", root.getAArray(1));
        assertEquals(2, root.sizeOfAArray());

        root.addA("f f");
        assertEquals(3, root.sizeOfAArray());
        assertEquals("f f", root.getAArray(2));

        // Test array setters

        // test m < n case
        String[] smaller = new String[]{"x", "y"};
        root.setAArray(smaller);
        assertEquals(2, root.sizeOfAArray());
        assertEquals("y", root.getAArray(1));

        // test m > n case
        String[] larger = new String[]{"p", "q", "r", "s"};
        root.setAArray(larger);
        assertEquals(4, root.sizeOfAArray());
        assertEquals("r", root.getAArray(2));
    }

    @Test
    void test2() throws Exception {
        String xml1 = "<A xmlns='http://openuri.org/sgs'>\ta\ta\t</A>";
        String xml2 = "<B xmlns='http://openuri.org/sgs'>\tb\tb\t</B>";
        String xml3 = "<C xmlns='http://openuri.org/sgs'>\tc\tc\t</C>";

        ADocument d1 = ADocument.Factory.parse(xml1);
        XmlString a = d1.xgetA();
        assertEquals(a.schemaType(), XmlString.type);
        assertEquals("\ta\ta\t", a.getStringValue());

        ADocument d2 = ADocument.Factory.parse(xml2);
        XmlString b = d2.xgetA();
        assertEquals(d2.schemaType(), BDocument.type);
        assertEquals(b.schemaType(), XmlNormalizedString.type);
        assertEquals(" b b ", b.getStringValue());

        ADocument d3 = ADocument.Factory.parse(xml3);
        XmlString c = d3.xgetA();
        assertEquals(d3.schemaType(), CDocument.type);
        assertEquals(c.schemaType(), XmlToken.type);
        assertEquals("c c", c.getStringValue());
    }

    private static final String[] invalidSchemas = {
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string'/> " +
        "  <xsd:element name='B' type='xsd:int' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:complexType name='foo'> " +
        "    <xsd:sequence> " +
        "      <xsd:element name='bar' substitutionGroup='A'/>" +
        "    </xsd:sequence> " +
        "  </xsd:complexType>" +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string' final='#all'/> " +
        "  <xsd:element name='B' type='xsd:string' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string' final='restriction'/> " +
        "  <xsd:element name='B' type='xsd:token' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string' substitutionGroup='B'/> " +
        "  <xsd:element name='B' type='xsd:string' substitutionGroup='C'/> " +
        "  <xsd:element name='C' type='xsd:string' substitutionGroup='D'/> " +
        "  <xsd:element name='D' type='xsd:string' substitutionGroup='E'/> " +
        "  <xsd:element name='E' type='xsd:string' substitutionGroup='A'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:token' substitutionGroup='B'/> " +
        "</xsd:schema>",
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
        "  <xsd:element name='A' type='xsd:string'/> " +
        "  <xsd:element name='B' type='xsd:string' substitutionGroup='A'/> " +
        "  <xsd:element name='Complex'> " +
        "    <xsd:complexType> " +
        "      <xsd:choice> " +
        "        <xsd:element ref='A'/>" +
        "        <xsd:element ref='B'/>" +
        "      </xsd:choice> " +
        "    </xsd:complexType> " +
        "  </xsd:element> " +
        "</xsd:schema>",
    };

    public static final String[] validSchemas = {
        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
        "  <xsd:complexType name='base'>" +
        "    <xsd:all>" +
        "      <xsd:element ref='head'/>" +
        "    </xsd:all>" +
        "  </xsd:complexType>" +
        "  <xsd:complexType name='restr'>" +
        "    <xsd:complexContent>" +
        "       <xsd:restriction base='base'>" +
        "         <xsd:all>" +
        "           <xsd:element ref='tail'/>" +
        "         </xsd:all>" +
        "       </xsd:restriction>" +
        "    </xsd:complexContent>" +
        "  </xsd:complexType>" +
        "  <xsd:element name='head' type='xsd:string'/>" +
        "  <xsd:element name='tail' substitutionGroup='head'/>" +
        "</xsd:schema>",
    };

    @Test
    void test3() throws Exception {
        final SchemaDocument[] invaldXSD = new SchemaDocument[invalidSchemas.length];

        // Parse the invalid schema files
        for (int i = 0; i < invalidSchemas.length; i++) {
            invaldXSD[i] = SchemaDocument.Factory.parse(invalidSchemas[i]);
        }

        // Now compile the invalid schemas, test that they fail
        for (SchemaDocument schemaDocument : invaldXSD) {
            assertThrows(XmlException.class, () -> XmlBeans.loadXsd(schemaDocument));
        }

        // Parse the valid schema files
        SchemaDocument[] validXSD = new SchemaDocument[validSchemas.length];
        for (int i = 0; i < validSchemas.length; i++) {
            validXSD[i] = SchemaDocument.Factory.parse(validSchemas[i]);
        }

        // Now compile the valid schemas, test that they succeed
        for (SchemaDocument schema : validXSD) {
            Assertions.assertDoesNotThrow(() -> XmlBeans.loadXsd(schema));
        }
    }

    private static final String[] invalidDocs = {
        "<abstractTest xmlns='http://openuri.org/sgs'>" +
        "    <abstract>content</abstract> " +
        "</abstractTest> ",
    };

    private static final String[] validDocs = {
        "<abstractTest xmlns='http://openuri.org/sgs'>" +
        "    <concrete>content</concrete> " +
        "</abstractTest> ",
    };

    @Test
    void test4() throws Exception {

        for (String invalidDoc : invalidDocs) {
            XmlObject xo = XmlObject.Factory.parse(invalidDoc);
            assertFalse(xo.validate(), "Doc was valid. Should be invalid: " + invalidDoc);
        }

        for (String validDoc : validDocs) {
            XmlObject xo = XmlObject.Factory.parse(validDoc);
            assertTrue(xo.validate(), "Doc was invalid. Should be valid: " + validDoc);
        }
    }

}
