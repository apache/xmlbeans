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
import org.apache.xmlbeans.XmlObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class AddToSelectionTest extends BasicCursorTestCase {

    private static String sXml = "<foo><b>0</b><b>1</b><b>2</b><b attr=\"a3\">3</b><b>4</b><b>5</b><b>6</b></foo>";

    @Test
    public void testAddToSelectionEnd() {
        m_xc.toEndDoc();
        m_xc.addToSelection();
        assertEquals(1, m_xc.getSelectionCount());
    }

    @Test
    public void testAddToSelectionStart() {
        m_xc.toStartDoc();
        m_xc.addToSelection();
        assertEquals(1, m_xc.getSelectionCount());
    }

    @Test
    public void testAddToSelectionAll() throws Exception {
        sXml = "<foo></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        XmlCursor.TokenType tok;
        m_xc.addToSelection();
        while ((tok = m_xc.toNextToken()) != XmlCursor.TokenType.NONE) {
            System.err.println(tok);
            m_xc.addToSelection();
        }
        assertEquals(4, m_xc.getSelectionCount());

        //check results
        XmlCursor m_xc1 = XmlObject.Factory.parse(sXml).newCursor();
        m_xc.toSelection(0); //reset cursor
        int i = m_xc.getSelectionCount();
        while ((tok = m_xc1.toNextToken()) != XmlCursor.TokenType.NONE) {
            //assertEquals(true,m_xc.hasNextSelection());
            assertEquals(m_xc.toNextToken(), tok);
            m_xc.toNextSelection();
        }
        //second cursor should be at the end of selections too...
        assertFalse(m_xc.toNextSelection());
        m_xc1.dispose();
    }

    @Test
    public void testAddToSelectionSet() {
        //not set but bag semantics
        int expRes = 100;

        m_xc.clearSelections();
        for (int i = 0; i < 100; i++) {
            m_xc.toStartDoc();
            m_xc.addToSelection();
        }
        assertEquals(expRes, m_xc.getSelectionCount());
    }

    @Test(expected = Throwable.class)
    public void testAddAfterDispose() {
        m_xc.dispose();
        m_xc.addToSelection();

    }

    @Before
    public void setUp() throws Exception {
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
    }

    @After
    public void tearDown() {
        if (m_xc == null) return;
        try {
            m_xc.clearSelections();
            super.tearDown();
        } catch (IllegalStateException e) { //cursor disposed
        } catch (Exception e) {

        }
    }
}

