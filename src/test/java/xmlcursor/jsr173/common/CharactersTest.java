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

/**
 * Methods tested
 * getText
 * getTextCharacters  x 2
 * getTextLength
 * getTextStart
 * hasText
 * Token Types should be DTD, ER, Chars, Comment, Space
 * currently DTD and ER are Not Impl
 */
public class CharactersTest {


    private static XmlCursor cur() {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();

        //   cur.insertAttributeWithValue(new QName("foo.org", "at0", "pre"), "val0");
        cur.insertComment(" some comment ");
        cur.beginElement(new QName("foo.org", "foo", ""));
        cur.insertAttribute("localName");
        cur.insertChars("some text");
        cur.insertElement("foo2");
        cur.toNextToken(); //close foo elt
        cur.insertChars("\t");
        cur.toStartDoc();

        return cur;
    }

    @Test
    void testHasText() throws XMLStreamException {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());

            // assertEquals( XMLStreamConstants.ATTRIBUTE, m_stream.next()  );
            //  assertFalse( m_stream.hasText() );

            assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
            assertTrue(m_stream.hasText());

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertFalse(m_stream.hasText());

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertTrue(m_stream.hasText());

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertFalse(m_stream.hasText());
            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertFalse(m_stream.hasText());
            assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
            assertFalse(m_stream.hasText());

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
//           assertTrue(  m_stream.isWhiteSpace());
            assertTrue(m_stream.hasText());

            m_stream.close();
        }
    }

    //also testing getTextStart and getTextLength
    @Test
    void testGetTextCharacters() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();

            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());
            assertThrows(IllegalStateException.class, m_stream::getTextLength);

            assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
            char[] result = m_stream.getTextCharacters();
            assertEquals(" some comment ", new String(result).substring(m_stream.getTextStart(),
                m_stream.getTextLength()));

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getTextLength);

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            result = m_stream.getTextCharacters();
            assertEquals("some text", new String(result).substring(m_stream.getTextStart(), m_stream.getTextLength()));

            m_stream.next();
            m_stream.next();//skip empty elt
            m_stream.next(); //end foo
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            result = m_stream.getTextCharacters();
            assertEquals("\t", new String(result).substring(m_stream.getTextStart(), m_stream.getTextLength()));
            m_stream.next();
            assertThrows(IllegalStateException.class, m_stream::getTextLength);

            m_stream.close();
        }

    }

    @Test
    void testGetTextCharactersBufferNegStart() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getTextCharacters(-1, new char[10], 12, 12));
            m_stream.close();
        }
    }

    @Test
    void testGetTextCharactersBufferNull() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertThrows(NullPointerException.class, () -> m_stream.getTextCharacters(0, null, 12, 12));
            m_stream.close();
        }
    }

    @Test
    void testGetTextCharactersLargeSrcOff() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getTextCharacters(110, new char[10], 0, 9));
            m_stream.close();
        }
    }

    @Test
    void testGetTextCharactersLargeTrgOff() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getTextCharacters(110, new char[10], 10, 9));
            m_stream.close();
        }
    }

    @Test
    void testGetTextCharactersLargeLen() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            char[] buff = new char[9];
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getTextCharacters(0, buff, 0, 30));
            m_stream.close();
        }
    }

    //off+len past end
    @Test
    void testGetTextCharactersLargeSum() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            char[] buff = new char[9];
            assertThrows(IndexOutOfBoundsException.class, () -> m_stream.getTextCharacters(0, buff, 3, 10));
            m_stream.close();
        }
    }

    @Test
    void testGetText() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.START_DOCUMENT, m_stream.getEventType());
            assertThrows(IllegalStateException.class, m_stream::getText);

            assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
            String result = m_stream.getText();
            assertEquals(" some comment ", result);

            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertThrows(IllegalStateException.class, m_stream::getText);

            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            result = m_stream.getText();
            assertEquals("some text", result);

            m_stream.next();
            m_stream.next();//skip empty elt
            m_stream.next(); //end foo
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            result = m_stream.getText();
            assertEquals("\t", result);
            m_stream.next();
            assertThrows(IllegalStateException.class, m_stream::getText);

            m_stream.close();
        }
    }
}