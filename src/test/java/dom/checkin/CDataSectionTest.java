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
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;


public class CDataSectionTest extends CharacterDataTest {

    public CDataSectionTest() {
        sXml = "<script/>";
        /*
TODO:
sXml="<script>"+
            "<![CDATA["+
            "function matchwo(a,b){"+
            "return 0   }"+
            "]]>"+
            "<![CDATA[foobar]]>"+
            "</script>";
*/
    }

    @Test
    public void testNodeName() {
        assertEquals("#cdata-section", m_node.getNodeName());
    }

    @Test
    public void testNodeType() {
        assertEquals(Node.CDATA_SECTION_NODE, m_node.getNodeType());
    }

    @Test
    public void testNodeValue() {
        assertEquals("function matchwo(a,b){\"+\n\t    \"return 0   }",
                m_node.getNodeValue());
    }

    @Test
    public void testNextSibling() {
        Node nxtSibling = m_node.getNextSibling();
        assertEquals("foobar", nxtSibling.getNodeValue());

    }

    @Test
    public void testPreviousSibling() {
        Node prSibling = m_node.getPreviousSibling();
        assertEquals(null, prSibling);
    }

    @Test
    public void testParent() {
        Node parent = m_node.getParentNode();
        assertEquals(m_doc.getFirstChild(), parent);
        assertEquals("script", parent.getLocalName());
    }

    @Test
    public void testSetNodeValue() {
        m_node.setNodeValue("new CDATA");
        assertEquals("new CDATA", m_node.getNodeValue());
    }

    public void moveToNode() {
        m_node = m_doc.getDocumentElement();
        CDATASection c1 = m_doc.createCDATASection("function matchwo(a,b){\"+\n" +
                "\t    \"return 0   }");
        CDATASection c2 = m_doc.createCDATASection("foobar");
        m_node.appendChild(c1);
        m_node.appendChild(c2);
        m_node = m_node.getFirstChild();//function

    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();

    }
}
