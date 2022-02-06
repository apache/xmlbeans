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


package xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;

public class ToChildTest {

    private static final String XML ="<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>";

	@Test
	void testToChildNonExisting() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			assertFalse(m_xc.toChild("yana"));
		}
	}

	@Test
	void testToChildInvalidName() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			assertThrows(IllegalArgumentException.class, () -> m_xc.toChild(""));
		}
	}

	@Test
	void testToChildNull() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			assertThrows(IllegalArgumentException.class, () -> m_xc.toChild((String)null));
		}
	}

	@Test
	void testNameCollision() throws Exception {
		String sExpectedValue = "<bar>txt0</bar>";
		try (XmlCursor m_xc = cur("<foo><bar>txt0</bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("bar"));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	@Test
	void testSameNameDescendant() throws Exception {
		String sExpectedValue = "<bar><bar>txt0<bar/></bar></bar>";
		try (XmlCursor m_xc = cur("<foo><bar><bar>txt0<bar/></bar></bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("bar"));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	@Test
	void testTextChild() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			toNextTokenOfType(m_xc, TokenType.TEXT);
			assertFalse(m_xc.toChild("bar"));
		}
	}

	@Test
	void testNullNS() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			String sExpectedResult = "<bar>text</bar>";
			assertTrue(m_xc.toChild(null, "bar"));
			assertEquals(sExpectedResult, m_xc.xmlText());
		}
	}

	@Test
	void testNullName() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			assertThrows(IllegalArgumentException.class, () -> m_xc.toChild("uri:foo.org", null));
		}
	}

	@Test
	void testNamespaceOKNameInvalid() throws Exception {
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild("fo", "test"));
		}
	}

	@Test
	void testNamespaceInvalidNameOK() throws Exception {
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild("bar", "bar"));
		}
	}

	@Test
	void testNormalCase() throws Exception {
		String sExpectedResult = "<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("uri:foo.org", "bar"));
			assertEquals(sExpectedResult, m_xc.xmlText());
		}
	}

	@Test
	void testUriNameCollision() throws Exception {
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("uri:foo.org", "bar"));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

    //same URI diff names
	@Test
	void testFakeNameCollision() throws Exception {
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("uri:foo.org", "bar"));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	//diff URI same names
	@Test
	void testFakeNameCollision3() throws Exception {
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("uri:foo.org", "bar"));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	@Test
	void testSameNameDescendant1() throws Exception {
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><bar><fo:bar>txt0<bar/></fo:bar></bar><bar>txt1</bar></foo>")) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild("uri:foo.org", "bar"));
		}
	}

	@Test
	void testSameNameDescendant2() throws Exception {
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><bar><fo:bar>txt0<bar/></fo:bar></bar><bar>txt1</bar><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild("uri:foo.org", "bar"));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	@Test
	void testNegativeIndex() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			assertFalse(m_xc.toChild(-1));
		}
	}

	@Test
	void testIndexOKFirst() throws Exception {
		String sExpectedValue = "<bar>text</bar>";
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild(0));//text is not children
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	@Test
	void testIndexOKLast() throws Exception {
		String sExpectedValue = "<char>zap<dar>wap</dar><ear>yap</ear></char>";
		int nChildCount = 2;
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild(nChildCount - 1));
			assertEquals(sExpectedValue, m_xc.xmlText());
			m_xc.toParent();
			m_xc.toLastChild();
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}

	@Test
	void testLargeIndex() throws Exception {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild(20));
		}
	}

	@Test
	void testInd0Count0() throws Exception {
		try (XmlCursor m_xc = cur("<foo/>")) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild(0));
		}
	}

	@Test
	void testToChildQNameDNE0() throws Exception {
		QName searchVal = new QName("fake:uri", "bar");
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild(searchVal, 1));
		}
	}

	@Test
	void testToChildQNameDNE1() throws Exception {
		QName searchVal = new QName("uri:foo.org", "bar", "pre");
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			assertFalse(m_xc.toChild(searchVal, 1));
		}
	}

	@Test
	void testToChildQNameOKIndexOK() throws Exception {
		QName searchVal = new QName("uri:foo.org", "bar", "fo");
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild(searchVal, 0));
			assertEquals(sExpectedValue, m_xc.xmlText());
			assertFalse(m_xc.toChild(searchVal, 1));
			assertFalse(m_xc.toChild(searchVal, -1));
		}
	}

	@Test
	void testQNameNameCollision() throws Exception {
		int nInvalidCount = 2;
		QName searchVal = new QName("uri:foo.org", "bar", "fo");
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			assertTrue(m_xc.toChild(searchVal, 0));
			assertEquals(sExpectedValue, m_xc.xmlText());
			assertFalse(m_xc.toChild(searchVal, nInvalidCount));
		}
	}


	@Test
	void testFakeQNameCollision() throws Exception {
		String sExpectedValue = "<fo2:bar xmlns:fo=\"uri:foo.org\" xmlns:fo2=\"uri:foo.org\">txt0</fo2:bar>";
		try (XmlCursor m_xc = cur("<foo xmlns:fo=\"uri:foo.org\" xmlns:fo2=\"uri:foo.org\"><fo2:bar>txt0</fo2:bar><fo:bar>txt1</fo:bar></foo>")) {
			m_xc.toFirstChild();
			QName searchVal = new QName("uri:foo.org", "bar", "fo");
			assertTrue(m_xc.toChild(searchVal, 0));
			assertEquals(sExpectedValue, m_xc.xmlText());
		}
	}
}
