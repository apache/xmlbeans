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


import dom.common.Loader;
import dom.common.TestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NodeListTest implements TestSetup {

    Document m_doc;
    Document m_docNS;
    Node m_node;
    NodeList m_nodeList;
    String sXml = "<foo><ch0>val0</ch0><ch1>val1</ch1><ch2>val2</ch2><ch3>val3</ch3><ch4>val4</ch4></foo>";
    int nCount = 5;

    @Test
    void testLength() {
        assertEquals(m_nodeList.getLength(), nCount);
    }

    @Test
    void testItem() {
        for (int i = 0; i < m_nodeList.getLength(); i++)
            assertEquals("ch" + i, m_nodeList.item(i).getNodeName());
    }

    @Test
    void testItemNeg() {
        assertNull(m_nodeList.item(-1));
    }

    @Test
    void testItemLarge() {
        assertNull(m_nodeList.item(nCount + 1));
    }

    @Test
    void voidTestLive() {
        m_node.removeChild(m_nodeList.item(1));//"ch1"
        assertEquals(m_nodeList.getLength(), nCount - 1);
        assertEquals("ch2", m_nodeList.item(1).getNodeName());
    }

    @Test
    void moveToNode() {
        m_node = m_doc.getFirstChild();
        m_nodeList = m_node.getChildNodes();
    }

    public void loadSync() throws Exception {
        _loader = Loader.getLoader();
        m_doc = _loader.loadSync(sXml);

    }

    @BeforeEach
    public void setUp() throws Exception {
        _loader = Loader.getLoader();
        m_doc = (org.w3c.dom.Document) _loader.load(sXml);
        moveToNode();
    }

    private Loader _loader;
}
