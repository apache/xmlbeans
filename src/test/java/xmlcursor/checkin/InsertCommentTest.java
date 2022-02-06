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


package xmlcursor.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.*;


public class InsertCommentTest {

    @Test
    void testInsertCommentAtSTART() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            m_xc.selectPath("$this//bar");
            m_xc.toNextSelection();
            m_xc.insertComment(" new comment ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo><!-- new comment --><bar>text</bar></foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentInMiddleOfTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.toNextChar(2);
            assertEquals("xt", m_xc.getChars());
            m_xc.insertComment(" new comment ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<bar>te<!-- new comment -->xt</bar>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentAtEND() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_BAR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.END);
            m_xc.insertComment(" new comment ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<bar>text<!-- new comment --></bar>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentWithLTChar() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertComment("< new comment ");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo><!--< new comment -->text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentWithDoubleDash() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertComment(" -- ");
            m_xc.toStartDoc();
            assertEquals("<foo><!-- -  -->text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentWithDoubleDashNoWS() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertComment("--");
            m_xc.toStartDoc();
            assertEquals("<foo><!--- -->text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentWithEndDash() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertComment(" -");
            m_xc.toStartDoc();
            assertEquals("<foo><!--  -->text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentWithEmptyString() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertComment("");
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo><!---->text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentWithNull() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            m_xc.insertComment(null);
            toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("<foo><!---->text</foo>", m_xc.xmlText());
        }
    }

    @Test
    void testInsertCommentAtSTARTDOC() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_TEXT)) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.insertComment("should fail"));
        }
    }
}

