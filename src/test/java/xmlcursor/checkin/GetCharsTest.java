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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.assertEquals;


public class GetCharsTest extends BasicCursorTestCase {
    @Test
    public void testGetCharFromTEXTOffset() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(1);
        assertEquals("1234", m_xc.getChars());
    }

    @Test
    public void testGetCharFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_DIGITS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals("", m_xc.getChars());
    }

    @Test
    public void testGetCharFromCOMMENT() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_COMMENT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        assertEquals("", m_xc.getChars());
    }

}

