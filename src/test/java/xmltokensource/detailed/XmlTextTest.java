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


package xmltokensource.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.*;


public class XmlTextTest {
    @Test
    void testSAVENAMESPACESFIRST() throws Exception {
        String xml = "<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo>";
        String exp = "<foo xmlns=\"http://www.foo.org\" attr0=\"val0\">01234</foo>";
        try (XmlCursor m_xc = cur(xml)) {
            XmlOptions m_map = new XmlOptions();
            m_map.setSaveNamespacesFirst();
            assertEquals(exp, m_xc.xmlText(m_map));
        }
    }

    @Test
    void testSAVENAMESPACESlast() throws Exception {
        String xml = "<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo>";
        String exp = "<foo attr0=\"val0\" xmlns=\"http://www.foo.org\">01234</foo>";
        try (XmlCursor m_xc = cur(xml)) {
            assertEquals(exp, m_xc.xmlText(new XmlOptions()));
        }
    }

    @Test
    void testSaveSyntheticDocumentElement() throws Exception {
        try (XmlCursor m_xc = cur("<bar>text</bar>")) {
            XmlOptions m_map = new XmlOptions();
            m_map.setSaveSyntheticDocumentElement(new QName("foo"));
            assertEquals("<foo><bar>text</bar></foo>", m_xc.xmlText(m_map));
        }
    }

    @Test
    void testSavePrettyPrint() throws Exception {
        try (XmlCursor m_xc = cur("<a><b><c> text </c></b></a>")) {
            XmlOptions m_map = new XmlOptions();
            m_map.setSavePrettyPrint();
            String lnSep = System.getProperty("line.separator");
            assertEquals("<a>" + lnSep + "  <b>" + lnSep + "    <c> text </c>" + lnSep + "  </b>" + lnSep + "</a>", m_xc.xmlText(m_map));
        }
    }

    @Test
    void testSavePrettyPrintIndent3() throws Exception {
        try (XmlCursor m_xc = cur("<a><b><c> text </c></b></a>")) {
            XmlOptions m_map = new XmlOptions();
            m_map.setSavePrettyPrint();
            m_map.setSavePrettyPrintIndent(3);
            String lnSep = System.getProperty("line.separator");
            assertEquals("<a>" + lnSep + "   <b>" + lnSep + "      <c> text </c>" + lnSep + "   </b>" + lnSep + "</a>", m_xc.xmlText(m_map));
        }
    }

    @Test
    void testSavePrettyPrintIndentNeg1() throws Exception {
        try (XmlCursor m_xc = cur("<a>  \n  <b>  \n    <c> text   </c>   \n  </b>  \n  </a>")) {
            XmlOptions m_map = new XmlOptions();
            m_map.setSavePrettyPrint();
            m_map.setSavePrettyPrintIndent(-1);
            assertEquals("<a><b><c> text   </c></b></a>", m_xc.xmlText(m_map));
        }
    }

    @Test
    void testDefaultNamespace() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//FleetID");
            m_xc.toNextSelection();
            XmlOptions m_map = new XmlOptions();
            m_map.setSaveNamespacesFirst();
            String exp = "<FleetID xmlns=\"" + Common.CLM_NS + "\" " + Common.CLM_XSI_NS + ">FLEETNAME</FleetID>";
            assertEquals(exp, m_xc.xmlText(m_map));
        }
    }

    @Test
    void testSTARTDOCvsFirstChild() throws Exception {
        XmlObject m_xo = jobj(Common.TRANXML_FILE_CLM);

        try (XmlCursor m_xc = m_xo.newCursor();
            XmlCursor xc1 = m_xo.newCursor()) {
            xc1.toFirstChild();
            assertEquals(m_xc.xmlText().replaceFirst("(?s)<!--.*-->", ""), xc1.xmlText());
        }
    }

    @Test
    void testXmlTextFromTEXT() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_1ATTR_TEXT)) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals("text", m_xc.getChars());
        }
    }

    @Test
    void testXmlTextFromTEXTafterEND() throws Exception {
        try (XmlCursor m_xc = cur("<foo><bar> text </bar> ws \\r\\n </foo>")) {
            toNextTokenOfType(m_xc, TokenType.TEXT);
            toNextTokenOfType(m_xc, TokenType.TEXT);
            assertEquals(" ws \\r\\n ", m_xc.getChars());
        }
    }
}

