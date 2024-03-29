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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.w3c.domts.DOMTest.load;


/**
 * The method removeNamedItemNS removes a node specified by local name and namespace
 * Attempt to remove the xmlns and dmstc attributes of the first element node with the localName
 * employee.  Verify if the 2 attributes were successfully removed.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-D58B193">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-D58B193</a>
 */
public class namednodemapremovenameditemns04 {
    @Test
    void testRun() throws Throwable {
        NodeList elementList;
        Document doc = load("staffNS", true);
        elementList = doc.getElementsByTagNameNS("*", "employee");
        Node element = elementList.item(0);
        NamedNodeMap attributes = element.getAttributes();
        Attr attributeRemoved = (Attr) attributes.removeNamedItemNS("http://www.w3.org/2000/xmlns/", "xmlns");
        Attr attribute = (Attr) attributes.getNamedItemNS("http://www.w3.org/2000/xmlns/", "xmlns");
        assertNull(attribute, "namednodemapremovenameditemns04_1");
        attributeRemoved = (Attr) attributes.removeNamedItemNS("http://www.w3.org/2000/xmlns/", "dmstc");
        attribute = (Attr) attributes.getNamedItemNS("http://www.w3.org/2000/xmlns/", "dmstc");
        assertNull(attribute, "namednodemapremovenameditemns04_2");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/namednodemapremovenameditemns04";
    }

}
