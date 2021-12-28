/*
This Java source file was generated by test-to-java.xsl
and is a derived work from the source document.
The source document contained the following notice:



Copyright (c) 2001 World Wide Web Consortium,
(Massachusetts Institute of Technology, Institut National de
Recherche en Informatique et en Automatique, Keio University).  All
Rights Reserved.  This program is distributed under the W3C's Software
Intellectual Property License.  This program is distributed in the
hope that it will be useful, but WITHOUT ANY WARRANTY; without even
the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.

See W3C License http://www.w3.org/Consortium/Legal/ for more details.


*/

package org.w3c.domts.level2.core;


import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.domts.DOMTestCase;

import static org.junit.Assert.assertEquals;


/**
 * The "importNode(importedNode,deep)" method for a
 * Document should import the given importedNode into that Document.
 * The importedNode is of type Entity_Reference.
 * Only the EntityReference is copied, regardless of deep's value.
 * Create an entity reference whose name is "entRef1" in a different document.
 * Give it value "entRef1Value".
 * Invoke method importNode(importedNode,deep) on this document with importedNode
 * being "entRef1".
 * Method should return a node of type Entity_Reference (whose value is null) that
 * belongs to this document whose systemId is "staff.dtd".
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode">http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode</a>
 */
public class importNode10 extends DOMTestCase {
    @Test
    @Ignore
    public void testRun() throws Throwable {
        Document doc;
        Document aNewDoc;
        EntityReference entRef;
        Node aNode;
        Document ownerDocument;
        DocumentType docType;
        String system;
        String name;
        doc = load("staffNS", true);
        aNewDoc = load("staffNS", true);
        entRef = aNewDoc.createEntityReference("entRef1");
        entRef.setNodeValue("entRef1Value");
        aNode = doc.importNode(entRef, false);
        ownerDocument = aNode.getOwnerDocument();
        docType = ownerDocument.getDoctype();
        system = docType.getSystemId();
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
        name = aNode.getNodeName();
        assertEquals("nodeName", "entRef1", name);

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/importNode10";
    }

}