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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import test.xbean.xmlcursor.cr196679.TestDocument;
import test.xbean.xmlcursor.cr196679.TestType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.obj;


public class SelectionsTest {

    private static final String XML ="<foo><b>0</b><b>1</b><b>2</b><b attr=\"a3\">3</b><b>4</b><b>5</b><b>6</b></foo>";

	//average case test
	@Test
	void testNormalCase() throws Exception {
		XmlObject m_xo = obj(XML);
	    try (XmlCursor m_xc = m_xo.newCursor(); XmlCursor m_xc1 = m_xo.newCursor()) {
    		final int nSelectionsCount = 7;
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
			assertEquals(nSelectionsCount, getSelectionCount(m_xc));
    		assertEquals(nSelectionsCount, getSelectionCount(m_xc1));
	    }
	}

	private static int getSelectionCount(XmlCursor m_xc) {
		int i = 0;
		while (m_xc.hasNextSelection()) {
			m_xc.toNextSelection();
			assertEquals("" + i, m_xc.getTextValue());
			i++;
		}
		return i;
	}


	@Test
	void testToSelectionIllegalIndex() throws XmlException {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.selectPath("$this//b");
			boolean result = m_xc.toSelection(-1);
			assertFalse(result);

			result = m_xc.toSelection(m_xc.getSelectionCount() + 1);
			assertFalse(result, "Index > num selections");

			assertFalse(result, "Index < 0 ");
		}

	}

	@Test
	void testClearSelections() throws XmlException {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.selectPath("$this//b");
			m_xc.toSelection(0);
			m_xc.clearSelections();
			assertEquals("<b>0</b>", m_xc.xmlText());
		}
	}

	@Test
	void testCR196679() throws Exception {
		final String input =
			"<ns:test xmlns:ns=\"http://xbean.test/xmlcursor/CR196679\">\n" +
			"  <ns:name>myTest</ns:name>" +
			"  <ns:value>5</ns:value>" +
			"  </ns:test>";
		TestDocument testDoc = TestDocument.Factory.parse(input);
		TestType test = testDoc.getTest();

		String queryName = "declare namespace ns='http://xbean.test/xmlcursor/CR196679' $this/ns:name";
		String queryValue = "declare namespace ns='http://xbean.test/xmlcursor/CR196679' $this/ns:value";

		try (XmlCursor cursor = test.newCursor()) {
    		cursor.push();
    		cursor.selectPath(queryName);
    		cursor.toNextSelection();

    		assertEquals("myTest", cursor.getTextValue());

    		cursor.pop();
    		cursor.selectPath(queryValue);
    		cursor.toNextSelection();

    		assertEquals("5", cursor.getTextValue());//expected output is value=5
		}
	}

}
