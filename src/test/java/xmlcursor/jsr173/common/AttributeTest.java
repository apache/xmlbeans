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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.junit.jupiter.api.Assertions.*;

public class AttributeTest {

    private XmlCursor cur() {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"),
            "val0");
        cur.insertAttributeWithValue(new QName("", "at1", "pre"), "val1");
        cur.insertNamespace("pre", "foons.bar.org");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
        cur.insertChars("some text");
        cur.toNextToken();
        cur.toNextToken();//end elt
        cur.insertProcInst("xml-stylesheet", "http://foobar");

        cur.toStartDoc();
        return cur;
    }

    @Test
    void testAttrEvent() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toNextToken();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.getEventType());
            assertEquals(1, m_stream.getAttributeCount());
            assertEquals(m_stream.getAttributeValue(0), m_stream.getAttributeValue("foo.org", "at0"));

            assertFalse(m_stream.hasNext());
            m_stream.close();
        }
    }

    @Test
    void testAttrMethodsAtAttr() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toNextToken();
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            //move 2 first attr
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.getEventType());
            assertEquals(1, m_stream.getAttributeCount());
            assertEquals(m_stream.getAttributeValue(0), m_stream.getAttributeValue("foo.org", "at0"));

            //Below methods tested at index 0 and last at index tests
            //getAttributeLocalName(int)
            //getAttributeName(int)
            //getAttributeNamespace(int)
            //getAttributePrefix(int)
            //getAttributeType(int)
            //getAttributeValue(int)

            m_stream.close();
        }

    }

    @Test
    void testAttrMethodsAtStartElt() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toFirstChild();
            cur.toNextSibling();
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertEquals(1, m_stream.getAttributeCount());
            assertTrue(m_stream.isStartElement());
            assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
            assertEquals(m_stream.getAttributeValue(0), "");
            assertEquals(m_stream.getAttributeValue(0), m_stream.getAttributeValue("", "localName"));

            m_stream.close();
        }
    }

    private static void assertIllegalState1(XMLStreamReader m_stream) {
        assertThrows(IllegalStateException.class, m_stream::getAttributeCount);
    }

    private static void assertIllegalState2(XMLStreamReader m_stream) {
        assertThrows(IllegalStateException.class, () -> m_stream.getAttributeValue(0));
    }

    @Test
    void testAttrMethodsAtNamespace() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toNextToken();
            cur.toNextToken();
            assertEquals(XmlCursor.TokenType.NAMESPACE, cur.toNextToken());
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertIllegalState1(m_stream);
            assertIllegalState2(m_stream);
            m_stream.close();
        }
    }

    //
//    java.lang.IllegalStateException - if this is not a START_ELEMENT or ATTRIBUTE
//
    @Test
    void testAttrMethodsAtEndElt() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toFirstChild();
            cur.toNextSibling();
            cur.toNextToken();
            cur.toNextToken();
            assertEquals(XmlCursor.TokenType.END, cur.toNextToken()); //toEnd
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertIllegalState1(m_stream);
            assertIllegalState2(m_stream);
            m_stream.close();
        }
    }

    @Test
    void testAttrMethodsAtEndDoc() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toFirstChild();
            cur.toNextSibling();
            cur.toNextToken();
            cur.toNextToken();
            cur.toNextToken();
            cur.toNextToken();
            assertEquals(XmlCursor.TokenType.ENDDOC, cur.toNextToken());
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertIllegalState1(m_stream);
            assertIllegalState2(m_stream);
            m_stream.close();
        }
    }

    @Test
    void testAttrMethodstAtText() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toFirstChild();
            cur.toNextSibling();
            cur.toNextToken();
            assertEquals(XmlCursor.TokenType.TEXT, cur.toNextToken()); //text
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertIllegalState1(m_stream);
            assertIllegalState2(m_stream);
            m_stream.close();
        }
    }

    @Test
    void testAttrMethodstAtPI() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toFirstChild();
            cur.toNextSibling();
            cur.toNextToken();
            cur.toNextToken();
            cur.toNextToken();
            assertEquals(XmlCursor.TokenType.PROCINST, cur.toNextToken());
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertIllegalState1(m_stream);
            assertIllegalState2(m_stream);
            m_stream.close();
        }
    }

    /**
     * verify index correctness for all index methods
     * tested w/ cursor positioned at first attr
     * //getAttributeLocalName(int)
     * //getAttributeName(int)
     * //getAttributeNamespace(int)
     * //getAttributePrefix(int)
     * //getAttributeType(int)
     * //getAttributeValue(int)
     */
    @Test
    void testAttrMethodsNegIndex() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeLocalName(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeName(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeNamespace(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributePrefix(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeType(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeValue(-1));

            m_stream.close();
        }
    }

    @Test
    void testAttrMethodsLargeIndex() throws XMLStreamException {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();

            final int pos = m_stream.getAttributeCount();
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeLocalName(pos));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeName(pos));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeNamespace(pos));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributePrefix(pos));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeType(pos));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getAttributeValue(pos));

            m_stream.close();
        }
    }

    @Test
    void testAttrMethods0Index() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());

            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
            assertEquals(1, m_stream.getAttributeCount());

            assertEquals("val0", m_stream.getAttributeValue(0));

            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());

            assertEquals("val1", m_stream.getAttributeValue(0));
            //why does this crash here????
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next()); //ns
            m_stream.next(); //elt
            assertEquals("", m_stream.getAttributeValue(0));
            m_stream.close();
        }
    }

    @Test
    void testIsAttributeSpecified() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());
            assertThrows(IllegalStateException.class, () -> m_stream.isAttributeSpecified(0));
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
            assertFalse(m_stream.isAttributeSpecified(0));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.isAttributeSpecified(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.isAttributeSpecified(2));

            m_stream.close();
        }
    }
}