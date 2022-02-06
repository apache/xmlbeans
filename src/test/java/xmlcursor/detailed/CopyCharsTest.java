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
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;

public class CopyCharsTest {
    @Test
    void testCopyCharsToNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalArgumentException.class, () -> m_xc.copyChars(4, null));
        }
    }

    @Test
    void testCopyCharsNegative() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
             XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.copyChars(-1, xc0));
            assertEquals(TokenType.TEXT, xc0.currentTokenType());
            assertEquals("0123", xc0.getTextValue());

            xc0.toPrevToken();
            assertEquals(TokenType.START, xc0.prevTokenType());
            assertEquals("WXYZ0123", xc0.getTextValue());
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
            assertEquals(TokenType.START, xc1.prevTokenType());
            assertEquals("WXYZ", xc1.getTextValue());
        }
    }

    @Test
    void testCopyCharsZero() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
             XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(0, xc1.copyChars(0, xc0));
            assertEquals("0123", xc0.getTextValue());
            xc0.toPrevToken();
            assertEquals("0123", xc0.getTextValue());
            assertEquals(TokenType.TEXT, xc1.currentTokenType());
            assertEquals("WXYZ", xc1.getTextValue());
        }
    }

    @Test
    void testCopyCharsThis() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
             XmlCursor xc1 = m_xc.newCursor()) {
            assertTrue(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.copyChars(4, xc0));
            assertEquals("0123", xc0.getTextValue());
            xc0.toPrevToken();
            assertEquals("01230123", xc0.getTextValue());
            assertEquals("0123", xc1.getTextValue());
        }
    }

    @Test
    void testCopyCharsGTmax() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar>0123</bar><bar>WXYZ</bar></foo>");
             XmlCursor xc0 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT);
             XmlCursor xc1 = toNextTokenOfTypeCursor(m_xc, TokenType.TEXT)) {
            assertFalse(xc0.isAtSamePositionAs(xc1));
            assertEquals(4, xc1.copyChars(1000, xc0));
            // verify xc0
            assertEquals("0123", xc0.getTextValue());
            xc0.toPrevToken();
            assertEquals("WXYZ0123", xc0.getTextValue());
            // verify xc1
            assertEquals("WXYZ", xc1.getTextValue());
        }
    }

    @Test
    void testCopyCharsToDifferentDocument() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
             XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(xc1, TokenType.TEXT);
            assertEquals(5, m_xc.copyChars(5, xc1));
            assertEquals(5, xc1.toPrevChar(5));
            // verify xc1
            assertEquals("01234text", xc1.getTextValue());
            // verify m_xc
            assertEquals("01234", m_xc.getTextValue());
        }
    }

    @Test
    void testCopyCharsToEmptyDocumentSTARTDOC() throws Exception {
        XmlObject xo = XmlObject.Factory.newInstance();
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
             XmlCursor xc1 = xo.newCursor()) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            // assertEquals(5, m_xc.copyChars(5, xc1));
            assertThrows(IllegalArgumentException.class, () -> m_xc.copyChars(5, xc1));
        }
    }
}

