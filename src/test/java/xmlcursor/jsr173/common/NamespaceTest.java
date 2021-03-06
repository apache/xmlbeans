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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import static org.junit.Assert.*;


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
@Ignore("abstract class")
public abstract class NamespaceTest {

    private XMLStreamReader m_stream;

    //only valid at TAGs and ER
    //TODO: ER

    public abstract XMLStreamReader getStream(XmlCursor c) throws Exception;

    @Test
    public void testGetLocalName() throws Exception {
        try {
            m_stream.getLocalName();
            fail("no name at startdoc");
        } catch (IllegalStateException e) {
        }

        m_stream.next();
        m_stream.next();
        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertEquals("foo", m_stream.getLocalName());
        m_stream.next();
        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertEquals("foo", m_stream.getLocalName());
    }

    @Test
    public void testGetName() {
        //test in Element--only valid for Element events
    }

    @Test
    public void testGetPrefix() throws Exception {
        assertEquals(XMLStreamConstants.START_DOCUMENT,
            m_stream.getEventType());

        try {
            assertNull(m_stream.getPrefix());
            fail("no prefix here");
        } catch (IllegalStateException e) {
        }
        //  assertFalse( m_stream.hasText() );
        assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
        try {
            assertNull(m_stream.getPrefix());
            fail("no prefix here");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        try {
            assertNull(m_stream.getPrefix());
            fail("no prefix here");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertEquals("", m_stream.getPrefix());

        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        try {
            assertNull(m_stream.getPrefix());
            fail("no prefix here");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertEquals("", m_stream.getPrefix());
        assertEquals(XMLStreamConstants.PROCESSING_INSTRUCTION, m_stream.next());
        try {
            assertNull(m_stream.getPrefix());
            fail("no prefix here");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());

        assertEquals(XMLStreamConstants.COMMENT, m_stream.next());
        try {
            assertNull(m_stream.getPrefix());
            fail("no prefix here");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testGetNamespaceContext() throws Exception {
        //assertEquals(XMLStreamConstants.ATTRIBUTE, m_stream.next());
        //assertEquals("", m_stream.getNamespaceContext().getNamespaceURI(""));
        //assertEquals("", m_stream.getNamespaceContext().getPrefix("foo.org"));
        //java.util.Iterator it = m_stream.getNamespaceContext().getPrefixes("foo.bar");

        m_stream.next();
        m_stream.next();
        m_stream.next();

        assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
        //assertEquals("", m_stream.getNamespaceContext().getNamespaceURI(""))  ;
        //assertEquals("", m_stream.getNamespaceContext().getPrefix("foo.bar"))  ;
        assertNull(m_stream.getNamespaceContext().getPrefix("foo.bar"));
        java.util.Iterator it = m_stream.getNamespaceContext().getPrefixes("foo.bar");
    }

    /**
     * only valid at Tags and NS decl
     */
    @Test
    public void testGetNamespaceCount() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        assertEquals(1, m_stream.getNamespaceCount());

        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertEquals(2, m_stream.getNamespaceCount());

        //java.lang.IllegalStateException
        // - if this is not a START_ELEMENT, END_ELEMENT or NAMESPACE
        try {
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            m_stream.getNamespaceCount();
            fail("can't do this on a txt node");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertEquals(2, m_stream.getNamespaceCount());
    }

    /**
     * only valid at Tags and NS decl
     */
    @Test
    public void testGetNamespacePrefix() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        assertEquals("pre0", m_stream.getNamespacePrefix(0));

        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertEquals("pre", m_stream.getNamespacePrefix(0));
        assertEquals("pre1", m_stream.getNamespacePrefix(1));
        //java.lang.IllegalStateException
        // - if this is not a START_ELEMENT, END_ELEMENT or NAMESPACE

        try {
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.getEventType());
            m_stream.getNamespacePrefix(0);
            fail("can't do this on a txt node");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertEquals("pre", m_stream.getNamespacePrefix(0));
        assertEquals("pre1", m_stream.getNamespacePrefix(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNamespacePrefixNeg() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        m_stream.getNamespacePrefix(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNamespacePrefixLarge() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        assertEquals("", m_stream.getNamespacePrefix(3));
    }

    //3 methods here
    @Test
    public void testGetNamespaceURI() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        assertEquals("bea.com", m_stream.getNamespaceURI(0));

        assertEquals(XMLStreamConstants.START_ELEMENT, m_stream.next());
        assertEquals("foons.bar.org", m_stream.getNamespaceURI(0));
        assertEquals("foons1.bar1.org1", m_stream.getNamespaceURI(1));
        try {
            assertEquals(XMLStreamConstants.CHARACTERS, m_stream.next());
            m_stream.getNamespaceURI(0);
            fail("can't do this on a txt node");
        } catch (IllegalStateException e) {
        }

        assertEquals(XMLStreamConstants.END_ELEMENT, m_stream.next());
        assertEquals("foons.bar.org", m_stream.getNamespaceURI(0));
        assertEquals("foons1.bar1.org1", m_stream.getNamespaceURI(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNamespaceURINeg() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        assertEquals("", m_stream.getNamespaceURI(-1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNamespaceURILarge() throws Exception {
        m_stream.next();
        assertEquals(XMLStreamConstants.NAMESPACE, m_stream.next());
        assertEquals("", m_stream.getNamespaceURI(3));
    }

    @Before
    public void setUp() throws Exception {
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

        m_stream = getStream(cur);
    }

    @After
    public void tearDown() throws Exception {
        if (m_stream != null)
            m_stream.close();
    }
}

