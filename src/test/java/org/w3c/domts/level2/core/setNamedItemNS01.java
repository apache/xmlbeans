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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.w3c.domts.DOMTest.load;


/**
 * The "setNamedItemNS(arg)" method for a
 * NamedNodeMap should raise INUSE_ATTRIBUTE_ERR DOMException if
 * arg is an Attr that is already an attribute of another Element object.
 * <p>
 * Retrieve an attr node from the third "address" element whose local name
 * is "domestic" and namespaceURI is "http://www.netzero.com".
 * Invoke method setNamedItemNS(arg) on the map of the first "address" element with
 * arg being the attr node from above.  Method should raise
 * INUSE_ATTRIBUTE_ERR DOMException.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-258A00AF')/constant[@name='INUSE_ATTRIBUTE_ERR'])">http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-258A00AF')/constant[@name='INUSE_ATTRIBUTE_ERR'])</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-setNamedItemNS">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-setNamedItemNS</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-setNamedItemNS')/raises/exception[@name='DOMException']/descr/p[substring-before(.,':')='INUSE_ATTRIBUTE_ERR'])">http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-setNamedItemNS')/raises/exception[@name='DOMException']/descr/p[substring-before(.,':')='INUSE_ATTRIBUTE_ERR'])</a>
 */
public class setNamedItemNS01 {
    @Test
    void testRun() throws Throwable {
        Document doc = load("staffNS", true);
        NodeList elementList = doc.getElementsByTagName("address");
        Node anotherElement = elementList.item(2);
        NamedNodeMap anotherMap = anotherElement.getAttributes();
        Node arg = anotherMap.getNamedItemNS("http://www.netzero.com", "domestic");
        Node testAddress = elementList.item(0);
        NamedNodeMap map = testAddress.getAttributes();

        DOMException ex = assertThrows(DOMException.class, () -> map.setNamedItemNS(arg));
        assertEquals(DOMException.INUSE_ATTRIBUTE_ERR, ex.code, "throw_INUSE_ATTRIBUTE_ERR");
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/setNamedItemNS01";
    }

}
