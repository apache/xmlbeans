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
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import static org.junit.Assert.assertTrue;
import static org.w3c.domts.DOMTest.load;


/**
 * The method "hasFeature(feature,version)" tests if the DOMImplementation implements
 * a specific feature and if so returns true.
 * Call the hasFeature method on this DOMImplementation with a combination of features
 * versions as below.  Valid feature names are case insensitive and versions "2.0",
 * "1.0" and if the version is not specified, supporting any version of the feature
 * should return true.  Check if the value returned value was true.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-5CED94D7">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-5CED94D7</a>
 */
public class domimplementationhasfeature01 {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
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
        domImpl = doc.getImplementation();
        for (int indexd360e63 = 0; indexd360e63 < featuresXML.size(); indexd360e63++) {
            featureXML = (String) featuresXML.get(indexd360e63);
            success = domImpl.hasFeature(featureXML, version);
            assertTrue("domimplementationhasfeature01_XML_1", success);
            success = domImpl.hasFeature(featureXML, version1);
            assertTrue("domimplementationhasfeature01_XML_2", success);
        }
        for (int indexd360e74 = 0; indexd360e74 < featuresCore.size(); indexd360e74++) {
            featureCore = (String) featuresCore.get(indexd360e74);
            success = domImpl.hasFeature(featureCore, version);
            assertTrue("domimplementationhasfeature01_Core_1", success);
            success = domImpl.hasFeature(featureCore, version1);
            success = domImpl.hasFeature(featureCore, version2);
            assertTrue("domimplementationhasfeature01_Core_3", success);
        }

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/domimplementationhasfeature01";
    }

}
