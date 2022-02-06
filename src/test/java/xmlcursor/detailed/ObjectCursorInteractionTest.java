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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import test.xbean.xmlcursor.location.LocationDocument;
import test.xbean.xmlcursor.location.LocationDocument.Location;

import static org.junit.jupiter.api.Assertions.*;


public class ObjectCursorInteractionTest {
    @Test
    void testObjectNullEffectOnCursor() throws Exception {
        try (XmlCursor xc0 = getLocCursor()) {
            xc0.toFirstChild();
            assertEquals("DALLAS", xc0.getTextValue());
        }
    }

    private static XmlCursor getLocCursor() throws XmlException {
        String sXml =
            "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
            "<loc:CityName>DALLAS</loc:CityName><StateCode>TX</StateCode>" +
            "</loc:Location>";
        LocationDocument locDoc = LocationDocument.Factory.parse(sXml);
        Location loc = locDoc.getLocation();
        assertEquals("DALLAS", loc.getCityName());
        // the reference to loc is gone after the method returns
        // this test used to be a combination of setting loc = null and then running System.gc() and waiting 1 sec.
        // ... this is the equivalent
        return loc.newCursor();
    }

    @Test
    void testCursorCloseEffectOnObject() throws Exception {
        String sNamespace = "xmlns:loc=\"http://xbean.test/xmlcursor/Location\"";
        String sXml = "<loc:Location " + sNamespace + ">" +
                      "<loc:CityName>DALLAS</loc:CityName><loc:StateCode>TX</loc:StateCode></loc:Location>";
        LocationDocument locDoc = LocationDocument.Factory.parse(sXml);
        assertTrue(locDoc.validate());
        Location loc0 = locDoc.getLocation();
        Location loc1 = locDoc.getLocation();

        try (XmlCursor xc0 = loc0.newCursor();
            XmlCursor xc1 = loc1.newCursor()) {
            xc0.toFirstChild();
            xc1.toFirstChild();
            xc0.setTextValue("AUSTIN");

            assertEquals("AUSTIN", loc0.getCityName());
            loc1.setCityName("SAN ANTONIO");
            xc0.close();
            assertEquals("SAN ANTONIO", xc1.getTextValue());
            xc1.setTextValue("HOUSTON");
            xc1.close();
            assertEquals("HOUSTON", loc0.getCityName());
        }
    }

    @Test
    void testObjectRefAssignmentEffectOnCursor() throws Exception {
        final String sXml0 =
            "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
            "<loc:CityName>DALLAS</loc:CityName>" +
            "<loc:StateCode>TX</loc:StateCode>" +
            "</loc:Location>";
        final String sXml1 =
            "<loc:Location xmlns:loc=\"http://xbean.test/xmlcursor/Location\">" +
            "<loc:PostalCode>90210</loc:PostalCode>" +
            "<loc:CountryCode>US</loc:CountryCode>" +
            "</loc:Location>";
        final LocationDocument locDoc0 = LocationDocument.Factory.parse(sXml0);
        final Location loc0 = locDoc0.getLocation();

        assertEquals("DALLAS", loc0.getCityName());
        assertEquals("TX", loc0.getStateCode());
        assertNull(loc0.getPostalCode());
        assertNull(loc0.getCountryCode());

        final LocationDocument locDoc1 = (LocationDocument) XmlObject.Factory.parse(sXml1);
        final Location loc1 = locDoc1.getLocation();

        assertNull(loc1.getCityName());
        assertNull(loc1.getStateCode());
        assertEquals("90210", loc1.getPostalCode());
        assertEquals("US", loc1.getCountryCode());

        try (XmlCursor xc0 = loc0.newCursor(); XmlCursor xc1 = loc1.newCursor()) {
            assertEquals(sXml0, xc0.xmlText());
            assertEquals(sXml1, xc1.xmlText());
        }
    }
}

