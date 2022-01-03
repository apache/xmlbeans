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
import org.junit.Test;
import xmlcursor.common.BasicCursorTestCase;

import static org.junit.Assert.*;

public class ToNextSelectionTest extends BasicCursorTestCase {
    @Test
    public void testToNextSelectionMultipleReturns() throws Exception {
        String sXml = "<foo><b>0</b><b>1</b><b>2</b><b>3</b><b>4</b><b>5</b><b>6</b></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        m_xc.selectPath("$this//b");
        m_xc.toNextSelection();
        for (int i = 0; i < 6; i++) {
            assertEquals("" + i, m_xc.getTextValue());
            assertTrue(m_xc.toNextSelection());
        }
        assertEquals("6", m_xc.getTextValue());
        assertFalse(m_xc.toNextSelection());
    }

    @Test
    public void testToNextSelectionAfterClear() throws Exception {
        String sXml = "<foo><b>0</b><b>1</b><b>2</b><b>3</b><b>4</b><b>5</b><b>6</b></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        m_xc.selectPath("$this//b");
        m_xc.toNextSelection();
        for (int i = 0; i < 3; i++) {
            assertEquals("" + i, m_xc.getTextValue());
            assertTrue(m_xc.toNextSelection());
        }
        m_xc.clearSelections();
        assertEquals("3", m_xc.getTextValue());
        assertFalse(m_xc.toNextSelection());
    }

    @Test
    public void testToNextSelectionBeforeSelect() throws Exception {
        String sXml = "<foo><b>0</b><b>1</b><b>2</b><b>3</b><b>4</b><b>5</b><b>6</b></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        assertFalse(m_xc.toNextSelection());
        m_xc.selectPath("$this//b");
        m_xc.toNextSelection();
        for (int i = 0; i < 6; i++) {
            assertEquals("" + i, m_xc.getTextValue());
            assertTrue(m_xc.toNextSelection());
        }
        assertEquals("6", m_xc.getTextValue());
        assertFalse(m_xc.toNextSelection());
    }

    @Test
    public void testToNextSelectionOtherCursor() throws Exception {
        String sXml = "<foo><b>0</b><b>1</b><b>2</b><b>3</b><b>4</b><b>5</b><b>6</b></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        try (XmlCursor xc0 = m_xc.newCursor()) {
            m_xc.selectPath("$this//b");
            assertFalse(xc0.toNextSelection());
        }
    }

    @Test
    public void testToNextSelectionTwoCursorsDifferentSelections() throws Exception {
        String sXml = "<foo><a>X</a><b>0</b><a>Y</a><b>1</b><a>Z</a><b>2</b></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        try (XmlCursor xc0 = m_xc.newCursor()) {
            xc0.selectPath("$this//a");
            xc0.toNextSelection();
            assertEquals(3, xc0.getSelectionCount());
            m_xc.selectPath("$this//b");
            m_xc.toNextSelection();
            assertEquals(3, m_xc.getSelectionCount());
            assertTrue(xc0.toNextSelection());
            assertEquals("Y", xc0.getTextValue());
            assertTrue(m_xc.toNextSelection());
            assertEquals("1", m_xc.getTextValue());
        }
    }

    @Test
    public void testToNextSelectionTwoCursorsSameSelections() throws Exception {
        String sXml = "<foo><a>X</a><b>0</b><a>Y</a><b>1</b><a>Z</a><b>2</b></foo>";
        m_xc = XmlObject.Factory.parse(sXml).newCursor();
        try (XmlCursor xc0 = m_xc.newCursor()) {
            xc0.selectPath("$this//b");
            xc0.toNextSelection();
            assertEquals(3, xc0.getSelectionCount());
            m_xc.selectPath("$this//b");
            m_xc.toNextSelection();
            assertEquals(3, m_xc.getSelectionCount());
            assertTrue(xc0.toNextSelection());
            assertEquals("1", xc0.getTextValue());
            assertTrue(m_xc.toNextSelection());
            assertEquals("1", m_xc.getTextValue());
            assertTrue(xc0.toNextSelection());
            assertEquals("2", xc0.getTextValue());
            assertTrue(m_xc.toNextSelection());
            assertEquals("2", m_xc.getTextValue());
            assertFalse(xc0.toNextSelection());
            assertFalse(m_xc.toNextSelection());
        }
    }
}

