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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the iterators handed out by the lists that the generated getXxxList()
 * accessors return. The list itself is backed by an ArrayList here so that the
 * calls it makes can be counted - against the XML store, size() is a walk over
 * all of the children of the parent element.
 */
class JavaListIteratorTest {
    private final List<String> backing = new ArrayList<>();
    private int sizeCalls;

    private JavaListObject<String> list() {
        return new JavaListObject<>(
            backing::get,
            backing::set,
            backing::add,
            i -> backing.remove((int) i),
            () -> {
                sizeCalls++;
                return backing.size();
            }
        );
    }

    private void fill(String... values) {
        backing.addAll(Arrays.asList(values));
    }

    @Test
    void aPassOverTheListAsksForTheSizeAConstantNumberOfTimes() {
        for (int i = 0; i < 100; i++) {
            backing.add("v" + i);
        }

        List<String> list = list();
        sizeCalls = 0;

        int seen = 0;
        for (String value : list) {
            assertEquals("v" + seen++, value);
        }

        assertEquals(100, seen);
        // once to find the end of the list, once to confirm it - not one per element
        assertEquals(2, sizeCalls);
    }

    @Test
    void iterationSeesElementsAppendedWhileItIsRunning() {
        fill("a", "b", "c");

        List<String> seen = new ArrayList<>();
        for (String value : list()) {
            seen.add(value);
            if (seen.size() == 1) {
                backing.add("d");
            }
        }

        assertEquals(Arrays.asList("a", "b", "c", "d"), seen);
    }

    @Test
    void iteratorRemoveDropsTheElementAndCarriesOn() {
        fill("a", "b", "c", "d");

        List<String> seen = new ArrayList<>();
        for (Iterator<String> it = list().iterator(); it.hasNext(); ) {
            String value = it.next();
            seen.add(value);
            if ("b".equals(value) || "d".equals(value)) {
                it.remove();
            }
        }

        assertEquals(Arrays.asList("a", "b", "c", "d"), seen);
        assertEquals(Arrays.asList("a", "c"), backing);
    }

    @Test
    void nextThrowsOnceTheListIsExhausted() {
        fill("a");

        Iterator<String> it = list().iterator();
        assertEquals("a", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void listIteratorWalksInBothDirections() {
        fill("a", "b", "c");

        ListIterator<String> it = list().listIterator();
        assertEquals(-1, it.previousIndex());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals(2, it.nextIndex());
        assertEquals("b", it.previous());
        assertEquals("a", it.previous());
        assertFalse(it.hasPrevious());
        assertThrows(NoSuchElementException.class, it::previous);
    }

    @Test
    void listIteratorSetsAndAdds() {
        fill("a", "b");

        ListIterator<String> it = list().listIterator();
        it.next();
        it.set("z");
        it.add("y");

        assertEquals(Arrays.asList("z", "y", "b"), backing);
        assertEquals(2, it.nextIndex());
        assertEquals("b", it.next());
    }

    @Test
    void listIteratorRejectsAnIndexOutsideTheList() {
        fill("a", "b");

        List<String> list = list();
        assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(3));
        assertEquals(2, list.listIterator(2).nextIndex());
    }

    @Test
    void theListStillCompares() {
        fill("a", "b", "c");

        List<String> list = list();
        assertEquals(1, list.indexOf("b"));
        assertEquals(2, list.lastIndexOf("c"));
        assertTrue(list.contains("c"));
        assertEquals(Arrays.asList("a", "b", "c"), list);
        assertEquals(Arrays.asList("a", "b", "c").hashCode(), list.hashCode());
    }
}
