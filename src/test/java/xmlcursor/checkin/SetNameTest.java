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
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.cur;


public class SetNameTest {

    private static final String XML =
        "<bk:book at0=\"value0\" xmlns:bk=\"urn:loc.gov:books\">text0<author at0=\"v0\" at1=\"value1\"/></bk:book>";

    @Test
    void testNormalCase() throws XmlException {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			QName newName = new QName("newBook");
			m_xc.setName(newName);
			assertEquals(m_xc.getName(), newName);

			newName = new QName("uri:newUri", "newBook");
			m_xc.setName(newName);
			assertEquals(m_xc.getName(), newName);


			newName = new QName("uri:newUri", "newBook", "prefix");
			m_xc.setName(newName);
			assertEquals(m_xc.getName(), newName);

			//should work for attrs too...
			m_xc.toFirstAttribute();
			newName = new QName("uri:newUri", "newBook", "prefix");
			m_xc.setName(newName);
			assertEquals(m_xc.getName(), newName);
		}
    }

    @Test
    void testNoUri() throws XmlException {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			QName newName = new QName(null, "newBook");
			m_xc.setName(newName);
			assertEquals(m_xc.getName().getLocalPart(), "newBook");
		}
    }

    @Test
    void testNull() throws XmlException {
		try (XmlCursor m_xc = cur(XML)) {
			m_xc.toFirstChild();
			assertThrows(Exception.class, () -> m_xc.setName(null), "QName null");
		}
    }
}
