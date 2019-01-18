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


package dom.checkin;

import dom.common.NodeTest;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import static org.junit.Assert.*;


public class PITest extends NodeTest {

    public PITest() {
        sXml = "<bar><?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo>text</foo></bar>";
        //inspired by Common.XML_FOO_PROCINST;
    }

    @Test
    public void testNodeName() {
        assertEquals("xml-stylesheet", m_node.getNodeName());
    }

    @Test
    public void testLocalName() {
        assertEquals("xml-stylesheet", m_node.getNodeName());
    }

    @Test
    public void testNodeType() {
        assertEquals(Node.PROCESSING_INSTRUCTION_NODE, m_node.getNodeType());
    }

    @Test
    public void testNodeValue() {
        assertEquals(
                "type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"",
                m_node.getNodeValue());
    }

    @Test
    public void testNextSibling() {
        Node nxtSibling = m_node.getNextSibling();
        assertEquals("foo", nxtSibling.getNodeName());

    }

    @Test
    public void testSetNodeValue() {
        String sNewVal = "type=\"text/xsl\" xmlns=\"http://xbean.foo.org\"";
        m_node.setNodeValue(sNewVal);
        assertEquals(sNewVal, m_node.getNodeValue());

    }

    @Test
    public void testPreviousSibling() {
        Node prSibling = m_node.getPreviousSibling();
        assertNull(prSibling);
    }

    @Test
    public void testParent() {
        Node parent = m_node.getParentNode();
        assertEquals(m_doc.getFirstChild(), parent);
        assertEquals("bar", parent.getLocalName());
    }

    @Test
    public void testGetData() {
        assertEquals(
                "type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"",
                ((ProcessingInstruction) m_node).getData());
    }

    @Test
    public void testGetTarget() {
        assertEquals("xml-stylesheet",
                ((ProcessingInstruction) m_node).getTarget());
    }

    public void moveToNode() {
        m_node = m_doc.getDocumentElement().getFirstChild();//pi
        assertNotNull(m_node);
    }

    //TODO: Test PI with funky but legal chatacters in the name, eg. :
    @Test
    public void testPiTargetChars() {
        ProcessingInstruction node = m_doc.createProcessingInstruction(
                "foo:123-_", "some body");
        m_node.getParentNode().appendChild(node);
        m_node = m_node.getParentNode().getLastChild();
        assertEquals("foo:123-_", ((ProcessingInstruction) m_node).getTarget());
    }

    //TODO: Test Illegal PI Targets: xml target, starting with a digit
    @Test
    public void testPiTargetIllegalChars() {
        ProcessingInstruction node;
        try {
            node =
                    m_doc.createProcessingInstruction("7foo:?123-&",
                            "some body");
            fail("Can't start w/ a digit");
        }
        catch (DOMException e) {
            assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
        }

        try {
            node = m_doc.createProcessingInstruction("xml", "foo");
            fail("Can't be xml");
        }
        catch (DOMException e) {
            assertEquals(DOMException.INVALID_CHARACTER_ERR, e.code);
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }
}
