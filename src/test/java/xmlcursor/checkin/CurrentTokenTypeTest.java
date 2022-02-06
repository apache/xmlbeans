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


public class CurrentTokenTypeTest {

	@Test
    void testAttrType() throws XmlException{
		try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
			assertEquals(m_xc.currentTokenType(), TokenType.STARTDOC);
			assertEquals(m_xc.toNextToken(), TokenType.START);
			assertEquals(m_xc.toNextToken(), TokenType.ATTR);
			assertEquals(m_xc.toNextToken(), TokenType.ATTR);
			assertEquals(m_xc.toNextToken(), TokenType.TEXT);
			assertEquals(m_xc.toNextToken(), TokenType.END);
			assertEquals(m_xc.toNextToken(), TokenType.ENDDOC);
			assertEquals(m_xc.toNextToken(), TokenType.NONE);
		}
	}

	@Test
	void testCommentType() throws XmlException {
		try (XmlCursor m_xc = cur(Common.XML_FOO_COMMENT)) {
			assertEquals(m_xc.currentTokenType(), TokenType.STARTDOC);
			assertEquals(m_xc.toNextToken(), TokenType.COMMENT);
		}
     }

	@Test
    void testNamespaceType()throws XmlException{
		try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
			assertEquals(m_xc.currentTokenType(), TokenType.STARTDOC);
			assertEquals(m_xc.toNextToken(), TokenType.START);
			assertEquals(m_xc.toNextToken(), TokenType.NAMESPACE);
			assertEquals(m_xc.toNextToken(), TokenType.COMMENT);
			assertEquals(m_xc.toNextToken(), TokenType.TEXT);
			assertEquals(m_xc.toNextToken(), TokenType.START);
			assertEquals(m_xc.toNextToken(), TokenType.ATTR);
		}
    }

	@Test
    void testNoneType()throws XmlException{
		try (XmlCursor m_xc = cur("<a/>")) {
			m_xc.toEndDoc();
			assertEquals(m_xc.toNextToken(), TokenType.NONE);
		}
    }

	@Test
    void testProcinstType()throws XmlException{
		try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
			assertEquals(m_xc.currentTokenType(), TokenType.STARTDOC);
			assertEquals(m_xc.toNextToken(), TokenType.PROCINST);
		}
	}

	@Test
    void testTextType()throws XmlException{
		String sInputDoc = "<text>blah<test>test and some more test</test>" + "\u042F\u0436\n\r</text>";
		try (XmlCursor m_xc = cur(sInputDoc)) {
			assertEquals(m_xc.currentTokenType(), TokenType.STARTDOC);
			assertEquals(m_xc.toNextToken(), TokenType.START);
			assertEquals(m_xc.toNextToken(), TokenType.TEXT);
			assertEquals(m_xc.toNextToken(), TokenType.START);
			assertEquals(m_xc.toNextToken(), TokenType.TEXT);
			assertEquals(m_xc.toNextToken(), TokenType.END);
			assertEquals(m_xc.toNextToken(), TokenType.TEXT);
			assertEquals(m_xc.toNextToken(), TokenType.END);
			assertEquals(m_xc.toNextToken(), TokenType.ENDDOC);
		}
    }
}
