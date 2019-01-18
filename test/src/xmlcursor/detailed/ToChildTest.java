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

import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

public class ToChildTest extends BasicCursorTestCase {

    private String sDoc="<foo>early<bar>text</bar><char>zap<dar>wap</dar><ear>yap</ear></char></foo>";

    private int nChildCount=2; //num children if TEXT is a child
    /**
     * Testing toChild(String)
     * Cases:
     *      non-existing name
     *      2 children with same name
     *      nested child with same name
     *      Child of TEXT
     */

	@Test
	public void testToChildNonExisting() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		assertFalse(m_xc.toChild("yana"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToChildInvalidName() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toChild("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToChildNull() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toChild((String)null);
	}

	@Test
	public void testNameCollision() throws Exception {
		sDoc = "<foo><bar>txt0</bar><bar>txt1</bar></foo>";
		String sExpectedValue = "<bar>txt0</bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("bar"));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

	@Test
	public void testSameNameDescendant() throws Exception {
		sDoc = "<foo><bar><bar>txt0<bar/></bar></bar><bar>txt1</bar></foo>";
		String sExpectedValue = "<bar><bar>txt0<bar/></bar></bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("bar"));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

	@Test
	public void testTextChild() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		toNextTokenOfType(m_xc, TokenType.TEXT);
		assertFalse(m_xc.toChild("bar"));
	}

    /**
     * toChild(String,String)
     * Cases:
     *      non-existing ns, existing name
     *      non-existing name, existing ns
     *      2 children with same name
     *      2 children with same name, diff ns
     *      2 children with diff name, same ns
     *      nested child with same name & ns
     */


	@Test
	public void testNullNS() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		String sExpectedResult = "<bar>text</bar>";
		assertTrue(m_xc.toChild(null, "bar"));
		assertEquals(sExpectedResult, m_xc.xmlText());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullName() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		m_xc.toChild("uri:foo.org", null);
	}

	@Test
	public void testNamespaceOKNameInvalid() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild("fo", "test"));
	}

	@Test
	public void testNamespaceInvalidNameOK() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild("bar", "bar"));
	}

	@Test
	public void testNormalCase() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
		String sExpectedResult = "<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("uri:foo.org", "bar"));
		assertEquals(sExpectedResult, m_xc.xmlText());
	}

	@Test
	public void testUriNameCollision() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><fo:bar>txt1</fo:bar></foo>";
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("uri:foo.org", "bar"));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

    //same URI diff names
	@Test
	public void testFakeNameCollision() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("uri:foo.org", "bar"));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

	//diff URI same names
	@Test
	public void testFakeNameCollision3() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bar>txt0</fo:bar><bar>txt1</bar></foo>";
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt0</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("uri:foo.org", "bar"));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

	@Test
	public void testSameNameDescendant1() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><bar><fo:bar>txt0<bar/></fo:bar></bar><bar>txt1</bar></foo>";

		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild("uri:foo.org", "bar"));
	}

	@Test
	public void testSameNameDescendant2() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><bar><fo:bar>txt0<bar/></fo:bar></bar><bar>txt1</bar><fo:bar>txt1</fo:bar></foo>";
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild("uri:foo.org", "bar"));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}


    /**
     * toChild(int)
     * Cases:
     *       i<0
     *       i>numChildren
     *       i=0, numChildren=0
     */

	@Test
	public void testNegativeIndex() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		assertFalse(m_xc.toChild(-1));
	}

	@Test
	public void testIndexOKFirst() throws Exception {
		String sExpectedValue = "<bar>text</bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild(0));//text is not children
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

	@Test
	public void testIndexOKLast() throws Exception {
		String sExpectedValue = "<char>zap<dar>wap</dar><ear>yap</ear></char>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild(nChildCount - 1));
		assertEquals(sExpectedValue, m_xc.xmlText());
		m_xc.toParent();
		m_xc.toLastChild();
		assertEquals(sExpectedValue, m_xc.xmlText());
	}

	@Test
	public void testLargeIndex() throws Exception {
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild(20));

	}

	@Test
	public void testInd0Count0() throws Exception {
		sDoc = "<foo/>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild(0));
	}


	/**
	 * toChild(QName,int)
	 * Cases:
	 * QName dne,
	 * QName OK, i OK;i >numChildren;i<0
	 * Name collision, i=1;i>numChildren
	 * Siblings and a child with same qname, ask for 2nd sibling
	 */

	@Test
	public void testToChildQNameDNE0() throws Exception {
		QName searchVal = new QName("fake:uri", "bar");
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild(searchVal, 1));
	}

	@Test
	public void testToChildQNameDNE1() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
		QName searchVal = new QName("uri:foo.org", "bar", "pre");
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertFalse(m_xc.toChild(searchVal, 1));
	}

	@Test
	public void testToChildQNameOKIndexOK() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
		QName searchVal = new QName("uri:foo.org", "bar", "fo");
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild(searchVal, 0));
		assertEquals(sExpectedValue, m_xc.xmlText());
		assertFalse(m_xc.toChild(searchVal, 1));
		assertFalse(m_xc.toChild(searchVal, -1));
	}

	@Test
	public void testQNameNameCollision() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\"><fo:bars>txt0</fo:bars><fo:bar>txt1</fo:bar></foo>";
		nChildCount = 2;
		QName searchVal = new QName("uri:foo.org", "bar", "fo");
		String sExpectedValue = "<fo:bar xmlns:fo=\"uri:foo.org\">txt1</fo:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		assertTrue(m_xc.toChild(searchVal, 0));
		assertEquals(sExpectedValue, m_xc.xmlText());
		int nInvalidCount = 2;
		if (nInvalidCount >= nChildCount)
			assertFalse(m_xc.toChild(searchVal, nInvalidCount));
		else fail("Broken Test");
	}


	@Test
	public void testFakeQNameCollision() throws Exception {
		sDoc = "<foo xmlns:fo=\"uri:foo.org\" xmlns:fo2=\"uri:foo.org\"><fo2:bar>txt0</fo2:bar><fo:bar>txt1</fo:bar></foo>";
		String sExpectedValue = "<fo2:bar xmlns:fo=\"uri:foo.org\" xmlns:fo2=\"uri:foo.org\">txt0</fo2:bar>";
		m_xc = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		QName searchVal = new QName("uri:foo.org", "bar", "fo");
		assertTrue(m_xc.toChild(searchVal, 0));
		assertEquals(sExpectedValue, m_xc.xmlText());
	}
}
