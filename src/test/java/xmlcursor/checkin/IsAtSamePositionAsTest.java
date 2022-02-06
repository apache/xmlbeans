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
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.obj;


public class IsAtSamePositionAsTest {

	@Test
	void testNormalCase() throws XmlException {
		XmlObject m_xo = obj(Common.XML_FOO_DIGITS);
	    try (XmlCursor m_xc = m_xo.newCursor();
			XmlCursor m_xc1 = m_xo.newCursor()) {
    		m_xc.toFirstChild();
    		m_xc1.toFirstChild();
    		assertTrue(m_xc.isAtSamePositionAs(m_xc1));
	    }
	}

	@Test
	void testSamePosDiffDoc() throws Exception {
	    try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS);
			XmlCursor m_xc1 = cur(Common.XML_FOO_DIGITS)) {
    		m_xc.toFirstChild();
    		m_xc1.toFirstChild();
    		assertThrows(IllegalArgumentException.class, () -> m_xc.isAtSamePositionAs(m_xc1));
	    }
	}

	@Test
	void testDiffPosSameDoc() throws Exception {
		XmlObject m_xo = obj(Common.XML_FOO_DIGITS);
	    try (XmlCursor m_xc = m_xo.newCursor();
			 XmlCursor m_xc1 = m_xo.newCursor()) {
    		m_xc.toFirstChild();
    		m_xc1.toFirstChild();
    		m_xc1.toFirstAttribute();
    		assertFalse(m_xc.isAtSamePositionAs(m_xc1));
	    }
	}

	@Test
	void testNull() throws XmlException {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			assertThrows(Exception.class, () -> m_xc.isAtSamePositionAs(null));
		}
	}

	@Test
	void testSelf() throws XmlException {
		try (XmlCursor m_xc = cur(Common.XML_FOO_DIGITS)) {
			m_xc.toFirstChild();
			assertTrue(m_xc.isAtSamePositionAs(m_xc));
		}
	}
}
