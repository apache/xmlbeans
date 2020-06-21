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
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;


public class DocumentTest extends NodeWithChildrenTest {


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
    public void testNodeName() {
        assertEquals("#document", m_node.getNodeName());
    }

    @Test
    public void testNodeType() {
        assertEquals(Node.DOCUMENT_NODE, m_node.getNodeType());
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
        assertNull(m_node.getPreviousSibling());
    }

    @Test
    public void testParent() {
        assertNull(m_node.getParentNode());
    }

    @Test
    public void testOwnerDocument() {
        assertNull(m_node.getOwnerDocument());//API spec
    }

    @Test
    public void testChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    public void testFirstChild() {
        assertEquals("foo", m_node.getFirstChild().getLocalName());
    }

    @Test
    public void testLastChild() {
        assertEquals("foo", m_node.getLastChild().getLocalName());
    }

    @Test
    public void testAppendChild() {
        try {
            super.testAppendChild();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testInsertBefore() {
        try {
            super.testInsertBefore();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testInsertBeforeNullTarget() {
        try {
            super.testInsertBeforeNullTarget();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testInsertExistingNode() {
        try {
            super.testInsertExistingNode(m_node.getFirstChild());
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testInsertBeforeInvalidRefNode() {
        try {
            super.testInsertBeforeInvalidRefNode();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
        catch (AssertionError af) {
            assertEquals(((DOMException) af.getCause()).code,
                    DOMException.HIERARCHY_REQUEST_ERR);
        }
    }

    @Test
    public void testAppendChildIllegal0() {
        try {
            super.testAppendChildIllegal0();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
        catch (AssertionError af) {
            assertEquals(((DOMException) af.getCause()).code,
                    DOMException.HIERARCHY_REQUEST_ERR);
        }
    }

    @Test
    public void testAppendChildIllegal1() {
        try {
            super.testAppendChildIllegal1();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
        catch (AssertionError af) {
            assertEquals(((DOMException) af.getCause()).code,
                    DOMException.HIERARCHY_REQUEST_ERR);
        }
    }

    @Test
    public void testGetChildNodes() {
        assertEquals(1, m_node.getChildNodes().getLength());
    }

    @Test
    public void testSetPrefix() {
        super.testSetPrefix(); //see charData--is the exception correct
    }

    @Test
    public void testInsertExisitingNode() {
        Node child = m_doc.getFirstChild().getFirstChild();//some text
        if (child == m_node)
            child = m_doc.getLastChild();
        try{
         super.testInsertExistingNode(child);
        }catch (DOMException de){
            //never can insert anything unless doc is empty
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);

        }
    }

    @Test
    public void testAppendChildExisting() {
        Node child = m_node.getFirstChild().getFirstChild();
        try {
            super.testAppendChildExisting(child);
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    public void testNormalize() {
        //unque doc child--normalize in elt. or text or comment, etc
    }

    @Test
    public void testInsertBeforeDocFrag() {
        try {
            super.testInsertBeforeDocFrag();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testAppendChildDocFrag() {
        try {
            super.testAppendChildDocFrag();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testReplaceChildFront() {
        Node node = m_doc.getDocumentElement();
        assertEquals(node, m_node.getFirstChild());
        Node newNode = m_doc.createElement("fooBAR");
        super.testReplaceChild(newNode, node);
        assertEquals(m_doc.getDocumentElement(), newNode);
    }

    @Test
    public void testReplaceChildEnd() {
        Node node = m_doc.getDocumentElement();
        assertEquals(node, m_node.getFirstChild());
        Node newNode = m_doc.createElement("fooBAR");
        super.testReplaceChild(newNode, node);
    }

    @Test
    public void testReplaceChildDocFrag() {
        try {
            super.testReplaceChildDocFrag();
        }
        catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    public void testCreateAttribute() {
        Attr att = m_doc.createAttribute("at0");
        assertNull(att.getOwnerElement());
        assertEquals(m_doc, att.getOwnerDocument());

        assertFalse(att.hasChildNodes());
        assertEquals("", att.getValue());
    }

    @Test
    public void testCreateAttributeNS() {
        Attr att = m_doc.createAttributeNS("foo:uri", "at0");
        assertEquals("foo:uri", att.getNamespaceURI());
        assertNull(att.getOwnerElement());
        assertEquals(m_doc, att.getOwnerDocument());
    }

    @Test
    public void testCreateCDATASection() {
        CDATASection cdata = m_doc.createCDATASection("<CDATA Section>");
        assertNull(cdata.getParentNode());
        assertEquals(m_doc, cdata.getOwnerDocument());

        cdata = m_doc.createCDATASection(null);
        assertNull(cdata.getParentNode());
        assertEquals(m_doc, cdata.getOwnerDocument());
        assertEquals("", cdata.getData());
    }

    @Test
    public void testCreateComment() {
        Comment comment = m_doc.createComment("A comment");
        assertNull(comment.getParentNode());
        assertEquals(m_doc, comment.getOwnerDocument());

        comment = m_doc.createComment(null);
        assertEquals("", comment.getData());
    }

    @Test
    public void testCreateDocumentFragment() {
        DocumentFragment doc_frag = m_doc.createDocumentFragment();
        assertNull(doc_frag.getParentNode());
        assertEquals(m_doc, doc_frag.getOwnerDocument());

    }

    @Test
    public void testCreateElement() {
        Element elt1 = m_doc.createElement("elt1");
        assertNull(elt1.getParentNode());
        assertEquals(m_doc, elt1.getOwnerDocument());

    }

    @Test
    public void testCreateElementNS() {
        Element elt1 = m_doc.createElementNS("uri:foo", "ns:elt1");
        assertEquals("uri:foo", elt1.getNamespaceURI());
        assertNull(elt1.getParentNode());
        assertEquals(m_doc, elt1.getOwnerDocument());
    }

    @Test
    public void testCreateProcessingInstruction() {
        ProcessingInstruction pi = null;
        try {
            pi = m_doc.createProcessingInstruction("xml", "version 1.0");
            assertNull(pi.getParentNode());
            assertEquals(m_doc, pi.getOwnerDocument());
            fail(" this is a no-go");
        }
        catch (Throwable t) {
            //TODO: ensure right exception here
        }
        String val = null;
        pi = m_doc.createProcessingInstruction("xml-foo", val);
        assertEquals("", pi.getData());


        try {
            pi = m_doc.createProcessingInstruction(null, "foo");
            fail("PI target can't be null");
        }
        catch (IllegalArgumentException e) {

        }


        try {
            pi = m_doc.createProcessingInstruction("invalid@", "foo");
            fail("Invalid pi name");
        }
        catch (DOMException de) {
            assertEquals(DOMException.INVALID_CHARACTER_ERR, de.code);
        }

    }

    @Test
    public void testCreateTextNode() {
        Text txt0 = m_doc.createTextNode("foo");
        assertNull(txt0.getParentNode());
        assertEquals(m_doc, txt0.getOwnerDocument());

        txt0 = m_doc.createTextNode(null);
        assertEquals("", txt0.getData());
    }

    @Test
    public void testGetDocumentElement() {
        assertEquals(m_doc.getDocumentElement(), m_node.getFirstChild());
    }

    @Test
    public void testGetElementsByTagName() {
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
    public void testGetElementsByTagNamePreorder() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagName("foo");
        assertEquals(2, result.getLength());
        assertEquals("txt0", result.item(0).getFirstChild().getNodeValue());
        assertEquals("nestedfoo",
                result.item(1).getFirstChild().getNodeValue());
    }

    @Test
    public void testGetElementsByTagNameDNE() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagName("foobar");
        assertEquals(0, result.getLength());
    }

    @Test
    public void testGetElementsByTagNameNS() {
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
        assertEquals("nestedfoo",
                result.item(1).getFirstChild().getNodeValue());
        NodeList result1 = ((Document) m_node).getElementsByTagNameNS("",
                "foo");
        assertTrue(compareNodeList(result, result1));


        result = ((Document) m_node).getElementsByTagNameNS(null, "*");
        assertEquals(4, result.getLength());
        assertEquals("ZeD", ((Element) result.item(3)).getTagName());
    }

    @Test
    public void testGetElementsByTagNameNS_DNE() {
        m_node = m_docNS;
        NodeList result = ((Document) m_node).getElementsByTagNameNS("uri:foo",
                "zed");
        assertEquals(0, result.getLength());

        result =
                ((Document) m_node).getElementsByTagNameNS("foo:uri_DNE",
                        "foo");
        assertEquals(0, result.getLength());

    }

    @Test
    public void testGetImplementation() {
        assertTrue(
                m_doc.getImplementation().toString().startsWith(
                        "org.apache.xmlbeans.impl.store"));
    }

    @Test
    public void testImportNode() {

        Node toImport = m_docNS.getFirstChild();
        ((Document) m_node).importNode(toImport, true);

        toImport = m_docNS.getLastChild();
        ((Document) m_node).importNode(toImport, false);

        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();

        try {
            parser.parse(new InputSource(new StringReader(sXmlNS)));
        }
        catch (org.xml.sax.SAXException se) {
            se.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }

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
    public void testImportAttrNode() {
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
    public void testImportDocFrag() {
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
    public void testImportDocument() {
        DOMException e1 = null;
        try {
            m_docNS.importNode(m_doc, false);
        }
        catch (DOMException de) {
            e1 = de;
        }
        try {
            m_docNS.importNode(m_doc, true);
            fail("This should fail");
        }
        catch (DOMException de) {
            assertEquals(DOMException.NOT_SUPPORTED_ERR, de.code);
        }
        if (e1 == null)
            fail("Cant import doc node");
        assertEquals(DOMException.NOT_SUPPORTED_ERR, e1.code);
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
    public void testImportElement() {
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

    /**
     * DOCUMENT_TYPE_NODE
     * Test in ../ImportUnsupportedNodes
     */
    /**
     * ENTITY_NODE
     * Test in ../ImportUnsupportedNodes
     */

    /**
     * ENTITY_REFERENCE_NODE
     * Test in ../ImportUnsupportedNodes
     */
    /**
     * NOTATION_NODE
     * Test in ../ImportUnsupportedNodes
     */
    /**
     * PROCESSING_INSTRUCTION_NODE
     * The imported node copies its target and data
     * values from those of the source node.
     */
    @Test
    public void testImportPI() {
        Node pi = m_doc.createProcessingInstruction("xml-stylesheet",
                "do something");
        m_doc.getFirstChild().appendChild(pi);

        Node imported = m_docNS.importNode(pi, false);
        assertNull(imported.getParentNode());
        assertEquals(Node.PROCESSING_INSTRUCTION_NODE, imported.getNodeType());
        assertEquals("do something",
                ((ProcessingInstruction) imported).getData());
        assertEquals("xml-stylesheet",
                ((ProcessingInstruction) imported).getTarget());
        assertEquals(imported.getOwnerDocument(), m_docNS);
    }

    /**
     * TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE
     * These three types of nodes inheriting from CharacterData copy their
     * data and length attributes from those of the source node.
     */
    @Test
    public void testImportChars() {
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
        imported =
                m_docNS.importNode(m_doc.getFirstChild().getLastChild(), false);

        assertEquals(Node.COMMENT_NODE, imported.getNodeType());
        assertEquals("some text", ((Comment) imported).getData());
        assertEquals(9, ((Comment) imported).getLength());
        assertEquals(imported.getOwnerDocument(), m_docNS);


    }

    @Test
    public void testImportNodeNull() {
        Node _Null = null;
        ((Document) m_node).importNode(_Null, true);
        ((Document) m_node).importNode(_Null, false);

    }

    public void moveToNode() {
        m_node = m_doc;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        moveToNode();
    }
}
