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
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.*;


public class ToCursorTest extends BasicCursorTestCase {
    @Test
    public void testToCursorMoves() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        xc0.toEndDoc();
        try {
            assertTrue(m_xc.toCursor(xc0));
            assertTrue(xc0.isAtSamePositionAs(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    /**
     * FIXED: toCursor(null) does not return a boolean but throws an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToCursorNull() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        m_xc.toFirstChild();
        m_xc.toCursor(null);
    }

    @Test
    public void testToCursorDifferentDocs() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        String s = m_xc.xmlText();
        toNextTokenOfType(xc0, TokenType.TEXT);
        try {
            assertFalse(m_xc.toCursor(xc0));
            assertEquals(s, m_xc.xmlText());
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testToCursorThis() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        m_xc.toFirstChild();
        String s = m_xc.xmlText();
        assertTrue(m_xc.toCursor(m_xc));
        assertEquals(s, m_xc.xmlText());
    }

}

