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

package org.apache.xmlbeans.impl.marshal.util.collections;

/**
 minimal, simplisitic typesafe version of ArrayList for longs.
 wraps an long[]

 not syncronized.
 */


public final class LongList
    implements Accumulator
{
    private long[] store;
    private int size = 0;


    public LongList()
    {
        this(Accumulator.DEFAULT_INITIAL_CAPACITY);
    }

    public LongList(int initial_capacity)
    {
        store = new long[initial_capacity];
    }

    public long[] getMinSizedArray()
    {
        if (size == store.length) {
            return store;
        }

        long[] new_a = new long[size];
        System.arraycopy(store, 0, new_a, 0, size);
        store = new_a;
        return new_a;
    }

    public Object getFinalArray()
    {
        return getMinSizedArray();
    }

    public int getCapacity()
    {
        return store.length;
    }

    public int getSize()
    {
        return size;
    }

    public void append(Object o)
    {
        assert (o instanceof Number);
        add(((Number)o).longValue());
    }

    public void appendDefault()
    {
        add(0L);
    }

    public void set(int index, Object value)
    {
        set(index, ((Number)value).longValue());
    }


    public void set(int index, long value)
    {
        ensureCapacity(index + 1);
        if (index >= size) {
            size = index + 1;
        }
        store[index] = value;
    }


    /**
     * Appends the specified element to the end of this list.
     *
     * @param i element to be appended to this list.
     */
    public void add(long i)
    {
        ensureCapacity(size + 1);
        store[size++] = i;
    }

    public long get(int idx)
    {
        //let array do range checking.
        return store[idx];
    }


    /**
     * Increases the capacity of this <tt>LongList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity)
    {
        int oldCapacity = store.length;
        if (minCapacity > oldCapacity) {
            long oldData[] = store;
            int newCapacity = (oldCapacity * 2) + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            store = new long[newCapacity];
            System.arraycopy(oldData, 0, store, 0, size);
        }
    }


}

