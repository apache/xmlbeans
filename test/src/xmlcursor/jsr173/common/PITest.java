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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Methods tested:
 * getPIData
 * getPITarget
 */
@Ignore("abstract class")
public abstract class PITest {

    private XMLStreamReader m_stream;

    public abstract XMLStreamReader getStream(XmlCursor c) throws Exception;

    @Test
    public void testGetPiData() throws XMLStreamException {
        assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION,
            m_stream.next());
        assertEquals("http://foobar", m_stream.getPIData());
        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertNull(m_stream.getPIData());
    }

    @Test
    public void testGetPiTarget() throws XMLStreamException {
        assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION,
            m_stream.next());
        assertEquals("xml-stylesheet", m_stream.getPITarget());
        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertNull(m_stream.getPITarget());
    }

    @Before
    public void setUp() throws Exception {
        XmlCursor cur = XmlObject.Factory.newInstance().newCursor();
        cur.toNextToken();
        cur.insertProcInst("xml-stylesheet", "http://foobar");
        cur.insertElement("foobar");
        cur.toStartDoc();
        m_stream = getStream(cur);
    }

    @After
    public void tearDown() throws Exception {
        if (m_stream != null)
            m_stream.close();
    }
}
