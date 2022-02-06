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

package xmlcursor.jsr173.common;


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Methods tested
 * getLocalName
 * getName
 * getNamespaceContext
 * getNamespaceCount
 * getNamespacePrefix
 * getNamespaceURI  x 3
 * getPrefix
 */
public class NamespaceTest {

    private static XmlCursor cur() {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"),
            "val0");
        cur.insertNamespace("pre0", "bea.com");

        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertNamespace("pre", "foons.bar.org");
        cur.insertNamespace("pre1", "foons1.bar1.org1");
        cur.insertChars("some text");
        cur.toNextToken();
        cur.toNextToken();//end elt
        cur.insertProcInst("xml-stylesheet", "http://foobar");
        cur.insertChars("\t");
        cur.insertComment(" some comment ");

        cur.toStartDoc();

        return cur;
    }

    @Test
    void testGetLocalName() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertThrows(IllegalStateException.class, m_stream::getLocalName, "no name at startdoc");
            m_stream.next();
            m_stream.next();
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals("foo", m_stream.getLocalName());
            m_stream.next();
            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals("foo", m_stream.getLocalName());
            m_stream.close();
        }
    }

    @Test
    void testGetPrefix() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());
            assertThrows(IllegalStateException.class, m_stream::getPrefix, "no prefix here");

            //  assertFalse( m_stream.hasText() );
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getPrefix, "no prefix here");

            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getPrefix, "no prefix here");

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals("", m_stream.getPrefix());

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getPrefix, "no prefix here");

            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals("", m_stream.getPrefix());
            assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getPrefix, "no prefix here");

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());

            assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getPrefix, "no prefix here");

            m_stream.close();
        }
    }

    @Test
    void testGetNamespaceContext() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            m_stream.next();
            m_stream.next();
            m_stream.next();

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertNull(m_stream.getNamespaceContext().getPrefix("foo.bar"));
            assertNotNull(m_stream.getNamespaceContext().getPrefixes("foo.bar"));

            m_stream.close();
        }
    }

    /**
     * only valid at Tags and NS decl
     */
    @Test
    void testGetNamespaceCount() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertEquals(1, m_stream.getNamespaceCount());

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals(2, m_stream.getNamespaceCount());

            //java.lang.IllegalStateException
            // - if this is not a START_ELEMENT, END_ELEMENT or NAMESPACE

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getNamespaceCount, "can't do this on a txt node");

            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals(2, m_stream.getNamespaceCount());

            m_stream.close();
        }
    }

    /**
     * only valid at Tags and NS decl
     */
    @Test
    void testGetNamespacePrefix() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();

            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertEquals("pre0", m_stream.getNamespacePrefix(0));

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals("pre", m_stream.getNamespacePrefix(0));
            assertEquals("pre1", m_stream.getNamespacePrefix(1));
            //java.lang.IllegalStateException
            // - if this is not a START_ELEMENT, END_ELEMENT or NAMESPACE

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.getEventType());
            assertThrows(IllegalStateException.class, () -> m_stream.getNamespacePrefix(0), "can't do this on a txt node");

            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals("pre", m_stream.getNamespacePrefix(0));
            assertEquals("pre1", m_stream.getNamespacePrefix(1));
            m_stream.close();
        }
    }

    @Test
    void testGetNamespacePrefixNeg() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getNamespacePrefix(-1));
            m_stream.close();
        }
    }

    @Test
    void testGetNamespacePrefixLarge() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getNamespacePrefix(3));
            m_stream.close();
        }
    }

    //3 methods here
    @Test
    void testGetNamespaceURI() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertEquals("bea.com", m_stream.getNamespaceURI(0));

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals("foons.bar.org", m_stream.getNamespaceURI(0));
            assertEquals("foons1.bar1.org1", m_stream.getNamespaceURI(1));
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertThrows(IllegalStateException.class, () -> m_stream.getNamespaceURI(0), "can't do this on a txt node");

            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals("foons.bar.org", m_stream.getNamespaceURI(0));
            assertEquals("foons1.bar1.org1", m_stream.getNamespaceURI(1));

            m_stream.close();
        }
    }

    @Test
    void testGetNamespaceURINeg() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getNamespaceURI(-1));
            m_stream.close();
        }
    }

    @Test
    void testGetNamespaceURILarge() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getNamespaceURI(3));
            m_stream.close();
        }
    }
}

