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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.assertEquals;


public class CurrentTokenTypeTest extends BasicCursorTestCase {

    String sInputDoc;

    /**
        ATTR
	COMMENT
	END
	ENDDOC
	NAMESPACE
	NONE
	PROCINST
	START
	STARTDOC
	TEXT
    */

	@Test
    public void testAttrType() throws XmlException{
		sInputDoc = Common.XML_FOO_2ATTR_TEXT;
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		assertEquals(m_xc.currentTokenType(), XmlCursor.TokenType.STARTDOC);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.START);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.ATTR);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.ATTR);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.TEXT);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.END);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.ENDDOC);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.NONE);
	}

	@Test
	public void testCommentType() throws XmlException {
		sInputDoc = Common.XML_FOO_COMMENT;
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		assertEquals(m_xc.currentTokenType(), XmlCursor.TokenType.STARTDOC);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.COMMENT);
     }

	@Test
    public void testNamespaceType()throws XmlException{
		sInputDoc = Common.XML_FOO_NS_PREFIX;
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();

		assertEquals(m_xc.currentTokenType(), XmlCursor.TokenType.STARTDOC);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.START);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.NAMESPACE);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.COMMENT);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.TEXT);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.START);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.ATTR);
    }

	@Test
    public void testNoneType()throws XmlException{
		sInputDoc = "<a/>";
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		m_xc.toEndDoc();
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.NONE);
    }

	@Test
    public void testProcinstType()throws XmlException{
		sInputDoc = Common.XML_FOO_PROCINST;
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		assertEquals(m_xc.currentTokenType(), XmlCursor.TokenType.STARTDOC);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.PROCINST);
	}

	@Test
    public void testTextType()throws XmlException{
		sInputDoc = "<text>blah<test>test and some more test</test>" + "\u042F\u0436\n\r</text>";
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		assertEquals(m_xc.currentTokenType(), XmlCursor.TokenType.STARTDOC);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.START);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.TEXT);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.START);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.TEXT);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.END);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.TEXT);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.END);
		assertEquals(m_xc.toNextToken(), XmlCursor.TokenType.ENDDOC);
    }
}
