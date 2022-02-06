/*
 *   Copyright 2004 The Apache Software Foundation
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

package misc.detailed;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharEncodingTest {

    // Piccolo fails when trying to parse I18N chars in some QNames
    // String 2 fails with piccolo and hence with xbeans
    @Test
    void testCharEncodingI18N() throws ParserConfigurationException, IOException, SAXException {
        String I18N_test_string1 = "<i18n xmlns:\u00c1\u00c1\u00c1=\"\u00c1\u00c1\u00c1\" type=\"\u00c1\u00c1\u00c1:t\"/>";
        String I18N_test_string2 = "<i18n xmlns:\u30af\u30af\u30af=\"\u30af\u30af\u30af\" type=\"\u30af\u30af\u30af:t\"/>";

        parseXmlWithSAXAPI(I18N_test_string1,
            "Xerces",
            "org.apache.xerces.parsers.SAXParser",
            "org.apache.xerces.jaxp.SAXParserFactoryImpl");

        parseXmlWithSAXAPI(I18N_test_string2,
            "Xerces",
            "org.apache.xerces.parsers.SAXParser",
            "org.apache.xerces.jaxp.SAXParserFactoryImpl");

    }

    // Piccolo has an issue with handling external identifiers when the value is PUBLIC
    // refer : http://cafeconleche.org/SAXTest/results/com.bluecast.xml.Piccolo/xmltest/valid/not-sa/009.xml.html
    // results for the SAX conformance suite. This has been fixed in newer versions of Piccolo
    @Test
    void testExternalPublicIdentifier() throws XmlException, ParserConfigurationException, IOException, SAXException {
        // repro using piccolo and other parsers via JAXP API
        String netPubEntity =
            "<!DOCTYPE doc PUBLIC \"whatever\" \"http://www.w3.org/2001/XMLSchema.dtd\" [\n" +
            "<!ATTLIST doc a2 CDATA \"v2\">\n" +
            "]>\n" +
            "<doc></doc>\n" +
            "";

        parseXmlWithSAXAPI(netPubEntity,
            "Xerces",
            "org.apache.xerces.parsers.SAXParser",
            "org.apache.xerces.jaxp.SAXParserFactoryImpl");

        // parse same string using scomp
        XmlOptions options = new XmlOptions();
        List<XmlError> errors = new ArrayList<>();
        options.setErrorListener(errors);
        XmlObject.Factory.parse(netPubEntity, options);

        assertTrue(errors.isEmpty(), "Errors when parsing external PUBLIC identifier");
    }


    // for reference  - the values for System Properties to switch between different parser implementaion for JAXP
    // ----------------------------------------------------------------------------------------------------------
    // System Property                               Parser                        Value
    // ----------------------------------------------------------------------------------------------------------
    // javax.xml.parsers.DocumentBuilderFactory     Xerces              org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
    //
    // org.xml.sax.driver                           Xerces              org.apache.xerces.parsers.SAXParser
    //
    // javax.xml.parsers.SAXParserFactory           Xerces              org.apache.xerces.jaxp.SAXParserFactoryImpl
    // ----------------------------------------------------------------------------------------------------------

    // This method parsers the input xml string using the SAX API with the parser specified using the
    // "javax.xml.parsers.SAXParserFactory" and "org.xml.sax.driver" system properties
    public void parseXmlWithSAXAPI(String xmlInput, String parserName, String saxdriverprop, String saxparserfactoryprop)
        throws ParserConfigurationException, SAXException, IOException {
        // Set the system props to pick the appropriate parser implementation
        System.setProperty("org.xml.sax.driver", saxdriverprop);
        System.setProperty("javax.xml.parsers.SAXParserFactory", saxparserfactoryprop);

        SAXParserFactory saxparserfactory = SAXParserFactory.newInstance();
        saxparserfactory.setNamespaceAware(false);

        XMLReader xmlreader = saxparserfactory.newSAXParser().getXMLReader();
        xmlreader.parse(new InputSource(new StringReader(xmlInput)));
    }

}

