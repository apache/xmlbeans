/*
This Java source file was generated by test-to-java.xsl
and is a derived work from the source document.
The source document contained the following notice:


Copyright (c) 2001-2003 World Wide Web Consortium,
(Massachusetts Institute of Technology, Institut National de
Recherche en Informatique et en Automatique, Keio University). All
Rights Reserved. This program is distributed under the W3C's Software
Intellectual Property License. This program is distributed in the
hope that it will be useful, but WITHOUT ANY WARRANTY; without even
the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.
See W3C License http://www.w3.org/Consortium/Legal/ for more details.

*/

package org.w3c.domts.level2.core;


import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.w3c.domts.DOMTest.load;


/**
 * The "createAttribute(name)" method creates an Attribute
 * node of the given name.
 * Retrieve the entire DOM document and invoke its
 * "createAttribute(name)" method.  It should create a
 * new Attribute node with the given name. The name, value
 * and type of the newly created object are retrieved and
 * output.
 * Unlike the DOM 1 Core equivalent, this test can expect the
 * attribute name to be upper case for HTML documents.
 * * @see <a href="http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-core#ID-1084891198">http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-core#ID-1084891198</a>
 *
 * @see <a href="http://www.w3.org/Bugs/Public/show_bug.cgi?id=236">http://www.w3.org/Bugs/Public/show_bug.cgi?id=236</a>
 * @see <a href="http://lists.w3.org/Archives/Public/www-dom-ts/2003Jun/0011.html">http://lists.w3.org/Archives/Public/www-dom-ts/2003Jun/0011.html</a>
 */
public class hc_documentcreateattribute {
    @Test
    public void testRun() throws Throwable {
        Document doc;
        Attr newAttrNode;
        String attrValue;
        String attrName;
        int attrType;
        doc = load("hc_staff", true);
        newAttrNode = doc.createAttribute("style");
        attrValue = newAttrNode.getNodeValue();
        assertEquals("value", "", attrValue);
        attrName = newAttrNode.getNodeName();
        assertEquals("name", "style", attrName);
        attrType = newAttrNode.getNodeType();
        assertEquals("type", 2, attrType);

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/hc_documentcreateattribute";
    }

}
