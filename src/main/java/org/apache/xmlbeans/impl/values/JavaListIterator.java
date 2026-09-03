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

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Iterator over one of the live lists handed out by the generated
 * <code>getXxxList()</code> accessors.
 * <p>
 * The iterators {@link java.util.AbstractList} supplies ask the list for its
 * {@link List#size()} on every call to {@link #hasNext()}. For a list backed by the
 * XML store that size is a walk over all of the children of the parent element, so a
 * single pass over the list ends up doing O(n^2) work on top of the cost of reading
 * the elements themselves. This iterator asks for the size only when it thinks it has
 * reached the end, so a pass costs one size() call rather than one per element.
 * <p>
 * The list stays live: elements appended while the iteration is in progress are still
 * picked up, because reaching the end of the run re-reads the size before giving up.
 */
class JavaListIterator<T> implements ListIterator<T> {
    private final List<T> list;

    /** index of the element that a call to {@link #next()} would return */
    private int cursor;

    /** index of the element returned by the last next()/previous(), -1 if there is none */
    private int lastRet = -1;

    /** the last size read back from the list, -1 if it needs to be read again */
    private int knownSize = -1;

    JavaListIterator(List<T> list, int index) {
        this.list = list;
        this.cursor = index;
    }

    @Override
    public boolean hasNext() {
        if (knownSize < 0 || cursor >= knownSize) {
            knownSize = list.size();
        }

        return cursor < knownSize;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            T value = list.get(cursor);
            lastRet = cursor++;
            return value;
        } catch (IndexOutOfBoundsException e) {
            // the list shrank underneath us since the size was last read
            knownSize = -1;
            throw new NoSuchElementException();
        }
    }

    @Override
    public boolean hasPrevious() {
        return cursor > 0;
    }

    @Override
    public T previous() {
        if (cursor <= 0) {
            throw new NoSuchElementException();
        }

        try {
            T value = list.get(cursor - 1);
            lastRet = --cursor;
            return value;
        } catch (IndexOutOfBoundsException e) {
            knownSize = -1;
            throw new NoSuchElementException();
        }
    }

    @Override
    public int nextIndex() {
        return cursor;
    }

    @Override
    public int previousIndex() {
        return cursor - 1;
    }

    @Override
    public void remove() {
        if (lastRet < 0) {
            throw new IllegalStateException();
        }

        list.remove(lastRet);

        if (lastRet < cursor) {
            cursor--;
        }

        lastRet = -1;
        knownSize = -1;
    }

    @Override
    public void set(T t) {
        if (lastRet < 0) {
            throw new IllegalStateException();
        }

        list.set(lastRet, t);
    }

    @Override
    public void add(T t) {
        list.add(cursor++, t);
        lastRet = -1;
        knownSize = -1;
    }
}
