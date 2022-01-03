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
import static org.junit.Assert.assertFalse;


public class MoveCharsTest extends BasicCursorTestCase {
    @Test
    public void testMoveCharsOverlap() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try (XmlCursor xc1 = m_xo.newCursor()) {
            toNextTokenOfType(xc1, TokenType.TEXT);
            xc1.toNextChar(2);
            assertEquals("234", xc1.getChars());
            assertEquals(3, m_xc.moveChars(3, xc1));
            assertEquals("34", m_xc.getChars());
            assertEquals("34", xc1.getChars());
        }
    }

    @Test
    public void testMoveCharsNoOverlap() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try (XmlCursor xc1 = m_xo.newCursor();
            XmlCursor xc2 = m_xo.newCursor()) {
            xc1.toCursor(m_xc);
            xc2.toCursor(m_xc);
            xc1.toNextChar(3);
            xc2.toNextChar(4);

            assertEquals("34", xc1.getChars());
            assertEquals("4", xc2.getChars());
            assertEquals(2, m_xc.moveChars(2, xc1));
            assertEquals("20134", m_xc.getChars());
            assertEquals("34", xc1.getChars());
            assertEquals("4", xc2.getChars());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveCharsToNull() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.moveChars(4, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testMoveCharsSibling() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        try (XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
            XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(4, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            System.out.println("we are here");
            assertEquals(TokenType.END, xc1.currentTokenType());

            xc1.getTextValue();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testMoveCharsNegative() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        try (XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
            XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(-1, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            assertEquals(TokenType.END, xc1.currentTokenType());
            xc1.getTextValue();
        }
    }

    @Test
    public void testMoveCharsZero() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        try (XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
            XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(0, xc1.moveChars(0, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("0123", xc0.getTextValue());
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
            assertEquals("WXYZ", xc1.getChars());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveCharsToSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try (XmlCursor xc0 = m_xo.newCursor()) {
            m_xc.moveChars(4, xc0);
        }
    }

    @Test
    public void testMoveCharsToPROCINST() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_PROCINST);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        try (XmlCursor xc0 = m_xo.newCursor()) {
            toNextTokenOfType(xc0, TokenType.PROCINST);
            m_xc.moveChars(1, xc0);
            xc0.toPrevToken();

            assertEquals("t", xc0.getChars());
            assertEquals("ext", m_xc.getChars());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testMoveCharsGTmax() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        m_xc = m_xo.newCursor();
        try (XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
            XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(1000, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());

            assertEquals(TokenType.END, xc1.currentTokenType());

            xc1.getTextValue();
        }
    }

    @Test
    public void testMoveCharsToNewDocument() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        XmlObject xo = XmlObject.Factory.parse(Common.XML_FOO_2ATTR_TEXT);
        try (XmlCursor xc1 = xo.newCursor()) {
            toNextTokenOfType(xc1, TokenType.TEXT);
            assertEquals(5, m_xc.moveChars(5, xc1));
            xc1.toParent();
            // verify xc1
            assertEquals("01234text", xc1.getTextValue());
        }
        // verify m_xc
        assertEquals(TokenType.END, m_xc.currentTokenType());
    }
}

