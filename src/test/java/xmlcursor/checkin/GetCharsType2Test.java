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
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class GetCharsType2Test {

    @Test
    void testGetCharsType2LessThanBufLength() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            char[] buf = new char[5];
            assertEquals(3, m_xc.getChars(buf, 0, 3));
            String s = new String(buf);
            assertEquals("012\0\0", s);
        }
    }

    @Test
    void testGetCharsType2GTBufLengthMinusOffset() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            char[] buf = new char[5];
            assertEquals(2, m_xc.getChars(buf, 3, 3));
            assertEquals('\0', buf[0]);
            assertEquals('\0', buf[1]);
            assertEquals('\0', buf[2]);
            assertEquals('0', buf[3]);
            assertEquals('1', buf[4]);
        }
    }

    @Test
    void testGetCharsType2FromATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.START);
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.END);
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromENDDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            m_xc.toEndDoc();
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }

    @Test
    void testGetCharsType2FromCOMMENT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            m_xc.toEndDoc();
            char[] buf = new char[5];
            assertEquals(0, m_xc.getChars(buf, 3, 4));
        }
    }
}

