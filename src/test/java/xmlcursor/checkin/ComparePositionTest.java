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

import static org.junit.Assert.assertEquals;


public class ComparePositionTest extends BasicCursorTestCase {
    @Test
    public void testComparePositionThis() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toFirstChild();
        assertEquals(0, m_xc.comparePosition(m_xc));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComparePositionDifferentDocs() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        m_xc.toFirstChild();
        xc0.toFirstChild();
        try {
            m_xc.comparePosition(xc0);
        } finally {
            xc0.dispose();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComparePositionNull() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        m_xc.toFirstChild();
        m_xc.comparePosition(null);
    }

    @Test
    public void testComparePositionRightInTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        try {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.TEXT);
            xc0.toNextChar(1);
            assertEquals(-1, m_xc.comparePosition(xc0));
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testComparePositionLeftInTEXT() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        try {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.TEXT);
            m_xc.toNextChar(1);
            assertEquals(1, m_xc.comparePosition(xc0));
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testComparePositionENDandENDDOC() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        try {
            m_xc.toEndDoc();
            xc0.toEndDoc();
            xc0.toPrevToken();
            assertEquals(1, m_xc.comparePosition(xc0));
        } finally {
            xc0.dispose();
        }
    }
}

