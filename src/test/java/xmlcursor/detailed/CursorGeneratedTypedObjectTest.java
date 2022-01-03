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

package xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CodeList309;
import org.tranxml.tranXML.version40.GeographicLocationDocument;
import org.tranxml.tranXML.version40.LocationIdentifierDocument;
import person.Person;
import person.PersonDocument;
import test.xbean.xmlcursor.location.LocationDocument;
import tools.util.JarUtil;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;


public class CursorGeneratedTypedObjectTest {
    @Test
    public void testGetObjectValidateLocation() throws Exception {
        String sNamespace = "xmlns:loc=\"http://xbean.test/xmlcursor/Location\"";
        String sXml =
            "<loc:Location " + sNamespace + ">" +
            "<loc:CityName>DALLAS</loc:CityName>" +
            "<loc:StateCode>TX</loc:StateCode>" +
            "</loc:Location>";
        LocationDocument locDoc = LocationDocument.Factory.parse(sXml);
        try (XmlCursor xc = locDoc.newCursor()) {
            xc.toFirstChild();
            LocationDocument.Location loc = (LocationDocument.Location) xc.getObject();
            assertTrue(loc.validate());

            try (XmlCursor xc0 = xc.newCursor()) {
                xc0.toEndDoc();
                xc0.toPrevToken();
                //  xc0.insertElementWithText("SubdivisionCode", "xyz");
                xc0.insertElementWithText(
                    new QName("http://xbean.test/xmlcursor/Location", "SubdivisionCode", "loc"),
                    "xyz");
                xc0.toCursor(xc);


                String sExpectedXML =
                    "<loc:Location " + sNamespace + ">" +
                    "<loc:CityName>DALLAS</loc:CityName>" +
                    "<loc:StateCode>TX</loc:StateCode>" +
                    "<loc:SubdivisionCode>xyz</loc:SubdivisionCode>" +
                    "</loc:Location>";

                String sOExpectedXML =
                    "<xml-fragment " + sNamespace + ">" +
                    "<loc:CityName>DALLAS</loc:CityName>" +
                    "<loc:StateCode>TX</loc:StateCode>" +
                    "<loc:SubdivisionCode>xyz</loc:SubdivisionCode>" +
                    "</xml-fragment>";
                XmlOptions map = new XmlOptions();
                //map.put(XmlOptions.SAVE_PRETTY_PRINT, "");
                //map.put(XmlOptions.SAVE_PRETTY_PRINT_INDENT, new Integer(-1));

                assertEquals(sExpectedXML, xc0.xmlText(map));
                loc = (LocationDocument.Location) xc0.getObject();
                assertEquals(sOExpectedXML, loc.xmlText());
                assertTrue(loc.validate());
                assertEquals("DALLAS", loc.getCityName());
                assertEquals("TX", loc.getStateCode());
                assertEquals("xyz", loc.getSubdivisionCode());
            }
        }
    }

    @Test
    public void testGetObjectGL() throws Exception {
        String sNamespace = "xmlns=\"http://www.tranxml.org/TranXML/Version4.0\" xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\"";
        CarLocationMessageDocument clm = CarLocationMessageDocument.Factory.parse(
            JarUtil.getResourceFromJar(
                Common.TRANXML_FILE_CLM));
        try (XmlCursor xc = clm.newCursor()) {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                          "$this//GeographicLocation");
            xc.toNextSelection();

            GeographicLocationDocument.GeographicLocation gl0 = (GeographicLocationDocument.GeographicLocation) xc.getObject();
            assertTrue(gl0.validate());

            try (XmlCursor xc0 = xc.newCursor()) {
                xc0.toLastChild();
                assertEquals("TX", xc0.getTextValue());
                xc0.toNextToken();
                xc0.toNextToken();
                xc0.toNextToken();
                xc0.toNextToken();
                assertEquals(TokenType.END, xc0.currentTokenType());

                xc0.beginElement("LocationIdentifier",
                    "http://www.tranxml.org/TranXML/Version4.0");
                xc0.insertAttributeWithValue("Qualifier", "FR");
                xc0.toEndToken();
                xc0.toNextToken();//move past the end token
                xc0.insertElementWithText("CountrySubdivisionCode",
                    "http://www.tranxml.org/TranXML/Version4.0", "xyz");
                xc0.toCursor(xc);

                String sExpectedXML =
                    "<GeographicLocation " + sNamespace + ">\n" +
                    "\t\t\t<CityName>DALLAS</CityName>\n" +
                    "\t\t\t<StateOrProvinceCode>TX</StateOrProvinceCode>\n" +
                    "\t\t<LocationIdentifier Qualifier=\"FR\"/><CountrySubdivisionCode>xyz</CountrySubdivisionCode>" +
                    "</GeographicLocation>";

                XmlOptions map = new XmlOptions();
                //  map.put(XmlOptions.SAVE_PRETTY_PRINT, "");
                //  map.put(XmlOptions.SAVE_PRETTY_PRINT_INDENT, new Integer(-1));
                assertEquals(sExpectedXML, xc0.xmlText());

                String sOExpectedXML =
                    "<xml-fragment xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\">\n" +
                    "\t\t\t<ver:CityName xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">" +
                    "DALLAS</ver:CityName>\n" +
                    "\t\t\t<ver:StateOrProvinceCode xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">" +
                    "TX</ver:StateOrProvinceCode>\n" +
                    "\t\t<ver:LocationIdentifier Qualifier=\"FR\" " +
                    "xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\"/>" +
                    "<ver:CountrySubdivisionCode xmlns:ver=\"http://www.tranxml.org/TranXML/Version4.0\">xyz" +
                    "</ver:CountrySubdivisionCode></xml-fragment>";

                GeographicLocationDocument.GeographicLocation gl = (GeographicLocationDocument.GeographicLocation) xc0.getObject();
                assertEquals(sOExpectedXML, gl.xmlText(map));
                assertTrue(gl.validate());


                assertEquals("DALLAS", gl.getCityName().getStringValue());
                assertEquals("TX", gl.getStateOrProvinceCode());
                LocationIdentifierDocument.LocationIdentifier li = gl.getLocationIdentifier();
                assertNotNull("LocationIdentifier unexpectedly null", li);
                assertEquals(CodeList309.FR,
                    gl.getLocationIdentifier().getQualifier());
                assertEquals("xyz", gl.getCountrySubdivisionCode());
            }
        }
    }


    @Test
    public void testGetObjectPerson() throws Exception {
        String sFF = "<First>Fred</First><Last>Flintstone</Last>";
        String sXml = "<Person xmlns=\"person\"><Name>" + sFF +
                      "</Name></Person>";
        try (XmlCursor xc = XmlObject.Factory.parse(sXml).newCursor()) {
            PersonDocument pdoc = (PersonDocument) xc.getObject();

            xc.toFirstChild();

            try (XmlCursor xcPlaceHolder = xc.newCursor()) {
                Person p = (Person) xc.getObject();
                assertTrue(p.validate());
                // move to </Person>
                xc.toEndToken();

                xc.insertElement("Sibling", "person");
                xc.toPrevToken();
                xc.insertElement("Name", "person");
                xc.toPrevToken();
                xc.insertElementWithText("First", "person", "Barney");
                xc.insertElementWithText("Last", "person", "Rubble");

                p = (Person) xcPlaceHolder.getObject();
                assertTrue(p.validate());

                assertEquals("Fred", p.getName().getFirst());
                assertEquals("Flintstone", p.getName().getLast());
                Person[] ap = p.getSiblingArray();
                assertEquals(1, ap.length);
                assertEquals("Barney", ap[0].getName().getFirst());
                assertEquals("Rubble", ap[0].getName().getLast());
            }
        }
    }
}
