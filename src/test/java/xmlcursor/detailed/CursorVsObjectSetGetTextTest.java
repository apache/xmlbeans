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


package  xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CityNameDocument.CityName;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;


public class CursorVsObjectSetGetTextTest {
    @Test
    void testSetGet() throws Exception {
        CarLocationMessageDocument clm = (CarLocationMessageDocument) jobj(Common.TRANXML_FILE_CLM);
        assertNotNull(clm);
        GeographicLocation[] aGL = new GeographicLocation[3];

        try (XmlCursor xc = clm.newCursor()) {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");
            xc.toNextSelection();
            for (int i = 0; i < 3; i++) {
                aGL[i] = (GeographicLocation) xc.getObject();
                assertEquals("DALLAS", aGL[i].getCityName().getStringValue());
                xc.toNextSelection();

                CityName cname = aGL[i].getCityName();
                cname.setStringValue("SEATTLE");
                aGL[i].setCityName(cname);
            }
            xc.toStartDoc();
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");

            xc.toNextSelection();

            for (int i = 0; i < 3; i++) {
                assertTrue(xc.toFirstChild());
                assertEquals("SEATTLE", xc.getTextValue());
                xc.setTextValue("PORTLAND");
                xc.toNextSelection();
            }

            for (int i = 0; i < 3; i++) {
                assertEquals("PORTLAND", aGL[i].getCityName().getStringValue());
            }
        }
    }
}

