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
import static org.w3c.domts.DOMTest.load;


/**
 * The importNode method imports a node from another document to this document.
 * The returned node has no parent; (parentNode is null). The source node is not
 * altered or removed from the original document but a new copy of the source node
 * is created.
 * Using the method importNode with deep=true/false, import a entity nodes ent2 and ent6
 * from this document to a new document object.  Verify if the nodes have been
 * imported correctly by checking the nodeNames of the imported nodes and public and system ids.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core">http://www.w3.org/TR/DOM-Level-2-Core/core</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode">http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode</a>
 */
public class documentimportnode19 {
    @Test
    @Disabled
    public void testRun() throws Throwable {
        DocumentType docTypeNull = null;

        Document doc = load("staffNS", true);
        DOMImplementation domImpl = doc.getImplementation();
        DocumentType docType = doc.getDoctype();
        Document docImp = domImpl.createDocument("http://www.w3.org/DOM/Test", "a:b", docTypeNull);
        NamedNodeMap nodeMap = docType.getEntities();
        Entity entity2 = (Entity) nodeMap.getNamedItem("ent2");
        Entity entity6 = (Entity) nodeMap.getNamedItem("ent6");
        Entity entityImp2 = (Entity) docImp.importNode(entity2, false);
        Entity entityImp6 = (Entity) docImp.importNode(entity6, true);
        String nodeName = entity2.getNodeName();
        String nodeNameImp = entityImp2.getNodeName();
        assertEquals(nodeName, nodeNameImp, "documentimportnode19_Ent2NodeName");
        nodeName = entity6.getNodeName();
        nodeNameImp = entityImp6.getNodeName();
        assertEquals(nodeName, nodeNameImp, "documentimportnode19_Ent6NodeName");
        String systemId = entity2.getSystemId();
        String systemIdImp = entityImp2.getSystemId();
        assertEquals(systemId, systemIdImp, "documentimportnode19_Ent2SystemId");
        systemId = entity6.getSystemId();
        systemIdImp = entityImp6.getSystemId();
        assertEquals(systemId, systemIdImp, "documentimportnode19_Ent6SystemId");
        String notationName = entity2.getNotationName();
        String notationNameImp = entityImp2.getNotationName();
        assertEquals(notationName, notationNameImp, "documentimportnode19_Ent2NotationName");
        notationName = entity6.getNotationName();
        notationNameImp = entityImp6.getNotationName();
        assertEquals(notationName, notationNameImp, "documentimportnode19_Ent6NotationName");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/documentimportnode19";
    }

}
