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


import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.Assert.assertTrue;
import static org.w3c.domts.DOMTest.load;


/**
 * The "feature" parameter in the
 * isSupported(feature,version)" method is the name
 * of the feature and the version is the version number of the
 * feature to test.   CORE is a legal value for the feature parameter
 * (Test for CORE, upper case).
 * Legal values for the version parameter are 1.0 and 2.0
 * (Test for 1.0).
 * Retrieve the root node of the DOM document by invoking
 * the "getDocumentElement()" method.   This should create a
 * node object on which the "isSupported(feature,version)"
 * method is invoked with "feature" equal to "CORE" and the version equal to 1.0.
 * The method should return a boolean "true".
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-Node-supports">http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-Node-supports</a>
 */
public class isSupported12 {
    @Test
    public void testRun() throws Throwable {
        java.util.List features = new java.util.ArrayList();
        features.add("Core");
        features.add("XML");
        features.add("HTML");
        features.add("Views");
        features.add("StyleSheets");
        features.add("CSS");
        features.add("CSS2");
        features.add("Events");
        features.add("UIEvents");
        features.add("MouseEvents");
        features.add("MutationEvents");
        features.add("HTMLEvents");
        features.add("Range");
        features.add("Traversal");
        //features.add(new Double("bogus.bogus.bogus"));

        Document doc;
        Node rootNode;
        String featureElement;
        boolean state;
        doc = load("staff", false);
        rootNode = doc.getDocumentElement();
        state = rootNode.isSupported("Core", "2.0");
        assertTrue("Core2", state);
        for (int indexd467e91 = 0; indexd467e91 < features.size(); indexd467e91++) {
            featureElement = (String) features.get(indexd467e91);
            state = rootNode.isSupported(featureElement, "1.0");
        }
        for (int indexd467e96 = 0; indexd467e96 < features.size(); indexd467e96++) {
            featureElement = (String) features.get(indexd467e96);
            state = rootNode.isSupported(featureElement, "2.0");
        }

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/isSupported12";
    }

}
