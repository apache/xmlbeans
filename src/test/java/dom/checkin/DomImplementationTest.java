/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package dom.checkin;

import dom.common.Loader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import static org.junit.jupiter.api.Assertions.*;

public class DomImplementationTest {

    DOMImplementation m_imp;
    DocumentType m_docType;
    Document result;

    Document m_docNS;
    String sXmlNS;

    public DomImplementationTest() {
        sXmlNS =
                "<foo xmlns:myns=\"uri:foo\" at0=\"val0\" myns:at0=\"val01\" at2=\"val2\" at3=\"val3\" at4=\"val4\"/>";
    }

    //$TODO: non null doctype
    @Test
    void testCreateDocument() {
        String sUri = "http://foo.org";
        String sQName = "qname";
        result = m_imp.createDocument(sUri, sQName, m_docType);
        assertEquals(sQName, result.getDocumentElement().getLocalName());
        assertEquals(sUri, result.getDocumentElement().getNamespaceURI());
    }


    //$TODO: implem. w/o "XML" feature; WRONG_DOCUMENT_ERR
    //NOT_SUPPORTED_ERR
    @Test
    void testCreateDocumentInvalid() {
        String sUri = "http://foo.org";
        String sQName = "<qname";
        try {
            result = m_imp.createDocument(sUri, sQName, m_docType);
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.INVALID_CHARACTER_ERR);
        }

        sUri = null;
        sQName = "foo:qname";
        try {
            result = m_imp.createDocument(sUri, sQName, m_docType);
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.NAMESPACE_ERR);
        }

        sUri = "myuri";
        sQName = "xml:qname";
        try {
            result = m_imp.createDocument(sUri, sQName, m_docType);
        }
        catch (DOMException de) {
            assertEquals(de.code, DOMException.NAMESPACE_ERR);
        }

    }

    @Test
    void testHasFeature() {
        String[] features = {
            "Core", "XML", "Events", "MutationEvents", "Range", "Traversal", "HTML", "Views", "StyleSheets", "CSS", "CSS2", "UIEvents", "HTMLEvents"
        };
        boolean bResult = true;
        for (int i = 0; i < features.length; i++) {
            if (i > 1) bResult = false;
            System.out.println(m_imp + "============== " + features[i] +
                    " =============" +
                    bResult);
            assertEquals(bResult, m_imp.hasFeature(features[i], "2.0"));
        }

    }

    @Test
    void testHasFeatureIlegal() {
        assertFalse(m_imp.hasFeature(null, "2.0"));
        assertFalse(m_imp.hasFeature("foobar", "2.0"));
        assertFalse(m_imp.hasFeature("xml", "-2"));
        assertTrue(m_imp.hasFeature("xml", null));
        assertTrue(m_imp.hasFeature("xml", ""));
        assertFalse(m_imp.hasFeature("xml", "300"));
    }

    public void moveToNode() {
        m_imp = m_docNS.getImplementation();
    }

    public void loadSync() throws Exception {
        _loader = Loader.getLoader();


        if (sXmlNS.length() > 0)
            m_docNS = _loader.loadSync(sXmlNS);

    }

    @BeforeEach
    public void setUp() throws Exception {
        _loader = Loader.getLoader();
        m_docNS = (org.w3c.dom.Document) _loader.load(sXmlNS);
        moveToNode();
    }

    private Loader _loader;
}
