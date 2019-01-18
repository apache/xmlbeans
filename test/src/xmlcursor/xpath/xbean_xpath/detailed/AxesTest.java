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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    private String sXmlChild =
        "<foo> <bar xmlns:pre=\"http://uri.com\" at0='val0'>" +
        "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</bar></foo>";

    private XmlOptions options = new XmlOptions();

    private String sXmlDesc =
        "<foo> <foo xmlns:pre=\"http://uri.com\" at0='val0'>" +
        "<pre:baz xmlns:baz='http://uri' baz:at0='val1'/>txt child</foo></foo>";

    @Before
    public void setUp() {
        options.put("use xbean for xpath");
    }

    @Test
    public void testChildAxisAbbrev() throws XmlException {
        String sQuery1 = "./foo/bar";
        String sExpected =
            "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
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

    @Test
    public void testChildAxis() throws XmlException {
        String sQuery1 = "./foo/child::bar";
        String sExpected =
            "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    public void testChildAxisDot() throws XmlException {
        String sQuery1 = "$this/foo/./bar";
        String sExpected =
            "<bar at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
            "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</bar>";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    public void testChildAxisDNE() throws XmlException {
        String sQuery1 = "$this/foo/./baz";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        c.selectPath(sQuery1, options);
        assertEquals(0, c.getSelectionCount());
    }

    @Test
    @Ignore
    public void testDescendantAxis() throws XmlException {
        String sQuery1 = "./descendant::foo";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz  baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    public void testDescendantAxisAbbrev() throws XmlException {
        String sQuery1 = ".//foo";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testDescAxisDot() throws XmlException {
        String sQuery1 = "$this/descendant::foo/.";
        String sExpected = "<foo at0=\"val0\" xmlns:pre=\"http://uri.com\">" +
                           "<pre:baz  baz:at0=\"val1\" xmlns:baz=\"http://uri\"/>txt child</foo>";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testDescAxisDNE() throws XmlException {
        String sQuery1 = "$this/descendant::baz";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1, options);
        assertEquals(0, c.getSelectionCount());
    }

    @Test
    public void testChildAttribute() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this/foo/bar/attribute::at0";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    public void testChildAttributeAbbrev() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this/foo/bar/@at0";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    public void testDescAttribute() throws XmlException {
        String sExpected = "<xml-fragment at0=\"val0\" xmlns:pre=\"http://uri.com\"/>";
        String sQuery1 = "$this//attribute::at0";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testDescendantOrSelfAxis() throws XmlException {

        String sQuery1 = "./descendant-or-self::foo";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
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

    @Test
    @Ignore
    public void testDescendantOrSelfAxisDot() throws XmlException {
        String sQuery1 = "./descendant-or-self::foo";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
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

    @Test
    @Ignore
    public void testDescendantOrSelfAxisDNE() throws XmlException {

        String sQuery1 = "$this/descendant-or-self::baz";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1, options);
        assertEquals(0, c.getSelectionCount());

    }

    @Test
    @Ignore
    public void testSelfAxis() throws XmlException {
        String sQuery1 = "$this/self::foo";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        String sExpected =
            c.xmlText();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    public void testSelfAxisAbbrev() throws XmlException {
        String sQuery1 = ".";
        XmlCursor c = XmlObject.Factory.parse(sXmlChild).newCursor();
        String sExpected =
            c.xmlText();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testSelfAxisDot() throws XmlException {

        String sQuery1 = "./self::foo";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        String sExpected =
            c.xmlText();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testSelfAxisDNE() throws XmlException {

        String sQuery1 = "$this/self::baz";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        c.selectPath(sQuery1, options);
        assertEquals(0, c.getSelectionCount());

    }

    @Test
    @Ignore
    public void testNamespaceAxis() throws XmlException {

        String sQuery1 = "$this/namespace::http://uri.com";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        String sExpected =
            c.xmlText();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals(XmlCursor.TokenType.TEXT, c.toNextToken());
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testNamespaceAxisDot() throws XmlException {

        String sQuery1 = "./*/namespace::http://uri.com";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        String sExpected =
            c.xmlText();

        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals("foo", c.getName().getLocalPart());

        c.selectPath(sQuery1, options);
        assertEquals(1, c.getSelectionCount());
        c.toNextSelection();
        assertEquals(sExpected, c.xmlText());
    }

    @Test
    @Ignore
    public void testNamespaceAxisDNE() throws XmlException {

        String sQuery1 = "$this/namespace::*";
        XmlCursor c = XmlObject.Factory.parse(sXmlDesc).newCursor();
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        assertEquals(XmlCursor.TokenType.TEXT, c.toNextToken());
        assertEquals(XmlCursor.TokenType.START, c.toNextToken());
        //to namespace
        assertEquals(XmlCursor.TokenType.NAMESPACE, c.toNextToken());
        c.selectPath(sQuery1, options);
        assertEquals(0, c.getSelectionCount());
    }
}

