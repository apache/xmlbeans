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
 * This class tests next, hasNext, nextTag,getEventType and isXXX methods
 */
public class IsXXXTest {

    private static XmlCursor cur() {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"), "val0");
        cur.insertAttributeWithValue(new QName("", "at1", "pre"), "val1");
        cur.insertNamespace("pre", "foons.bar.org");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
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
    void testAll() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.getEventType());
            assertFalse(m_stream.isAttributeSpecified(0));

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.getEventType());
            assertFalse(m_stream.isAttributeSpecified(0));

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
            assertEquals(XMLStreamConstants.NAMESPACE, m_stream.getEventType());
            assertFalse(m_stream.isCharacters());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.getEventType());
            assertTrue(m_stream.isStartElement());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.getEventType());
            assertTrue(m_stream.isCharacters());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.getEventType());
            assertTrue(m_stream.isEndElement());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, m_stream.next());
            assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, m_stream.getEventType());


            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.getEventType());
            assertTrue(m_stream.isCharacters());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
            assertEquals(XMLStreamConstants.COMMENT, m_stream.getEventType());
            assertFalse(m_stream.isCharacters());

            assertTrue(m_stream.hasNext());
            assertEquals(XMLStreamConstants.END_DOCUMENT, m_stream.next());
            assertEquals(XMLStreamConstants.END_DOCUMENT, m_stream.getEventType());

            assertFalse(m_stream.hasNext());

            m_stream.close();
        }
    }
}