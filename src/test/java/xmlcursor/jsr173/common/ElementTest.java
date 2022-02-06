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

public class ElementTest {

    private static XmlCursor cur() {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"),
            "val0");
        cur.insertNamespace("pre", "foons.bar.org");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
        cur.insertChars("some text");
        cur.toNextToken();
        cur.toNextToken();//end elt
        cur.insertElement("foo1");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertChars("\t");
        cur.insertElement("foo2");
        cur.insertChars("hooa");

        cur.toStartDoc();
        return cur;
    }


    @Test
    void testGetElementText() throws Exception {
        try (XmlCursor cur = cur()) {
            //first element?
            cur.toFirstChild();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals("some text", m_stream.getElementText());
            m_stream.close();
        }
    }

    @Test
    void testGetElementTextEmptyElt() throws Exception {
        try (XmlCursor cur = cur()) {
            //first element?
            cur.toFirstChild();
            cur.toNextSibling();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals("", m_stream.getElementText());
            m_stream.close();
        }
    }

    @Test
    void testGetElementTextMixedContent() throws Exception {
        try (XmlCursor cur = cur()) {
            //first element?
            cur.toFirstChild();
            cur.toNextSibling();
            cur.toNextSibling();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
            assertThrows(XMLStreamException.class, m_stream::getElementText, "Mixed content needs exception");
            m_stream.close();
        }
            //mixed content txt1, PI, COMMENT,txt2:
            //should coalesce txt1 & txt2
        try (XmlCursor cur = XmlObject.Factory.newInstance().newCursor()) {
            cur.toNextToken();
            cur.beginElement("foo");
            cur.insertChars("  \n ");
            cur.insertComment("My comment");
            cur.insertProcInst("xml-stylesheet", "http://foobar");
            cur.insertChars("txt1\t");
            cur.toStartDoc();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals("  \n txt1\t", m_stream.getElementText());
            m_stream.close();
        }
    }

    @Test
    void testGetNameAtStartElt() throws Exception {
        try (XmlCursor cur = cur()) {
            //first element
            cur.toFirstChild();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
            m_stream.close();
        }
    }

    @Test
    void testGetNameAtEndElt() throws Exception {
        try (XmlCursor cur = cur()) {
            cur.toFirstChild();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals(new QName("foo.org", "foo", ""), m_stream.getName());
            m_stream.close();
        }
    }

    @Test
    void testHasName() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            m_stream.next();
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertTrue(m_stream.hasName());
            m_stream.close();
        }
    }

    //call at a bad place..here just attr but should exhaust all
    @Test
    void testGetNameIllegal() throws Exception {
        try (XmlCursor cur = cur()) {
            //attr
            cur.toNextToken();
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertThrows(IllegalStateException.class, m_stream::getName, "getName illegal pos");
            assertFalse(m_stream.hasName());
            m_stream.close();
        }
    }
}