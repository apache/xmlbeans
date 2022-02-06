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

package scomp.namespace.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import org.xmlsoap.schemas.soap.envelope.EnvelopeDocument;
import tools.xml.XmlComparator;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PreserveNamespaces {

    //tests for preserving/copying namespace declarations when doing an XmlObject.set()
    @Test
    void testDroppedXsdNSDecl() throws XmlException {
        String input =
            "<soap:Envelope \n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
            "xmlns:tns=\"http://Walkthrough/XmlWebServices/\" \n" +
            "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soap:Body>\n" +
            "    <tns:ConvertTemperature>\n" +
            "      <dFahrenheit xsi:type=\"xsd:double\">88</dFahrenheit>\n" +
            "    </tns:ConvertTemperature>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";
        // Test for XSD namespace declaration dropped
        EnvelopeDocument env1 = EnvelopeDocument.Factory.parse(input);

        EnvelopeDocument env2 = EnvelopeDocument.Factory.newInstance();
        env2.addNewEnvelope().setBody(env1.getEnvelope().getBody());

        // compare the 2 body elements using the Xml Comparator. This uses a cursor to walk thro the docs and compare elements and attributes
        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();
        assertTrue(XmlComparator.lenientlyCompareTwoXmlStrings(env1.getEnvelope().getBody().xmlText(), env2.getEnvelope().getBody().xmlText(), diag));

        // navigate to the dFahrenhiet element and check for the XSD namespace
        try (XmlCursor env2Cursor = env2.newCursor()) {
            assertTrue(env2Cursor.toFirstChild());      // <Envelope>
            assertTrue(env2Cursor.toFirstChild());      // <Body>
            assertTrue(env2Cursor.toFirstChild());      // <ConvertTemperature>
            assertTrue(env2Cursor.toFirstChild());      // <dFahrenheit>
            assertEquals(new QName("", "dFahrenheit"), env2Cursor.getName(), "Element name mismatch!");
            assertEquals("88", env2Cursor.getTextValue(), "Element val mismatch!");
            assertEquals("http://www.w3.org/2001/XMLSchema", env2Cursor.namespaceForPrefix("xsd"), "XSD Namespace has been dropped");
        }
    }

    @Test
    void testsModifiedXsdNSPrefix() throws XmlException {
        String input =
            "<soap:Envelope \n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
            "xmlns:tns=\"http://Walkthrough/XmlWebServices/\" \n" +
            "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soap:Body>\n" +
            "      <xsd:element name=\"myname\" type=\"xsd:string\"/>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";
        // XSD namespace used in QName values and elements
        EnvelopeDocument env1 = EnvelopeDocument.Factory.parse(input);

        EnvelopeDocument env2 = EnvelopeDocument.Factory.newInstance();
        env2.addNewEnvelope().setBody(env1.getEnvelope().getBody());

        // compare the 2 body elements using the Xml Comparator. This uses a cursor to walk thro the docs and compare elements and attributes
        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();
        assertTrue(XmlComparator.lenientlyCompareTwoXmlStrings(env1.getEnvelope().getBody().xmlText(), env2.getEnvelope().getBody().xmlText(), diag), "new envelope has missing XSD namespace declaration");

        // navigate to the 'element' element and check for the XSD namespace
        try (XmlCursor env2Cursor = env2.newCursor()) {
            assertTrue(env2Cursor.toFirstChild());      // <Envelope>
            assertTrue(env2Cursor.toFirstChild());      // <Body>
            assertTrue(env2Cursor.toFirstChild());      // <element>
            assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "element"), env2Cursor.getName(), "Element name mismatch!");
            assertEquals("http://www.w3.org/2001/XMLSchema", env2Cursor.namespaceForPrefix("xsd"), "XSD Namespace has been dropped");
        }
    }

    @Test
    void testsFaultCodeNSUpdate() throws XmlException {
        String input =
            "<soap:Envelope \n" +
            "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soap:Body>\n" +
            "      <soap:Fault>\n" +
            "           <faultcode>soap:Server</faultcode>\n" +
            "           <faultstring>my error message</faultstring>\n" +
            "      </soap:Fault>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";
        EnvelopeDocument env1 = EnvelopeDocument.Factory.parse(input);

        // Test for NS of the faultcode element
        EnvelopeDocument env2 = EnvelopeDocument.Factory.newInstance();
        env2.addNewEnvelope().setBody(env1.getEnvelope().getBody());

        // compare the 2 body elements using the Xml Comparator. This uses a cursor to walk thro the docs and compare elements and attributes
        tools.xml.XmlComparator.Diagnostic diag = new tools.xml.XmlComparator.Diagnostic();
        assertTrue(XmlComparator.lenientlyCompareTwoXmlStrings(env1.getEnvelope().getBody().xmlText(), env2.getEnvelope().getBody().xmlText(), diag), "new envelope has missing XSD namespace declaration");

        // navigate to the soap element and check for the 'soap' namespace
        try (XmlCursor env2Cursor = env2.newCursor()) {
            assertTrue(env2Cursor.toFirstChild());      // <Envelope>
            assertTrue(env2Cursor.toFirstChild());      // <Body>
            assertTrue(env2Cursor.toFirstChild());      // <Fault>
            assertTrue(env2Cursor.toFirstChild());      // <faultcode>
            assertEquals(env2Cursor.getName(), new QName("", "faultcode"), "Element name mismatch!");
            assertEquals("http://schemas.xmlsoap.org/soap/envelope/", env2Cursor.namespaceForPrefix("soap"), "soap Namespace has been dropped");
        }
    }

}
