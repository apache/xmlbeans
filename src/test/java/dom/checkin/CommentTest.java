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

import dom.common.CharacterDataTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class CommentTest extends CharacterDataTest {


    public CommentTest() {
        sXml = Common.XML_FOO_COMMENT;
        sXmlNS = sXml;
    }

    @Test
    void testNodeName() {
        assertEquals("#comment", m_node.getNodeName());
    }

    @Test
    void testNodeValue() {
        assertEquals(" comment text ", m_node.getNodeValue());
    }

    @Test
    void testNodeType() {
        assertEquals(Node.COMMENT_NODE, m_node.getNodeType());
    }

    @Test
    void testNextSibling() {
        Node nxtSibling = m_node.getNextSibling();
        assertEquals("foo", nxtSibling.getLocalName());
        assertEquals(m_doc.getChildNodes().item(1), nxtSibling);
        assertEquals(1, nxtSibling.getChildNodes().getLength());
        assertEquals("text", ((Text) nxtSibling.getFirstChild()).getData());
    }

    @Test
    void testPreviousSibling() {
        Node prSibling = m_node.getPreviousSibling();
        assertNull(prSibling);
    }

    @Test
    void testParent() {
        Node parent = m_node.getParentNode();
        assertEquals(m_doc, parent);
    }

    @Test
    void testSetNodeValue() {
        m_node.setNodeValue("new comment");
        assertEquals("new comment", m_doc.getFirstChild().getNodeValue());
    }


    public void moveToNode() {
        m_node = m_doc.getFirstChild();

    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }
}
