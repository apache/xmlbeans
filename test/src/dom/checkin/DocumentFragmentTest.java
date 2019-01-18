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

import dom.common.NodeWithChildrenTest;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import static org.junit.Assert.*;


public class DocumentFragmentTest extends NodeWithChildrenTest {


    public DocumentFragmentTest() {
        sXml =
            "<foo at0=\"val0\" a" +
            "t1=\"val1\" at2=\"val2\" at3=\"val3\" at4=\"val4\"><bar bat0=\"val0\"/></foo>";

        sXmlNS =
            "<foo xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\"/>";
    }

    @Test
    public void testNodeName() {
        assertEquals("#document-fragment", m_node.getNodeName());
    }

    @Test
    public void testNodeType() {
        assertEquals(Node.DOCUMENT_FRAGMENT_NODE, m_node.getNodeType());
    }

    @Test
    public void testNodeValue() {
        assertNull(m_node.getNodeValue());
    }

    @Test
    public void testNextSibling() {
        assertNull(m_node.getNextSibling());
    }

    @Test
    public void testPreviousSibling() {
        assertNotNull(m_node);
        assertNull(m_node.getPreviousSibling());
    }

    @Test
    public void testParent() {
        assertNull(m_node.getParentNode());
    }

    @Test
    public void testGetChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    public void testFirstChild() {
        assertEquals("foo", m_node.getFirstChild().getNodeName());
    }

    @Test
    public void testLastChild() {
        assertEquals("foo", m_node.getLastChild().getNodeName());
    }

    @Test
    public void testInsertExisitingNode() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node)
            child = m_doc.getLastChild();
        super.testInsertExistingNode(child);
    }

    @Test
    public void testAppendChildExisting() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node)
            child = m_doc.getLastChild();
        //if still the same, SOL
        super.testAppendChildExisting(child);
    }
        
    public void moveToNode() {
        m_node = m_doc.createDocumentFragment();
        m_node.appendChild(m_doc.createElement("foo"));

    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }
}
