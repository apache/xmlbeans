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
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.w3c.domts.DOMTest.load;


/**
 * The createDocument method should throw a NAMESPACE_ERR if the qualifiedName has
 * a prefix and the namespaceURI is null.
 * <p>
 * Call the createDocument on this DOMImplementation with null namespaceURI and a
 * qualifiedName that has a namespace prefix using this DOMImplementation.
 * Check if the NAMESPACE_ERR is thrown.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-DOM-createDocument">http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-DOM-createDocument</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-DOM-createDocument">http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-DOM-createDocument</a>
 */
public class domimplementationcreatedocument04 {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        Document newDoc;
        String namespaceURI = null;

        String qualifiedName = "dom:root";
        DocumentType docType = null;

        doc = load("staffNS", false);
        domImpl = doc.getImplementation();

        {
            boolean success = false;
            try {
                newDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
                fail("qualifiedName has a prefix and the namespaceURI is null: NAMESPACE_ERR");
            } catch (DOMException ex) {
                assertEquals(ex.code, DOMException.NAMESPACE_ERR);
            }

        }

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/domimplementationcreatedocument04";
    }

}
