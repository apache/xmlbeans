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
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;
import xmlcursor.common.Common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class IsInSameDocumentTest extends BasicCursorTestCase {
    @Test
    public void testSameDocSTARTDOCandENDDOC() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        xc0.toEndDoc();
        try {
            assertTrue(m_xc.isInSameDocument(xc0));
            assertTrue(xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testSameDocNAMESPACEandATTR() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_DIGITS).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        try {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            toNextTokenOfType(xc0, TokenType.ATTR);
            assertTrue(m_xc.isInSameDocument(xc0));
            assertTrue(xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testSameDocNull() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        assertFalse(m_xc.isInSameDocument(null));
    }

    @Test
    public void testSameDocDifferentDocs() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc0, TokenType.TEXT);
        try {
            assertFalse(m_xc.isInSameDocument(xc0));
            assertFalse(xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }

    @Test
    public void testSameDocTEXTpositional() throws Exception {
        m_xc = XmlObject.Factory.parse(Common.XML_FOO_1ATTR_TEXT).newCursor();
        XmlCursor xc0 = m_xc.newCursor();
        toNextTokenOfType(m_xc, TokenType.TEXT);
        toNextTokenOfType(xc0, TokenType.TEXT);
        xc0.toNextChar(2);
        try {
            assertTrue(m_xc.isInSameDocument(xc0));
            assertTrue(xc0.isInSameDocument(m_xc));
        } finally {
            xc0.dispose();
        }
    }
}
