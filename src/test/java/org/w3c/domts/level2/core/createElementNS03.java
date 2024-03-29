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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.w3c.domts.DOMTest.load;


/**
 * The "createElementNS(namespaceURI,qualifiedName)" method for a
 * Document should raise INVALID_CHARACTER_ERR DOMException if
 * qualifiedName contains an illegal character.
 * <p>
 * Invoke method createElementNS(namespaceURI,qualifiedName) on this document
 * with qualifiedName containing an illegal character from illegalChars[].
 * Method should raise INVALID_CHARACTER_ERR DOMException for all characters
 * in illegalChars[].
 *
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-DocCrElNS">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-DocCrElNS</a>
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-DocCrElNS')/raises/exception[@name='DOMException']/descr/p[substring-before(.,':')='INVALID_CHARACTER_ERR'])">http://www.w3.org/TR/DOM-Level-2-Core/core#xpointer(id('ID-DocCrElNS')/raises/exception[@name='DOMException']/descr/p[substring-before(.,':')='INVALID_CHARACTER_ERR'])</a>
 */
public class createElementNS03 {
    @Test
    void testRun() throws Throwable {
        String namespaceURI = "http://www.wedding.com/";
        String[] illegalQNames = {
            "{", "}", "~", "'", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")",
            "+", "=", "[", "]", "\\", "/", ";", "`", "<", ">", ",", "a ", "\""
        };
        Document doc = load("staffNS", false);
        for (String qualifiedName : illegalQNames) {
            DOMException ex = assertThrows(DOMException.class, () -> doc.createElementNS(namespaceURI, "person:" + qualifiedName));
            assertEquals(DOMException.INVALID_CHARACTER_ERR, ex.code, "throw_INVALID_CHARACTER_ERR");
        }
    }

    /**
     * Gets URI that identifies the test
     *
     * @return uri identifier of test
     */
    public String getTargetURI() {
        return "http://www.w3.org/2001/DOM-Test-Suite/level2/core/createElementNS03";
    }

}
