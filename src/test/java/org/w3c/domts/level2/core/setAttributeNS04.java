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
import org.w3c.dom.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.w3c.domts.DOMTest.load;


/**
 * The "setAttributeNS(namespaceURI,qualifiedName,value)" method adds a new attribute.
 * If an attribute with the same local name and namespace URI is already present
 * on the element, its prefix is changed to be the prefix part of the "qualifiedName",
 * and its vale is changed to be the "value" paramter.
 * null value if no previously existing Attr node with the
 * same name was replaced.
 * <p>
 * Add a new attribute to the "emp:address" element.
 * Check to see if the new attribute has been successfully added to the document
 * by getting the attributes value, namespace URI, local Name and prefix.
 * The prefix will be changed to the prefix part of the "qualifiedName"
 * and its value changed to the "value" parameter.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#">http://www.w3.org/TR/DOM-Level-2-Core/core#</a>
 */
public class setAttributeNS04 {
    @Test
    void testRun() throws Throwable {
        Document doc = load("staffNS", true);
        NodeList elementList = doc.getElementsByTagName("emp:address");
        Node testAddr = elementList.item(0);
        ((Element) /*Node */testAddr).setAttributeNS("http://www.nist.gov", "newprefix:zone", "newValue");
        Attr addrAttr = ((Element) /*Node */testAddr).getAttributeNodeNS("http://www.nist.gov", "zone");
        String resultAttr = ((Element) /*Node */testAddr).getAttributeNS("http://www.nist.gov", "zone");
        assertEquals("newValue", resultAttr, "attrValue");
        String resultNamespaceURI = addrAttr.getNamespaceURI();
        assertEquals("http://www.nist.gov", resultNamespaceURI, "nsuri");
        String resultLocalName = addrAttr.getLocalName();
        assertEquals("zone", resultLocalName, "lname");
        String resultPrefix = addrAttr.getPrefix();
        assertEquals("newprefix", resultPrefix, "prefix");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/setAttributeNS04";
    }

}
