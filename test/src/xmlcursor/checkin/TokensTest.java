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

import static org.junit.Assert.*;


public class TokensTest extends BasicCursorTestCase {

    @Test
    public void testHasNextToken() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertTrue(m_xc.hasNextToken());
    }

    @Test
    public void testHasNextTokenENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        assertFalse(m_xc.hasNextToken());
    }

    @Test
    public void testHasPrevToken() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        assertTrue(m_xc.hasPrevToken());
    }

    @Test
    public void testHasPrevTokenSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        assertFalse(m_xc.hasPrevToken());
    }

    @Test
    public void testToEndTokenFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        assertEquals(TokenType.ENDDOC, m_xc.toEndToken());
    }

    @Test
    public void testToEndTokenFromSTART() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(TokenType.END, m_xc.toEndToken());
    }

    @Test
    public void testToEndTokenFromTEXTmiddle() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        m_xc.toNextChar(1);
        assertEquals(TokenType.NONE, m_xc.toEndToken());
    }

    @Test
    public void testToFirstContentTokenFromSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toFirstContentToken();
        assertEquals(TokenType.START, m_xc.currentTokenType());
    }

    @Test
    public void testToFirstContentTokenFromATTR() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.ATTR);
        assertEquals(TokenType.NONE, m_xc.toFirstContentToken());
        assertEquals(TokenType.ATTR, m_xc.currentTokenType());
    }

    @Test
    public void testToFirstContentTokenFromSTARTwithContent() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(TokenType.TEXT, m_xc.toFirstContentToken());
    }

    @Test
    public void testToFirstContentTokenFromSTARTwithoutContent() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(TokenType.END, m_xc.toFirstContentToken());
    }

    @Test
    public void testToNextTokenFromENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        assertEquals(TokenType.NONE, m_xc.toNextToken());
    }

    @Test
    public void testToNextTokenNAMESPACE() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_NS);
        m_xc = m_xo.newCursor();
        toNextTokenOfType(m_xc, TokenType.START);
        assertEquals(TokenType.NAMESPACE, m_xc.toNextToken());
    }

    @Test
    public void testToPrevTokenSTARTDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        assertEquals(TokenType.NONE, m_xc.toPrevToken());
        assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
    }

    @Test
    public void testToPrevTokenENDDOC() throws Exception {
        m_xo = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT);
        m_xc = m_xo.newCursor();
        m_xc.toEndDoc();
        assertEquals(TokenType.END, m_xc.toPrevToken());
    }
}

