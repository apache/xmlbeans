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


import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.w3c.domts.DOMTest.load;


/**
 * The "getElementsByTagNameNS(namespaceURI,localName)" method returns a NodeList
 * of all descendant Elements with a given local name and namespace URI in the
 * order in which they are encountered in a preorder traversal of this Element tree.
 * Create a NodeList of all the descendant elements
 * using the special value "*" as the namespaceURI and "address" as the
 * localName.
 * The method should return a NodeList of Elements that have
 * "address" as the local name.
 * This test is derived from getElementsByTagNameNS04
 * * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-1938918D">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-1938918D</a>
 */
public class getElementsByTagNameNS11 {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        Element docElem;
        NodeList elementList;
        Node child;
        String childName;
        java.util.List result = new java.util.ArrayList();

        java.util.List expectedResult = new java.util.ArrayList();
        expectedResult.add("address");
        expectedResult.add("address");
        expectedResult.add("address");
        expectedResult.add("emp:address");
        expectedResult.add("address");

        doc = load("staffNS", false);
        docElem = doc.getDocumentElement();
        elementList = docElem.getElementsByTagNameNS("*", "address");
        for (int indexd409e60 = 0; indexd409e60 < elementList.getLength(); indexd409e60++) {
            child = elementList.item(indexd409e60);
            childName = child.getNodeName();
            result.add(childName);
        }
        assertEquals("nodeNames", expectedResult, result);

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/getElementsByTagNameNS11";
    }

}
