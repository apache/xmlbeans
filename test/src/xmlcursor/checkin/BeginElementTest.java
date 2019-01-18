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
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BeginElementTest extends BasicCursorTestCase {

	private String sLocalName="localName";
    private String sUri="fakeURI";
    private String sDefaultPrefix=sUri.substring(0,3); //$BUGBUG:WHY???
    private String sExpectedStart="<"+sDefaultPrefix+":localName xmlns:"+sDefaultPrefix+"=\"fakeURI\"/>";

    private String sInputDoc=Common.XML_FOO_DIGITS;


	@Test
	public void testBeginElementStr() throws Exception {
		//same for string API
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		toNextTokenOfType(m_xc, TokenType.START);
		m_xc.beginElement(sLocalName, sUri);
		toPrevTokenOfType(m_xc, TokenType.START);
		assertEquals(m_xc.xmlText(), sExpectedStart);
	}

	@Test
	public void testBeginElementQName() throws Exception {
		//Qname call

		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		//insert new under the first element
		toNextTokenOfType(m_xc, TokenType.START);
		QName qName = new QName(sUri, sLocalName);
		m_xc.beginElement(qName);
		checkResult(qName);
	}

	@Test
	public void testBeginElementQNamePrefix() throws Exception {
		//Qname with prefix
		String sPrefix = "pre";
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		toNextTokenOfType(m_xc, TokenType.START);
		QName qName = new QName(sUri, sLocalName, sPrefix);
		System.out.println("Java prefix Qname: " + qName);
		m_xc.beginElement(qName);
		checkResult(qName);
	}

	//pre: cursor is not moved after beginElt call
	private void checkResult(QName qName) {
		TokenType tok = m_xc.toPrevToken();
		assertEquals(m_xc.getName(), qName);
	}

	public void testBeginElementStartDoc(String sLocalName, String sUri) throws Exception {
		m_xc = XmlObject.Factory.parse(sInputDoc).newCursor();
		m_xc.beginElement(sLocalName, sUri);
		m_xc.toPrevToken();
		m_xc.toPrevToken();
		assertTrue(m_xc.isStartdoc());
	}
}
