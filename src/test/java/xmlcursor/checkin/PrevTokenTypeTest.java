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
import org.junit.Before;
import xmlcursor.common.BasicCursorTestCase;

import static org.junit.Assert.*;


public class PrevTokenTypeTest extends BasicCursorTestCase {

	@org.junit.Test
    public void testAllTokensTest(){
		m_xc.toEndDoc();
		assertTrue(m_xc.isEnddoc());
		assertTrue(m_xc.isFinish());
		assertEquals(TokenType.END, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isEnd());
		assertTrue(m_xc.isFinish());
		assertEquals(TokenType.END, m_xc.prevTokenType());
		m_xc.toPrevToken();


		assertTrue(m_xc.isEnd());
		assertEquals(TokenType.TEXT, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isText());
		assertFalse(m_xc.isContainer());
		assertEquals(TokenType.ATTR, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isAttr());
		assertTrue(m_xc.isAnyAttr());
		assertEquals(TokenType.ATTR, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isAttr());
		assertTrue(m_xc.isAnyAttr());
		assertEquals(TokenType.START, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isStart());
		assertTrue(m_xc.isContainer());
		assertEquals(TokenType.TEXT, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isText());
		assertEquals(TokenType.COMMENT, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isComment());
		assertEquals(TokenType.PROCINST, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isProcinst());
		assertEquals(TokenType.NAMESPACE, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isNamespace());
		assertTrue(m_xc.isAnyAttr());
		assertFalse(m_xc.isAttr());
		assertEquals(TokenType.START, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isStart());
		assertTrue(m_xc.isContainer());
		assertEquals(TokenType.STARTDOC, m_xc.prevTokenType());
		m_xc.toPrevToken();

		assertTrue(m_xc.isStartdoc());
		assertTrue(m_xc.isContainer());
		assertEquals(TokenType.NONE, m_xc.prevTokenType());
		//assert won't move further
		assertEquals(TokenType.NONE, m_xc.toPrevToken());
		assertEquals(true, m_xc.isStartdoc());
    }

	@Before
    public void setUp() throws Exception{
		String sDoc = "<foo xmlns:edi='http://ecommerce.org/schema'><?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><!-- the 'price' element's namespace is http://ecommerce.org/schema -->  <edi:price units='Euro' date='12-12-03'>32.18</edi:price></foo>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
    }
}
