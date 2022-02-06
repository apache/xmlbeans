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
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlString;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.jcur;


public class MultipleCursorSetTest {
    @Test
    void testMultipleCursorSet() throws Exception {

        try (XmlCursor xc = jcur(Common.TRANXML_FILE_CLM)) {
            xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//EquipmentNumber");
            xc.toNextSelection();
            final XmlString xs = (XmlString) xc.getObject();
            assertEquals("123456", xs.getStringValue());
            assertEquals(TokenType.TEXT, xc.toNextToken());

            try (XmlCursor x0 = nextCur(xc);
                 XmlCursor x1 = nextCur(xc);
                 XmlCursor x2 = nextCur(xc);
                 XmlCursor x3 = nextCur(xc);
                 XmlCursor x4 = nextCur(xc);
                 XmlCursor x5 = nextCur(xc);
            ) {
                xc.close();

                XmlCursor[] aCursors = { x0, x1, x2, x3, x4, x5 };
                for (XmlCursor cur1 : aCursors) {
                    for (XmlCursor cur2 : aCursors) {
                        if (cur1 != cur2) {
                            assertFalse(cur1.isAtSamePositionAs(cur2));
                        }
                    }
                }
                xs.setStringValue("XYZ");
                for (XmlCursor cur1 : aCursors) {
                    for (XmlCursor cur2 : aCursors) {
                        assertTrue(cur1.isAtSamePositionAs(cur2));
                    }
                    assertThrows(IllegalStateException.class, cur1::getTextValue);
                }
                assertEquals("XYZ", xs.getStringValue());
            }
        }
    }

    private static XmlCursor nextCur(XmlCursor xc) {
        xc.toNextChar(1);
        return xc.newCursor();
    }
}

