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
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.toNextTokenOfType;


public class InsertNamespaceTest {
    @Test
    void testInsertNamespaceAfterSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertNamespace("prefix", "value");
            m_xc.toStartDoc();
            XmlOptions map = new XmlOptions();
            map.setSaveNamespacesFirst(true);
            assertEquals("<foo xmlns:prefix=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText(map));
        }
    }

    @Test
    void testInsertNamespaceAfterATTR() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            toNextTokenOfType(m_xc, TokenType.ATTR);
            m_xc.insertNamespace("prefix", "value");
            m_xc.toStartDoc();
            XmlOptions map = new XmlOptions();
            map.setSaveNamespacesFirst();
            assertEquals("<foo xmlns:prefix=\"value\" attr0=\"val0\" attr1=\"val1\">text</foo>", m_xc.xmlText(map));
        }
    }

    @Test
    void testInsertNamespaceInsideTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertNamespace("prefix", "value"));
        }
    }

    @Test
    void testInsertNamespaceFromSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_2ATTR_TEXT)) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertNamespace("prefix", "value"));
        }
    }

    @Test
    void testInsertNamespaceAfterPROCINST() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            m_xc.toNextToken();
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertNamespace("prefix", "value"));
        }
    }

    @Test
    void testInsertNamespaceAfterNAMESPACE() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            m_xc.toNextToken();
            m_xc.insertNamespace("prefix", "value");
            m_xc.toStartDoc();
            assertEquals("<foo xmlns=\"http://www.foo.org\" xmlns:prefix=\"value\"/>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertDuplicateNamespace() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertNamespace("prefix", "http://www.foo.org");
            m_xc.insertNamespace("prefix", "http://www.foo.org");
            m_xc.toStartDoc();
            assertEquals("<foo xmlns:prefix=\"http://www.foo.org\">text</foo>", m_xc.xmlText());
        }
    }

    @Test
    @Disabled
    public void testInsertNamespaceWithNullPrefix() throws Exception {
        // According to Eric V...  This test is not valid
        // Eric's comments:
        // is erroneous.  <foo> must be in no namespace.
        // By setting the default namespace to "http://www.foo.org"
        // and having it saved out as such, <foo> would be in that
        // namespace.  So, the saver does not save out that namespace
        // (note that mapping a prefix to no namespace "" is illegal).


        // Below is the original code.
        // m_xo = XmlObject.Factory.parse(Common.XML_FOO,
        // XmlOptions.AUTOTYPE_DOCUMENT_LAX);
        // m_xc = m_xo.newCursor();
        // toNextTokenOfType(m_xc, TokenType.END);
        // m_xc.insertNamespace(null, "http://www.foo.org");
        // m_xc.toStartDoc();
        // assertEquals("<foo xmlns=\"http://www.foo.org\"/>", m_xc.xmlText());
    }

    @Test
    void testInsertNamespaceWithNullValue() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO)) {
            toNextTokenOfType(m_xc, TokenType.END);

            //EricV: this should be OK, but make sure the saver
            // doesn't serialize it since it's not legal XML

            m_xc.insertNamespace("prefix", null);
            m_xc.toStartDoc();
            assertEquals("<foo/>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertEmptyNamespace() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.END);
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertNamespace("", ""));
        }
    }
}

