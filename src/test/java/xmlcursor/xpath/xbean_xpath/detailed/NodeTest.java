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

package xmlcursor.xpath.xbean_xpath.detailed;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.cur;

/**
 * Nodes Tested:
 *
 * NameTest
 *    "*"
 *    NCName:*
 *    QName
 * NodeType
 *    comment
 *    node
 *    pi
 *    text
 * PI(Literal)
 */
public class NodeTest {

    private static final String sXmlChild =
        "<foo> <bar xmlns:pre=\"http://uri.com\" at0='val0'>" +
        "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</bar>" +
        "</foo>";

    private static final String sXmlPI =
        "<foo><?xml-stylesheet target=\"http://someuri\"?></foo>";


    @Test
    void testNameTestStar() throws XmlException {
        String sQuery1 = "./*";

        try (XmlCursor c = cur(sXmlChild)) {
            String sExpected = c.xmlText();
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testNameTestNCName() throws XmlException {
        String sQuery1 = "$this//*";
        String sExpected = "<pre:baz baz:at0=\"val1\" " +
                           "xmlns:baz=\"http://uri\" xmlns:pre=\"http://uri.com\"/>";

        try (XmlCursor c = cur(sXmlChild)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.toNextToken();
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("bar", c.getName().getLocalPart());
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testNameTestQName_1() throws XmlException {
        String sQuery1 = "declare namespace pre=\"http://uri.com\"; $this//pre:*";
        String sExpected =
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\" xmlns:pre=\"http://uri.com\"/>";

        try (XmlCursor c = cur(sXmlChild)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    //test a QName that DNE
    @Test
    void testNameTestQName_2() throws XmlException {
        String sQuery1 = "declare namespace pre=\"http://uri\"; $this//pre:baz";

        try (XmlCursor c = cur(sXmlChild)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1);
            assertEquals(0, c.getSelectionCount());
        }
    }

    @Test
    void testNameTestQName_3() throws XmlException {
        String sQuery1 = "$this//bar";
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";

        try (XmlCursor c = cur(sXmlChild)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testNodeTypeComment() {

    }


    @Test
    void testNodeTypeNodeAbbrev() throws XmlException {
        String sQuery1 = "$this/foo/*";
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    /**
     * Will not support natively
     */
    @Test
    @Disabled
    public void testNodeTypeNode() throws XmlException {
        String sQuery1 = "$this/foo/node()";
        String sExpected = "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testNodeTypePI() throws XmlException {
        String sExpected = "<foo><?xml-stylesheet target=\"http://someuri\"?></foo>";
        String sQuery = "./foo/processing-instruction()";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testNodeTypeText() throws XmlException {
        String sQuery1 = "$this//text()";
        String sExpected = " ";

        try (XmlCursor c = cur(sXmlChild)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testPI() throws XmlException {
        try (XmlCursor c = cur(sXmlPI)) {
            String sExpected = "<?xml-stylesheet target=\"http://someuri\"?>";
            String sQuery = "./foo/processing-instruction('xml-stylesheet')";
            c.selectPath(sQuery);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testPIDNE() throws XmlException {
        try (XmlCursor c = cur(sXmlPI)) {
            String sQuery = "./foo/processing-instruction('stylesheet')";
            c.selectPath(sQuery);
            assertEquals(0, c.getSelectionCount());
        }
    }
}
