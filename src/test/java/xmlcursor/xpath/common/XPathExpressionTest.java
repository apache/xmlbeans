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
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;
import xmlcursor.common.Common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xmlcursor.common.BasicCursorTestCase.cur;
import static xmlcursor.xpath.common.XPathTestBase.getQuery;

/**
 * Verifies XPath with Expressions
 * http://www.w3schools.com/xpath/xpath_expressions.asp
 */
public class XPathExpressionTest {

    private static final String XML =
        "<foo>" +
        "<bar><price at=\"val0\">3.00</price>" +
        "<price at=\"val1\">2</price></bar><bar1>3.00</bar1>" +
        "</foo>";


    //("/catalog/cd[price>10.80]/price
    //Numerical Expressions

    /**
     * + Addition 6 + 4 10
     */
    @Test
    void testAddition() throws XmlException {
        String sXpath = getQuery("testAddition", 0);
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals("<price at=\"val0\">3.00</price>", m_xc.xmlText());
        }
    }

    /**
     * - Subtraction 6 - 4 2
     */
    @Test
    void testSubtraction() throws XmlException {
        String sXpath = getQuery("testSubtraction", 0);
        String sExpected = "<price at=\"val1\">2</price>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * * Multiplication 6 * 4 24
     */
    @Test
    void testMultiplication() throws XmlException {
        String sXpath = getQuery("testMultiplication", 0);
        String sExpected = "<price at=\"val1\">2</price>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * div Division 8 div 4 2
     * NOTE: do a case where res is infinite (eg 10 div 3 or 22/7)
     */
    @Test
    void testDiv() throws XmlException {
        String sXpath = getQuery("testDiv", 0); //get the second(last) price child
        String sExpected = "<price at=\"val0\">3.00</price>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());

            m_xc.clearSelections();
            m_xc.toStartDoc();

            sXpath = getQuery("testDiv", 1); //get the second(last) price child
            sExpected = "<price at=\"val1\">2</price>";
            m_xc.selectPath(sXpath);
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());

            m_xc.clearSelections();
            m_xc.toStartDoc();

            String sXpathZero = getQuery("testDiv", 2);
            m_xc.selectPath(sXpathZero);
            assertThrows(Exception.class, m_xc::getSelectionCount, "Division by 0");
            assertEquals(0, m_xc.getSelectionCount());

            m_xc.clearSelections();
            m_xc.toStartDoc();

            String sXpathInf = getQuery("testDiv", 3);
            m_xc.selectPath(sXpathInf);
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * mod Modulus (division remainder) 5 mod 2 1
     */
    @Test
    void testMod() throws XmlException {
        String sXpath = getQuery("testMod", 0); //get the second(last) price child
        String sExpected = "<price at=\"val1\">2</price>";

        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());

            m_xc.clearSelections();
            m_xc.toStartDoc();

            sXpath = getQuery("testMod", 1); //get the second(last) price child

            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());

            String sXpathZero = "10 mod 0";
            m_xc.clearSelections();
            m_xc.toStartDoc();
            m_xc.selectPath(sXpathZero);
            assertThrows(Exception.class, m_xc::getSelectionCount);
        }
    }

    //Equality Expressions

    /**
     * = Like (equal) price=9.80 true (if price is 9.80)
     */
    @Test
    void testEqual() throws XmlException {
        String sXml = "<foo><bar>" +
                      "<price at=\"val0\">3.00</price>" +
                      "<price at=\"val1\">2</price></bar><bar>" +
                      "<price>5.00</price></bar></foo>";

        try (XmlCursor m_xc = cur(sXml)) {
            String sXpath = getQuery("testEqual", 0);
            String sExpected = "<bar><price>5.00</price></bar>";
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    //Existential semantics of equality in a node set
    //check this--not sure how to create this test
    @Test
    void testEqualityNodeset() throws XmlException {
        String sXpath = getQuery("testEqualityNodeset", 0);
        String sExpected = "<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * != Not like (not equal) price!=9.80 false
     */
    @Test
    void testNotEqual() throws XmlException {
        String sXpath = getQuery("testNotEqual", 0); //has to be double-comparison
        String sExpected = "<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        try (XmlCursor m_xc = cur(XML)) {
            assertEquals(0, m_xc.getSelectionCount());
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            System.out.println(m_xc.xmlText());
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    //Relational Expressions

    /**
     * < Less than price<9.80 false (if price is 9.80)
     */
    @Test
    void testLessThan() throws XmlException {
        String sXpath = getQuery("testLessThan", 0);
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(0, m_xc.getSelectionCount());
        }
    }

    /**
     * <= Less or equal price<=9.80 true
     */
    @Test
    void testLessOrEqual() throws XmlException {
        String sXpath = getQuery("testLessOrEqual", 0);
        String sExpected = "<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * > Greater than price>9.80 false
     */
    @Test
    void testGreaterThan() throws XmlException {
        String sXpath = getQuery("testGreaterThan", 0);
        String sExpected = "<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * >= Greater or equal price>=9.80 true
     */
    @Test
    void testGreaterOrEqual() throws XmlException {
        String sXpath = getQuery("testGreaterOrEqual", 0);
        String sExpected = "<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    //Boolean Expressions

    /**
     * or or price=9.80 or price=9.70 true (if price is 9.80)
     */
    @Test
    void testOr() throws XmlException {
        String sXpath = getQuery("testOr", 0);
        String sExpected = "<price at=\"val1\">2</price>";
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(1, m_xc.getSelectionCount());
            m_xc.toNextSelection();
            assertEquals(sExpected, m_xc.xmlText());
        }
    }

    /**
     * and and  price<=9.80 and price=9.70 false
     */
    @Test
    void testAnd() throws XmlException {
        String sXpath = getQuery("testAnd", 0);
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.selectPath(sXpath);
            assertEquals(0, m_xc.getSelectionCount());
        }
    }

    private void verifySelection(XmlCursor c, String[] expected) {
        int count = c.getSelectionCount();
        assertEquals(expected.length, count);
        for (int i = 0; i < count; i++) {
            c.toNextSelection();
            assertEquals(expected[i], c.xmlText());
        }
    }

    @Test
    void testForExpression() throws Exception {
        String sXml =
            "<bib>\n" +
            "  <book>\n" +
            "    <title>TCP/IP Illustrated</title>\n" +
            "    <author>Stevens</author>\n" +
            "    <publisher>Addison-Wesley</publisher>\n" +
            "  </book>\n" +
            "  <book>\n" +
            "    <title>Advanced Programming in the Unix environment</title>\n" +
            "    <author>Stevens</author>\n" +
            "    <publisher>Addison-Wesley</publisher>\n" +
            "  </book>\n" +
            "  <book>\n" +
            "    <title>Data on the Web</title>\n" +
            "    <author>Abiteboul</author>\n" +
            "    <author>Buneman</author>\n" +
            "    <author>Suciu</author>\n" +
            "  </book>\n" +
            "</bib>";

        String query =
            "for $a in distinct-values(//author) " +
            "return ($a," +
            "        for $b in //book[author = $a]" +
            "        return $b/title)";

        String[] exp = {
            "<xml-fragment>Stevens</xml-fragment>",
            "<title>TCP/IP Illustrated</title>",
            "<title>Advanced Programming in the Unix environment</title>",
            "<xml-fragment>Abiteboul</xml-fragment>",
            "<title>Data on the Web</title>",
            "<xml-fragment>Buneman</xml-fragment>",
            "<title>Data on the Web</title>",
            "<xml-fragment>Suciu</xml-fragment>",
            "<title>Data on the Web</title>"
        };

        try (XmlCursor c = cur(sXml)) {
            c.selectPath(query);
            verifySelection(c, exp);
        }
    }

    @Test
    void testFor_1() throws Exception {
        String query =
            "for $i in (10, 20),\n" +
            "    $j in (1, 2)\n" +
            "return ($i + $j)";

        try (XmlCursor c = cur("<a/>")) {
            c.selectPath(query);
            String[] expected = new String[]{
                Common.wrapInXmlFrag("11"),
                Common.wrapInXmlFrag("12"),
                Common.wrapInXmlFrag("21"),
                Common.wrapInXmlFrag("22")
            };
            verifySelection(c, expected);
        }
    }

    @Test
    void testFor_2() throws Exception {
        try (XmlCursor c = cur("<a/>")) {
            String query = "sum (for $i in (10, 20)" +
                           "return $i)";
            c.selectPath(query);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals(Common.wrapInXmlFrag("30"), c.xmlText());
        }
    }

    @Test
    void testIf() throws Exception {
        String sXML =
            "<root>" +
            "<book price='20'>Pooh</book>" +
            "<cd price='25'>Pooh</cd>" +
            "<book price='50'>Maid</book>" +
            "<cd price='25'>Maid</cd>" +
            "</root>";

        String query1 =
            "if (//book[1]/@price) " +
            "  then //book[1] " +
            "  else 0";

        String query2 =
            "for $b1 in //book, $b2 in //cd " +
            "return " +
            "if ( $b1/@price < $b2/@price )" +
            " then $b1" +
            " else $b2";

        try (XmlCursor c = cur(sXML)) {
            c.selectPath(query1);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals("<book price=\"20\">Pooh</book>", c.xmlText());

            c.selectPath(query2);
            assertEquals(4, c.getSelectionCount());
            c.toNextSelection();
            assertEquals("<book price=\"20\">Pooh</book>", c.xmlText());
            c.toNextSelection();
            assertEquals("<book price=\"20\">Pooh</book>", c.xmlText());
            c.toNextSelection();
            assertEquals("<cd price=\"25\">Pooh</cd>", c.xmlText());
            c.toNextSelection();
            assertEquals("<cd price=\"25\">Maid</cd>", c.xmlText());
        }
    }

    @Test
    void testQuantifiedExpression() throws Exception {
        String query =
            "some $x in (1, 2, 3), $y in (2, 3, 4) " +
            "satisfies $x + $y = 4";

        try (XmlCursor c = cur("<root></root>")) {
            c.selectPath(query);
            assertEquals(1, c.getSelectionCount());
            c.toNextSelection();
            assertEquals("<xml-fragment>true</xml-fragment>", c.xmlText());
        }
    }

}
