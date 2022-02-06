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
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;


public class GetTextValueTest {

    // Depth first concatenation of all text leaves
    @Test
    void testNormalCase() throws XmlException {
        String sExpected = "  32.18";
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            int nCopied = m_xc.getTextValue(buffer, 0, 100);
            assertEquals(sExpected.length(), nCopied);
            assertEquals(sExpected, new String(buffer).substring(0, nCopied));
        }
    }

    @Test
    void testGetNull() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.getTextValue(null, 0, 10));
        }
    }

    @Test
    void testNegativeOffset() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.getTextValue(buffer, -1, 100));
        }
    }

    @Test
    void testNonZeroOffset() throws XmlException {
        String sExpected = "T\0  32.18";
        char[] buffer = new char[10];
        buffer[0] = 'T';
        int nOffset = 2;
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            int nCopied = m_xc.getTextValue(buffer, 2, 8);
            assertEquals(7, nCopied);
            assertEquals(sExpected, new String(buffer).substring(0, nCopied + nOffset));
            assertEquals("", new String(buffer).substring(nOffset + nCopied, buffer.length).trim());
        }
    }

    @Test
    void testLargeOffset() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            assertThrows(IllegalArgumentException.class, () -> m_xc.getTextValue(buffer, 101, 1));
        }
    }

    //charCount<=0: should be a noop
    //BUT: Assumption is that <0=infinity, so all is copies
    @Test
    void testNegativeCharCount() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            String sExpected = m_xc.getTextValue();
            int nCount = m_xc.getTextValue(buffer, 0, -1);
            assertEquals(sExpected.length(), nCount);
            assertEquals(sExpected, new String(buffer, 0, nCount));
        }
    }

    @Test
    void testZeroCharCount() throws XmlException {
        char[] buffer = new char[10];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            int nCopied = m_xc.getTextValue(buffer, 0, 0);
            assertEquals(0, nCopied);
            assertEquals("", new String(buffer).trim());
        }
    }

    @Test
    void testLargeCharCount() throws XmlException {
        String sExpected = "  32.18";
        char[] buffer = new char[200];
        int nCharCount = 300;
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            assertTrue(Common.XML_FOO_NS_PREFIX.length() < nCharCount);
            assertFalse(buffer.length >= nCharCount);
            int nCopied = m_xc.getTextValue(buffer, 0, nCharCount);
            assertEquals(sExpected.length(), nCopied);
            assertEquals(sExpected, new String(buffer).substring(0, nCopied));
        }
    }

    //offset+selection>buffer
    @Test
    void testSelectionPastEnd() throws XmlException {
        String sExpected = "  3";
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            int nCopied = m_xc.getTextValue(buffer, 97, 4);
            assertEquals(sExpected.length(), nCopied);
            assertEquals(sExpected, new String(buffer, 97, nCopied));
            assertEquals("", new String(buffer, 0, 97).trim());
        }
    }


    //End,Enddoc,Namespace should
    //return 0 as per spec
    //NB: Design changed, should work now
    @Test
    void testGetNonTextElement() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            int nCopied = m_xc.getTextValue(buffer, 0, 100);
            String sExpected = "http://ecommerce.org/schema";
            assertEquals(sExpected, new String(buffer, 0, nCopied));
            assertEquals(sExpected.length(), nCopied);
            toNextTokenOfType(m_xc, TokenType.END);
            assertThrows(IllegalStateException.class, () -> m_xc.getTextValue(buffer, 0, 100));

            toNextTokenOfType(m_xc, TokenType.ENDDOC);
            assertThrows(IllegalStateException.class, () -> m_xc.getTextValue(buffer, 0, 100));
        }
    }

    //test text of comment, PI or Attr
    @Test
    void testCommentPIAttr() throws Exception {
        String sExpected = "http://ecommerce.org/schema";
        int nSize = sExpected.length();
        char[] buffer = new char[nSize + 1];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            int nCopied = m_xc.getTextValue(buffer, 0, nSize);
            assertEquals(sExpected, new String(buffer).substring(0, nCopied));
            assertEquals(sExpected.length(), nCopied);
        }


        String sTestXml = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo at0=\"value0\">text</foo>";
        try (XmlCursor m_xc = cur(sTestXml)) {
            int nCopied = m_xc.getTextValue(buffer, 0, nSize);
            //assert attributes are skipped
            assertEquals("text", new String(buffer).substring(0, nCopied));
            assertEquals("text".length(), nCopied);

            buffer = new char[100];
            toNextTokenOfType(m_xc, TokenType.ATTR);
            nCopied = m_xc.getTextValue(buffer, 0, 100);
            assertEquals("value0", new String(buffer).substring(0, nCopied));
            assertEquals("value0".length(), nCopied);

            sExpected = "type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"";
            nSize = sExpected.length();
            toPrevTokenOfType(m_xc, TokenType.PROCINST);
            nCopied = m_xc.getTextValue(buffer, 0, nSize);
            assertEquals(sExpected, new String(buffer).substring(0, nCopied));
            assertEquals(sExpected.length(), nCopied);
        }
    }
}
