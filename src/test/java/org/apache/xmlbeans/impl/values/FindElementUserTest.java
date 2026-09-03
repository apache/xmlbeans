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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Exercises TypeStore.find_element_user directly - it is what every generated
 * getXxxArray(i) accessor resolves an index through.
 */
class FindElementUserTest {
    private static final String NS = "urn:find-element-user";

    private static final QName A = new QName(NS, "a");
    private static final QName B = new QName(NS, "b");
    private static final QName C = new QName(NS, "c");
    private static final QName Z = new QName(NS, "z");
    private static final QName NO_NS_A = new QName("", "a");

    private static final QNameSet AC = QNameSet.forArray(new QName[]{A, C});
    private static final QNameSet ABC = QNameSet.forArray(new QName[]{A, B, C});
    private static final QNameSet JUST_B = QNameSet.forArray(new QName[]{B});

    /**
     * Elements in document order: a0 b0 a1 c0 a2 b1 a3. The attribute, the comment,
     * the processing instruction and the loose text are all there to be skipped over.
     */
    private static final String MIXED =
        "<root xmlns='" + NS + "' xmlns:t='" + NS + "' t:a='attribute'>" +
        "loose text" +
        "<a>a0</a>" +
        "<!-- comment -->" +
        "<b>b0</b>" +
        "<a>a1</a>" +
        "<?pi data?>" +
        "<c>c0</c>" +
        "more loose text" +
        "<a>a2</a>" +
        "<b>b1</b>" +
        "<a>a3</a>" +
        "</root>";

    private static TypeStore store(String xml) throws XmlException {
        return store(XmlObject.Factory.parse(xml), "root");
    }

    private static TypeStore store(XmlObject doc, String element) {
        XmlObject[] found = doc.selectChildren(new QName(NS, element));
        assertEquals(1, found.length);
        return ((TypeStoreUser) found[0]).get_store();
    }

    private static String text(TypeStoreUser user) {
        if (user == null) {
            return null;
        }

        try (XmlCursor cursor = ((XmlObject) user).newCursor()) {
            return cursor.getTextValue();
        }
    }

    private static String repeated(String name, int count) {
        StringBuilder xml = new StringBuilder("<root xmlns='" + NS + "'>");
        for (int i = 0; i < count; i++) {
            xml.append('<').append(name).append('>').append(name).append(i).append("</").append(name).append('>');
        }
        return xml.append("</root>").toString();
    }

    private static int[] shuffled(int n) {
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }

        Random random = new Random(1234);
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int t = order[i];
            order[i] = order[j];
            order[j] = t;
        }

        return order;
    }

    // ---- find_element_user(QName, int) ----

    @Test
    void byNameFindsTheNthElementOfThatName() throws Exception {
        TypeStore store = store(MIXED);

        assertEquals("a0", text(store.find_element_user(A, 0)));
        assertEquals("a1", text(store.find_element_user(A, 1)));
        assertEquals("a2", text(store.find_element_user(A, 2)));
        assertEquals("a3", text(store.find_element_user(A, 3)));

        assertEquals("b0", text(store.find_element_user(B, 0)));
        assertEquals("b1", text(store.find_element_user(B, 1)));

        assertEquals("c0", text(store.find_element_user(C, 0)));
    }

    @Test
    void byNameIgnoresAttributesCommentsProcessingInstructionsAndText() throws Exception {
        TypeStore store = store(MIXED);

        // the root carries an attribute whose name is the same QName as the "a" elements
        assertEquals("a0", text(store.find_element_user(A, 0)));
        assertNull(store.find_element_user(A, 4));
    }

    @Test
    void byNameReturnsNullPastTheLastMatch() throws Exception {
        TypeStore store = store(MIXED);

        assertNull(store.find_element_user(A, 4));
        assertNull(store.find_element_user(B, 2));
        assertNull(store.find_element_user(C, 1));
        assertNull(store.find_element_user(A, 4000));
    }

    @Test
    void byNameReturnsNullForANameThatIsNotThere() throws Exception {
        TypeStore store = store(MIXED);

        assertNull(store.find_element_user(Z, 0));
        assertNull(store.find_element_user(NO_NS_A, 0));
    }

    @Test
    void byNameANegativeIndexResolvesToTheFirstMatch() throws Exception {
        // not obviously the right answer, but it is what this method has always done
        TypeStore store = store(MIXED);

        assertEquals("a0", text(store.find_element_user(A, -1)));
        assertEquals("a0", text(store.find_element_user(A, -100)));
        assertEquals("b0", text(store.find_element_user(B, -1)));
        assertNull(store.find_element_user(Z, -1));
    }

    @Test
    void byNameReturnsTheSameUserEveryTime() throws Exception {
        TypeStore store = store(MIXED);

        TypeStoreUser first = store.find_element_user(A, 2);
        assertNotNull(first);
        assertSame(first, store.find_element_user(A, 2));
        assertSame(first, store.find_element_user(A, 2));

        // and after a lookup that moves the cursor elsewhere
        store.find_element_user(A, 0);
        store.find_element_user(B, 1);
        assertSame(first, store.find_element_user(A, 2));
    }

    @Test
    void byNameFindsTheSameElementWhateverOrderTheIndexesComeIn() throws Exception {
        int n = 200;
        TypeStore store = store(repeated("a", n));

        for (int i = 0; i < n; i++) {
            assertEquals("a" + i, text(store.find_element_user(A, i)));
        }

        for (int i = n - 1; i >= 0; i--) {
            assertEquals("a" + i, text(store.find_element_user(A, i)));
        }

        for (int i : shuffled(n)) {
            assertEquals("a" + i, text(store.find_element_user(A, i)));
        }

        for (int i = 0; i < n / 2; i++) {
            assertEquals("a" + i, text(store.find_element_user(A, i)));
            assertEquals("a" + (n - 1 - i), text(store.find_element_user(A, n - 1 - i)));
        }
    }

    @Test
    void byNameOnAnElementWithNoElementChildren() throws Exception {
        XmlObject doc = XmlObject.Factory.parse("<root xmlns='" + NS + "'>just text</root>");
        TypeStore store = store(doc, "root");

        assertNull(store.find_element_user(A, 0));
        assertNull(store.find_element_user(A, 1));
        assertNull(store.find_element_user(A, -1));
    }

    // ---- find_element_user(QNameSet, int) ----

    @Test
    void bySetFindsTheNthElementMatchingTheSet() throws Exception {
        TypeStore store = store(MIXED);

        // a0 a1 c0 a2 a3 in document order
        assertEquals("a0", text(store.find_element_user(AC, 0)));
        assertEquals("a1", text(store.find_element_user(AC, 1)));
        assertEquals("c0", text(store.find_element_user(AC, 2)));
        assertEquals("a2", text(store.find_element_user(AC, 3)));
        assertEquals("a3", text(store.find_element_user(AC, 4)));
        assertNull(store.find_element_user(AC, 5));

        // every element child
        String[] all = {"a0", "b0", "a1", "c0", "a2", "b1", "a3"};
        for (int i = 0; i < all.length; i++) {
            assertEquals(all[i], text(store.find_element_user(ABC, i)));
        }
        assertNull(store.find_element_user(ABC, all.length));
    }

    @Test
    void bySetANegativeIndexResolvesToTheFirstMatch() throws Exception {
        TypeStore store = store(MIXED);

        assertEquals("a0", text(store.find_element_user(AC, -1)));
        assertEquals("b0", text(store.find_element_user(JUST_B, -3)));
    }

    @Test
    void bySetReturnsNullWhenNothingMatches() throws Exception {
        TypeStore store = store(MIXED);

        assertNull(store.find_element_user(QNameSet.forArray(new QName[]{Z}), 0));
        assertNull(store.find_element_user(QNameSet.EMPTY, 0));
    }

    @Test
    void bySetFindsTheSameElementWhateverOrderTheIndexesComeIn() throws Exception {
        int n = 200;
        TypeStore store = store(repeated("a", n));

        for (int i = 0; i < n; i++) {
            assertEquals("a" + i, text(store.find_element_user(AC, i)));
        }

        for (int i = n - 1; i >= 0; i--) {
            assertEquals("a" + i, text(store.find_element_user(AC, i)));
        }

        for (int i : shuffled(n)) {
            assertEquals("a" + i, text(store.find_element_user(AC, i)));
        }
    }

    // ---- the two of them together ----

    @Test
    void lookupsByNameAndBySetDoNotShareACachedPosition() throws Exception {
        TypeStore store = store(MIXED);

        String[] bySet = {"a0", "b0", "a1", "c0", "a2", "b1", "a3"};
        String[] byName = {"a0", "a1", "a2", "a3"};

        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < bySet.length; i++) {
                assertEquals(bySet[i], text(store.find_element_user(ABC, i)));
                assertEquals(byName[i % byName.length], text(store.find_element_user(A, i % byName.length)));
                assertEquals("b" + (i % 2), text(store.find_element_user(B, i % 2)));
                assertEquals("b" + (i % 2), text(store.find_element_user(JUST_B, i % 2)));
            }
        }
    }

    @Test
    void twoDifferentSetsOverTheSameParentDoNotShareACachedPosition() throws Exception {
        TypeStore store = store(MIXED);

        for (int round = 0; round < 3; round++) {
            assertEquals("c0", text(store.find_element_user(AC, 2)));
            assertEquals("a1", text(store.find_element_user(ABC, 2)));
            assertEquals("b1", text(store.find_element_user(JUST_B, 1)));
            assertEquals("a3", text(store.find_element_user(AC, 4)));
            assertEquals("b0", text(store.find_element_user(ABC, 1)));
        }
    }

    @Test
    void lookupsAgainstTwoParentsDoNotDisturbEachOther() throws Exception {
        String xml =
            "<outer xmlns='" + NS + "'>" +
            "<root><a>left0</a><a>left1</a><a>left2</a></root>" +
            "<root><a>right0</a><a>right1</a><a>right2</a></root>" +
            "</outer>";

        XmlObject outer = XmlObject.Factory.parse(xml).selectChildren(new QName(NS, "outer"))[0];
        XmlObject[] roots = outer.selectChildren(new QName(NS, "root"));
        assertEquals(2, roots.length);

        TypeStore left = ((TypeStoreUser) roots[0]).get_store();
        TypeStore right = ((TypeStoreUser) roots[1]).get_store();

        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < 3; i++) {
                assertEquals("left" + i, text(left.find_element_user(A, i)));
                assertEquals("right" + i, text(right.find_element_user(A, i)));
                assertEquals("left" + (2 - i), text(left.find_element_user(AC, 2 - i)));
                assertEquals("right" + (2 - i), text(right.find_element_user(AC, 2 - i)));
            }
        }
    }

    // ---- and after the document changes ----

    @Test
    void followsElementsBeingRemoved() throws Exception {
        TypeStore store = store(repeated("a", 6));

        assertEquals("a3", text(store.find_element_user(A, 3)));

        store.remove_element(A, 0);
        assertEquals("a1", text(store.find_element_user(A, 0)));
        assertEquals("a4", text(store.find_element_user(A, 3)));

        store.remove_element(A, 4);
        assertNull(store.find_element_user(A, 4));
        assertEquals("a4", text(store.find_element_user(AC, 3)));
    }

    @Test
    void followsElementsBeingInserted() throws Exception {
        TypeStore store = store(repeated("a", 4));

        assertEquals("a2", text(store.find_element_user(A, 2)));

        TypeStoreUser inserted = store.insert_element_user(A, 2);
        assertNotNull(inserted);
        assertSame(inserted, store.find_element_user(A, 2));
        assertEquals("a2", text(store.find_element_user(A, 3)));
        assertEquals("a3", text(store.find_element_user(A, 4)));
        assertNull(store.find_element_user(A, 5));

        // and the set based lookup sees it in the same place
        assertSame(inserted, store.find_element_user(AC, 2));
        assertEquals("a3", text(store.find_element_user(AC, 4)));
    }

    @Test
    void followsElementsBeingRenamedAroundIt() throws Exception {
        TypeStore store = store(repeated("a", 4));

        assertEquals("a1", text(store.find_element_user(A, 1)));

        try (XmlCursor cursor = ((XmlObject) store.find_element_user(A, 1)).newCursor()) {
            cursor.setName(B);
        }

        assertEquals("a2", text(store.find_element_user(A, 1)));
        assertEquals("a1", text(store.find_element_user(B, 0)));
        assertEquals("a1", text(store.find_element_user(ABC, 1)));
        assertNull(store.find_element_user(A, 3));
    }
}
