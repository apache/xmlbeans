/*
This Java source file was generated by test-to-java.xsl
and is a derived work from the source document.
The source document contained the following notice:


Copyright (c) 2004 World Wide Web Consortium,
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


import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.*;

import static org.junit.Assert.fail;
import static org.w3c.domts.DOMTest.load;


/**
 * An attempt to add remove an notation using removeNamedItemNS should result in
 * a NO_MODIFICATION_ERR or a NOT_FOUND_ERR.
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-D46829EF">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-D46829EF</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-removeNamedItemNS">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-removeNamedItemNS</a>
 */
public class hc_notationsremovenameditemns1 {
    @Test
    @Ignore
    public void testRun() throws Throwable {
        Document doc;
        NamedNodeMap notations;
        DocumentType docType;
        Node retval;
        doc = load("hc_staff", true);
        docType = doc.getDoctype();
        notations = docType.getNotations();

        try {
            retval = notations.removeNamedItemNS("http://www.w3.org/1999/xhtml", "alpha");
            fail("throw_NO_MOD_OR_NOT_FOUND_ERR");

        } catch (DOMException ex) {
            switch (ex.code) {
                case 7:
                    break;
                case 8:
                    break;
                default:
                    throw ex;
            }
        }

    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/hc_notationsremovenameditemns1";
    }

}
