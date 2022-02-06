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


package xmlcursor.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class ToCursorTest {
    @Test
    void testToCursorMoves() throws Exception {

        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = m_xc.newCursor()) {
            xc0.toEndDoc();

            assertTrue(m_xc.toCursor(xc0));
            assertTrue(xc0.isAtSamePositionAs(m_xc));
        }
    }

    /**
     * FIXED: toCursor(null) does not return a boolean but throws an exception.
     */
    @Test
    void testToCursorNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toFirstChild();
            assertThrows(IllegalArgumentException.class, () -> m_xc.toCursor(null));
        }
    }

    @Test
    void testToCursorDifferentDocs() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            String s = m_xc.xmlText();
            toNextTokenOfType(xc0, TokenType.TEXT);

            assertFalse(m_xc.toCursor(xc0));
            assertEquals(s, m_xc.xmlText());
        }
    }

    @Test
    void testToCursorThis() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toFirstChild();
            String s = m_xc.xmlText();
            assertTrue(m_xc.toCursor(m_xc));
            assertEquals(s, m_xc.xmlText());
        }
    }

}

