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


package xmlcursor.checkin;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xmlcursor.common.BasicCursorTestCase.cur;


public class AddToSelectionTest {

    private static final String XML = "<foo><b>0</b><b>1</b><b>2</b><b attr=\"a3\">3</b><b>4</b><b>5</b><b>6</b></foo>";

    @Test
    void testAddToSelectionEnd() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.toEndDoc();
            m_xc.addToSelection();
            assertEquals(1, m_xc.getSelectionCount());
        }
    }

    @Test
    void testAddToSelectionStart() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.toStartDoc();
            m_xc.addToSelection();
            assertEquals(1, m_xc.getSelectionCount());
        }
    }

    @Test
    void testAddToSelectionAll() throws Exception {
        String xml = "<foo></foo>";
        try (XmlCursor m_xc = cur(xml)) {
            m_xc.addToSelection();
            while (m_xc.toNextToken() != XmlCursor.TokenType.NONE) {
                m_xc.addToSelection();
            }
            assertEquals(4, m_xc.getSelectionCount());

            // check results
            try (XmlCursor m_xc1 = cur(xml)) {
                // reset cursor
                m_xc.toSelection(0);
                XmlCursor.TokenType tok;
                while ((tok = m_xc1.toNextToken()) != XmlCursor.TokenType.NONE) {
                    // assertEquals(true,m_xc.hasNextSelection());
                    assertEquals(m_xc.toNextToken(), tok);
                    m_xc.toNextSelection();
                }
                // second cursor should be at the end of selections too...
                assertFalse(m_xc.toNextSelection());
            }
        }
    }

    @Test
    void testAddToSelectionSet() throws XmlException {
        //not set but bag semantics
        int expRes = 100;

        try (XmlCursor m_xc = cur(XML)) {
            for (int i = 0; i < 100; i++) {
                m_xc.toStartDoc();
                m_xc.addToSelection();
            }
            assertEquals(expRes, m_xc.getSelectionCount());
        }
    }

    @Test
    void testAddAfterClose() throws XmlException {
        try (XmlCursor m_xc = cur(XML)) {
            m_xc.close();
            // TODO: refine exception class
            assertThrows(Throwable.class, m_xc::addToSelection);
        }

    }
}

