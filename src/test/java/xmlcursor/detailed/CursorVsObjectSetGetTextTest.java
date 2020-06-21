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
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CityNameDocument.CityName;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import tools.util.JarUtil;
import xmlcursor.common.Common;

import static org.junit.Assert.*;


public class CursorVsObjectSetGetTextTest {
    @Test
    public void testSetGet() throws Exception {
        CarLocationMessageDocument clm =
                (CarLocationMessageDocument) XmlObject.Factory.parse(
                        JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM));
        assertNotNull(clm);
        XmlCursor xc = clm.newCursor();
        GeographicLocation[] aGL = new GeographicLocation[3];

        try {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                          "$this//GeographicLocation");
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
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT +
                          "$this//GeographicLocation");

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
        } finally {
            xc.dispose();
        }
    }


    class Bookmark extends XmlCursor.XmlBookmark {
        public String text;

        Bookmark(String text) {
            this.text = text;
        }
    }
}

