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

package org.apache.xmlbeans.impl.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.Set;

/**
 * Behaves the same way as JDK1.4 java.util.LinkedHashSet, but in JDK1.3 as well.
 * Backed by SequencedHashMap to preserve iteration order.
 */
public class SequencedHashSet extends AbstractSet implements Set,
    Cloneable /*, java.io.Serializable */
{
    private transient SequencedHashMap map;

    // Dummy value
    private static final Object EXISTS = new Object();

    /**
     * Constructs a new, empty sequenced set.
     */
    public SequencedHashSet()
    {
        map = new SequencedHashMap();
    }

    /**
     * Constructs a new sequenced set containing the elements in the specified
     * collection.
     */
    public SequencedHashSet(Collection c)
    {
        map = new SequencedHashMap(c.size());
        addAll(c);
    }

    /**
     * Constructs a new, empty sequenced set with
     * the specified initial capacity and the specified load factor.
     *
     * @param      initialSize       the initial size of the hash map.
     * @param      loadFactor        the load factor of the hash map.
     */
    public SequencedHashSet(int initialSize, float loadFactor)
    {
        map = new SequencedHashMap(initialSize, loadFactor);
    }

    /**
     * Constructs a new, empty sequenced set with the specified initial size.
     *
     * @param      initialSize   the initial size of the hash table.
     */
    public SequencedHashSet(int initialSize)
    {
        map = new SequencedHashMap(initialSize);
    }

    /**
     * Returns an iterator over the elements in this set.
     */
    public Iterator iterator()
    {
        return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     */
    public int size()
    {
        return map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains(Object o)
    {
        return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     */
    public boolean add(Object o)
    {
        return map.put(o, EXISTS)==null;
    }

    /**
     * Removes the given element from this set if it is present.
     */
    public boolean remove(Object o)
    {
        return map.remove(o)==EXISTS;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear()
    {
        map.clear();
    }

    /**
     * Returns a shallow copy of this set.
     *
     * @return a shallow copy of this set.
     */
    public Object clone() throws CloneNotSupportedException
    {
        SequencedHashSet set = (SequencedHashSet)super.clone();
        set.map = (SequencedHashMap)map.clone();
        return set;
    }

    /*
     * Save the state of this <tt>SequencedHashSet</tt> instance to a stream.
     *
     * @serialData The size of the backing <tt>SequencedHashMap</tt> instance
     *             (int), and its load factor (float) are emitted, followed by
     *             the size of the set (the number of elements it contains)
     *             (int), followed by all of its elements (each an Object) in
     *             no particular order.
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException
    {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(map.size());

        // Write out all elements in the proper order.
        for (Iterator i=map.keySet().iterator(); i.hasNext(); )
            s.writeObject(i.next());
    }
     */

    /*
     * Reconstitute the <tt>HashSet</tt> instance from a stream (that is,
     * deserialize it).
    private synchronized void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in HashMap capacity and load factor and create backing HashMap
        //int capacity = s.readInt();
        //float loadFactor = s.readFloat();
        //map = new SequencedHashMap(capacity, loadFactor);

        // Read in size
        int size = s.readInt();
        map = new SequencedHashMap(size);

        // Read in all elements in the proper order.
        for (int i=0; i < size; i++)
        {
            Object e = s.readObject();
            map.put(e, EXISTS);
        }
        throw new RuntimeException("readObject in SequencedHashSet");
    }
     */
}
