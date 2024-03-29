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


package dom.detailed;

import dom.common.Loader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextTest {
    String sXml="<foo at0=\"no_ns_attr\"></foo>";
    String sXmlNS="<foo><foobar xmlns:myns=\"http://foo.org\" xmlns:other=\"other.org\">footext</foobar></foo>";
    Document m_doc,
	m_docNS;
    Node m_node;

    @Test
    void testTextToAttrValue(){
        m_node = m_docNS.getFirstChild().getFirstChild().getFirstChild();//footext
        Attr attrib = (Attr) m_docNS.getFirstChild().getFirstChild().getAttributes().getNamedItem("xmlns:myns");
        assertEquals("http://foo.org", attrib.getNodeValue());
        //attrib.replaceChild(m_node,attrib.getFirstChild());
        attrib.setValue(m_node.getNodeValue());
        assertEquals("footext", attrib.getNodeValue());
        //assertFalse(m_docNS.getFirstChild().getFirstChild().hasChildNodes());
    }

    @BeforeEach
    public void setUp() throws Exception {

        Loader loader = Loader.getLoader();
        if (sXml == null && sXmlNS == null) throw new IllegalArgumentException("Test bug : Initialize xml strings");
        m_doc = loader.load(sXml);
        if (sXmlNS != null && sXmlNS.length() > 0)
            m_docNS = loader.load(sXmlNS);

        m_node = m_doc.getFirstChild();
    }
}
