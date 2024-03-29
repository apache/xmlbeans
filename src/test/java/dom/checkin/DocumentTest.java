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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;


public class DocumentTest extends NodeWithChildrenTest {

    public void moveToNode() {
        m_node = m_doc;
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }


    public DocumentTest() {
        sXml = "<foo at0=\"val0\" at1=\"val1\" at2=\"val2\" at3=\"val3\" at4=\"val4\"><bar bat0=\"val0\"/></foo>";

        sXmlNS = "<bar xmlns:other=\"uri:other\" xmlns:myns=\"uri:foo\">" +
                "<foo at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" myns:at3=\"val3\" at4=\"val4\">" +
                "txt0<foo>nestedfoo</foo><myns:yana/>" +
                "</foo>" +
                "<myns:foo>nstext<ZeD/></myns:foo>" +
                "</bar>";
    }

    @Test
    void testNodeName() {
        assertEquals("#document", m_node.getNodeName());
    }

    @Test
    void testNodeType() {
        assertEquals(Node.DOCUMENT_NODE, m_node.getNodeType());
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
        assertNull(m_node.getPreviousSibling());
    }

    @Test
    void testParent() {
        assertNull(m_node.getParentNode());
    }

    @Test
    protected void testOwnerDocument() {
        assertNull(m_node.getOwnerDocument());//API spec
    }

    @Test
    void testChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    protected void testFirstChild() {
        assertEquals("foo", m_node.getFirstChild().getLocalName());
    }

    @Test
    protected void testLastChild() {
        assertEquals("foo", m_node.getLastChild().getLocalName());
    }

    @Test
    public void testAppendChild() {
        DOMException de = assertThrows(DOMException.class, super::testAppendChild);
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    public void testInsertBefore() {
        DOMException de = assertThrows(DOMException.class, super::testInsertBefore);
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    public void testInsertBeforeNullTarget() {
        DOMException de = assertThrows(DOMException.class, super::testInsertBeforeNullTarget);
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    void testInsertExistingNode() {
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertExistingNode(m_node.getFirstChild()));
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

//    @Test
//    public void testAppendChildIllegal0() {
//        try {
//            super.testAppendChildIllegal0();
//        }
//        catch (DOMException de) {
//            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
//        }
//        catch (AssertionError af) {
//            assertEquals(((DOMException) af.getCause()).code,
//                    DOMException.HIERARCHY_REQUEST_ERR);
//        }
//    }

//    @Test
//    public void testAppendChildIllegal1() {
//        try {
//            super.testAppendChildIllegal1();
//        }
//        catch (DOMException de) {
//            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
//        }
//        catch (AssertionError af) {
//            assertEquals(((DOMException) af.getCause()).code,
//                    DOMException.HIERARCHY_REQUEST_ERR);
//        }
//    }

    @Test
    protected void testGetChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    protected void testSetPrefix() {
        super.testSetPrefix(); //see charData--is the exception correct
    }

    @Test
    void testInsertExisitingNode() {
        Node child = m_doc.getFirstChild().getFirstChild();
        if (child == m_node) {
            child = m_doc.getLastChild();
        }
        Node c = child;
        DOMException de = assertThrows(DOMException.class, () -> super.testInsertExistingNode(c));
        //never can insert anything unless doc is empty
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    void testAppendChildExisting() {
        Node child = m_node.getFirstChild().getFirstChild();
        DOMException de = assertThrows(DOMException.class, () -> super.testAppendChildExisting(child));
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Disabled
    public void testNormalize() {
        //unque doc child--normalize in elt. or text or comment, etc
    }

    @Test
    public void testInsertBeforeDocFrag() {
        DOMException de = assertThrows(DOMException.class, super::testInsertBeforeDocFrag);
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    public void testAppendChildDocFrag() {
        DOMException de = assertThrows(DOMException.class, super::testAppendChildDocFrag);
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    void testReplaceChildFront() {
        Node node = m_doc.getDocumentElement();
        assertEquals(node, m_node.getFirstChild());
        Node newNode = m_doc.createElement("fooBAR");
        super.testReplaceChild(newNode, node);
        assertEquals(m_doc.getDocumentElement(), newNode);
    }

    @Test
    void testReplaceChildEnd() {
        Node node = m_doc.getDocumentElement();
        assertEquals(node, m_node.getFirstChild());
        Node newNode = m_doc.createElement("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    @Test
    public void testReplaceChildDocFrag() {
        DOMException de = assertThrows(DOMException.class, super::testReplaceChildDocFrag);
        assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
    }

    @Test
    void testCreateAttribute() {
        Attr att = m_doc.createAttribute("at0");
        assertNull(att.getOwnerElement());
        assertEquals(m_doc, att.getOwnerDocument());

        assertFalse(att.hasChildNodes());
        assertEquals("", att.getValue());
    }

    @Test
    void testCreateAttributeNS() {
        Attr att = m_doc.createAttributeNS("foo:uri", "at0");
        assertEquals("foo:uri", att.getNamespaceURI());
        assertNull(att.getOwnerElement());
        assertEquals(m_doc, att.getOwnerDocument());
    }

    @Test
    void testCreateCDATASection() {
        CDATASection cdata = m_doc.createCDATASection("<CDATA Section>");
        assertNull(cdata.getParentNode());
        assertEquals(m_doc, cdata.getOwnerDocument());

        cdata = m_doc.createCDATASection(null);
        assertNull(cdata.getParentNode());
        assertEquals(m_doc, cdata.getOwnerDocument());
        assertEquals("", cdata.getData());
    }

    @Test
    void testCreateComment() {
        Comment comment = m_doc.createComment("A comment");
        assertNull(comment.getParentNode());
        assertEquals(m_doc, comment.getOwnerDocument());

        comment = m_doc.createComment(null);
        assertEquals("", comment.getData());
    }

    @Test
    void testCreateDocumentFragment() {
        DocumentFragment doc_frag = m_doc.createDocumentFragment();
        assertNull(doc_frag.getParentNode());
        assertEquals(m_doc, doc_frag.getOwnerDocument());

    }

    @Test
    void testCreateElement() {
        Element elt1 = m_doc.createElement("elt1");
        assertNull(elt1.getParentNode());
        assertEquals(m_doc, elt1.getOwnerDocument());

    }

    @Test
    void testCreateElementNS() {
        Element elt1 = m_doc.createElementNS("uri:foo", "ns:elt1");
        assertEquals("uri:foo", elt1.getNamespaceURI());
        assertNull(elt1.getParentNode());
        assertEquals(m_doc, elt1.getOwnerDocument());
    }

    @Test
    void testCreateProcessingInstruction() {
        DOMException de1 = assertThrows(DOMException.class, () -> m_doc.createProcessingInstruction("xml", "version 1.0"));
        assertEquals(DOMException.INVALID_CHARACTER_ERR, de1.code);

        ProcessingInstruction pi = m_doc.createProcessingInstruction("xml-foo", null);
        assertEquals("", pi.getData());

        assertThrows(IllegalArgumentException.class, () -> m_doc.createProcessingInstruction(null, "foo"), "PI target can't be null");

        DOMException de3 = assertThrows(DOMException.class, () -> m_doc.createProcessingInstruction("invalid@", "foo"), "Invalid pi name");
        assertEquals(DOMException.INVALID_CHARACTER_ERR, de3.code);
    }

    @Test
    void testCreateTextNode() {
        Text txt0 = m_doc.createTextNode("foo");
        assertNull(txt0.getParentNode());
        assertEquals(m_doc, txt0.getOwnerDocument());

        txt0 = m_doc.createTextNode(null);
        assertEquals("", txt0.getData());
    }

    @Test
    void testGetDocumentElement() {
        assertEquals(m_doc.getDocumentElement(), m_node.getFirstChild());
    }

    @Test
    void testGetElementsByTagName() {
        //move node @ foo
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagName("*");
        int nEltCount = 6;//num elts in the XML
        assertEquals(nEltCount, result.getLength());

        result = ((Document) m_node).getElementsByTagName("zed");
        assertEquals(0, result.getLength());

    }

    //elts need to come out in preorder order
    @Test
    void testGetElementsByTagNamePreorder() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagName("foo");
        assertEquals(2, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo", result.item(1).getFirstChild().getNodeValue());
    }

    @Test
    void testGetElementsByTagNameDNE() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagName("foobar");
        assertEquals(0, result.getLength());
    }

    @Test
    void testGetElementsByTagNameNS() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagNameNS("*", "*");
        int nEltCount = 6;
     /*   assertEquals(nEltCount, result.getLength());

        result = ((Document) m_node).getElementsByTagNameNS("*", "foo");
        nEltCount = 3;
        assertEquals(nEltCount, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo",
                result.item(1).getFirstChild().getNodeValue());
        assertEquals("nstext", result.item(2).getFirstChild().getNodeValue());


        result = ((Document) m_node).getElementsByTagNameNS("uri:foo", "foo");
        assertEquals(1, result.getLength());
        assertEquals("nstext", result.item(0).getFirstChild().getNodeValue());
    */
        result = ((Document) m_node).getElementsByTagNameNS(null, "foo");
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo", result.item(1).getFirstChild().getNodeValue());
        NodeList result1 = ((Document) m_node).getElementsByTagNameNS("",
                "foo");
        assertTrue(compareNodeList(result, result1));


        result = ((Document) m_node).getElementsByTagNameNS(null, "*");
        assertEquals(4, result.getLength());
        assertEquals("ZeD", ((Element) result.item(3)).getTagName());
    }

    @Test
    void testGetElementsByTagNameNS_DNE() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagNameNS("uri:foo", "zed");
        assertEquals(0, result.getLength());

        result = ((Document) m_node).getElementsByTagNameNS("foo:uri_DNE", "foo");
        assertEquals(0, result.getLength());
    }

    @Test
    void testGetImplementation() {
        assertTrue(m_doc.getImplementation().toString().startsWith("org.apache.xmlbeans.impl.store"));
    }

    @Test
    void testImportNode() throws IOException, SAXException {

        Node toImport = m_docNS.getFirstChild();
        ((Document) m_node).importNode(toImport, true);

        toImport = m_docNS.getLastChild();
        ((Document) m_node).importNode(toImport, false);

        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();

        parser.parse(new InputSource(new StringReader(sXmlNS)));

        Document xercesDocument = parser.getDocument();
        assertNotNull(xercesDocument);
        toImport = xercesDocument.getFirstChild();
        ((Document) m_node).importNode(toImport, true);

        toImport = xercesDocument.getLastChild();
        ((Document) m_node).importNode(toImport, false);

        toImport = null;
        ((Document) m_node).importNode(toImport, false);

        ((Document) m_node).importNode(toImport, true);
    }

    /**
     * ATTRIBUTE_NODE
     * The ownerElement attribute is set to null
     * and the specified flag is set to true on the generated Attr
     * The descendants of the source Attr are recursively imported and the resulting
     * nodes reassembled to form the corresponding subtree
     * Note that the deep parameter has no effect on Attr nodes;
     * they always carry their children with them when imported
     */
    @Test
    void testImportAttrNode() {
        Node toImport = m_doc.getFirstChild().getAttributes().item(0);
        toImport.appendChild(m_doc.createTextNode("more text"));
        Node imported = m_docNS.importNode(toImport, false);

        assertNull(imported.getParentNode());
        assertEquals(Node.ATTRIBUTE_NODE, imported.getNodeType());
        assertEquals(2, imported.getChildNodes().getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);
    }

    /**
     * DOCUMENT_FRAGMENT_NODE
     * If the deep option was set to true,
     * the descendants of the source element are
     * recursively imported and the resulting nodes reassembled to form the
     * corresponding subtree.
     * Otherwise, this simply generates an empty DocumentFragment.
     */
    @Test
    void testImportDocFrag() {
        Node toImport = m_doc.createDocumentFragment();
        toImport.appendChild(m_doc.getFirstChild());
        toImport.appendChild(m_doc.createTextNode("some text"));

        Node imported = m_docNS.importNode(toImport, false);
        assertNull(imported.getParentNode());
        assertEquals(Node.DOCUMENT_FRAGMENT_NODE, imported.getNodeType());
        assertFalse(imported.hasChildNodes());
        assertEquals(imported.getOwnerDocument(), m_docNS);

        imported = m_docNS.importNode(toImport, true);
        assertNull(imported.getParentNode());
        assertEquals(Node.DOCUMENT_FRAGMENT_NODE, imported.getNodeType());
        assertEquals(2, imported.getChildNodes().getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);
    }

    /**
     * DOCUMENT_NODE
     * Document nodes cannot be imported.
     */
    @Test
    void testImportDocument() {
        DOMException e1 = assertThrows(DOMException.class, () -> m_docNS.importNode(m_doc, false));
        assertEquals(DOMException.NOT_SUPPORTED_ERR, e1.code);

        DOMException e2 = assertThrows(DOMException.class, () -> m_docNS.importNode(m_doc, true));
        assertEquals(DOMException.NOT_SUPPORTED_ERR, e2.code);
    }


    /**
     * ELEMENT_NODE
     * Specified attribute nodes of the source element are imported,
     * and the generated Attr nodes are attached to the generated Element.
     * Default attributes are not copied, though
     * if the document being imported into defines default
     * attributes for this element name, those are assigned.
     * If the importNode deep parameter was set to true, the descendants of
     * the source element are recursively imported and the resulting nodes
     * reassembled to form the corresponding subtree.
     */
    //TODO: specified and default attributes
    @Test
    void testImportElement() {
        Node toImport = m_doc.getFirstChild();
        Node imported = m_docNS.importNode(toImport, false);

        assertNull(imported.getParentNode());
        assertEquals(Node.ELEMENT_NODE, imported.getNodeType());
        assertEquals(0, imported.getChildNodes().getLength());
        assertEquals(5, imported.getAttributes().getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);

        imported = m_docNS.importNode(toImport, true);

        assertNull(imported.getParentNode());
        assertEquals(Node.ELEMENT_NODE, imported.getNodeType());
        assertEquals(1, imported.getChildNodes().getLength());
        assertEquals(5, imported.getAttributes().getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);
    }

    /*
     * DOCUMENT_TYPE_NODE
     * Test in ../ImportUnsupportedNodes
     */
    /*
     * ENTITY_NODE
     * Test in ../ImportUnsupportedNodes
     */

    /*
     * ENTITY_REFERENCE_NODE
     * Test in ../ImportUnsupportedNodes
     */
    /*
     * NOTATION_NODE
     * Test in ../ImportUnsupportedNodes
     */


    /**
     * PROCESSING_INSTRUCTION_NODE
     * The imported node copies its target and data
     * values from those of the source node.
     */
    @Test
    void testImportPI() {
        Node pi = m_doc.createProcessingInstruction("xml-stylesheet",
                "do something");
        m_doc.getFirstChild().appendChild(pi);

        Node imported = m_docNS.importNode(pi, false);
        assertNull(imported.getParentNode());
        assertEquals(Node.PROCESSING_INSTRUCTION_NODE, imported.getNodeType());
        assertEquals("do something", ((ProcessingInstruction) imported).getData());
        assertEquals("xml-stylesheet", ((ProcessingInstruction) imported).getTarget());
        assertEquals(imported.getOwnerDocument(), m_docNS);
    }

    /**
     * TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE
     * These three types of nodes inheriting from CharacterData copy their
     * data and length attributes from those of the source node.
     */
    @Test
    void testImportChars() {
        //import CDATA--nothing to do--it's always text

        //import text
        Node txt = m_doc.createTextNode("some text");
        m_doc.getFirstChild().appendChild(txt);

        Node imported = m_docNS.importNode(
                m_doc.getFirstChild().getLastChild(), false);

        assertNull(imported.getParentNode());
        assertEquals(Node.TEXT_NODE, imported.getNodeType());
        assertEquals("some text", ((Text) imported).getData());
        assertEquals(9, ((Text) imported).getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);



        //import Comment
        txt = m_doc.createComment("some text");
        m_doc.getFirstChild().appendChild(txt);
        assertNull(imported.getParentNode());
        imported = m_docNS.importNode(m_doc.getFirstChild().getLastChild(), false);

        assertEquals(Node.COMMENT_NODE, imported.getNodeType());
        assertEquals("some text", ((Comment) imported).getData());
        assertEquals(9, ((Comment) imported).getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);


    }

    @Test
    void testImportNodeNull() {
        assertDoesNotThrow(() -> ((Document) m_node).importNode(null, true));
        assertDoesNotThrow(() -> ((Document) m_node).importNode(null, false));
    }
}
