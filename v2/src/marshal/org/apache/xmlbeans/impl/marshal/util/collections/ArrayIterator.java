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

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class ArrayIterator
    implements Iterator
{
    private final Object[] array;
    private final int maxIndex;
    private int index;

    public ArrayIterator(Object[] a)
    {
        this(a, 0, a.length);
    }

    public ArrayIterator(Object[] a, int off, int len)
    {
        if (off < 0) throw new IllegalArgumentException();
        if (off > a.length) throw new IllegalArgumentException();
        if (len > a.length - off) throw new IllegalArgumentException();
        array = a;
        index = off;
        maxIndex = len + off;
    }

    public boolean hasNext()
    {
        return index < maxIndex;
    }

    public Object next()
    {
        if (index >= maxIndex) throw new NoSuchElementException();
        return array[index++];
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}

