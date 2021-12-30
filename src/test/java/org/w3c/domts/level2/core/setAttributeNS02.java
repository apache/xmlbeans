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

import static org.junit.Assert.assertTrue;
import static org.w3c.domts.DOMTest.load;


/**
 * The "setAttributeNS(namespaceURI,qualifiedName,value)" method raises a
 * NAMESPACE_ERR DOMException if the specified
 * qualifiedName if malformed.
 * <p>
 * Attempt to add a new attribute on the second employee node.
 * An exception should be raised since the "qualifiedName" is malformed.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-258A00AF')/constant[@name='NAMESPACE_ERR'])">http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-258A00AF')/constant[@name='NAMESPACE_ERR'])</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-ElSetAttrNS">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-ElSetAttrNS</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-ElSetAttrNS')/raises/exception[@name='DOMException']/descr/p[substring-before(.,':')='NAMESPACE_ERR'])">http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-ElSetAttrNS')/raises/exception[@name='DOMException']/descr/p[substring-before(.,':')='NAMESPACE_ERR'])</a>
 */
public class setAttributeNS02 {
    @Test
    public void testRun() throws Throwable {
        String namespaceURI = "http://www.nist.gov";
        String qualifiedName = "emp:";
        Document doc;
        NodeList elementList;
        Node testAddr;
        doc = load("staffNS", true);
        elementList = doc.getElementsByTagName("emp:employee");
        testAddr = elementList.item(0);

        {
            boolean success = false;
            try {
                ((Element) /*Node */testAddr).setAttributeNS(namespaceURI, qualifiedName, "newValue");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR,NAMESPACE_ERR DOMException if the specified\n" +
                       "\n" +
                       "   qualifiedName if malformed ", success);
        }

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/setAttributeNS02";
    }

}
