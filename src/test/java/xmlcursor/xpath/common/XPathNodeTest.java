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

package xmlcursor.xpath.common;


import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.common.BasicCursorTestCase.obj;


/**
 * Verifies XPath nodetest functions
 */
public class XPathNodeTest {

    private static String fixPath(String path) {
        return "." + path;
    }

    public void testAllNodes() {
        //e.g //A/B/*: tested by Zvon
    }

    @Test
    void testComment() throws Exception {
        try (XmlCursor m_xc = cur(Common.XML_FOO_NS_PREFIX)) {
            String sExpected = "<xml-fragment xmlns:edi=\"http://ecommerce.org/schema\"><!-- the 'price' element's namespace is http://ecommerce.org/schema -->" + Common.XMLFRAG_ENDTAG;//the comment string
            String sXPath = "//comment()";
            m_xc.selectPath(fixPath(sXPath));
            m_xc.toNextSelection();
            assertEquals(m_xc.xmlText(), sExpected);
        }
    }

    @Test
    void testNode() throws Exception {
        String sXPath = "//foo/node()";
        String[] sExpected = {
            Common.XMLFRAG_BEGINTAG + " " + Common.XMLFRAG_ENDTAG,
            "<node>foo</node>",
            Common.XMLFRAG_BEGINTAG + "txt" + Common.XMLFRAG_ENDTAG
        };

        // TODO: add asserts
        try (XmlCursor m_xc = cur("<foo> <node>foo</node>txt</foo>")) {
            m_xc.selectPath(fixPath(sXPath));
            int i = 0;
            // assertEquals("node() failed", sExpected.length, m_xc.getSelectionCount());
            while (m_xc.hasNextSelection()) {
                m_xc.toNextSelection();
                //assertEquals(m_xc.xmlText(), sExpected[i++]);
            }
        }
    }

    @Test
    void testPI() throws Exception {
        String sXPath1 = "//processing-instruction()";
        String sXPath2 = "//processing-instruction(\"xml-stylesheet\")";
        String sXPath3 = "//processing-instruction(\"xsl\")";
        String sExpected = Common.XMLFRAG_BEGINTAG + "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?>" + Common.XMLFRAG_ENDTAG;

        try (XmlCursor m_xc = cur(Common.XML_FOO_PROCINST)) {
            m_xc.selectPath(fixPath(sXPath1));
            assertEquals(m_xc.getSelectionCount(), 1);
            m_xc.toNextSelection();
            assertEquals(m_xc.xmlText(), sExpected);

            m_xc.clearSelections();
            m_xc.selectPath(fixPath(sXPath2));
            assertEquals(m_xc.xmlText(), sExpected);

            m_xc.clearSelections();
            //shouldn't select any nodes
            m_xc.selectPath(fixPath(sXPath3));
            assertEquals(m_xc.getSelectionCount(), 0);
        }
    }

    @Test
    void testText() throws Exception {
        String sXml = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><br>foo<foo>text</foo></br>";
        String sXPath = "//text()";
        String sExpected1 = Common.XMLFRAG_BEGINTAG + "foo" + Common.XMLFRAG_ENDTAG;
        String sExpected2 = Common.XMLFRAG_BEGINTAG + "text" + Common.XMLFRAG_ENDTAG;

        try (XmlCursor m_xc = cur(sXml)) {
            m_xc.selectPath(sXPath);
            assertEquals(m_xc.getSelectionCount(), 2);
            m_xc.toNextSelection();
            assertEquals(m_xc.xmlText(), sExpected1);
            m_xc.toNextSelection();
            assertEquals(m_xc.xmlText(), sExpected2);
        }
    }

    @Test
    void testTextObject() throws Exception {
        String sXml = "<?xml-stylesheet type=\"text/xsl\" xmlns=\"http://openuri.org/shipping/\"?><br>foo<foo>text</foo></br>";
        String sXPath = "//text()";
        String sExpected1 = Common.XMLFRAG_BEGINTAG + "foo<foo>text</foo>" + Common.XMLFRAG_ENDTAG;
        String sExpected2 = Common.XMLFRAG_BEGINTAG + "text" + Common.XMLFRAG_ENDTAG;
        XmlObject m_xo = obj(sXml);
        XmlObject[] res = m_xo.selectPath(sXPath);
        assertEquals(res.length, 2);
        assertEquals(res[0].xmlText(), sExpected1);
        assertEquals(res[1].xmlText(), sExpected2);
    }

    @Test
    void testNodeEquality() throws Exception {
        try (XmlCursor c = cur("<root><book isbn='012345' id='09876'/></root>")) {
            c.selectPath("//book[@isbn='012345'] is //book[@id='09876']");
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(Common.wrapInXmlFrag("true"), c.xmlText());
        }
    }

    @Test
    void testNodeOrder() throws Exception {
        try (XmlCursor c = cur("<root><book isbn='012345'/><book id='09876'/></root>")) {
            c.selectPath("//book[@isbn='012345'] << //book[@id='09876']");
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(Common.wrapInXmlFrag("true"), c.xmlText());

            c.selectPath("//book[@isbn='012345'] >> //book[@id='09876']");
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(Common.wrapInXmlFrag("false"), c.xmlText());
        }
    }

    @Test
    void testParent() throws Exception {
        String sXml = "<A><B><C></C></B></A>";
        XmlObject o;
        try (XmlCursor c = cur(sXml)) {
            c.toFirstContentToken();
            c.toFirstChild();
            c.toFirstChild();
            o = c.getObject();
        }
        assertEquals("<C/>", xmlText(o));
        XmlObject[] res = o.selectPath("..");
        assertEquals(1, res.length);
        assertEquals("<B><C/></B>", xmlText(res[0]));
    }

    private String xmlText(XmlObject o) {
        try (XmlCursor c = o.newCursor()) {
            return c.xmlText();
        }
    }

    @Test
    void testParent1() throws Exception {
        String sXml =
            "<AttributeCertificate " +
            "xmlns=\"http://www.eurecom.fr/security/xac#\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<Content>" +
            "<Validity>" +
            "<ValidityFrom>2005-02-10T11:02:57.590+01:00</ValidityFrom>" +
            "<ValidityTo>2006-02-10T11:02:57.590+01:00</ValidityTo>" +
            "</Validity></Content></AttributeCertificate>";

        XmlObject o;
        try (XmlCursor c = cur(sXml)) {
            c.toFirstContentToken();
            c.toFirstChild();
            c.toFirstChild();
            o = c.getObject();
        }

        QName qn = getName(o);
        assertEquals("http://www.eurecom.fr/security/xac#", qn.getNamespaceURI());
        assertEquals("Validity", qn.getLocalPart());
        XmlObject[] res = o.selectPath("..");
        assertEquals(1, res.length);
        XmlObject x = res[0];
        qn = getName(x);
        assertEquals("http://www.eurecom.fr/security/xac#", qn.getNamespaceURI());
        assertEquals("Content", qn.getLocalPart());
    }

    private QName getName(XmlObject o) {
        try (XmlCursor c = o.newCursor()) {
            return c.getName();
        }
    }
}


