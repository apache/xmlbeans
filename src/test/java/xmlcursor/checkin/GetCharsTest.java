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
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class GetCharsTest {

    @Test
    void testGetCharFromTEXTOffset() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(1);
            assertEquals("1234", m_xc.getChars());
        }
    }

    @Test
    void testGetCharFromATTR() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            assertEquals("", m_xc.getChars());
        }
    }

    @Test
    void testGetCharFromCOMMENT() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            assertEquals("", m_xc.getChars());
        }
    }

}

