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

package dom.detailed;

import dom.common.Loader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;


public class ImportUnsupportedNodesTest {
    String sXml="<foo at0=\"no_ns_attr\"></foo>";
    // String sXmlNS="<foo><foobar  xmlns:myns=\"http://foo.org\" xmlns:other=\"other.org\"><myns:bar/></foobar></foo>";
    Document m_doc;
    Node m_node;
    String sER="<!DOCTYPE note [<!ENTITY ORG \"IICD\">] >"
        +"<foo>&ORG;</foo>";


	//TODO: see if code coverage can help id gaps here...
	@BeforeEach
	public void setUp() throws Exception{
		Loader _loader = Loader.getLoader();
		if (sXml == null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
		m_doc = (org.w3c.dom.Document) _loader.load(sXml);

		m_node = m_doc.getFirstChild();
	}


	@Test
	@Disabled("not implemented")
    public void testImportEnitityNode()throws Exception{
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
		parser.parse(new InputSource(new StringReader(sER)));
		Document xercesDocument = parser.getDocument();
		assertNotNull(xercesDocument);
		Node toImport = xercesDocument.getDoctype().getEntities().item(0);
		assertEquals(Node.ENTITY_NODE, toImport.getNodeType());
		Node importedNode = m_doc.importNode(toImport, true);
		m_node.insertBefore(importedNode, m_node.getFirstChild());

		assertEquals(importedNode, m_node.getFirstChild());
		assertEquals(Node.ENTITY_NODE, m_node.getFirstChild().getNodeType());
    }

	@Test
	@Disabled("not implemented")
    public void testImportERNode()throws Exception{
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
		parser.parse(new InputSource(new StringReader(sER)));
		Document xercesDocument = parser.getDocument();
		assertNotNull(xercesDocument);
		Node toImport = xercesDocument.getDocumentElement().getFirstChild();

		assertEquals(Node.ENTITY_REFERENCE_NODE, toImport.getNodeType());
		Node importedNode = m_doc.importNode(toImport, true);
		m_node.insertBefore(importedNode, m_node.getFirstChild());

		assertEquals(importedNode, m_node.getFirstChild());
		assertEquals(Node.ENTITY_REFERENCE_NODE, m_node.getFirstChild().getNodeType());
    }

    /**
     *   DOCUMENT_TYPE_NODE
     *   cannot be imported.
     */
	@Test
    void testImportDocType() throws Exception{
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
		parser.parse(new InputSource(new StringReader(sER)));
		Document xercesDocument = parser.getDocument();
		assertNotNull(xercesDocument);
		Node toImport = xercesDocument.getDoctype();

		assertThrows(DOMException.class, () -> m_doc.importNode(toImport, true), "can't import DocType Node");

		assertThrows(DOMException.class, () -> m_doc.importNode(toImport, false));
     }

	@Test
	void testImportCDATAType() throws Exception{
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
		parser.parse(new InputSource(new StringReader(sER)));
		Document xercesDocument = parser.getDocument();
		assertNotNull(xercesDocument);
		Node toImport = xercesDocument.createCDATASection("My < CData");
		xercesDocument.getDocumentElement().appendChild(toImport);

		assertEquals(Node.CDATA_SECTION_NODE, toImport.getNodeType());
		Node importedNode = m_doc.importNode(toImport, true);
		m_node.insertBefore(importedNode, m_node.getFirstChild());

		assertEquals(importedNode, m_node.getFirstChild());
		assertEquals(Node.CDATA_SECTION_NODE, m_node.getFirstChild().getNodeType());


		assertEquals(Node.CDATA_SECTION_NODE, toImport.getNodeType());
		importedNode = m_doc.importNode(toImport, false);
		m_node.replaceChild(importedNode, m_node.getFirstChild());

		assertEquals(importedNode, m_node.getFirstChild());
		assertEquals(Node.CDATA_SECTION_NODE, m_node.getFirstChild().getNodeType());
     }
}

