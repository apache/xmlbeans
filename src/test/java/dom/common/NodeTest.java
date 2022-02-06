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

package dom.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public abstract class NodeTest implements TestSetup {
    protected Node m_node;

    protected Document m_doc;
    protected String sXml;

    protected Document m_docNS;
    protected String sXmlNS;
    //attributes

    @Test
    protected void testOwnerDocument() {
        assertEquals(m_doc, m_node.getOwnerDocument());
    }

    @Test
    protected void testPrefix() {
        assertNotNull(m_node);
        assertNull(m_node.getPrefix());
        // assertEquals("", m_node.getPrefix());
    }

    @Test
    protected void testNamespaceUri() {
        assertNotNull(m_node);
        assertNull(m_node.getNamespaceURI());
        //assertEquals("", m_node.getNamespaceURI());
    }

    @Test
    protected void testLocalName() {
        assertNotNull(m_node);
        assertNull(m_node.getLocalName());
        // assertEquals("", m_node.getLocalName());
    }

    //0 length list as of API
    @Test
    protected void testGetChildNodes() {
        assertEquals(0, m_node.getChildNodes().getLength());
    }

    @Test
    protected void testFirstChild() {
        assertNull(m_node.getFirstChild());
    }

    @Test
    protected void testLastChild() {
        assertNull(m_node.getLastChild());
    }

    /**
     * pathologic cases: newChild is m_node or an ancestor
     * newChild is from a different document
     * newChild is not allowed at this pos
     */
    protected void testAppendChild(Node newChild) {
        Node inserted = m_node.appendChild(newChild);
        if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            assertTrue(compareNodeListPrefix(newChild.getChildNodes(), m_node.getChildNodes()));
        } else {
            assertEquals(inserted, m_node.getLastChild());
        }
        if (isInTree(m_node, newChild)) //new child is in the tree
            //$NOTE: assert the child is removed first
            ;
    }

    /**
     * $NOTE:
     * override for Element;
     * override for Attribute
     * $TODO: ER results in a mutable copy
     */
    @Test
    protected void testCloneNode() {

        Node m_clone;
        m_clone = m_node.cloneNode(false);
        assertTrue(DomUtils.compareNodesShallow(m_node, m_clone));
//         assertEquals(true, DomUtils.compareNodeTreePtr(m_clone.getChildNodes(),m_node.getChildNodes())); //ptr eq for ch.
        assertNotSame(m_clone, m_node);


        m_clone = m_node.cloneNode(true);
        assertTrue(DomUtils.compareNodesDeep(m_node, m_clone)); //deep clone: should do for whole tree, not just ch.
        assertNotSame(m_clone, m_node);

        assertNull(m_clone.getParentNode());
    }

    protected void testInsertBefore(Node newChild, Node refChild) {

        int newChPos = getChildPos(m_node, newChild);
        int pos = getChildPos(m_node, refChild);
        Node prevParent = null;
        if (newChPos > -1) {
            prevParent = newChild.getParentNode();
        }
        NodeList childNodes = m_node.getChildNodes();
        int nOrigChildNum = childNodes.getLength(); //get it now, List is live


        if (newChild == null) {
            assertThrows(IllegalArgumentException.class, () -> m_node.insertBefore(newChild, refChild), "Inserting null");
            return;
        }
        Node inserted = m_node.insertBefore(newChild, refChild);


        if (refChild == null) {
            assertEquals(inserted, m_node.getLastChild());
        } else if (pos == -1) {
            //would have thrown exc
            Assertions.fail("Inserting after fake child");
        } else if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            assertTrue(compareNodeListPrefix(newChild.getChildNodes(), m_node.getChildNodes()));
        } else if (newChPos != -1) {
            //new child is in the tree
            //assert the child is removed first
            assertNotEquals(inserted.getParentNode(), prevParent);
        } else {
            assertEquals(newChild, childNodes.item(pos));
            assertEquals(nOrigChildNum + 1, m_node.getChildNodes().getLength());
        }

    }

    /**
     * $NOTE: override for element
     */
    @Test
    protected void testGetAttributes() {
        assertNull(m_node.getAttributes());
    }

    @Test
    protected void testHasChildNodes() {
        int i = m_node.getChildNodes().getLength();
        if (i > 0) {
            assertTrue(m_node.hasChildNodes());
        } else {
            assertFalse(m_node.hasChildNodes());
        }
    }

    //Override for Element
    @Test
    protected void testHasAttributes() {
        assertFalse(m_node.hasAttributes());
    }

    @Test
    protected void testIsSupported() {
        String[] features = new String[]{
            "Core", "XML", "Events", "MutationEvents", "Range", "Traversal", "HTML", "Views", "StyleSheets", "CSS", "CSS2", "UIEvents", "HTMLEvents"
        };
        for (String feature : features) {
            assertEquals("Core,XML".contains(feature) , m_node.isSupported(feature, "2.0"));
        }

    }

    void testRemoveChild(Node removed) {
        int pos = getChildPos(m_node, removed);
        int len = m_node.getChildNodes().getLength();
        if (removed == null) {
            DOMException de = assertThrows(DOMException.class, () -> m_node.removeChild(removed), "Should not be Removing non-existing node");
        } else if (pos == -1) {
            throw assertThrows(DOMException.class, () -> m_node.removeChild(removed), "Removing non-existing node");
        } else {
            m_node.removeChild(removed);
            assertEquals(len - 1, m_node.getChildNodes().getLength());
        }
    }

    /**
     * pathological cases:
     * node is DocFrag
     * node is already in tree
     */
    protected void testReplaceChild(final Node newChild, Node oldChild) {
        int pos = getChildPos(m_node, oldChild);
        boolean existing = isInTree(m_doc.getDocumentElement(), newChild); //new Child has a parent

        int len = m_node.getChildNodes().getLength();


        if (newChild == null) {
            assertThrows(IllegalArgumentException.class, () -> m_node.replaceChild(newChild, oldChild), "Inserting null");
        } else if (pos == -1) {
            DOMException de = assertThrows(DOMException.class, () -> m_node.replaceChild(newChild, oldChild), "Replacing non-existing node");
            if (DOMException.NOT_FOUND_ERR != de.code) {
                throw de;
            }
        } else if (existing) {
            Node oldParent = newChild.getParentNode();
            NodeList old = m_node.getChildNodes();
            assertEquals(oldChild, m_node.replaceChild(newChild, oldChild));
            assertNotEquals(newChild.getParentNode(), oldParent);
        } else if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            int new_len = newChild.getChildNodes().getLength();
            assertEquals(oldChild, m_node.replaceChild(newChild, oldChild));
            assertEquals(new_len + len - 1, m_node.getChildNodes().getLength());//new+old-one replaced
        } else {
            m_node.replaceChild(newChild, oldChild);
        }


    }

    //$NOTE:override for element and attribute
    @Test
    protected void testSetPrefix() {
        //any prefix here is invalid
        String val = "blah"; //Eric's default
        DOMException de = assertThrows(DOMException.class, () -> m_node.setPrefix(val), "set prefix only works for at/elt");
        assertEquals(DOMException.NAMESPACE_ERR, de.code);
    }

    private static int getChildPos(Node node, Node child) {
        if (child == null) {
            return -1;
        }
        NodeList ch = node.getChildNodes();
        for (int i = 0; i < ch.getLength(); i++) {
            if (ch.item(i) == child) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isInTree(Node root, Node find) {
        if (find == null) {
            return false;
        }
        if (root == null) {
            return false;
        }
        if (root == find) {
            return true;
        }
        NodeList ch = root.getChildNodes();
        boolean temp_res = false;
        for (int i = 0; i < ch.getLength(); i++) {
            temp_res = temp_res || isInTree(ch.item(i), find);
        }
        return temp_res;
    }

    protected static boolean compareNodeList(NodeList l1, NodeList l2) {
        if (l1.getLength() != l2.getLength()) {
            return false;
        }
        for (int i = 0; i < l1.getLength(); i++) {
            //pointer eq
            if (l1.item(i) != l2.item(i)) {
                return false;
            }
        }
        return true;
    }

    //l1 is a prefix of l2
    private static boolean compareNodeListPrefix(NodeList l1, NodeList l2) {
        if (l1.getLength() > l2.getLength()) {
            return false;
        }
        for (int i = 0; i < l1.getLength(); i++) {
            //pointer eq
            if (l1.item(i) != l2.item(i)) {
                return false;
            }
        }
        return true;
    }

    public void loadSync() {
        _loader = Loader.getLoader();

        if (sXml == null && sXmlNS == null) {
            throw new IllegalArgumentException("Test bug : Initialize xml strings");
        }
        m_doc = _loader.loadSync(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0) {
            m_docNS = _loader.loadSync(sXmlNS);
        }

    }


    public static Node getApacheNode(String sXml, boolean namespace, char type)
        throws Exception {
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.parse(new InputSource(new StringReader(sXml)));
        Document doc = parser.getDocument();

        String name = "apache_node";
        String nsname = "pre:apache_node";
        String uri = "uri:apache:test";

        switch (type) {
            case 'A':
                if (namespace) {
                    return doc.createAttributeNS(uri, nsname);
                } else {
                    return doc.createAttribute(name);
                }
            case 'E':
                if (namespace) {
                    return doc.createElementNS(uri, nsname);
                } else {
                    return doc.createElement(name);
                }
            default:
                return doc.createTextNode(name);

        }

    }

    //exposing a node for other tests...saver in particular
    public Node getNode() {
        return m_node;
    }

    @BeforeEach
    public void setUp() throws Exception {
        //m_doc=(org.w3c.dom.Document)org.apache.xmlbeans.XmlObject.Factory.parse(xml).newDomNode();
        _loader = Loader.getLoader();

        if (sXml == null && sXmlNS == null) {
            throw new IllegalArgumentException("Test bug : Initialize xml strings");
        }
        m_doc = _loader.load(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0) {
            m_docNS = _loader.load(sXmlNS);
        }
    }

    private Loader _loader;
}

