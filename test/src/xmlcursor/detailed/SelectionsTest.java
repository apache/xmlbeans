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
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import test.xbean.xmlcursor.cr196679.TestDocument;
import test.xbean.xmlcursor.cr196679.TestType;
import xmlcursor.common.BasicCursorTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class SelectionsTest extends BasicCursorTestCase {

    private static final String sXml="<foo><b>0</b><b>1</b><b>2</b><b attr=\"a3\">3</b><b>4</b><b>5</b><b>6</b></foo>";

    //average case test
	@Test
	public void testNormalCase() throws Exception {
		XmlCursor m_xc1 = m_xo.newCursor();
		int nSelectionsCount = 7;
		m_xc.selectPath("$this//a");
		assertFalse(m_xc.hasNextSelection());
		assertFalse(m_xc.toNextSelection());
		assertEquals(0, m_xc.getSelectionCount());

		m_xc.selectPath("$this//b");
		m_xc1.toFirstChild();
		m_xc1.toFirstChild();
		do {
			m_xc1.addToSelection();
		} while (m_xc1.toNextSibling());
		assertEquals(nSelectionsCount, m_xc.getSelectionCount());
		int i = 0;
		while (m_xc.hasNextSelection()) {
			m_xc.toNextSelection();
			assertEquals("" + i, m_xc.getTextValue());
			i++;
		}
		int j = 0;
		while (m_xc1.hasNextSelection()) {
			m_xc1.toSelection(j);
			assertEquals("" + j, m_xc1.getTextValue());
			j++;
		}
		assertEquals(nSelectionsCount, j);
		assertEquals(nSelectionsCount, i);
	}

	@Test
	public void testToSelectionIllegalIndex() {
		m_xc.selectPath("$this//b");
		boolean result = m_xc.toSelection(-1);
		assertFalse(result);

		try {
			result = m_xc.toSelection(m_xc.getSelectionCount() + 1);
			assertFalse("Index > num selections", result);
		} catch (IllegalStateException e) {
		}

		assertFalse("Index < 0 ", result);

	}

	@Test
	public void testClearSelections() {
		m_xc.selectPath("$this//b");
		m_xc.toSelection(0);
		m_xc.clearSelections();
		assertEquals("<b>0</b>", m_xc.xmlText());

	}

	@Test
	public void testCR196679() throws Exception {
		TestDocument testDoc = null;
		String input = "<ns:test xmlns:ns=\"http://xbean.test/xmlcursor/CR196679\">\n" +
			"  <ns:name>myTest</ns:name>" +
			"  <ns:value>5</ns:value>" +
			"  </ns:test>";
		testDoc = TestDocument.Factory.parse(input);
		TestType test = testDoc.getTest();

		String queryName =
			"declare namespace ns='http://xbean.test/xmlcursor/CR196679'" +
				"$this/ns:name";

		String queryValue =
			"declare namespace ns='http://xbean.test/xmlcursor/CR196679'" +
				"$this/ns:value";

		XmlCursor cursor = test.newCursor();
		cursor.push();
		cursor.selectPath(queryName);
		cursor.toNextSelection();

		assertEquals("myTest", cursor.getTextValue());

		cursor.pop();
		cursor.selectPath(queryValue);
		cursor.toNextSelection();

		assertEquals("5", cursor.getTextValue());//expected output is value=5

		cursor.dispose();

	}

	@Before
	public void setUp() throws Exception {
		m_xo = XmlObject.Factory.parse(sXml);
		m_xc = m_xo.newCursor();
	}
}
