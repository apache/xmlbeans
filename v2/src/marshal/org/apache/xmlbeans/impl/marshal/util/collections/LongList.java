/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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

    /**
     get array used as backing store.  do not modify this array.
     effeciency wins here vs. safety.
     */
    public long[] getStore()
    {
        return store;
    }

    /**
     get a new array just large enough to hold the items
     */
    public long[] getMinSizedArray()
    {
        long[] new_a = new long[size];
        System.arraycopy(store, 0, new_a, 0, size);
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
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            store = new long[newCapacity];
            System.arraycopy(oldData, 0, store, 0, size);
        }
    }


}

