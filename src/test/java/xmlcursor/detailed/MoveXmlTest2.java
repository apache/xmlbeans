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

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.*;

public class MoveXmlTest2 {

    private static final String XML = "<bk:book xmlns:bk='urn:loc.gov:books' at0=\"value0\"><!--BOOK COMMENT-->text0<author at0=\"v0\" at1=\"value1\"/></bk:book>";
    private static final String TARGET = "<target></target>";

    @Test
    void testNormalCase() throws XmlException {
        String sExpectedSrc = "<bk:book at0=\"value0\" xmlns:bk=\"urn:loc.gov:books\">text0<author at0=\"v0\" at1=\"value1\"/></bk:book>";

        try (XmlCursor m_xc = cur(XML); XmlCursor m_xc1 = cur(TARGET)) {
            toNextTokenOfType(m_xc1, TokenType.START);
            toNextTokenOfType(m_xc, TokenType.COMMENT);
            m_xc.moveXml(m_xc1);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            toPrevTokenOfType(m_xc1, TokenType.STARTDOC);
            assertEquals(sExpectedSrc, m_xc.xmlText());
            assertEquals("<!--BOOK COMMENT--><target/>", m_xc1.xmlText());


            //copy the namespace declaration exlplicitly
            toNextTokenOfType(m_xc1, TokenType.END);
            toNextTokenOfType(m_xc, TokenType.NAMESPACE);
            m_xc.moveXml(m_xc1);
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            toPrevTokenOfType(m_xc1, TokenType.STARTDOC);
            assertEquals("<!--BOOK COMMENT--><target xmlns:bk=\"urn:loc.gov:books\"/>", m_xc1.xmlText());
            assertEquals(sExpectedSrc, m_xc.xmlText());
        }
    }

    //to here at END
    @Test
    void testMoveNoop() throws XmlException {
        try (XmlCursor m_xc = cur(XML); XmlCursor m_xc1 = cur(TARGET)) {
            toNextTokenOfType(m_xc1, TokenType.START);
            toNextTokenOfType(m_xc, TokenType.END);
            assertThrows(IllegalArgumentException.class, () -> m_xc.moveXml(m_xc1));
            toPrevTokenOfType(m_xc, TokenType.STARTDOC);
            toPrevTokenOfType(m_xc1, TokenType.STARTDOC);

            toNextTokenOfType(m_xc1, TokenType.START);
            toNextTokenOfType(m_xc, TokenType.ENDDOC);
            assertThrows(IllegalArgumentException.class, () -> m_xc.moveXml(m_xc1));
        }
    }

    @Test
    void testInvalidToCursorPos() throws XmlException {
        try (XmlCursor m_xc = cur(XML); XmlCursor m_xc1 = cur(TARGET)) {
            //position the cursor within a tag <a <movedXML/>...</a>
            toNextTokenOfType(m_xc, TokenType.START);//m_xc on book at0
            assertTrue(m_xc.toFirstAttribute()); //at0 in book
            toNextTokenOfType(m_xc1, TokenType.START);
            assertThrows(Exception.class, () -> m_xc1.moveXml(m_xc));
        }
    }

    @Test
    void testMovedAttrNameCollision() throws Exception {

        try (XmlCursor m_xc = cur(XML); XmlCursor m_xc1 = cur(XML)) {
            toNextTokenOfType(m_xc, TokenType.START);//m_xc on book at0
            toNextTokenOfType(m_xc1, TokenType.START);
            toNextTokenOfType(m_xc1, TokenType.START);
            //toNextTokenOfType(m_xc1,TokenType.END);//to author
            assertTrue(m_xc1.toFirstAttribute());
            assertTrue(m_xc.toFirstAttribute()); //at0 in book
            if (m_xc.moveXml(m_xc1)) {
                toPrevTokenOfType(m_xc1, TokenType.START);
                m_xc1.toFirstAttribute();
                assertEquals("at0", m_xc1.getName().getLocalPart());
                assertTrue(m_xc1.toNextAttribute());
                assertEquals("at0", m_xc1.getName().getLocalPart());
            }
        }
    }

    /**
     * attempt to create an XML forest:
     * seems to be illegal semantics judging from beginElement
     * $NOTE: legal here
     */
    @Test
    void testInvalidXml() throws XmlException {
        try (XmlCursor m_xc = cur(XML); XmlCursor m_xc1 = cur(TARGET)) {
            toNextTokenOfType(m_xc, TokenType.START);
            toNextTokenOfType(m_xc1, TokenType.START);
            assertTrue(m_xc.moveXml(m_xc1));
        }
    }

    @Test
    void testNull() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            toNextTokenOfType(m_xc, TokenType.START);
            assertThrows(Exception.class, () -> m_xc.moveXml(null));
        }
    }

    @Test
    void testSelf() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            String sExpectedResult = m_xc.xmlText();
            toNextTokenOfType(m_xc, TokenType.START);
            if (m_xc.moveXml(m_xc)) {
                m_xc.toStartDoc();
                assertEquals(sExpectedResult, m_xc.xmlText());
            }
        }
    }

}
