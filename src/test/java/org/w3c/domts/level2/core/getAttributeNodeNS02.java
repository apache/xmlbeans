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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.w3c.domts.DOMTest.load;


/**
 * The "getAttributeNodeNS(namespaceURI,localName)" method retrieves an
 * attribute node by local name and NamespaceURI.
 * <p>
 * Retrieve the first emp:address element node.
 * The getAttributeNodeNS method returns an
 * Attr node, the "value" can be examined to ensure the
 * proper attribute node was retrieved.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-F68D095">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-F68D095</a>
 */
public class getAttributeNodeNS02 {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testAddr;
        Attr attribute;
        String attrName;
        doc = load("staffNS", false);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        attribute = testAddr.getAttributeNodeNS("http://www.nist.gov", "domestic");
        attrName = attribute.getNodeName();
        assertEquals("attrName", "emp:domestic", attrName);

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/getAttributeNodeNS02";
    }

}
