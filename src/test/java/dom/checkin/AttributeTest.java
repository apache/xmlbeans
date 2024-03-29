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


import dom.common.DomUtils;
import dom.common.NodeWithChildrenTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import static org.junit.jupiter.api.Assertions.*;


public class AttributeTest extends NodeWithChildrenTest {

    public AttributeTest() {
        String sDTD =
            "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE foodoc [" +
            "<!ELEMENT foo>" +
            "<!ATTLIST foo at_spec CDATA \"0\">" +
            "]>";

        sXml = "<foo xmlns:extra=\"bea.org\" xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\">some text</foo>";
        if (bDTD) {
            sXml = sDTD + sXml;
        }
        sXmlNS =
            "<foo xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\"/>";
    }

    @Test
    void testNodeName() {

        String sExpected = "myns:at0";
        assertEquals(sExpected, m_node.getNodeName());
    }

    @Test
    void testGetName() {

        String sExpected = "myns:at0";
        assertEquals(sExpected, ((Attr) m_node).getName());
    }

    @Test
    void testNodeType() {
        assertEquals(Node.ATTRIBUTE_NODE, m_node.getNodeType());
    }

    @Test
    void testNodeValue() {
        assertEquals("val01", m_node.getNodeValue());
    }


    //following are null here
    @Test
    void testNextSibling() {
        assertNull(m_node.getNextSibling());
    }

    @Test
    void testPreviousSibling() {
        assertNull(m_node.getPreviousSibling());
    }

    @Test
    void testParent() {
        assertNull(m_node.getParentNode());
    }

    @Test
    protected void testPrefix() {
        assertEquals("myns", m_node.getPrefix());
    }

    @Test
    protected void testNamespaceUri() {
        assertEquals("uri:foo", m_node.getNamespaceURI());
    }

    @Test
    protected void testLocalName() {
        assertEquals("at0", m_node.getLocalName());
    }

    @Test
    public void testAppendChild() {
        //elt
        Node newChild1 = m_doc.createElement("foo");
        DOMException de = assertThrows(DOMException.class, () -> m_node.appendChild(newChild1),
            "Cannot append an element children to attributes ");
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);

        Node newChild2 = m_doc.createTextNode("foobar");
        m_node.appendChild(newChild2);
        assertEquals(2, m_node.getChildNodes().getLength());

        // TODO
        m_node.normalize();
        assertEquals(1, m_node.getChildNodes().getLength());
        assertEquals("val01foobar", ((Text) m_node.getFirstChild()).getData());
    }

    @Test
    protected void testCloneNode() {
        Attr cloned, cloned1;
        cloned = (Attr) m_node.cloneNode(true);


        assertTrue(DomUtils.compareNodesDeep(m_node, cloned));
        assertNotSame(m_node, cloned);


        // TODO
        cloned1 = (Attr) m_node.cloneNode(false);
        // assertEquals(m_node.getChildNodes(), );
        assertTrue(DomUtils.compareNodesShallow(m_node, cloned));

        if (bDTD) {
            assertFalse(cloned.getSpecified());
            assertFalse(((Attr) m_node).getSpecified());
            m_node = m_doc.getAttributes().getNamedItem("at_spec");
            cloned = (Attr) m_node.cloneNode(true);
            cloned1 = (Attr) m_node.cloneNode(false);
            assertEquals(cloned, cloned1);
            assertEquals(m_node, cloned);
            assertNotSame(m_node, cloned);
            assertTrue(cloned.getSpecified());
            assertFalse(((Attr) m_node).getSpecified());
        }
    }

    /**
     * public void testXercesClone()throws Exception{
     * org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
     * parser.parse(new InputSource(new StringReader(sXml)));
     * Document xercesDocument = parser.getDocument();
     * assertFalse (xercesDocument==null);
     * <p/>
     * Node test = m_doc.getFirstChild();
     * assertTrue(test.hasChildNodes());
     * test = ((Element) test).getAttributeNodeNS("uri:foo", "at0");
     * Node clone=test.cloneNode(false);
     * assertTrue(clone.hasChildNodes());
     * <p/>
     * }
     */
    @Test
    protected void testGetChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    protected void testFirstChild() {
        assertEquals("val01", ((Text) m_node.getFirstChild()).getData());
    }

    @Test
    protected void testLastChild() {
        assertEquals("val01", ((Text) m_node.getLastChild()).getData());
    }

    @Test
    public void testInsertBefore() {
        Node newChild1 = m_doc.createElement("foo");
        assertEquals(1, m_node.getChildNodes().getLength());

        Node textNode = m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> m_node.insertBefore(newChild1, textNode));
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);

        Node newChild2 = m_doc.createTextNode("foo");
        m_node.insertBefore(newChild2, textNode);
        assertEquals("foo", m_node.getFirstChild().getNodeValue());
        assertEquals("val01", m_node.getLastChild().getNodeValue());
    }

    @Test
    void testRemoveChild() {
        //attr w/o a value
        Element owner = ((Attr) m_node).getOwnerElement();
        m_node.removeChild(m_node.getFirstChild());
        assertEquals("", ((Attr) m_node).getValue());
        //  assertEquals(false,owner.hasAttributeNS("uri:foo","at0"));
    }

    @Test
    void testReplaceChild() {
        //assertFalse(m_node.hasChildNodes());
        Node newChild1 = m_doc.createElement("foo");
        assertEquals(1, m_node.getChildNodes().getLength());
        DOMException de = assertThrows(DOMException.class, () -> m_node.replaceChild(newChild1, m_node.getFirstChild()),
            "can not put an element under an attr");
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);

        Node newChild2 = m_doc.createTextNode("realnewval");
        assertEquals(1, m_node.getChildNodes().getLength());
        m_node.replaceChild(newChild2, m_node.getFirstChild());
        assertEquals("realnewval", ((Attr) m_node).getValue());
    }

    @Test
    void testGetOwnerElement() {
        assertEquals("foo", ((Attr) m_node).getOwnerElement().getLocalName());
        Attr newNode = m_doc.createAttributeNS("foo1:org", "name");
        assertNull(newNode.getOwnerElement());
        newNode = m_doc.createAttribute("name");
        assertNull(newNode.getOwnerElement());
    }

    /*Not implem
    //TODO
    public void testGetSpecified(){
	if (bDTD){
	    Attr at_true=(Attr)m_doc.getAttributes().getNamedItem("at_spec");
	    assertEquals(false,at_true.getSpecified());
	}
	assertEquals(true,((Attr)m_node).getSpecified());
    }
    */
    @Test
    void testSetValue() {
        String newVal = "new<spec\u042Fchar";
        ((Attr) m_node).setValue(newVal);
        assertEquals(newVal, ((Attr) m_node).getValue());
    }

    @Test
    void testSetValueNull() {
        ((Attr) m_node).setValue("foo");
        String newVal = "";
        ((Attr) m_node).setValue(newVal);
        assertTrue(m_node.hasChildNodes());

        newVal = null;
        ((Attr) m_node).setValue(newVal);
        assertTrue(m_node.hasChildNodes());
    }

    @Test
    void testGetValue() {
        assertEquals("val01", ((Attr) m_node).getValue());
    }

    @Test
    public void testInsertBeforeDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createTextNode("foo1"));
        Node target = m_node.getFirstChild();
        super.testInsertBefore(child, target);
    }

    @Test
    public void testAppendChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createTextNode("foo"));
        super.testAppendChild(child);
    }

    @Test
    public void testReplaceChildDocFrag() {
        DocumentFragment child = m_doc.createDocumentFragment();
        child.appendChild(m_doc.createElement("foo"));
        child.appendChild(m_doc.createElement("foobar"));
        Node toReplace = m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> super.testReplaceChild(child, toReplace), "cannot insert element in attr");
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    public void testInsertBeforeNullTarget() {
        Node child1 = m_doc.createElementNS("org.foo.www", "foonode");
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(child1, null), "cannot insert element in attr");
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        Node child2 = m_doc.createTextNode("foonode");
        super.testInsertBefore(child2, null);
    }

    @Test
    void testInsertExistingNode() {
        Node toInsert1 = m_doc.getFirstChild();
        //elt under attr
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertExistingNode(toInsert1), "Shouldn't work for attrs");
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);

        //some text
        Node toInsert2 = m_doc.getFirstChild().getFirstChild();
        super.testInsertBefore(toInsert2, m_node.getFirstChild());
        assertEquals("some text", m_node.getFirstChild().getNodeValue());
        assertEquals(2, m_node.getChildNodes().getLength());
    }

    @Test
    void testSetNodeValue() {
        m_node.appendChild(m_doc.createTextNode("bar"));//attr w/ two values
        int nCount = m_node.getChildNodes().getLength();
        m_node.setNodeValue("blah");
        //assertEquals(1,m_node.getChildNodes().getLength());
        assertEquals("blah", m_node.getFirstChild().getNodeValue());
    }

    @Test
    void testAppendChildExisting() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node) {
            child = m_doc.getLastChild();
        }
        //if still the same, SOL
        super.testAppendChild(child);
    }

    @Test
    protected void testSetPrefix() {
        String newPrefix = "yana"; //should clear it
        m_node.setPrefix(newPrefix);
        assertEquals("yana:at0", m_node.getNodeName());
        newPrefix = "extra";
        m_node.setPrefix(newPrefix);
        assertEquals("uri:foo", m_node.getNamespaceURI());//URI never changes

        newPrefix = null;
        m_node.setPrefix(newPrefix);
        assertEquals("", m_node.getPrefix());

    }

    @Test
    void testInsertBeforeInvalidRefNode() {
        Node child = m_doc.createTextNode("foonode");
        Node target = m_doc.createElement("foo");
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertBefore(child, target), "Insert cannot happen");
        assertEquals(DOMException.NOT_FOUND_ERR, de.code);
    }

    @Test
    void testDomLevel1() {
        Attr at = m_doc.createAttribute("foobar");
        assertNull(at.getPrefix(), "L1 prefix null");
        assertNull(at.getLocalName(), "L1 LocalName null");
        assertNull(at.getNamespaceURI(), "L1 Uri null");
        DOMException de = assertThrows(DOMException.class, () -> at.setPrefix("foo"), "L1 prefix null");
        assertEquals(DOMException.NAMESPACE_ERR, de.code);
    }

    @Test
    void moveToNode() {
        m_node = m_doc.getFirstChild();
        m_node = ((Element) m_node).getAttributeNodeNS("uri:foo", "at0");
        assertEquals("val01", m_node.getNodeValue());
        assertTrue(m_node instanceof Attr);
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }
}






