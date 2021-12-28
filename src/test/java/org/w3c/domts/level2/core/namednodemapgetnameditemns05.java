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
import org.w3c.dom.*;
import org.w3c.domts.DOMTestCase;

import static org.junit.Assert.assertNull;


/**
 * The method getNamedItemNS retrieves a node specified by local name and namespace URI.
 * Retreieve the second address element and its attribute into a named node map.
 * Try retreiving the street attribute from the namednodemap using the
 * default namespace uri and the street attribute name.  Since the default
 * namespace doesnot apply to attributes this should return null.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-getNamedItemNS">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-getNamedItemNS</a>
 */
public class namednodemapgetnameditemns05 extends DOMTestCase {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        NodeList elementList;
        doc = load("staffNS", false);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = elementList.item(1);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.getNamedItemNS("*", "street");
        assertNull("namednodemapgetnameditemns05", attribute);

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/namednodemapgetnameditemns05";
    }

}