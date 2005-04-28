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

import misc.common.ParsersBase;

public class CharEncodingTest extends ParsersBase {

    // Piccolo fails when trying to parse I18N chars in some QNames
    // String 2 fails with piccolo and hence with xbeans
    public void testCharEncodingI18N() {
        String I18N_test_string1 = "<i18n xmlns:\u00c1\u00c1\u00c1=\"\u00c1\u00c1\u00c1\" type=\"\u00c1\u00c1\u00c1:t\"/>";
        String I18N_test_string2 = "<i18n xmlns:\u30af\u30af\u30af=\"\u30af\u30af\u30af\" type=\"\u30af\u30af\u30af:t\"/>";

        // Test all 3 parsers with string 1, this is a valid string and should pass
        parseXmlWithSAXAPI(I18N_test_string1,
                "Crimson",
                "org.apache.crimson.parser.XmlReaderImpl",
                "org.apache.crimson.jaxp.SAXParserFactoryImpl");

        parseXmlWithSAXAPI(I18N_test_string1,
                "Xerces",
                "org.apache.xerces.parsers.SAXParser",
                "org.apache.xerces.jaxp.SAXParserFactoryImpl");

        parseXmlWithSAXAPI(I18N_test_string1,
                "Piccolo",
                "org.apache.xmlbeans.impl.piccolo.xml.Piccolo",
                "org.apache.xmlbeans.impl.piccolo.xml.JAXPSAXParserFactory");

        // Now test all 3 parsers with string 2, this is a valid string and should pass but piccolo fails
        parseXmlWithSAXAPI(I18N_test_string2,
                "Crimson",
                "org.apache.crimson.parser.XmlReaderImpl",
                "org.apache.crimson.jaxp.SAXParserFactoryImpl");

        parseXmlWithSAXAPI(I18N_test_string2,
                "Xerces",
                "org.apache.xerces.parsers.SAXParser",
                "org.apache.xerces.jaxp.SAXParserFactoryImpl");

        parseXmlWithSAXAPI(I18N_test_string2,
                "Piccolo",
                "org.apache.xmlbeans.impl.piccolo.xml.Piccolo",
                "org.apache.xmlbeans.impl.piccolo.xml.JAXPSAXParserFactory");

    }
}

