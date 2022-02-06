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
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;


public class MoveCharsTest {

    @Test
    void testMoveCharsOverlap() throws Exception {
        XmlObject m_xo = obj(Common.XML_FOO_DIGITS);
        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc1 = m_xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            xc1.toNextChar(2);
            assertEquals("234", xc1.getChars());
            assertEquals(3, m_xc.moveChars(3, xc1));
            assertEquals("34", m_xc.getChars());
            assertEquals("34", xc1.getChars());
        }
    }

    @Test
    void testMoveCharsNoOverlap() throws Exception {
        XmlObject m_xo = obj(Common.XML_FOO_DIGITS);
        try (XmlCursor m_xc = m_xo.newCursor();
            XmlCursor xc1 = m_xo.newCursor();
            XmlCursor xc2 = m_xo.newCursor()) {

            toNextTokenOfType(m_xc, TokenType.TEXT);
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

    @Test
    void testMoveCharsToNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.moveChars(4, null));
        }
    }

    @Test
    void testMoveCharsSibling() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
             XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(4, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            assertEquals(TokenType.END, xc1.currentTokenType());
            assertThrows(IllegalStateException.class, xc1::getTextValue);
        }
    }

    @Test
    void testMoveCharsNegative() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
             XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(-1, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            assertEquals(TokenType.END, xc1.currentTokenType());
            assertThrows(IllegalStateException.class, xc1::getTextValue);
        }
    }

    @Test
    void testMoveCharsZero() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
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

    @Test
    void testMoveCharsToSTARTDOC() throws Exception {
        XmlObject m_xo = obj("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc0 = m_xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.moveChars(4, xc0));
        }
    }

    @Test
    void testMoveCharsToPROCINST() throws Exception {
        XmlObject m_xo = obj(Common.XML_FOO_PROCINST);
        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc0 = m_xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc0, TokenType.PROCINST);
            m_xc.moveChars(1, xc0);
            xc0.toPrevToken();

            assertEquals("t", xc0.getChars());
            assertEquals("ext", m_xc.getChars());
        }
    }

    @Test
    void testMoveCharsGTmax() throws Exception {
        XmlObject m_xo = obj("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");

        try (XmlCursor m_xc = m_xo.newCursor();
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
            XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.moveChars(1000, xc0));
            assertEquals("0123", xc0.getChars());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());

            assertEquals(TokenType.END, xc1.currentTokenType());

            assertThrows(IllegalStateException.class, xc1::getTextValue);
        }
    }

    @Test
    void testMoveCharsToNewDocument() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
             XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            assertEquals(5, m_xc.moveChars(5, xc1));
            xc1.toParent();
            // verify xc1
            assertEquals("01234text", xc1.getTextValue());
            // verify m_xc
            assertEquals(TokenType.END, m_xc.currentTokenType());
        }
    }
}

