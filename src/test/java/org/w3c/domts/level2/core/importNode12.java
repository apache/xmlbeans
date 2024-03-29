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


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.w3c.domts.DOMTest.assertURIEquals;
import static org.w3c.domts.DOMTest.load;


/**
 * The "importNode(importedNode,deep)" method for a
 * Document should import the given importedNode into that Document.
 * The importedNode is of type Entity.
 * Retrieve entity "ent4" from staffNS.xml document.
 * Invoke method importNode(importedNode,deep) on this document with deep as false.
 * Method should return a node of type Entity whose descendant is copied.
 * The returned node should belong to this document whose systemId is "staffNS.dtd"
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode">http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode</a>
 */
public class importNode12 {
    @Test
    @Disabled
    public void testRun() throws Throwable {
        Document doc = load("staffNS", true);
        Document aNewDoc = load("staffNS", true);

        DocumentType doc1Type = aNewDoc.getDoctype();
        NamedNodeMap entityList = doc1Type.getEntities();
        Entity entity2 = (Entity) entityList.getNamedItem("ent4");
        Entity entity1 = (Entity) doc.importNode(entity2, true);
        Document ownerDocument = entity1.getOwnerDocument();
        DocumentType docType = ownerDocument.getDoctype();
        String system = docType.getSystemId();
        assertURIEquals("systemId", "staffNS.dtd", system);
        String entityName = entity1.getNodeName();
        assertEquals("ent4", entityName, "entityName");
        Node child = entity1.getFirstChild();
        assertNotNull(child, "notnull");
        String childName = child.getNodeName();
        assertEquals("entElement1", childName, "childName");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/importNode12";
    }

}
