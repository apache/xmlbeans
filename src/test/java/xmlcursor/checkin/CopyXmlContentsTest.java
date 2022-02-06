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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;


public class CopyXmlContentsTest {
	@Test
	void testCopyToNull() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			toNextTokenOfType(m_xc, TokenType.TEXT);
			assertThrows(IllegalArgumentException.class, () -> m_xc.copyXmlContents(null));
		}
	}

	@Test
	void testCopyDifferentStoresLoadedByParseInvalidDest() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
			XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
			toNextTokenOfType(m_xc, TokenType.START);
			toNextTokenOfType(xc1, TokenType.START);
			xc1.close();
			assertThrows(IllegalStateException.class, () -> m_xc.copyXmlContents(xc1));
		}
	}

	@Test
	void testCopyDifferentStoresLoadedByParse() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
		 	XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
			toNextTokenOfType(m_xc, TokenType.START);
			toNextTokenOfType(xc1, TokenType.TEXT);
			m_xc.copyXmlContents(xc1);
			xc1.toParent();
			// verify xc1
			assertEquals("01234text", xc1.getTextValue());

			// verify m_xc
			toNextTokenOfType(m_xc, TokenType.TEXT); //get to the text
			assertEquals("01234", m_xc.getChars());
		}
	}

    /* the source is not a container*/
	@Test
	void testCopyDifferentStoresLoadedByParseInvalidSrc() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
			XmlCursor xc1 = cur(Common.XML_FOO_2ATTR_TEXT)) {
    		toNextTokenOfType(m_xc, TokenType.TEXT);
    		toNextTokenOfType(xc1, TokenType.START);
    		boolean result = m_xc.copyXmlContents(xc1);
    		assertFalse(result);
		}
	}

	@Test
	@Disabled
	public void testCopyOntoItself() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			toNextTokenOfType(m_xc, TokenType.TEXT);
			String sExpectedXml = m_xc.xmlText();
			m_xc.copyXmlContents(m_xc);

			//cursor is left immediately before copied material
			assertEquals(sExpectedXml, m_xc.getTextValue());
		}
	}

	@Test
	void testCopySelf() throws Exception {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			toNextTokenOfType(m_xc, TokenType.START);
			m_xc.copyXmlContents(m_xc);
			//cursor is left immediately before copied material
			m_xc.toStartDoc();
			//assertEquals(sExpectedXml.length(),m_xc.xmlText().length());
			String sExpectedXml = "<xml-fragment>01234<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo></xml-fragment>";
			assertEquals(sExpectedXml, m_xc.xmlText());
		}
	}


    /**
       Can't really copy the whole doc, so copy all the contents
       into a false root */
	@Test
	void testCopyWholeDoc() throws Exception {
		try (XmlCursor xc1 = cur(Common.XML_FOO_BAR_WS_TEXT);
		    XmlCursor xc2 = cur("<root></root>")) {
    		xc2.toFirstChild();
    		String sExpectedXml = xc1.xmlText();
    		xc1.copyXmlContents(xc2);
    		toPrevTokenOfType(xc2, TokenType.STARTDOC);
    		toNextTokenOfType(xc2, TokenType.START);
    		assertEquals(sExpectedXml, xc2.xmlText());
		}

		//namespaces are not copied
		try (XmlCursor xc1 = cur(Common.XML_FOO_NS_PREFIX);
		    XmlCursor xc2 = cur("<root></root>")) {
		    String sExpectedXml = xc1.xmlText();
    		xc2.toFirstChild();

    		xc1.copyXmlContents(xc2);
    		toPrevTokenOfType(xc2, TokenType.STARTDOC);
    		assertNotEquals(sExpectedXml, xc2.xmlText());
		}

		//attributes are not copied
		try (XmlCursor xc1 = cur(Common.XML_FOO_2ATTR);
    	    XmlCursor xc2 = cur("<root></root>")) {
    	    String sExpectedXml = xc1.xmlText();
    		xc2.toFirstChild();

    		xc1.copyXmlContents(xc2);
    		toPrevTokenOfType(xc2, TokenType.STARTDOC);
    		assertNotEquals(sExpectedXml, xc2.xmlText());
		}
	}
}
