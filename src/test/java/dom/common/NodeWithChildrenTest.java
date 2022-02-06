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

import org.junit.jupiter.api.Test;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class NodeWithChildrenTest extends NodeTest  {

    @Test
    void testRemoveChildEnd() {
        Node node = m_node.getLastChild();
        super.testRemoveChild(node);

    }

    @Test
    void testRemoveChild() {
        NodeList children = m_node.getChildNodes();
        int pos = children.getLength() / 2;
        Node node = children.item(pos);
        super.testRemoveChild(node);

    }

    @Test
    void testRemoveChildDiffImpl() throws Exception {
        Node toRemove = NodeTest.getApacheNode(sXml,true,'E');
        DOMException de = assertThrows(DOMException.class, () -> super.testRemoveChild(toRemove),
            "Removing node from a different impl");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    @Test
    void testRemoveChildDiffDoc() {
        Node toRemove = m_docNS.getDocumentElement();
        DOMException de = assertThrows(DOMException.class,
            () -> super.testRemoveChild(toRemove), "Removing node from a different doc");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    @Test
    void testRemoveChildFront() {
        Node node = m_node.getFirstChild();
        super.testRemoveChild(node);

    }

    @Test
    void testRemoveChildNull() {
        super.testRemoveChild(null);
    }

    @Test
    void testReplaceChild() {
        NodeList children = m_node.getChildNodes();
        int pos = children.getLength() / 2;
        Node newNode = (m_node instanceof Document)
            ? m_doc.createElement("fooBAR")
            : m_doc.createTextNode("fooBAR");
        Node node = children.item(pos);
        super.testReplaceChild(newNode, node);
    }

    @Test
    void testReplaceChildEnd() {
        Node node = m_node.getLastChild();
        Node newNode = m_doc.createTextNode("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    @Test
    void testReplaceChildFront() {
        Node node = m_node.getFirstChild();
        Node newNode = m_doc.createTextNode("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    @Test
    void testReplaceChildNullChild() {
        Node node = m_node.getChildNodes().item(0);
        Node newNode = null;
        assertNotNull(node);
        super.testReplaceChild(newNode, node);
    }

    @Test
    void testReplaceChildNull() {
        Node node = null;
        Node newNode = (m_node instanceof Document)
            ? ((Document) m_node).createElement("fooBAR")
            : m_node.getOwnerDocument().createElement("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    @Test
    void testReplaceChildDNE() {

        if (m_doc == null) {
            assertEquals(m_doc, m_node.getOwnerDocument());
        }

        //node to replace is not a child
        Node node1 =m_doc.createElement("foobar");
        Node newNode1a = m_doc.createElement("fooBAR");
        try {
            super.testReplaceChild(newNode1a, node1);
        } catch (DOMException de) {
            assertEquals(DOMException.NOT_FOUND_ERR, de.code);//Raised if oldChild is not a child of this node.
        }

         //newChild was created from a different document than the one that created this node

        Node newNode1b = m_docNS.createElement("fooBAR");
        assertNotEquals(m_docNS, m_node.getOwnerDocument());
        DOMException de2 = assertThrows(DOMException.class, () -> super.testReplaceChild(newNode1b, node1), "Node is from the wrong document");
        assertEquals(DOMException.WRONG_DOCUMENT_ERR, de2.code);

       //refChild was created from a different document than the one that created this node

        Node node2 = m_docNS.createElement("fooBAR");
        Node newNode2 =m_doc.createElement("fooBAR");
        DOMException de3 = assertThrows(DOMException.class, () -> super.testReplaceChild(newNode2, node2), "Node is from the wrong document");
        assertTrue((DOMException.WRONG_DOCUMENT_ERR == de3.code) || (DOMException.NOT_FOUND_ERR == de3.code));
    }


    // public void testInsertBeforeDiffDoc(){}:done above
    @Test
    void testReplace_replacement_DiffImpl() throws Exception {
        Node node = m_node.getFirstChild();
        Node newnode=NodeTest.getApacheNode(sXml,true,'T');
        DOMException de = assertThrows(DOMException.class, () -> super.testReplaceChild(newnode, node), "Inserting node created from a different impl");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    //ref child is diff impl
    @Test
    void testReplace_target_DiffImpl() throws Exception {
        Node node =NodeTest.getApacheNode(sXml,true,'E');
        Node newnode=m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> super.testReplaceChild(newnode, node), "Inserting node created from a different impl");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    @Test
    protected void testReplaceChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo"));
        child.appendChild(m_doc.createElement("foobar"));
        Node toReplace = m_node.getFirstChild();
        super.testReplaceChild(child, toReplace);
    }

    @Test
    protected void testInsertBefore() {
        Node target = m_node.getFirstChild();
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        assertNotNull(target);
        super.testInsertBefore(child, target);
    }

    @Test
    protected void testInsertBeforeNullTarget() {
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        super.testInsertBefore(child, null);
    }

    @Test
    void testInsertBeforeInvalidRefNode() {
        Node child = m_doc.createElementNS("org.foo.www", "foonode");
        Node target = m_doc.createElement("foo");

        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(child, target), "Insert cannot happen");
        assertEquals(DOMException.NOT_FOUND_ERR, de.code);
    }

    @Test
    void testInsertBeforeNewChildDiffDoc(){
        Node target = m_node.getFirstChild();
        Node toInsert=m_docNS.getDocumentElement();
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(toInsert, target), "Inserting node created from a different doc");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    @Test
    void testInsertBeforeNewChildDiffImpl() throws Exception {
        Node target = m_node.getFirstChild();
        Node toInsert=NodeTest.getApacheNode(sXml,true,'T');
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(toInsert, target), "Inserting node created from a different impl");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    @Test
    void testInsertBeforeRefChildDiffDoc(){
        Node target = m_docNS.getDocumentElement();
        Node toInsert = m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(toInsert, target), "Ref Child from a different doc");
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }

    @Test
    void testInsertBeforeRefChildDiffImpl() throws Exception {
        Node target = NodeTest.getApacheNode(sXml, true, 'T');
        Node toInsert = m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(toInsert, target));
        assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
    }


    @Test
    void testInsertBeforeNullChild() {
        Node target = m_doc.createElement("foo");
        super.testInsertBefore(null, target);
    }

    /**
     *  pre: child is not a parent ancestor
     */
    public void testAppendChildExisting(Node child) {
        if (child == m_node)
            child = m_doc.getLastChild();
        //if still the same, too bad
        super.testAppendChild(child);
    }

    /**
     * pre: child cannot be an ancestor of m_node
   */
    public void testInsertExistingNode(Node child) {
        Node target = m_node.getFirstChild();
        if (target != null && child != null && target.getParentNode()==child.getParentNode())
            child=child.getParentNode();
        assertFalse(target == null || child == null);
        super.testInsertBefore(child, target);
    }

    @Test
    protected void testInsertBeforeDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo1"));
        Node target = m_node.getFirstChild();
        super.testInsertBefore(child, target);
    }

    @Test
    protected void testAppendChild() {
        Node newNode = m_doc.createElement("foo");
        super.testAppendChild(newNode);
    }

    //try to append the parent
    @Test
    void testAppendChildIllegal0() {
        Node parent = m_node.getFirstChild();
        m_node = m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> super.testAppendChild(parent), "Appending parent");
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    //try to insert diff doc
    @Test
    void testAppendChildIllegal1() {
        Node newNode = m_docNS.createElement("newNode");
        DOMException de = assertThrows(DOMException.class, () -> super.testAppendChild(newNode), "Appending wrong doc");
        assertEquals(DOMException.WRONG_DOCUMENT_ERR, de.code);
    }

    //append doc frag
    @Test
    protected void testAppendChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo"));
        super.testAppendChild(child);
    }

    //TODO  : not implemented
    @Test
    void testNormalize() {
        int nCount=m_node.getChildNodes().getLength();
        String value="";
        if (m_node.getLastChild() instanceof Text)
            value= m_node.getLastChild().getNodeValue();

        int nExistingText=0;
        for (int i=nCount-1; i > -1; i--)
            if (m_node.getChildNodes().item(i) instanceof CharacterData)
                   nExistingText++;
        Node txt=m_doc.createTextNode("foo");
        m_node.appendChild(txt);

        txt=m_doc.createTextNode("");
        m_node.appendChild(txt);
        txt=m_doc.createTextNode(" bar");
        m_node.appendChild(txt);

        assertEquals(nCount + 3, m_node.getChildNodes().getLength());

        m_node.normalize();

        assertTrue((m_node.getLastChild() instanceof Text));
       // if (value.length()==0)nCount++;//if last node was a text nCount stays the same
        assertEquals(nCount - nExistingText + 1, m_node.getChildNodes().getLength());

        value+="foo bar";
        assertEquals(value, m_node.getLastChild().getNodeValue());
    }


    @Test
    void testSetPrefixInvalid() {
        //test only applies to Attrs and Elems
        if (!(m_node.getNodeType() == Node.ATTRIBUTE_NODE
            || m_node.getNodeType() == Node.ELEMENT_NODE))
            return;

        //qualifiedName is malformed
        DOMException de1 = assertThrows(DOMException.class, () -> m_node.setPrefix("invalid<"), "Invalid prefix name--see http://www.w3.org/TR/REC-xml#NT-BaseChar");
        assertEquals(DOMException.INVALID_CHARACTER_ERR, de1.code);

        //the qualifiedName has a prefix and the namespaceURI is null

        DOMException de2;
        if (m_node.getNamespaceURI() == null) {
            de2 = assertThrows(DOMException.class, () -> m_node.setPrefix("foo"), "Can not set prefix here");
        } else {
            de2 = assertThrows(DOMException.class, () -> m_node.setPrefix("xml"), "Xml is not a valid prefix here");
        }
        assertEquals(DOMException.NAMESPACE_ERR, de2.code);
    }

    @Test
    void testSetNodeValue() {
        int nCount = m_node.getChildNodes().getLength();
        m_node.setNodeValue("blah");
        assertEquals(nCount, m_node.getChildNodes().getLength());
        for (int i = 0; i < nCount; i++)
            assertNotEquals("blah", m_node.getChildNodes().item(i).getNodeValue());
    }
}
