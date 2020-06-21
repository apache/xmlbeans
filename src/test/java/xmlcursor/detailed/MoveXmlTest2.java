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
import org.apache.xmlbeans.XmlObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import static org.junit.Assert.*;

public class MoveXmlTest2 extends BasicCursorTestCase
        {

    private static String sTestXml = "<bk:book xmlns:bk='urn:loc.gov:books' at0=\"value0\"><!--BOOK COMMENT-->text0<author at0=\"v0\" at1=\"value1\"/></bk:book>";
    private static XmlCursor m_xc1;

    @Test
    public void testNormalCase()
    {
        String sExpectedTrg1 = "<!--BOOK COMMENT--><target/>";
        String sExpectedSrc1 = "<bk:book at0=\"value0\" " +
                "xmlns:bk=\"urn:loc.gov:books\">" +
                "text0<author at0=\"v0\" at1=\"value1\"/></bk:book>";


        toNextTokenOfType(m_xc1, TokenType.START);
        toNextTokenOfType(m_xc, TokenType.COMMENT);
        m_xc.moveXml(m_xc1);
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        toPrevTokenOfType(m_xc1, TokenType.STARTDOC);
        assertEquals(m_xc.xmlText(), sExpectedSrc1);
        assertEquals(m_xc1.xmlText(), sExpectedTrg1);


        sExpectedTrg1 =
                "<!--BOOK COMMENT--><target xmlns:bk=\"urn:loc.gov:books\"/>";
        sExpectedSrc1 = "<bk:book " +
                "at0=\"value0\" " +
                "xmlns:bk=\"urn:loc.gov:books\">" +
                "text0<author at0=\"v0\" at1=\"value1\"/>" +
                "</bk:book>";

        //copy the namespace declaration exlplicitly
        toNextTokenOfType(m_xc1, TokenType.END);
        toNextTokenOfType(m_xc, TokenType.NAMESPACE);
        m_xc.moveXml(m_xc1);

        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        toPrevTokenOfType(m_xc1, TokenType.STARTDOC);

        assertEquals(m_xc1.xmlText(), sExpectedTrg1);
        assertEquals(m_xc.xmlText(), sExpectedSrc1);


    }

    //to here at END
    @Test(expected = IllegalArgumentException.class)
    public void testMoveNoop()
    {

        toNextTokenOfType(m_xc1, TokenType.START);
        toNextTokenOfType(m_xc, TokenType.END);
        try {
            m_xc.moveXml(m_xc1);
            fail(" need IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
        toPrevTokenOfType(m_xc, TokenType.STARTDOC);
        toPrevTokenOfType(m_xc1, TokenType.STARTDOC);

        toNextTokenOfType(m_xc1, TokenType.START);
        toNextTokenOfType(m_xc, TokenType.ENDDOC);
        m_xc.moveXml(m_xc1);
    }

    @Test
    public void testInvalidToCursorPos()
    {
        //position the cursor within a tag <a <movedXML/>...</a>
        toNextTokenOfType(m_xc, TokenType.START);//m_xc on book at0
        assertTrue(m_xc.toFirstAttribute()); //at0 in book
        toNextTokenOfType(m_xc1, TokenType.START);
        try {
            if (m_xc1.moveXml(m_xc)) {

                fail("Should not be able to move the XML here ");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void testMovedAttrNameCollision() throws Exception
    {

        m_xc1 = XmlObject.Factory.parse(sTestXml).newCursor();
        toNextTokenOfType(m_xc, TokenType.START);//m_xc on book at0
        toNextTokenOfType(m_xc1, TokenType.START);
        toNextTokenOfType(m_xc1, TokenType.START);
        //toNextTokenOfType(m_xc1,TokenType.END);//to author
        assertTrue(m_xc1.toFirstAttribute());
        assertTrue(m_xc.toFirstAttribute()); //at0 in book
        if (m_xc.moveXml(m_xc1)) {
            toPrevTokenOfType(m_xc1, TokenType.START);
            m_xc1.toFirstAttribute();
            assertEquals(m_xc1.getName().getLocalPart(), "at0");
            assertTrue(m_xc1.toNextAttribute());
            assertEquals(m_xc1.getName().getLocalPart(), "at0");
        }
        m_xc1.dispose();
    }

    /**
     * attempt to create an XML forest:
     * seems to be illegal semantics judging from beginElement
     * $NOTE: legal here
     */
    @Test
    public void testInvalidXml()
    {
        toNextTokenOfType(m_xc, TokenType.START);
        toNextTokenOfType(m_xc1, TokenType.START);
        assertTrue(m_xc.moveXml(m_xc1));
    }

    @Test
    public void testNull()
    {
        toNextTokenOfType(m_xc, TokenType.START);
        try {
            m_xc.moveXml(null);
            fail("toHere null");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void testSelf()
    {
        String sExpectedResult = m_xc.xmlText();
        toNextTokenOfType(m_xc, TokenType.START);
        try {

            if (m_xc.moveXml(m_xc)) {
                m_xc.toStartDoc();
                assertEquals(sExpectedResult, m_xc.xmlText());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Before
    public void setUp() throws Exception
    {
        m_xc = XmlObject.Factory.parse(sTestXml).newCursor();
        String sTargetXml = "<target></target>";
        m_xc1 = XmlObject.Factory.parse(sTargetXml).newCursor();
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        if (m_xc1 != null) {
            m_xc1.dispose();
            m_xc1 = null;
        }
    }
}
