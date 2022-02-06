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

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xmlcursor.common.BasicCursorTestCase.*;


public class BeginElementTest {

	private static final String LOCAL_NAME ="localName";
    private static final String URI ="fakeURI";
    private static final String DEFAULT_PREFIX = URI.substring(0,3); //$BUGBUG:WHY???
    private static final String EXPECTED_START = "<" + DEFAULT_PREFIX + ":localName xmlns:" + DEFAULT_PREFIX + "=\"fakeURI\"/>";

	@Test
	void testBeginElementStr() throws XmlException {
		//same for string API
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			toNextTokenOfType(m_xc, TokenType.START);
			m_xc.beginElement(LOCAL_NAME, URI);
			toPrevTokenOfType(m_xc, TokenType.START);
			assertEquals(m_xc.xmlText(), EXPECTED_START);
		}
	}

	@Test
	void testBeginElementQName() throws Exception {
		//Qname call
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			//insert new under the first element
			toNextTokenOfType(m_xc, TokenType.START);
			QName qName = new QName(URI, LOCAL_NAME);
			m_xc.beginElement(qName);
			checkResult(m_xc, qName);
		}
	}

	@Test
	void testBeginElementQNamePrefix() throws Exception {
		//Qname with prefix
		String sPrefix = "pre";
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			toNextTokenOfType(m_xc, TokenType.START);
			QName qName = new QName(URI, LOCAL_NAME, sPrefix);
			m_xc.beginElement(qName);
			checkResult(m_xc, qName);
		}
	}

	@Test
	void testBeginElementStartDoc() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			m_xc.toFirstContentToken();
			m_xc.beginElement(LOCAL_NAME, URI);
			m_xc.toPrevToken();
			m_xc.toPrevToken();
			assertTrue(m_xc.isStartdoc());
		}
	}

	//pre: cursor is not moved after beginElt call
	private static void checkResult(XmlCursor m_xc, QName qName) {
		TokenType tok = m_xc.toPrevToken();
		assertEquals(m_xc.getName(), qName);
	}
}
