/*   Copyright 2007 The Apache Software Foundation
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
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;

/**
 * checkin tests for XmlCursor toNextSibling methods
 *
 */
public class ToNextSiblingTest {
    private static final String xml0 = "<root><a/><b/><c/></root>";
    private static final String xml1 = "<root xmlns=\"somenamespace\"><a/><b/><c/></root>";

    /** test toNextSibling(String name) where there is no namespace */
    @Test
    void testName() throws Exception {
        try (XmlCursor m_xc = cur(xml0)) {
            m_xc.toNextToken();
            m_xc.toChild(0);
            assertEquals("", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
            m_xc.push();

            // name exists
            assertTrue(m_xc.toNextSibling("c"));
            assertEquals("", m_xc.getName().getNamespaceURI());
            assertEquals("c", m_xc.getName().getLocalPart());
            m_xc.pop();

            // name does not exist
            assertFalse(m_xc.toNextSibling("d"));
            // cursor hasn't moved
            assertEquals("", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
        }
    }

    /** test toNextSibling(String name) where there is a namespace */
    @Test
    void testIncompleteName() throws Exception {
        try (XmlCursor m_xc = cur(xml1)) {
            m_xc.toNextToken();
            m_xc.toChild(0);
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());

            // name exists, but is incomplete by itself without the namespace
            assertFalse(m_xc.toNextSibling("c"));
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
        }
    }

    /** test toNextSibling(String namespace, String localname) */
    @Test
    void testNamespaceAndLocalName0() throws Exception {
        try (XmlCursor m_xc = cur(xml0)) {
            m_xc.toNextToken();
            m_xc.toChild(0);
            assertEquals("", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
            m_xc.push();

            // name exists
            assertTrue(m_xc.toNextSibling("", "c"));
            assertEquals("", m_xc.getName().getNamespaceURI());
            assertEquals("c", m_xc.getName().getLocalPart());
            m_xc.pop();

            // name does not exist
            assertFalse(m_xc.toNextSibling("", "d"));
            // cursor hasn't moved
            assertEquals("", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
        }
    }

    /** test toNextSibling(String namespace, String localname) */
    @Test
    void testNamespaceAndLocalName1() throws Exception {
        try (XmlCursor m_xc = cur(xml1)) {
            m_xc.toNextToken();
            m_xc.toChild(0);
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
            m_xc.push();

            // name exists
            assertTrue(m_xc.toNextSibling("somenamespace", "c"));
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("c", m_xc.getName().getLocalPart());
            m_xc.pop();

            // name does not exist
            assertFalse(m_xc.toNextSibling("somenamespace", "d"));
            // cursor hasn't moved
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
        }
    }

    /** test toNextSibling(QName qName) */
    @Test
    void testQName() throws Exception {
        try (XmlCursor m_xc = cur(xml1)) {
            m_xc.toNextToken();
            m_xc.toChild(0);
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
            m_xc.push();

            // name exists
            assertTrue(m_xc.toNextSibling(new QName("somenamespace", "c")));
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("c", m_xc.getName().getLocalPart());
            m_xc.pop();

            // name does not exist
            assertFalse(m_xc.toNextSibling(new QName("somenamespace", "d")));
            // cursor hasn't moved
            assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
            assertEquals("a", m_xc.getName().getLocalPart());
        }
    }
}
