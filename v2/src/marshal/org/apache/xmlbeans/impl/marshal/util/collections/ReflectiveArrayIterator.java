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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author   Copyright (c) 2003 by BEA Systems, Inc. All Rights Reserved.
 */
public class ReflectiveArrayIterator
    implements Iterator
{
    private final Object array;
    private final int maxIndex;
    private int index;

    public ReflectiveArrayIterator(Object a)
    {
        this(a, 0, Array.getLength((a)));
    }

    public ReflectiveArrayIterator(Object a, int off, int len)
    {
        if (!a.getClass().isArray()) {
            throw new IllegalArgumentException();
        }
        final int asize = Array.getLength(a);
        if (off < 0) throw new IllegalArgumentException();
        if (off > asize) throw new IllegalArgumentException();
        if (len > asize - off) throw new IllegalArgumentException();

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
        return Array.get(array, index++);
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
