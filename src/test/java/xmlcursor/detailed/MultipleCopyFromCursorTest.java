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
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.CodeList309;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import org.tranxml.tranXML.version40.LocationIdentifierDocument.LocationIdentifier;
import xmlcursor.common.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jobj;


public class MultipleCopyFromCursorTest {

    @Test
    void testMultipleCopy() throws Exception {
        CarLocationMessageDocument clm = (CarLocationMessageDocument)jobj(Common.TRANXML_FILE_CLM);
        assertNotNull(clm);
        try (XmlCursor xc = clm.newCursor()) {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");
            xc.toNextSelection();

            try (XmlCursor x0 = nextSel(xc); XmlCursor x1 = nextSel(xc); XmlCursor x2 = nextSel(xc)) {
                Stream.of(x0, x1, x2).forEach(XmlCursor::toNextSelection);
                xc.toStartDoc();
                xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");
                assertTrue(xc.getSelectionCount() > 0);
                assertTrue(xc.toNextSelection());
                x0.toLastChild();
                assertEquals("TX", x0.getTextValue());

                x0.toNextToken();
                x0.toNextToken();
                x0.toNextToken();
                x0.toNextToken();
                assertEquals(TokenType.END, x0.currentTokenType());

                x0.beginElement("LocationIdentifier", "http://www.tranxml.org/TranXML/Version4.0");
                x0.insertAttributeWithValue("Qualifier", "FR");
                x0.toEndToken();
                x0.toNextToken();//move past the end token
                x0.insertElementWithText("CountrySubdivisionCode", "http://www.tranxml.org/TranXML/Version4.0", "xyz");
                x0.toCursor(xc);
                GeographicLocation gl = (GeographicLocation) x0.getObject();
                XmlOptions validateOptions = new XmlOptions();
                List<XmlError> errors = new ArrayList<>();
                validateOptions.setErrorListener(errors);
                assertTrue(gl.validate(validateOptions));

                assertEquals("DALLAS", gl.getCityName().getStringValue());
                assertEquals("TX", gl.getStateOrProvinceCode());
                LocationIdentifier li = gl.getLocationIdentifier();
                assertNotNull(li, "Cursor0: LocationIdentifier unexpectedly null");
                assertEquals(CodeList309.FR, gl.getLocationIdentifier().getQualifier());
                assertEquals("xyz", gl.getCountrySubdivisionCode());


                for (XmlCursor cur : new XmlCursor[]{x1, x2}) {
                    cur.removeXml();
                    x0.copyXml(cur);
                    // must move to PrevElement to get to the START of the copied section.
                    cur.toPrevSibling();

                    gl = (GeographicLocation) cur.getObject();

                    assertEquals("DALLAS", gl.getCityName().getStringValue());
                    assertEquals("TX", gl.getStateOrProvinceCode());
                    li = gl.getLocationIdentifier();
                    assertNotNull(li, "Cursor: LocationIdentifier unexpectedly null");
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


