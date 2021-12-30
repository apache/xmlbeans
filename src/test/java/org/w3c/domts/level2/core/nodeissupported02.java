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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import static org.junit.Assert.assertTrue;
import static org.w3c.domts.DOMTest.load;


/**
 * The method "isSupported(feature,version)" Tests whether the DOM implementation
 * implements a specific feature and that feature is supported by this node.
 * Call the isSupported method on a new attribute node with a combination of features
 * versions and versions as below.  Valid feature names are case insensitive and versions
 * "2.0", "1.0" and if the version is not specified, supporting any version of the feature
 * should return true.  Check if the value returned value was true.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-Node-supports">http://www.w3.org/TR/DOM-Level-2-Core/core#Level-2-Core-Node-supports</a>
 */
public class nodeissupported02 {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        Attr attribute;
        String version = "";
        String version1 = "1.0";
        String version2 = "2.0";
        String featureCore;
        String featureXML;
        boolean success;
        java.util.List featuresXML = new java.util.ArrayList();
        featuresXML.add("XML");
        featuresXML.add("xmL");

        java.util.List featuresCore = new java.util.ArrayList();
        featuresCore.add("Core");
        featuresCore.add("CORE");

        doc = load("staffNS", false);
        attribute = doc.createAttribute("TestAttr");
        for (int indexd514e63 = 0; indexd514e63 < featuresXML.size(); indexd514e63++) {
            featureXML = (String) featuresXML.get(indexd514e63);
            success = attribute.isSupported(featureXML, version);
            assertTrue("nodeissupported02_XML1", success);
            success = attribute.isSupported(featureXML, version1);
            assertTrue("nodeissupported02_XML2", success);
        }
        for (int indexd514e74 = 0; indexd514e74 < featuresCore.size(); indexd514e74++) {
            featureCore = (String) featuresCore.get(indexd514e74);
            success = attribute.isSupported(featureCore, version);
            assertTrue("nodeissupported02_Core1", success);
            success = attribute.isSupported(featureCore, version1);
            success = attribute.isSupported(featureCore, version2);
            assertTrue("nodeissupported02_Core3", success);
        }

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/nodeissupported02";
    }

}
