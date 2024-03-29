/*
This Java source file was generated by test-to-java.xsl
and is a derived work from the source document.
The source document contained the following notice:



Copyright (c) 2001-2003 World Wide Web Consortium,
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.w3c.domts.DOMTest.load;


/**
 * The method setNamedItemNS adds a node using its namespaceURI and localName and
 * raises a WRONG_DOCUMENT_ERR if arg was created from a different document than the
 * one that created this map.
 * Retreieve the second element whose local name is address and its attribute into a named node map.
 * Do the same for another document and retreive its street attribute.  Call the setNamedItemNS
 * using the first namedNodeMap and the retreive street attribute of the second.  This should
 * raise a WRONG_DOCUMENT_ERR.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-setNamedItemNS">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-setNamedItemNS</a>
 * @see <a href="http://www.w3.org/Bugs/Public/show_bug.cgi?id=259">http://www.w3.org/Bugs/Public/show_bug.cgi?id=259</a>
 */
public class namednodemapsetnameditemns03 {
    @Test
    void testRun() throws Throwable {
        String nullNS = null;

        Document doc = load("staffNS", true);
        NodeList elementList = doc.getElementsByTagNameNS("*", "address");
        Element element = (Element) elementList.item(1);
        NamedNodeMap attributes = element.getAttributes();
        Document docAlt = load("staffNS", true);
        NodeList elementListAlt = docAlt.getElementsByTagNameNS("*", "address");
        Element elementAlt = (Element) elementListAlt.item(1);
        NamedNodeMap attributesAlt = elementAlt.getAttributes();
        Attr attr = (Attr) attributesAlt.getNamedItemNS(nullNS, "street");

        DOMException ex = assertThrows(DOMException.class, () -> attributes.setNamedItemNS(attr));
        assertEquals(DOMException.WRONG_DOCUMENT_ERR, ex.code, "namednodemapsetnameditemns03");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/namednodemapsetnameditemns03";
    }

}
