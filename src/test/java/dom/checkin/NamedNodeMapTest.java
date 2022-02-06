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
import dom.common.NodeTest;
import dom.common.TestSetup;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import static org.junit.jupiter.api.Assertions.*;

public class NamedNodeMapTest implements TestSetup {
    private Document m_doc;
    private Document m_docNS;
    private Node m_node;
    private NamedNodeMap m_nodeMap;
    private final String sXml = "<foo at0=\"val0\" at1=\"val1\" at2=\"val2\" at3=\"val3\" at4=\"val4\"><bar bat0=\"val0\">abc</bar></foo>";
    private final String sXmlNS = "<foo xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" myns:at4=\"val4\">  <bar>abc</bar></foo>";
    private Node result;
    private int nCount = 5;
    private Loader _loader;


    @BeforeEach
    public void setUp() throws Exception {
        m_doc = (org.w3c.dom.Document) XmlObject.Factory.parse( sXml ).getDomNode();
        m_docNS = (org.w3c.dom.Document) XmlObject.Factory.parse( sXmlNS ).getDomNode();
        moveToNode();
    }


    @Test
    void testLength() {
        //assertEquals(m_nodeMap.length,nCount);
        assertEquals(m_nodeMap.getLength(), nCount);
    }

    @Test
    void testGetNamedItem() {
        result = m_nodeMap.getNamedItem("at0");
        assertEquals("val0", result.getNodeValue());

        result = m_nodeMap.getNamedItem("at4");
        assertEquals("val4", result.getNodeValue());
    }

    @Test
    void testGetNamedItemDNE() {
        result = m_nodeMap.getNamedItem("attt4");
        assertNull(result);
    }

    @Test
    void testGetNamedItemNS() {
        m_nodeMap = m_docNS.getFirstChild().getAttributes();

        result = m_nodeMap.getNamedItemNS("uri:foo", "at0");
        assertEquals("val01", result.getNodeValue());
        assertEquals("myns:at0", result.getNodeName());
        assertEquals("myns", result.getPrefix());

        result = m_nodeMap.getNamedItemNS("uri:foo", "at0");
        assertEquals("val01", result.getNodeValue());

        result = m_nodeMap.getNamedItemNS("", "at3");
        assertEquals("val3", result.getNodeValue());

        result = m_nodeMap.getNamedItemNS(null, "at3");
        assertEquals("val3", result.getNodeValue());
    }

    @Test
    void testGetNamedItemNS_DNE() {
        m_nodeMap = m_docNS.getFirstChild().getAttributes();

        result = m_nodeMap.getNamedItemNS("uri:fol", "at0");
        assertNull(result);

        result = m_nodeMap.getNamedItemNS("uri:foo", "at1");
        assertNull(result);

        result = m_nodeMap.getNamedItemNS("uri:foo", null);
        assertNull(result);

        /*  This test is only possible if "" neq null
            result=m_nodeMap.getNamedItemNS("","at4");
            assertEquals(null,result);
           */
    }

    @Test
    void testItem() {
        result = m_nodeMap.item(0);
        assertEquals("val0", result.getNodeValue());
        result = m_nodeMap.item(3);
        assertEquals("val3", result.getNodeValue());
    }

    @Test
    void testItemNeg() {
        assertNotNull(m_nodeMap);
        assertNull(m_nodeMap.item(-1));
    }

    @Test
    void testItemLarge() {
        assertNotNull(m_nodeMap);
        assertNull(m_nodeMap.item(m_nodeMap.getLength() + 1));
    }

    /**
     * $NOTE: to do
     * read-only map
     * attr w/ default val
     */
    @Test
    void testRemoveNamedItemNull() {
        try {
            m_nodeMap.removeNamedItem(null);
            Assertions.fail("removing a non-existing value");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.NOT_FOUND_ERR);
        }
    }

    @Test
    void testRemoveNamedItem() {
        try {
            m_nodeMap.removeNamedItem("at7");
            Assertions.fail("removing a non-existing value");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.NOT_FOUND_ERR);
        }

        result = m_nodeMap.removeNamedItem("at3");
        assertEquals("val3", result.getNodeValue());
        assertNull(m_nodeMap.getNamedItem("at3"));
        //liveness test
        assertEquals(m_node.getAttributes().getLength(), nCount - 1);
        assertNull(m_node.getAttributes().getNamedItem("at3"));
    }

    @Test
    void testRemoveNamedItemNS() {
        m_node = m_docNS.getDocumentElement();
        m_nodeMap = m_node.getAttributes();
        result = m_nodeMap.getNamedItemNS("uri:foo", "at0");
        assertEquals("val01", result.getNodeValue());

        nCount = m_node.getAttributes().getLength();

        if (bDTD) {
            assertEquals(m_nodeMap.getNamedItem("at0").getNodeValue(), "val0"); //default ns attr still here
        }

        result = m_nodeMap.getNamedItemNS("uri:foo", "at4");
        assertEquals(result, m_nodeMap.removeNamedItemNS("uri:foo", "at4"));
        assertNull(m_nodeMap.getNamedItemNS("uri:foo", "at4"));

        assertEquals(nCount - 1, m_node.getAttributes().getLength());

        result = m_nodeMap.removeNamedItemNS(null, "at3");
        assertEquals("val3", result.getNodeValue());
        assertNull(m_nodeMap.getNamedItem("at3"));
        assertEquals(m_node.getAttributes().getLength(), nCount - 2);

        //liveness test
        assertEquals(nCount - 2, m_docNS.getFirstChild().getAttributes().getLength());
        assertNull(m_docNS.getFirstChild().getAttributes().getNamedItem("at3"));
    }

    @Test
    void testRemoveNamedItemNS_DNE() {
        m_nodeMap = m_docNS.getFirstChild().getAttributes();
        int nLen = m_node.getAttributes().getLength();
        try {
            result = m_nodeMap.removeNamedItemNS("uri:fo1", "at0");
            if (m_node.getAttributes().getLength() != nLen)
                Assertions.fail("removing a non-existing attr");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.NOT_FOUND_ERR);
        }

        try {
            result = m_nodeMap.getNamedItemNS("uri:fo1", null);
            if (result != null)
                Assertions.fail("removing a non-existing attr");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.NOT_FOUND_ERR);
        }
    }

    /**
     * pathological cases:
     * node created from diff doc
     * node in diff elt
     * node not an attr
     */
    @Test
    void testSetNamedItem() {
        Node newAt = m_doc.createAttribute("newAt");
        ((Attr) newAt).setValue("newval");
        m_nodeMap.setNamedItem(newAt);
        assertEquals(nCount + 1, m_nodeMap.getLength());
        result = m_nodeMap.getNamedItem("newAt");
        assertEquals("newval", result.getNodeValue());

        //node cr. diff doc
        newAt = m_docNS.createAttribute("newAt");
        ((Attr) newAt).setValue("newval");
        try {
            m_nodeMap.setNamedItem(newAt);
            Assertions.fail("Inserting node created from a different doc");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }

        //node in diff elt
        newAt = m_node.getFirstChild().getAttributes().getNamedItem("bat0");
        try {
            m_nodeMap.setNamedItem(newAt);
            Assertions.fail("Inserting node in use");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.INUSE_ATTRIBUTE_ERR);
        }

        //not an attr
        newAt = m_doc.createElement("newElt");
        try {
            m_nodeMap.setNamedItem(newAt);
            Assertions.fail("Inserting node  different doc");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.HIERARCHY_REQUEST_ERR);
        }

    }

    @Test
    void testSetNamedItemNull() {
        assertThrows(IllegalArgumentException.class, () -> m_nodeMap.setNamedItem(null));
    }

    @Test
    void testSetNamedItemDiffImpl() throws Exception {
        Node toSet = NodeTest.getApacheNode(sXml, false, 'A');
        try {
            m_nodeMap.setNamedItem(toSet);
            Assertions.fail("Inserting node  different impl");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }
    }

    @Test
    void testSetNamedItemNS() {
        Attr newAt = m_doc.createAttributeNS("uri:foo", "newAt");
        newAt.setValue("newval");
        m_nodeMap.setNamedItemNS(newAt);
        assertEquals(nCount + 1, m_nodeMap.getLength());
        result = m_nodeMap.getNamedItemNS("uri:foo", "newAt");
        assertEquals("newval", result.getNodeValue());
        //OK, reset value
        newAt.setValue("newval_overwrite");
        m_nodeMap.setNamedItemNS(newAt);
        assertEquals(nCount + 1, m_nodeMap.getLength());
        result = m_nodeMap.getNamedItemNS("uri:foo", "newAt");
        assertEquals("newval_overwrite", result.getNodeValue());

        newAt = m_doc.createAttributeNS("uri:foo1", "newAt");
        newAt.setValue("newval1");
        //insert a new item
        m_nodeMap.setNamedItemNS(newAt);
        assertEquals(nCount + 2, m_nodeMap.getLength());
        result = m_nodeMap.getNamedItemNS("uri:foo1", "newAt");
        assertEquals("newval1", result.getNodeValue());
        //the path cases are the same as in SetNamedItem
    }

    @Test
    void testSetNamedItemNSNull() {
        assertThrows(IllegalArgumentException.class, () -> m_nodeMap.setNamedItemNS(null));
    }

    @Test
    void testSetNamedItemNSDiffImpl() throws Exception {
        Node toSet = NodeTest.getApacheNode(sXml, true, 'A');
        try {
            m_nodeMap.setNamedItemNS(toSet);
            Assertions.fail("Inserting node  different impl");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.WRONG_DOCUMENT_ERR);
        }
    }

    //try to set a node of a diff type than the current collection
    @Test
    void testSetNamedItemDiffType() throws Exception {
        Node toSet = m_doc.createElement("foobar");
        try {
            m_nodeMap.setNamedItem(toSet);
            Assertions.fail("Inserting node  different impl");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.HIERARCHY_REQUEST_ERR);
        }
    }

    @Test
    void testSetNamedItemNSDiffType() throws Exception {
        Node toSet = m_doc.createElementNS("foo:org", "com:foobar");
        try {
            m_nodeMap.setNamedItemNS(toSet);
            Assertions.fail("Inserting node  different impl");
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.HIERARCHY_REQUEST_ERR);
        }
    }


    public void moveToNode() {
        m_node = m_doc.getFirstChild();
        m_nodeMap = m_node.getAttributes();

    }

    public void loadSync() throws Exception {
        _loader = Loader.getLoader();
        m_doc = _loader.loadSync(sXml);
        m_docNS = _loader.loadSync(sXmlNS);

    }
}

