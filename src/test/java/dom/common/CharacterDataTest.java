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


package dom.common;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public abstract class CharacterDataTest extends NodeTest {

    @Test
    void testAppendChild() {
        Node newChild = m_doc.createElement("foo");
        try {
            m_node.appendChild(newChild);
        } catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    //super method not relevant
    @Test
    void testInsertBefore() {
        Node newChild = m_doc.createElement("foo");
        assertFalse(m_node.hasChildNodes());
        Node nullNode = m_node.getFirstChild();
        try {
            m_node.insertBefore(newChild, nullNode);
        } catch (DOMException de) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, de.code);
        }
    }

    @Test
    void testRemoveChild() {
        assertFalse(m_node.hasChildNodes());
    }

    @Test
    void testReplaceChild() {
        Node newChild = m_doc.createElement("foo");
        assertFalse(m_node.hasChildNodes());
        if (m_node.getFirstChild() != null)
            try {
                m_node.replaceChild(newChild, m_node.getFirstChild());
            } catch (DOMException de) {
                if (DOMException.HIERARCHY_REQUEST_ERR != de.code)
                    throw de;
            }

    }

    @Test
    void testAppendData() {
        String sOrig = ((CharacterData) m_node).getData();
        String sNewData = "some new data";
        ((CharacterData) m_node).appendData(sNewData);
        String sExpected = sOrig + sNewData;
        assertEquals(sExpected, ((CharacterData) m_node).getData());
    }

    @Test
    void testAppendDataNull() {
        String sOrig = ((CharacterData) m_node).getData();
        ((CharacterData) m_node).appendData("");
        assertEquals(sOrig, ((CharacterData) m_node).getData());

        ((CharacterData) m_node).appendData(null);
        assertEquals(sOrig, ((CharacterData) m_node).getData());
    }

    @Test
    void testDeleteDataNegOff() {
        _testDeleteData(-1, 10);
    }

    @Test
    void testDeleteDataNegLen() {
        _testDeleteData(1, -10);
    }

    @Test
    void testDeleteDataLargeOff() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testDeleteData(nDataLen + 1, 10);
    }

    @Test
    void testDeleteDataLargeLen() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testDeleteData(0, nDataLen + 30);
    }

    @Test
    void testDeleteDataAverage() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testDeleteData(0, nDataLen / 2);
    }

    private void _testDeleteData(int offset, int count) {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        if (offset < 0 || offset > nDataLen || count < 0) {
            DOMException de = assertThrows(DOMException.class, () -> ((CharacterData) m_node).deleteData(offset, count), "Deleting OOB chars");
            assertEquals(de.code, DOMException.INDEX_SIZE_ERR);
        } else {
            ((CharacterData) m_node).deleteData(offset, count);
            if (count == 0) {
                assertEquals(sData, ((CharacterData) m_node).getData());
            } else if (offset + count == nDataLen || (offset + count) > nDataLen) {
                assertEquals("", ((CharacterData) m_node).getData());
            } else if (offset == 0) {
                assertEquals(sData.substring(count), ((CharacterData) m_node).getData());
            } else {
                assertEquals(sData.substring(0, offset) + sData.substring(offset + count, sData.length() - (offset + count)), ((CharacterData) m_node).getData());
            }
        }
    }

    @Test
    void testGetData() {
        char[] buff = new char[200];
        java.util.Arrays.fill(buff, 'a');
        ((CharacterData) m_node).setData(new String(buff));
        try {
            assertEquals(new String(buff), ((CharacterData) m_node).getData());

        } catch (DOMException de) {
            assertEquals(de.code, DOMException.DOMSTRING_SIZE_ERR);
        }
    }

    @Test
    void testGetLength() {
        int nDataLen = ((CharacterData) m_node).getData().length();
        assertEquals(((CharacterData) m_node).getLength(), nDataLen);
    }

    @Test
    void testInsertNull() {
        _testInsertData(0, null);
    }

    @Test
    void testInsertNeg() {
        _testInsertData(-1, "foo");
    }

    @Test
    void testInsertOOB() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testInsertData(nDataLen + 2, "foo");
    }

    @Test
    void testInsertAverage() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testInsertData(nDataLen / 2, "foobar");
    }

    private void _testInsertData(int offset, String toInsert) {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        if (offset < 0 || offset > nDataLen) {
            DOMException de = assertThrows(DOMException.class, () -> ((CharacterData) m_node).insertData(offset, toInsert), "Inserting OOB chars");
            assertEquals(de.code, DOMException.INDEX_SIZE_ERR);
        } else {
            ((CharacterData) m_node).insertData(offset, toInsert);
            if (toInsert == null) {
                assertEquals(sData, ((CharacterData) m_node).getData());
            } else if (offset == nDataLen) {
                assertEquals(sData + toInsert, ((CharacterData) m_node).getData());
            } else {
                String s1 = sData.substring(0, offset);
                String s2 = sData.substring(offset, nDataLen);
                assertEquals(s1 + toInsert + s2, ((CharacterData) m_node).getData());
            }
        }
    }

    @Test
    void testReplaceDataNull() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testReplaceData(0, nDataLen, null);
    }

    @Test
    void testReplaceDataNegOff() {
        _testReplaceData(-1, 3, "foo");
    }

    @Test
    void testReplaceDataNegCount() {
        _testReplaceData(1, -3, "foo");
    }

    @Test
    void testReplaceDataZeroCount() {
        _testReplaceData(1, 0, "foo");
    }

    @Test
    void testReplaceDataLargeOff() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testReplaceData(nDataLen + 1, 2, "foo");
    }

    @Test
    void testReplaceDataLargeCount() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testReplaceData(0, nDataLen + 2, "foo");
    }

    @Test
    void testReplaceDataLarge() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testReplaceData(nDataLen / 2, nDataLen / 2 + 1, "foo");
    }

    @Test
    void testReplaceDataAverage() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testReplaceData(nDataLen / 2, nDataLen / 2, "foobar");
    }


    private void _testReplaceData(int offset, int count, String newStr) {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        if (offset < 0 || offset > nDataLen || count < 0) {
            DOMException de = assertThrows(DOMException.class, () -> ((CharacterData) m_node).replaceData(offset, count, newStr), "Deleting OOB chars");
            assertEquals(de.code, DOMException.INDEX_SIZE_ERR);
        } else {
            ((CharacterData) m_node).replaceData(offset, count, newStr);
            if (count == 0) {
                assertEquals(sData, ((CharacterData) m_node).getData());
            } else if (newStr == null) {
                assertEquals("", ((CharacterData) m_node).getData());
            } else if (offset + count == nDataLen || (offset + count) > nDataLen) {
                String sOld = sData.substring(0, offset);
                assertEquals(sOld + newStr, ((CharacterData) m_node).getData());
            } else if (offset == 0) {
                assertEquals(sData.substring(count, sData.length() - count) + newStr, ((CharacterData) m_node).getData());
            } else {
                assertEquals(sData.substring(0, offset) + newStr + sData.substring(offset + count), ((CharacterData) m_node).getData());
            }
        }

    }

    @Test
    void testSetDataNull() {
        ((CharacterData) m_node).setData(null);
        assertEquals("", ((CharacterData) m_node).getData());
    }

    @Test
    void testSubstringDataNegOff() {
        _testSubstringData(-1, 10);
    }

    @Test
    void testSubstringDataNegLen() {
        _testSubstringData(1, -10);
    }

    @Test
    void testSubstringDataLargeOff() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testSubstringData(nDataLen + 1, 10);
    }

    @Test
    void testSubstringDataLargeLen() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testSubstringData(0, nDataLen + 30);
    }

    @Test
    void testSubstringDataAverage() {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        _testSubstringData(0, nDataLen / 2);
    }

    private void _testSubstringData(int offset, int count) {
        String sData = ((CharacterData) m_node).getData();
        int nDataLen = sData.length();
        String result;
        if (offset < 0 || offset > nDataLen || count < 0) {
            DOMException de = assertThrows(DOMException.class, () -> ((CharacterData) m_node).substringData(offset, count), "Deleting OOB chars");
            assertEquals(de.code, DOMException.INDEX_SIZE_ERR);
        } else {
            result = ((CharacterData) m_node).substringData(offset, count);
            if (count == 0) {
                assertEquals("", result);
            } else if (offset + count == nDataLen || (offset + count) > nDataLen) {
                assertEquals(sData, result);
            } else {
                assertEquals(sData.substring(offset, count), result);
            }
        }
    }

    @Test
    protected void testSetPrefix() {
        DOMException de = assertThrows(DOMException.class, () -> m_node.setPrefix("foobar"),
            "Can't set prefix on node other than Element or Attribute");
        assertEquals(DOMException.NAMESPACE_ERR, de.code);
    }
}





