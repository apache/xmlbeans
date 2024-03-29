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
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;


public class ElementTest extends NodeWithChildrenTest {

    public ElementTest() {
        String sDTD =
            "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE bardoc [" +
            "<!ELEMENT bar>" +
            "<!ELEMENT foo>" +
            "<!ATTLIST bar at_spec CDATA \"0\">" +
            "]>";
        sXmlNS =
            "<bar xmlns:other=\"uri:other\" xmlns:myns=\"uri:foo\">" +
            "<foo  myns:at0=\"val01\" myns:at2=\"at2\" at2=\"val2\" myns:at3=\"val3\" at4=\"val4\">" +
            "txt0<foo>nestedfoo</foo><myns:yana/>" +
            "</foo>" +
            "<myns:foo>nstext<ZeD/></myns:foo>" +
            "</bar>";
        if (bDTD)
            sXmlNS = sDTD + sXmlNS;
        sXml = Common.XML_FOO_BAR_NESTED_SIBLINGS;
    }

    @Test
    void testNodeName() {
        assertEquals("zed", m_node.getNodeName());
    }

    @Test
    void testNodeType() {
        assertEquals(Node.ELEMENT_NODE, m_node.getNodeType());
    }

    @Test
    void testNodeValue() {
        assertNull(m_node.getNodeValue());
    }

    @Test
    void testNextSibling() {
        assertNull(m_node.getNextSibling());
    }

    @Test
    void testPreviousSibling() {
        Node prSib = m_node.getPreviousSibling();
        assertEquals("text0", prSib.getNodeValue());
    }

    @Test
    void testParent() {
        Node parent = m_node.getParentNode();
        assertEquals("bar", parent.getLocalName());
        assertEquals(m_doc.getFirstChild().getFirstChild(), parent);
    }

    @Test
    protected void testPrefix() {
        assertEquals("", m_node.getPrefix());

        m_node = m_docNS.getDocumentElement().getChildNodes().item(1);
        assertEquals("myns:foo", m_node.getNodeName());
        assertEquals("myns", m_node.getPrefix());
    }

    @Test
    protected void testNamespaceUri() {
        assertEquals("", m_node.getNamespaceURI());
    }

    @Test
    protected void testCloneNode() {
        super.testCloneNode();
    }

    /**
     * Clone node with atts
     */
    @Test
    void testCloneNodeAttrs() {
        //the foo elt
        Node toClone = m_docNS.getFirstChild();
        Node clone1=toClone.cloneNode(false);
        NamedNodeMap attrSet1=toClone.getAttributes();
        assertTrue(DomUtils.compareNamedNodeMaps(attrSet1, clone1.getAttributes()));
        Node clone2 = toClone.cloneNode(true);
        assertTrue(DomUtils.compareNamedNodeMaps(attrSet1, clone2.getAttributes()));
    }

    @Test
    protected void testHasAttributes() {
        super.testHasAttributes();
        m_node = m_doc.getFirstChild();
        assertTrue(m_node.hasAttributes());
    }

    @Test
    void testGetAttribute() {
        m_node = m_docNS.getFirstChild();
        if (bDTD)
            assertEquals("0", ((Element) m_node).getAttribute("at_spec"));
        assertEquals("val2", ((Element) m_node.getFirstChild()).getAttribute("at2"));
    }

    @Test
    void testGetAttributeDNE() {
        m_node = m_docNS.getFirstChild();
        assertEquals("", ((Element) m_node).getAttribute("at3"));
        assertEquals("", ((Element) m_node).getAttribute("foobar"));
        String sNull = null;
        assertEquals("", ((Element) m_node).getAttribute(sNull));
    }

    @Test
    void testGetAttributeNode() {
        m_node = m_docNS.getFirstChild();
        assertEquals("bar", ((Element) m_node).getTagName());
        //assertEquals("uri:foo",((Attr)((Element)m_node).getAttributeNodeNS("xmlns","myns")).getNodeValue());
        m_node = m_node.getFirstChild();
        assertEquals("val2", ((Element) m_node).getAttributeNode("at2").getNodeValue());
        if (bDTD)
            assertEquals("0", ((Element) m_node).getAttributeNode("at_spec").getNodeValue());
    }

    @Test
    void testGetAttributeNodeDNE() {
        m_node = m_docNS.getFirstChild();
        assertNull(((Element) m_node).getAttributeNode("at3"));
        assertNull(((Element) m_node).getAttributeNode("foobar"));
        String sNull = null;
        assertEquals("", ((Element) m_node).getAttribute(sNull));
    }

    @Test
    void getAttributeNodeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        if (bDTD) {
            assertEquals("0", ((Element) m_node).getAttributeNodeNS("", "at_spec").getNodeValue());
        }
        assertEquals("val01", ((Element) m_node).getAttributeNodeNS("uri:foo", "at0").getNodeValue());
        assertEquals("val2", ((Element) m_node).getAttributeNodeNS(null, "at2").getNodeValue());
        assertEquals("val3", ((Element) m_node).getAttributeNodeNS("uri:foo", "at3").getNodeValue());
    }

    @Test
    void testGetAttributeNodeNS_DNE() {
        m_node = m_docNS.getFirstChild();
        assertNull(((Element) m_node).getAttributeNodeNS("", "at3"));
        assertNull(((Element) m_node).getAttributeNodeNS("uri:foo", "at1"));
        String sNull = null;
        assertNull(((Element) m_node).getAttributeNodeNS("uri:foo", sNull));
    }

    @Test
    void testGetAttributeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        if (bDTD)
            assertEquals("0", ((Element) m_node).getAttributeNS(null, "at_spec"));
        assertEquals("val01", ((Element) m_node).getAttributeNS("uri:foo", "at0"));
        assertEquals("val2", ((Element) m_node).getAttributeNS("", "at2"));
    }

    @Test
    void testGetAttributeNS_DNE() {
        m_node = m_docNS.getFirstChild();
        assertEquals("", ((Element) m_node).getAttributeNS("", "at3"));
        assertEquals("", ((Element) m_node).getAttributeNS("uri:foo", "at1"));
        String sNull = null;
        assertEquals("", ((Element) m_node).getAttributeNS("uri:foo", sNull));
    }

    @Test
    void testGetElementsByTagName() {
        //move node @ foo
        m_node = m_node.getParentNode().getParentNode();
        NodeList result = ((Element) m_node).getElementsByTagName("*");
        int nEltCount = 5;//num elts in the XML
        assertEquals(nEltCount - 1, result.getLength());

        result = ((Element) m_node).getElementsByTagName("zed");
        assertEquals(2, result.getLength());
        assertEquals("nested0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nested1", result.item(1).getFirstChild().getNodeValue());
    }

    @Test
    void testGetElementsByTagNameDNE() {
        NodeList result = ((Element) m_node.getParentNode()).getElementsByTagName(
            "foobar");
        assertEquals(0, result.getLength());
    }

    //elts need to come out in preorder order
    @Test
    void testGetElementsByTagNamePreorder() {
        m_node = m_docNS.getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagName("foo");
        assertEquals(2, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo", result.item(1).getFirstChild().getNodeValue());
    }

    @Test
    void testGetElementsByTagNameDescendant() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagName("foo");//self should not be selected
        assertEquals(1, result.getLength());
        assertEquals("nestedfoo", result.item(0).getFirstChild().getNodeValue());
    }

    @Test
    void testGetElementsByTagNameNS() {
        m_node = m_docNS.getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagNameNS("*", "*");
        int nEltCount = 6;
        assertEquals(nEltCount - 1, result.getLength());

        result = ((Element) m_node).getElementsByTagNameNS("*", "foo");
        nEltCount = 3;
        assertEquals(nEltCount, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo", result.item(1).getFirstChild().getNodeValue());
        assertEquals("nstext", result.item(2).getFirstChild().getNodeValue());


        result = ((Element) m_node).getElementsByTagNameNS("uri:foo", "foo");
        assertEquals(1, result.getLength());
        assertEquals("nstext", result.item(0).getFirstChild().getNodeValue());

        result = ((Element) m_node).getElementsByTagNameNS(null, "foo");
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo", result.item(1).getFirstChild().getNodeValue());
        NodeList result1 = ((Element) m_node).getElementsByTagNameNS("", "foo");
        assertTrue(compareNodeList(result, result1));


        result = ((Element) m_node).getElementsByTagNameNS(null, "*");
        assertEquals(3, result.getLength());
        assertEquals("ZeD", ((Element) result.item(2)).getTagName());
    }

    @Test
    void testGetElementsByTagNameNS_DNE() {
        m_node = m_docNS.getFirstChild();
        NodeList result = ((Element) m_node).getElementsByTagNameNS("uri:foo",
            "zed");
        assertEquals(0, result.getLength());

        result =
            ((Element) m_node).getElementsByTagNameNS("foo:uri_DNE", "foo");
        assertEquals(0, result.getLength());
    }

    @Test
    void testGetTagName() {
        m_node =
            m_docNS.getFirstChild().getChildNodes().item(1).getChildNodes()
                .item(1);
        assertEquals("ZeD", ((Element) m_node).getTagName());
    }

    @Test
    void testHasAttribute() {
        m_node = m_docNS.getFirstChild();
        if (bDTD)
            assertTrue(((Element) m_node).hasAttribute("at_spec"));

        m_node = m_docNS.getFirstChild();
        assertFalse(((Element) m_node).hasAttribute("at3"));
        assertFalse(((Element) m_node).hasAttribute("at0"));
    }

    @Test
    void testHasAttributeNS() {
        m_node = m_docNS.getFirstChild();
        if (bDTD)
            assertTrue(((Element) m_node).hasAttributeNS(null, "at_spec"));

        m_node = m_node.getFirstChild();
        assertTrue(((Element) m_node).hasAttributeNS("uri:foo", "at3"));
        assertFalse(((Element) m_node).hasAttributeNS("uri:foo:org", "at0"));
        assertFalse(((Element) m_node).hasAttributeNS("uri:foo", null));
    }

    @Test
    void testRemoveAttribute() {
        m_node = m_docNS.getFirstChild();
        //remove default
        if (bDTD) {

            ((Element) m_node).removeAttribute("at_spec");
            assertEquals(1, m_node.getAttributes().getLength());
        }

        m_node = m_node.getFirstChild();
        assertEquals("foo", m_node.getNodeName());
        assertEquals(5, m_node.getAttributes().getLength());
        ((Element) m_node).removeAttribute("at2");
        assertEquals(4, m_node.getAttributes().getLength());

        //DNE
        ((Element) m_node).removeAttribute("at3");
        assertEquals(4, m_node.getAttributes().getLength());

    }

    @Test
    void testRemoveAttributeNode() {
        Node removed;
        //remove default
        m_node = m_docNS.getFirstChild();
        if (bDTD) {
            ((Element) m_node).removeAttributeNode(
                ((Element) m_node).getAttributeNode("at_spec"));
            assertEquals(1, m_node.getAttributes().getLength());
        }
        m_node = m_node.getFirstChild();
        assertEquals("foo", m_node.getNodeName());
        assertEquals(5, m_node.getAttributes().getLength());
        Attr remove = ((Element) m_node).getAttributeNode("at2");
        removed = ((Element) m_node).removeAttributeNode(remove);
        assertNotNull(removed);
        assertEquals(4, m_node.getAttributes().getLength());
        assertEquals(removed, remove);
    }

    @Test
    void testRemoveAttributeNode_DNE() {
        //DNE
        Attr remove1 = ((Element) m_node).getAttributeNode("at3");
        DOMException de1 = assertThrows(DOMException.class, () -> ((Element) m_node).removeAttributeNode(remove1),
            "removing Non existing attr");
        assertEquals(DOMException.NOT_FOUND_ERR, de1.code);

        Attr remove2 = null;
        DOMException de2 = assertThrows(DOMException.class, () -> ((Element) m_node).removeAttributeNode(remove2),
            "removing Non existing attr");
        assertEquals(DOMException.NOT_FOUND_ERR, de2.code);

        //differentParent
        Attr remove3 = m_doc.getDocumentElement().getAttributeNode("attr0");
        DOMException de3 = assertThrows(DOMException.class, () -> ((Element) m_node).removeAttributeNode(remove3),
            "removing Non existing attr");
        assertEquals(DOMException.NOT_FOUND_ERR, de3.code);
    }

    @Test
    void testRemoveAttributeNS() {
        //remove default
        m_node = m_docNS.getFirstChild();
        if (bDTD) {
            ((Element) m_node).removeAttributeNS(null, "at_spec");
            assertEquals(1, m_node.getAttributes().getLength());
        }
        m_node = ((Element) m_node).getFirstChild();
        ((Element) m_node).removeAttributeNS("uri:foo", "at0");
        assertEquals(4, m_node.getAttributes().getLength());

        //DNE
        ((Element) m_node).removeAttributeNS(null, "at3");
        assertEquals(4, m_node.getAttributes().getLength());

        ((Element) m_node).removeAttributeNS("uri:foo", null);
        assertEquals(4, m_node.getAttributes().getLength());
    }

    @Test
    void testSetAttribute() {
        m_node = m_doc.getDocumentElement();
        DOMException de = assertThrows(DOMException.class, () -> ((Element) m_node).setAttribute("invalid<", "0"), "Invalid attr name");
        assertEquals(DOMException.INVALID_CHARACTER_ERR, de.code);

        ((Element) m_node).setAttribute("attr0", "newval");
        assertEquals("newval", ((Element) m_node).getAttribute("attr0"));


        ((Element) m_node).setAttribute("attr1", "newval");
        assertEquals("newval", ((Element) m_node).getAttribute("attr1"));
        assertEquals(2, m_node.getAttributes().getLength());
    }

    @Test
    void testSetAttributeNode() {
        Attr result;
        Attr newAttr = m_doc.createAttribute("attr0");
        Attr oldAttr = ((Element) m_node).getAttributeNode("attr0");
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNode(newAttr);
        assertEquals(oldAttr, result);
        assertEquals("newval", ((Element) m_node).getAttributeNode("attr0").getNodeValue());

        //insert self
        try {
            Attr at0 = ((Element) m_node).getAttributeNode("attr0");
            String v1 = at0.getNodeValue();
            ((Element) m_node).setAttributeNode(at0);
            assertEquals(v1, ((Element) m_node).getAttribute("attr0"));
        } catch (DOMException de) {
            assertEquals(de.code, DOMException.INUSE_ATTRIBUTE_ERR);
        }

        //insert new
        newAttr = m_doc.createAttribute("attr1");
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNode(newAttr);
        assertNull(result);
        assertEquals("newval", ((Element) m_node).getAttributeNode("attr1").getNodeValue());
        assertEquals(2, m_node.getAttributes().getLength());
    }

    @Test
    void testSetAttributeNodeDiffDoc() {
        Attr result;
        Attr newAttr = m_docNS.createAttribute("attr0");
        DOMException de = assertThrows(DOMException.class, () -> ((Element) m_node).setAttributeNode(newAttr),
            "Attr Node diff doc in use");
        assertEquals(DOMException.WRONG_DOCUMENT_ERR, de.code);
    }

    @Test
    void testSetAttributeNodeInUse() {
        //insert new
        m_node = m_node.getParentNode().getParentNode();
        Attr newAttr = ((Element) m_node).getAttributeNode("attr0");
        m_node = m_node.getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> ((Element) m_node).setAttributeNode(newAttr));
        assertEquals(DOMException.INUSE_ATTRIBUTE_ERR, de.code);
    }

    @Test
    void testSetAttributeNodeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        Attr result;
        Attr newAttr = m_docNS.createAttributeNS("uri:foo", "at0");
        Attr oldAttr = ((Element) m_node).getAttributeNodeNS("uri:foo", "at0");
        assertNotNull(oldAttr);
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNodeNS(newAttr);
        assertEquals(oldAttr, result);
        Attr insertedAtt = ((Element) m_node).getAttributeNodeNS("uri:foo",
            "at0");
        assertNotNull(insertedAtt);
        assertEquals("newval", insertedAtt.getNodeValue());

        //insert new
        int nAttrCnt = m_node.getAttributes().getLength();
        newAttr = m_docNS.createAttributeNS("uri:foo", "attr1");
        newAttr.setValue("newval");
        result = ((Element) m_node).setAttributeNode(newAttr);
        assertNull(result);
        assertEquals("newval", ((Element) m_node).getAttributeNS("uri:foo", "attr1"));
        assertEquals(nAttrCnt + 1, m_node.getAttributes().getLength());

        //insert new
        newAttr = m_docNS.createAttributeNS("uri:foo:org", "attr1");
        newAttr.setValue("newURIval");
        result = ((Element) m_node).setAttributeNodeNS(newAttr);

        assertNull(result);
        assertEquals("newURIval", ((Element) m_node).getAttributeNS("uri:foo:org", "attr1"));
        assertEquals(nAttrCnt + 2, m_node.getAttributes().getLength());
    }

    @Test
    void testSetAttributeNS() {
        m_node = m_docNS.getFirstChild().getFirstChild();
        //overwrite
        ((Element) m_node).setAttributeNS("uri:foo", "at0", "newval");
        assertEquals("newval", ((Element) m_node).getAttributeNS("uri:foo", "at0"));


        ((Element) m_node).setAttributeNS("uri:foo:org", "attr1", "newval");
        assertEquals("newval", ((Element) m_node).getAttributeNS("uri:foo:org", "attr1"));
        assertEquals(6, m_node.getAttributes().getLength());
    }

    @Test
    void testSetAttributeNSBadNS() {
        //qualifiedName is malformed
        DOMException de1 = assertThrows(DOMException.class, () ->
            ((Element) m_node).setAttributeNS("foo:org", "invalid<", "0"), "Invalid attr name");
        assertEquals(DOMException.INVALID_CHARACTER_ERR, de1.code);

        //the qualifiedName has a prefix and the namespaceURI is null
        DOMException de2 = assertThrows(DOMException.class, () ->
            ((Element) m_node).setAttributeNS(null, "myfoo:at", "0"), "Invalid attr name");
        assertEquals(DOMException.NAMESPACE_ERR, de2.code);
    }

    @Test
    void testSetAttributeNSBadNS_xmlns() {
        //the qualifiedName, or its prefix, is "xmlns" and the namespaceURI is different from " http://www.w3.org/2000/xmlns/".
        DOMException de1 = assertThrows(DOMException.class, () ->
            ((Element) m_node).setAttributeNS("foo:org:uri", "xmlns", "0"), "Invalid attr name");
        assertEquals(DOMException.NAMESPACE_ERR, de1.code);

        DOMException de2 = assertThrows(DOMException.class, () ->
            ((Element) m_node).setAttributeNS("foo:org:uri", "xmlns:foo", "0"), "Invalid attr name");
        assertEquals(DOMException.NAMESPACE_ERR, de2.code);
    }

    @Test
    void testSetAttributeNSBadNS_xml() {
        //if the qualifiedName has a prefix that is "xml"
        // and the namespaceURI is different from " http://www.w3.org/XML/1998/namespace"
        DOMException de = assertThrows(DOMException.class, () ->
            ((Element) m_node).setAttributeNS("foo:org:uri", "xml:foo", "0"), "Invalid attr name");
        assertEquals(DOMException.NAMESPACE_ERR, de.code);
    }

    @Test
    protected void testGetChildNodes() {
        m_node = m_node.getParentNode();
        assertEquals(2, m_node.getChildNodes().getLength());
    }

    @Test
    protected void testFirstChild() {
        assertEquals("nested0", m_node.getFirstChild().getNodeValue());
    }

    @Test
    protected void testLastChild() {
        assertEquals("nested0", m_node.getLastChild().getNodeValue());
    }

    //code coverage: need a node with penultimate elt and last text
    @Test
    void testLastChildMixedContent() {
        Node prevSibling = m_doc.createElement("penultimateNode");
        m_node.insertBefore(prevSibling, m_node.getFirstChild());
        assertEquals("nested0", m_node.getLastChild().getNodeValue());
    }

    @Test
    protected void testGetAttributes() {
        assertEquals(0, m_node.getAttributes().getLength());
    }

    @Test
    protected void testLocalName() {
        assertEquals("zed", m_node.getLocalName());
    }

    @Test
    protected void testSetPrefix() {
        //set a null prefix
        m_node = m_docNS.getFirstChild().getFirstChild().getChildNodes().item(2);//<myns:yana/>
        assertNotNull(m_node);
        m_node.setPrefix(null);
        assertEquals("", m_node.getPrefix());

        m_node.setPrefix("other");

        assertEquals("other:yana", m_node.getNodeName());
        assertEquals("other:yana", ((Element) m_node).getTagName());
        // assertEquals("uri:other",m_node.getNamespaceURI());--this is the URI @ creation--never changes
        assertEquals(1, ((Element) m_docNS.getDocumentElement()).getElementsByTagName("other:yana").getLength());
    }

    @Test
    void testNormalizeNode() throws Exception {
        m_node = m_node.getParentNode();
        m_node.replaceChild(m_doc.createTextNode("txt1"), m_node.getLastChild());
        assertEquals(2, m_node.getChildNodes().getLength());

        m_node.normalize();
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    void testNormalizeNodeNoChildren() throws Exception {
        m_node = m_doc.createElement("foobar");
        assertEquals(0, m_node.getChildNodes().getLength());
        m_node.normalize();
        assertEquals(0, m_node.getChildNodes().getLength());
    }

    @Test
    void testNormalizeNodeOneChild() throws Exception {
        m_node = m_doc.createElement("foobar");
        m_node.appendChild(m_doc.createElement("foobar"));
        assertEquals(1, m_node.getChildNodes().getLength());
        m_node.normalize();
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    void testAppendChildExisting() {
        m_node = m_docNS.getFirstChild().getLastChild();
        Node child = m_docNS.getFirstChild().getFirstChild();
        super.testAppendChildExisting(child);
    }

    @Test
    void testInsertExisitingNode() {
        m_node = m_docNS.getFirstChild().getLastChild();
        Node child = m_docNS.getFirstChild().getFirstChild();
        super.testAppendChildExisting(child);
    }

    @Test
    void testDomLevel1() {
        Element elt = m_doc.createElement("foobar");
        assertNull(elt.getPrefix(), "L1 prefix null");
        assertNull(elt.getLocalName(), "L1 LocalName null");
        assertNull(elt.getNamespaceURI(), "L1 Uri null");
        DOMException de = assertThrows(DOMException.class, () -> elt.setPrefix("foo"), "L1 prefix null");
        assertEquals(DOMException.NAMESPACE_ERR, de.code);
    }

    public void moveToNode() {
        m_node = m_doc.getFirstChild().getFirstChild().getChildNodes().item(1);//zed node;
        assertNotNull(m_node);
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }
}
