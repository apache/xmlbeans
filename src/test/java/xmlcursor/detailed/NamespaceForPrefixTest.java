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

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;

public class NamespaceForPrefixTest {
    @Test
    void testNamespaceForPrefixFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            assertEquals("uri1", m_xc.namespaceForPrefix("pre1"));
            assertEquals("uri2", m_xc.namespaceForPrefix("pre2"));
            assertEquals("uri3", m_xc.namespaceForPrefix("pre3"));
        }
    }

    @Test
    void testNamespaceForPrefixFromSTARTDOCInvalid() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            assertNull(m_xc.namespaceForPrefix("pre4"));
        }
    }

    @Test
    void testNamespaceForPrefixFromSTARTDOCNull() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            assertEquals("uridefault", m_xc.namespaceForPrefix(null));
        }
    }

    @Test
    void testNamespaceForPrefixFromSTARTDOCEmptyString() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\">text</foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            assertEquals("uridefault", m_xc.namespaceForPrefix(""));
        }
    }

    @Test
    void testNamespaceForPrefixFromSTART() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toFirstChild();
            assertEquals("http://www.w3.org/2000/10/XMLSchema-instance", m_xc.namespaceForPrefix("xsi"));
        }
    }

    @Test
    void testNamespaceForPrefixFromSTARTdefaultNamespace() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.toFirstChild();
            assertEquals("http://www.tranxml.org/TranXML/Version4.0", m_xc.namespaceForPrefix(""));
        }
    }

    @Test
    void testNamespaceForPrefixFromATTR() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>")) {
            m_xc.toFirstChild();
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            m_xc.toStartDoc();
            m_xc.selectPath("declare default element namespace \"nsa\";" + "$this//bar");
            m_xc.toNextSelection();
            m_xc.toFirstAttribute();
            assertThrows(IllegalStateException.class, () -> m_xc.namespaceForPrefix(null));
        }
    }

    @Test
    void testNamespaceForPrefixFromEND() throws Exception {
        try (XmlCursor m_xc = cur("<foo xmlns=\"nsa\"><bar attr0=\"val0\">text</bar></foo>")) {
            m_xc.toFirstChild();
            System.out.println("i am here " + m_xc.currentTokenType());
            m_xc.insertNamespace("pre1", "uri1");
            m_xc.insertNamespace("pre2", "uri2");
            m_xc.insertNamespace("pre3", "uri3");
            m_xc.insertNamespace(null, "uridefault");
            toNextTokenOfType(m_xc, TokenType.END);
            assertThrows(IllegalStateException.class, () -> m_xc.namespaceForPrefix(null));
        }
    }
}

