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
import org.w3c.dom.Node;

import static org.junit.Assert.assertTrue;
import static org.w3c.domts.DOMTest.load;


/**
 * The "feature" parameter in the
 * isSupported(feature,version)" method is the name
 * of the feature and the version is the version number of the
 * feature to test.   XML is a legal value for the feature parameter
 * (Test for xml, lower case).
 * Legal values for the version parameter are 1.0 and 2.0
 * (Test for 2.0).
 * <p>
 * Retrieve the root node of the DOM document by invoking
 * the "getDocumentElement()" method.   This should create a
 * node object on which the "isSupported(feature,version)"
 * method is invoked with "feature" equal to "xml" and the version equal to 2.0.
 * The method should return a boolean "true".
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-Node-supports">http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-Node-supports</a>
 */
public class isSupported06 {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        Node rootNode;
        boolean state;
        doc = load("staff", false);
        rootNode = doc.getDocumentElement();
        state = rootNode.isSupported("xml", "2.0");
        assertTrue("throw_True", state);

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/isSupported06";
    }

}
