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
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xmlcursor.common.BasicCursorTestCase.cur;

/**
 * Axes Tested:
 * child
 * descendant
 * attribute
 * descendant-or-self
 * self
 * namespace
 */
public class AxesTest {
    private static final String sXmlChild =
        "<foo> <bar xmlns:pre=\"http://uri.com\" at0='val0'>" +
        "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</bar></foo>";

    private final XmlOptions options = new XmlOptions();

    private static final String sXmlDesc =
        "<foo> <foo xmlns:pre=\"http://uri.com\" at0='val0'>" +
        "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</foo></foo>";

    @BeforeEach
    public void setUp() {
        options.setXPathUseXmlBeans();
    }

    @Test
    void testChildAxisAbbrev() throws XmlException {
        String sQuery1 = "./foo/bar";
        String sExpected =
            "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());

            sQuery1 = "$this/foo/child::bar";
            c.clearSelections();
            c.toStartDoc();
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testChildAxis() throws XmlException {
        String sQuery1 = "./foo/child::bar";
        String sExpected =
            "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testChildAxisDot() throws XmlException {
        String sQuery1 = "$this/foo/./bar";
        String sExpected =
            "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testChildAxisDNE() throws XmlException {
        String sQuery1 = "$this/foo/./baz";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1, options);
            assertEquals(0, c.getSelectionCount());
        }
    }

    @Test
    @Disabled
    public void testDescendantAxis() throws XmlException {
        String sQuery1 = "./descendant::foo";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz  baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testDescendantAxisAbbrev() throws XmlException {
        String sQuery1 = ".//foo";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testDescAxisDot() throws XmlException {
        String sQuery1 = "$this/descendant::foo/.";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz  baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testDescAxisDNE() throws XmlException {
        String sQuery1 = "$this/descendant::baz";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1, options);
            assertEquals(0, c.getSelectionCount());
        }
    }

    @Test
    void testChildAttribute() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this/foo/bar/attribute::at0";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testChildAttributeAbbrev() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this/foo/bar/@at0";

        try (XmlCursor c = cur(sXmlChild)) {
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testDescAttribute() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this//attribute::at0";

        try (XmlCursor c = cur(sXmlChild)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testDescendantOrSelfAxis() throws XmlException {
        String sQuery1 = "./descendant-or-self::foo";

        try (XmlCursor c = cur(sXmlDesc)) {
            String[] sExpected = {
                c.xmlText()
                , "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                  "<pre:baz  baz:at0=\"val1\"" +
                  " xmlns:baz=\"http://uri\"/>txt child</foo>"
            };

            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(2, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected[0], c.xmlText());
            c.toNextSelection();
            assertEquals(sExpected[1], c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testDescendantOrSelfAxisDot() throws XmlException {
        String sQuery1 = "./descendant-or-self::foo";

        try (XmlCursor c = cur(sXmlDesc)) {
            String[] sExpected = new String[]
                {
                    c.xmlText()
                    , "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                      "<pre:baz  baz:at0=\"val1\"" +
                      " xmlns:baz=\"http://uri\"/>txt child</foo>"
                };


            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1, options);

            c.selectPath(sQuery1, options);
            assertEquals(2, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected[0], c.xmlText());
            c.toNextSelection();
            assertEquals(sExpected[1], c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testDescendantOrSelfAxisDNE() throws XmlException {
        String sQuery1 = "$this/descendant-or-self::baz";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1, options);
            assertEquals(0, c.getSelectionCount());
        }
    }

    @Test
    @Disabled
    public void testSelfAxis() throws XmlException {
        String sQuery1 = "$this/self::foo";

        try (XmlCursor c = cur(sXmlDesc)) {
            String sExpected = c.xmlText();

            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    void testSelfAxisAbbrev() throws XmlException {
        String sQuery1 = ".";

        try (XmlCursor c = cur(sXmlChild)) {
            String sExpected = c.xmlText();

            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testSelfAxisDot() throws XmlException {
        String sQuery1 = "./self::foo";

        try (XmlCursor c = cur(sXmlDesc)) {
            String sExpected = c.xmlText();

            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testSelfAxisDNE() throws XmlException {
        String sQuery1 = "$this/self::baz";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            c.selectPath(sQuery1, options);
            assertEquals(0, c.getSelectionCount());
        }
    }

    @Test
    @Disabled
    public void testNamespaceAxis() throws XmlException {
        String sQuery1 = "$this/namespace::http://uri.com";

        try (XmlCursor c = cur(sXmlDesc)) {
            String sExpected = c.xmlText();

            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals(XmlCursor.TokenType.TEXT, c.toNextToken());
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testNamespaceAxisDot() throws XmlException {
        String sQuery1 = "./*/namespace::http://uri.com";

        try (XmlCursor c = cur(sXmlDesc)) {
            String sExpected = c.xmlText();

            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals("foo", c.getName().getLocalPart());

            c.selectPath(sQuery1, options);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(sExpected, c.xmlText());
        }
    }

    @Test
    @Disabled
    public void testNamespaceAxisDNE() throws XmlException {
        String sQuery1 = "$this/namespace::*";

        try (XmlCursor c = cur(sXmlDesc)) {
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            assertEquals(XmlCursor.TokenType.TEXT, c.toNextToken());
            assertEquals(XmlCursor.TokenType.START, c.toNextToken());
            //to namespace
            assertEquals(XmlCursor.TokenType.NAMESPACE, c.toNextToken());
            c.selectPath(sQuery1, options);
            assertEquals(0, c.getSelectionCount());
        }
    }
}

