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
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Methods tested
 * close()
 * getCharacterEncodingScheme()
 * getEncoding()
 * getLocation
 * getProperty
 * getVersion
 * isStandalone
 * require
 * standaloneSet
 */
public class GeneralMethodsTest {

    private static XmlCursor cur() throws Exception {
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

        cur.toStartDoc();

        return cur;
    }

    @Test
    void testClose() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.close();
            m_stream.next();
        }
    }

    @Test
    void testAll() throws Exception {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream1 = cur.newXMLStreamReader();
            XmlDocumentProperties opt = cur.documentProperties();
            opt.setEncoding("utf-8");
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            m_stream.next();
            m_stream.next();
            assertEquals("utf-8", m_stream.getCharacterEncodingScheme());
            assertEquals("utf-8", m_stream1.getCharacterEncodingScheme());
            assertNull(m_stream1.getEncoding());
            //TODO: why is this still -1???
            Location l = m_stream.getLocation();
            assertEquals(-1, l.getCharacterOffset());
            assertEquals(-1, l.getColumnNumber());
            assertEquals(-1, l.getLineNumber());
            assertNull(l.getPublicId());
            assertNull(l.getSystemId());


            m_stream.getVersion();
            assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.getEventType());
            //only elements can have localnames
            assertThrows(IllegalStateException.class, () -> m_stream.require(10, "", "at1"));

            m_stream.next();
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            m_stream.require(1, "foo.org", "foo");
            assertThrows(XMLStreamException.class, () -> m_stream.require(10, "", ""));

            m_stream.close();
        }
    }
}