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
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CodeList309;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import org.tranxml.tranXML.version40.LocationIdentifierDocument.LocationIdentifier;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static xmlcursor.common.BasicCursorTestCase.jobj;


public class MultipleCopyTest {
    @Test
    void testMultipleCopy() throws Exception {
        CarLocationMessageDocument clm = (CarLocationMessageDocument) jobj(Common.TRANXML_FILE_CLM);
        assertNotNull(clm);

        try (XmlCursor xc = clm.newCursor()) {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");
            xc.toNextSelection();
            GeographicLocation gl = (GeographicLocation) xc.getObject();

            try (XmlCursor x0 = nextSel(xc); XmlCursor x1 = nextSel(xc); XmlCursor x2 = nextSel(xc)) {

                LocationIdentifier li = gl.addNewLocationIdentifier();
                li.setQualifier(CodeList309.FR);

                gl.setLocationIdentifier(li);
                assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());

                gl.setCountrySubdivisionCode("xyz");

                for (XmlCursor cur : new XmlCursor[]{x1, x2}) {
                    cur.removeXml();
                    x0.copyXml(cur);
                    // must move to PrevElement to get to the START of the copied section.
                    cur.toPrevSibling();

                    gl = (GeographicLocation) cur.getObject();

                    assertEquals("DALLAS", gl.getCityName().getStringValue());
                    assertEquals("TX", gl.getStateOrProvinceCode());
                    assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());
                    assertEquals("xyz", gl.getCountrySubdivisionCode());
                }
            }
        }
    }

    private static XmlCursor nextSel(XmlCursor xc) {
        try {
            return xc.newCursor();
        } finally {
            xc.toNextSelection();
        }
    }
}

