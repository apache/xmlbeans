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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Methods tested:
 * getPIData
 * getPITarget
 */
public class PITest {


    private static XmlCursor cur() {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();
        cur.insertProcInst("xml-stylesheet", "http://foobar");
        cur.insertElement("foobar");
        cur.toStartDoc();
        return cur;
    }

    @Test
    void testGetPiData() throws XMLStreamException {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, m_stream.next());
            assertEquals("http://foobar", m_stream.getPIData());
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertNull(m_stream.getPIData());
            m_stream.close();
        }
    }

    @Test
    void testGetPiTarget() throws XMLStreamException {
        try (XmlCursor cur = cur()) {
            XMLStreamReader m_stream = cur.newXMLStreamReader();
            assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, m_stream.next());
            assertEquals("xml-stylesheet", m_stream.getPITarget());
            assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
            assertNull(m_stream.getPITarget());
            m_stream.close();
        }
    }
}
