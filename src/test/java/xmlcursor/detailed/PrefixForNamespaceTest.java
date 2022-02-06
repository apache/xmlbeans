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


package xmlcursor.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.*;


public class PrefixForNamespaceTest {
    @Test
    void testprefixForNamespaceFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            assertEquals("pre1", m_xc.prefixForNamespace("uri1"));
            assertEquals("pre2", m_xc.prefixForNamespace("uri2"));
            assertEquals("pre3", m_xc.prefixForNamespace("uri3"));
        }
    }

    @Test
    void testprefixForNamespaceFromSTARTDOCInvalid() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("ns1", "uri1");
            m_xc.insertNamespace("ns2", "uri2");
            m_xc.insertNamespace("ns3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            assertEquals("uri4", m_xc.prefixForNamespace("uri4"));
        }
    }

    @Test
    void testprefixForNamespaceFromSTARTDOCNull() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.prefixForNamespace(null));
        }
    }

    @Test
    void testprefixForNamespaceFromSTARTDOCEmptyString() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.prefixForNamespace(""));
        }
    }

    @Test
    void testprefixForNamespaceFromSTART() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toFirstChild();
            assertEquals("xsi", m_xc.prefixForNamespace("http://www.w3.org/2000/10/XMLSchema-instance"));
        }
    }

    @Test
    void testprefixForNamespaceFromSTARTdefaultNamespace() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toFirstChild();
            assertEquals("", m_xc.prefixForNamespace("http://www.tranxml.org/TranXML/Version4.0"));
        }
    }

    @Test
    void testprefixForNamespaceFromATTR() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            m_xc.selectPath("declare default element namespace \"nsa\";" + "$this//bar");
            m_xc.toFirstAttribute();
            assertEquals("nsa", m_xc.prefixForNamespace("nsa"));
            assertEquals("pre1", m_xc.prefixForNamespace("uri1"));
        }
    }

    @Test
    void testprefixForNamespaceFromEND() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            toNextTokenOfType(m_xc, TokenType.END);
            //the default prefix
            assertEquals("", m_xc.prefixForNamespace("nsa"));
            // assertEquals("pre1", m_xc.prefixForNamespace("uri1"));
        }
    }
}

