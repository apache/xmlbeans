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


package xmltokensource.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xmlcursor.common.BasicCursorTestCase;

import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class NewDomNodeTest extends BasicCursorTestCase {
    public static final String DOC_FRAGMENT = "#document-fragment";
    private static final String DOC = "#document";
    private XmlOptions m_map = new XmlOptions();

    @Test
    public void testNewDomNode() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo>01234   <bar>text</bar>   chars </foo>");
        Node doc = m_xo.newDomNode();
        assertEquals(DOC, doc.getNodeName());
        NodeList nl = doc.getChildNodes();
        assertEquals(1, nl.getLength());
        Node node = nl.item(0);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        assertEquals("foo", node.getLocalName());
        nl = node.getChildNodes();
        assertEquals(3, nl.getLength());
        node = nl.item(0);
        assertEquals(Node.TEXT_NODE, node.getNodeType());
        assertEquals("01234   ", node.getNodeValue());
        node = nl.item(1);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        assertEquals("bar", node.getLocalName());
        node = nl.item(2);
        assertEquals(Node.TEXT_NODE, node.getNodeType());
        assertEquals("   chars ", node.getNodeValue());
        // dive into bar
        nl = nl.item(1).getChildNodes();
        assertEquals(1, nl.getLength());
        node = nl.item(0);
        assertEquals(Node.TEXT_NODE, node.getNodeType());
        assertEquals("text", node.getNodeValue());
    }

    @Test
    public void testNewDomNodeWithNamespace() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo xmlns=\"ns\">01234   <bar>text</bar>   chars </foo>");
        Node doc = m_xo.newDomNode();
        assertNotNull(doc);
        assertEquals(DOC, doc.getNodeName());
    }

    @Test
    public void testNewDomNodeWithOptions() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo attr=\"val\" xmlns=\"ns\">01234   <bar>text</bar>   chars </foo>");
        XmlOptions map = new XmlOptions();
        map.setLoadStripComments();
        map.setLoadReplaceDocumentElement(new QName(""));
        map.setSaveNamespacesFirst();
        Node doc = m_xo.newDomNode(map);
        assertEquals(DOC, doc.getNodeName());
        NodeList nl = doc.getChildNodes();
        assertEquals(1, nl.getLength());
        Node node = nl.item(0);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        assertEquals("foo", node.getLocalName());
        nl = node.getChildNodes();
        assertEquals(3, nl.getLength());
        node = nl.item(0);
        assertEquals(Node.TEXT_NODE, node.getNodeType());
        assertEquals("01234   ", node.getNodeValue());
        node = nl.item(1);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        assertEquals("bar", node.getLocalName());
        node = nl.item(2);
        assertEquals(Node.TEXT_NODE, node.getNodeType());
        assertEquals("   chars ", node.getNodeValue());
        // dive into bar
        nl = nl.item(1).getChildNodes();
        assertEquals(1, nl.getLength());
        node = nl.item(0);
        assertEquals(Node.TEXT_NODE, node.getNodeType());
        assertEquals("text", node.getNodeValue());
    }

    @Test
    public void testNewDomNodeRoundTrip() throws Exception {
        m_xo = XmlObject.Factory.parse("<foo>01234   <bar>text</bar>   chars </foo>");
        Node doc = m_xo.newDomNode();
        assertNotNull(doc);
        XmlObject xo = XmlObject.Factory.parse(doc);
        m_xc = m_xo.newCursor();
        try (XmlCursor xc1 = xo.newCursor()) {
            compareDocTokens(m_xc, xc1);
        }
    }
}

