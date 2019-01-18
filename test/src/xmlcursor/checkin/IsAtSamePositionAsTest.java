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
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.*;


public class IsAtSamePositionAsTest extends BasicCursorTestCase{

    private static String sDoc=Common.XML_FOO_DIGITS;

	@Test
	public void testNormalCase() {
		XmlCursor m_xc1 = m_xo.newCursor();
		m_xc.toFirstChild();
		m_xc1.toFirstChild();
		assertTrue(m_xc.isAtSamePositionAs(m_xc1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSamePosDiffDoc() throws Exception {
		XmlCursor m_xc1 = XmlObject.Factory.parse(sDoc).newCursor();
		m_xc.toFirstChild();
		m_xc1.toFirstChild();
		m_xc.isAtSamePositionAs(m_xc1);
	}

	@Test
	public void testDiffPosSameDoc() throws Exception {
		XmlCursor m_xc1 = m_xo.newCursor();
		m_xc.toFirstChild();
		m_xc1.toFirstChild();
		m_xc1.toFirstAttribute();
		assertFalse(m_xc.isAtSamePositionAs(m_xc1));
	}

	@Test(expected = Exception.class)
	public void testNull() {
		m_xc.isAtSamePositionAs(null);
	}

	@Test
	public void testSelf() {
		m_xc.toFirstChild();
		assertEquals(true, m_xc.isAtSamePositionAs(m_xc));
	}

	@Before
	public void setUp() throws Exception {
		m_xo = XmlObject.Factory.parse(sDoc);
		m_xc = m_xo.newCursor();
	}
}
