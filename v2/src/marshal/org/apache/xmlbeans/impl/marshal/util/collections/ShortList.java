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
 minimal, simplisitic typesafe version of ArrayList for shorts.
 wraps an short[]

 not syncronized.
 */


public final class ShortList
    implements Accumulator
{
    private short[] store;
    private int size = 0;


    public ShortList()
    {
        this(Accumulator.DEFAULT_INITIAL_CAPACITY);
    }

    public ShortList(int initial_capacity)
    {
        store = new short[initial_capacity];
    }

    public Object getFinalArray()
    {
        return getMinSizedArray();
    }

    /**
     get a new array just large enough to hold the items
     */
    public short[] getMinSizedArray()
    {
        short[] new_a = new short[size];
        System.arraycopy(store, 0, new_a, 0, size);
        return new_a;
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
        add(((Number)o).shortValue());
    }

    public void appendDefault()
    {
        add((short)0);
    }


    public void set(int index, Object value)
    {
        set(index, ((Number)value).shortValue());
    }

    public int size()
    {
        return size;
    }


    public void set(int index, short value)
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
    public void add(short i)
    {
        ensureCapacity(size + 1);
        store[size++] = i;
    }

    public short get(int idx)
    {
        //let array do range checking.
        return store[idx];
    }


    /**
     * Increases the capacity of this <tt>ShortList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity)
    {
        int oldCapacity = store.length;
        if (minCapacity > oldCapacity) {
            short oldData[] = store;
            int newCapacity = (oldCapacity * 2) + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            store = new short[newCapacity];
            System.arraycopy(oldData, 0, store, 0, size);
        }
    }


}

