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

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

/**
 * checkin tests for XmlCursor toNextSibling methods
 *
 */
public class ToNextSiblingTest extends BasicCursorTestCase {
    private static String xml0 = "<root><a/><b/><c/></root>";
    private static String xml1 = "<root xmlns=\"somenamespace\"><a/><b/><c/></root>";

    /** test toNextSibling(String name) where there is no namespace */
    @Test
    public void testName() throws Exception {
        m_xc = XmlObject.Factory.parse(xml0).newCursor();
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

    /** test toNextSibling(String name) where there is a namespace */
    @Test
    public void testIncompleteName() throws Exception {
        m_xc = XmlObject.Factory.parse(xml1).newCursor();
        m_xc.toNextToken();
        m_xc.toChild(0);
        assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
        assertEquals("a", m_xc.getName().getLocalPart());

        // name exists, but is incomplete by itself without the namespace
        assertFalse(m_xc.toNextSibling("c"));
        assertEquals("somenamespace", m_xc.getName().getNamespaceURI());
        assertEquals("a", m_xc.getName().getLocalPart());
    }

    /** test toNextSibling(String namespace, String localname) */
    @Test
    public void testNamespaceAndLocalName0() throws Exception {
        m_xc = XmlObject.Factory.parse(xml0).newCursor();
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

    /** test toNextSibling(String namespace, String localname) */
    @Test
    public void testNamespaceAndLocalName1() throws Exception {
        m_xc = XmlObject.Factory.parse(xml1).newCursor();
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

    /** test toNextSibling(QName qName) */
    @Test
    public void testQName() throws Exception {
        m_xc = XmlObject.Factory.parse(xml1).newCursor();
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
