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

public class SetTextValueTest {

    /**
     * Depth first concatenation of all text leaves
     */
    @Test
    void testSTARTDOC() throws XmlException {
        String sExpected = Common.XMLFRAG_BEGINTAG + "&lt;newdoc/>" + Common.XMLFRAG_ENDTAG;
        char[] buffer = "<newdoc/>".toCharArray();
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            m_xc.setTextValue(buffer, 0, buffer.length);
            //toPrevTokenOfType(m_xc,TokenType.STARTDOC);
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    @Test
    void testSTART() throws XmlException {
        String sNewVal = "new test value ";
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\">" + sNewVal + "</foo>";
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            char[] buffer = sNewVal.toCharArray();
            m_xc.setTextValue(buffer, 0, buffer.length);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    @Test
    void testAttr() throws XmlException {
        String sNewVal = "US\u0024 ";
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\"><!-- the 'price' element's namespace is http://ecommerce.org/schema -->  <edi:price units=\"" +
                sNewVal +
                "\">32.18</edi:price></foo>";
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.ATTR);
            char[] buffer = sNewVal.toCharArray();
            m_xc.setTextValue(buffer, 0, buffer.length);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    @Test
    void testComment() throws XmlException {
        String sNewVal = "My new comment ";
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\"><!--" +
                sNewVal +
                "-->  <edi:price units=\"Euro\">32.18</edi:price></foo>";
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            char[] buffer = sNewVal.toCharArray();
            m_xc.setTextValue(buffer, 0, buffer.length);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    @Test
    void testPI() throws Exception {
        String sTestXml = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><foo at0=\"value0\">text</foo>";
        try (XmlCursor m_xc = cur(sTestXml)) {
            String sNewVal = "type=\"text/html\" xmlns=\"http://newUri.org\" ";
            String sExpected = "<?xml-stylesheet " + sNewVal + "?><foo at0=\"value0\">text</foo>";
            toNextTokenOfType(m_xc, TokenType.PROCINST);
            char[] buffer = sNewVal.toCharArray();
            m_xc.setTextValue(buffer, 0, buffer.length);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    @Test
    void testSetNull() throws XmlException {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IllegalArgumentException.class, () -> m_xc.setTextValue(null, 0, 10));
        }
    }

    @Test
    void testNegativeOffset() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IndexOutOfBoundsException.class, () -> m_xc.setTextValue(buffer, -1, 98));
        }
    }


    @Test
    void testNonZeroOffset() throws XmlException {
        char[] buffer = "Test".toCharArray();
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            String sExpected = "st";
            m_xc.setTextValue(buffer, 2, buffer.length - 2);
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(sExpected, m_xc.getChars());
        }
    }


    @Test
    void testLargeOffset() throws XmlException {
        String sNewVal = " 20";
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(IndexOutOfBoundsException.class, () -> m_xc.setTextValue(sNewVal.toCharArray(), 5, 3));
        }
    }

    //charCount<=0: should be a noop
    @Test
    void testNegativeCharCount() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            String sExpected = m_xc.xmlText();
            assertThrows(IndexOutOfBoundsException.class, () -> m_xc.setTextValue(buffer, 10, -1));
        }
    }

    @Test
    void testZeroCharCount() throws XmlException {
        char[] buffer = new char[100];
        String sExpected = "<foo xmlns:edi=\"http://ecommerce.org/schema\"/>";
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
            toNextTokenOfType(m_xc, TokenType.START);
            //since the operation is delete+replace
            //0,0 is equivalent to a delete
            m_xc.setTextValue(buffer, 0, 0);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    @Test
    void testLargeCharCount() throws XmlException {
        String sNewVal = " 20";
        int nCharCount = 10;
        assertTrue(sNewVal.length() < nCharCount);
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.setTextValue(sNewVal.toCharArray(), 0, nCharCount);
//        toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals(sNewVal, m_xc.getTextValue());
        }
    }

    //offset+selection>buffer
    @Test
    void testSelectionPastEnd() throws XmlException {
        String sNewVal = " 20";
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.START);
            m_xc.setTextValue(sNewVal.toCharArray(), 2, 4);
//        toPrevTokenOfType(m_xc, TokenType.START);
            assertEquals("0", m_xc.getTextValue());
        }
    }

    //spec doesn't say anything about text???
    @Test
    void testText() throws XmlException {
        char[] buff = "5000 ".toCharArray();
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue(buff, 0, buff.length));
        }
    }

    //$NOTE:did I forget a type
    @Test
    void testSetIllegalCursorPos() throws XmlException {
        char[] buffer = new char[100];
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            toNextTokenOfType(m_xc, TokenType.END);
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue(buffer, 0, 100));

            toNextTokenOfType(m_xc, TokenType.ENDDOC);
            assertThrows(IllegalStateException.class, () -> m_xc.setTextValue(buffer, 0, 100), "SetText in ENDDOC token");
        }
    }
}
