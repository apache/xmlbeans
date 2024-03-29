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


import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.w3c.domts.DOMTest.load;


/**
 * The importNode method imports a node from another document to this document.
 * The returned node has no parent; (parentNode is null). The source node is not
 * altered or removed from the original document but a new copy of the source node
 * is created.
 * Using the method importNode with deep=false, import a newly created DocumentFragment node
 * with the first address element from this Document appended to it into this document.
 * Since deep=false, an empty DocumentFragment should be returned
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core">http://www.w3.org/TR/DOM-Level-2-Core/core</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode">http://www.w3.org/TR/DOM-Level-2-Core/core#Core-Document-importNode</a>
 */
public class documentimportnode09 {
    @Test
    void testRun() throws Throwable {
        Document doc = load("staffNS", true);
        DocumentFragment docFragment = doc.createDocumentFragment();
        NodeList childList = doc.getElementsByTagNameNS("*", "address");
        Node addressNode = childList.item(0);
        docFragment.appendChild(addressNode);
        Node importedDocFrag = doc.importNode(docFragment, false);
        boolean success = importedDocFrag.hasChildNodes();
        assertFalse(success, "documentimportnode09");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/documentimportnode09";
    }

}
