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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Verifies XPath with Expressions
 * http://www.w3schools.com/xpath/xpath_expressions.asp
 */
@Ignore("abstract class")
public abstract class XPathExpressionTest extends BaseXPathTest {

    //("/catalog/cd[price>10.80]/price
    //Numerical Expressions


    /**
     * + Addition 6 + 4 10
     */
    @Test
    public void testAddition() {
        String sXpath=getQuery("testAddition",0);
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals("<price at=\"val0\">3.00</price>",m_xc.xmlText());
    }
    /**
     * - Subtraction 6 - 4 2
     */
    @Test
    public void testSubtraction() {
        String sXpath=getQuery("testSubtraction",0);
        String sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * * Multiplication 6 * 4 24
     */
    @Test
    public void testMultiplication() {
        String sXpath=getQuery("testMultiplication",0);
        String sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * div Division 8 div 4 2
     * NOTE: do a case where res is infinite (eg 10 div 3 or 22/7)
     */
    @Test
    public void testDiv() {
        String sXpath=getQuery("testDiv",0); //get the second(last) price child
        String sExpected="<price at=\"val0\">3.00</price>";
        m_xc.selectPath(sXpath);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        m_xc.clearSelections();
        m_xc.toStartDoc();

        sXpath=getQuery("testDiv",1); //get the second(last) price child
        sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        m_xc.clearSelections();
        m_xc.toStartDoc();

        String sXpathZero=getQuery("testDiv",2);
        int i = 0;
        try{
            m_xc.selectPath(sXpathZero);
            i = m_xc.getSelectionCount();
            fail("Division by 0");
        } catch (Exception ignored){}
        assertEquals(0,i);

        m_xc.clearSelections();
        m_xc.toStartDoc();

        String sXpathInf=getQuery("testDiv",3);
        m_xc.selectPath(sXpathInf);
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * mod Modulus (division remainder) 5 mod 2 1
     */
    @Test(expected = Exception.class)
    public void testMod() {
        String sXpath=getQuery("testMod",0); //get the second(last) price child
        String sExpected="<price at=\"val1\">2</price>";

        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        m_xc.clearSelections();
        m_xc.toStartDoc();


        sXpath=getQuery("testMod",1); //get the second(last) price child

        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());

        String sXpathZero="10 mod 0";
        m_xc.clearSelections();
        m_xc.toStartDoc();
        m_xc.selectPath(sXpathZero);
        m_xc.getSelectionCount();
    }

    //Equality Expressions
    /**
     * = Like (equal) price=9.80 true (if price is 9.80)
     */
    @Test
    public void testEqual() throws XmlException {
        String sXml="<foo><bar>" +
                "<price at=\"val0\">3.00</price>" +
                "<price at=\"val1\">2</price></bar><bar>" +
                "<price>5.00</price></bar></foo>";
        m_xc=XmlObject.Factory.parse(sXml).newCursor();
        String sXpath=getQuery("testEqual",0);
        String sExpected="<bar><price>5.00</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    //Existential semantics of equality in a node set
    //check this--not sure how to create this test
    @Test
    public void testEqualityNodeset() {
        String sXpath=getQuery("testEqualityNodeset",0);
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * != Not like (not equal) price!=9.80 false
     */
    @Test
    public void testNotEqual() {
        assertEquals(0,m_xc.getSelectionCount());
        String sXpath=getQuery("testNotEqual",0); //has to be double-comparison
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        System.out.println(m_xc.xmlText());
        assertEquals(sExpected,m_xc.xmlText());
    }

    //Relational Expressions
    /**
     * < Less than price<9.80 false (if price is 9.80)
     */
    @Test
    public void testLessThan() {
        String sXpath=getQuery("testLessThan",0);
        m_xc.selectPath(sXpath);
        assertEquals(0,m_xc.getSelectionCount());
    }

    /**
     * <= Less or equal price<=9.80 true
     */
    @Test
    public void testLessOrEqual() {
        String sXpath=getQuery("testLessOrEqual",0);
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * > Greater than price>9.80 false
     */
    @Test
    public void testGreaterThan() {
        String sXpath=getQuery("testGreaterThan",0);
        String sExpected="<bar><price at=\"val0\">3.00</price><price at=\"val1\">2</price></bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * >= Greater or equal price>=9.80 true
     */
    @Test
    public void testGreaterOrEqual() {
        String sXpath=getQuery("testGreaterOrEqual",0);
        String sExpected="<bar>" +
                "<price at=\"val0\">3.00</price><price at=\"val1\">2</price>" +
                "</bar>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    //Boolean Expressions
    /**
     * or or price=9.80 or price=9.70 true (if price is 9.80)
     */
    @Test
    public void testOr() {
        String sXpath=getQuery("testOr",0);
        String sExpected="<price at=\"val1\">2</price>";
        m_xc.selectPath(sXpath);
        assertEquals(1,m_xc.getSelectionCount());
        m_xc.toNextSelection();
        assertEquals(sExpected,m_xc.xmlText());
    }

    /**
     * and and  price<=9.80 and price=9.70 false
     */
    @Test
    public void testAnd() {
        String sXpath=getQuery("testAnd",0);
        m_xc.selectPath(sXpath);
        assertEquals(0,m_xc.getSelectionCount());
    }

    @Before
    public void setUp()throws Exception {
        super.setUp();
        String sXml = "<foo>" +
                      "<bar><price at=\"val0\">3.00</price>" +
                      "<price at=\"val1\">2</price></bar><bar1>3.00</bar1>" +
                      "</foo>";
        m_xc=XmlObject.Factory.parse(sXml).newCursor();
    }

}
