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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.w3c.domts.DOMTest.load;


/**
 * The "getPrefix()" method
 * returns the namespace prefix of this node, or null if unspecified.
 * For nodes of any type other than ELEMENT_NODE and ATTRIBUTE_NODE,
 * this is always null.
 * <p>
 * Retrieve the first emp:employeeId node and get the first child of this node.
 * Since the first child is Text node invoking the "getPrefix()"
 * method will cause "null" to be returned.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-NodeNSPrefix">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-NodeNSPrefix</a>
 */
public class prefix02 {
    @Test
    void testRun() throws Throwable {
        String sExpected = null;
        Document doc = load("staffNS", false);
        NodeList elementList = doc.getElementsByTagName("emp:employeeId");
        Node testEmployee = elementList.item(0);
        Node textNode = testEmployee.getFirstChild();
        String prefix = textNode.getPrefix();
        assertEquals(sExpected, prefix, "textNodePrefix");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/prefix02";
    }

}
