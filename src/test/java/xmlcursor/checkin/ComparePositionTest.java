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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class ComparePositionTest {

    @Test
    void testComparePositionThis() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toFirstChild();
            assertEquals(0, m_xc.comparePosition(m_xc));
        }
    }

    @Test
    void testComparePositionDifferentDocs() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toFirstChild();
            xc0.toFirstChild();
            assertThrows(IllegalArgumentException.class, () -> m_xc.comparePosition(xc0));
        }
    }

    @Test
    void testComparePositionNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            m_xc.toFirstChild();
            assertThrows(IllegalArgumentException.class, () -> m_xc.comparePosition(null));
        }
    }

    @Test
    void testComparePositionRightInTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = m_xc.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.TEXT);
            xc0.toNextChar(1);
            assertEquals(-1, m_xc.comparePosition(xc0));
        }
    }

    @Test
    void testComparePositionLeftInTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = m_xc.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.TEXT);
            m_xc.toNextChar(1);
            assertEquals(1, m_xc.comparePosition(xc0));
        }
    }

    @Test
    void testComparePositionENDandENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT);
             XmlCursor xc0 = m_xc.newCursor()) {
            m_xc.toEndDoc();
            xc0.toEndDoc();
            xc0.toPrevToken();
            assertEquals(1, m_xc.comparePosition(xc0));
        }
    }
}

