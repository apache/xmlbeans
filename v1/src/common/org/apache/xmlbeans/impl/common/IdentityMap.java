/**
 * Copyright (c) 2003-2004, Joe Walnes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce
 * the above copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of XStream nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.apache.xmlbeans.impl.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Behaves the same way as JDK1.4 java.util.IdentityHashMap, but in JDK1.3 as well.
 * <p/>
 * Modified version of ObjectIdDictionary from XStream.
 */
public class IdentityMap implements Map
{
    private transient Map map;

    private static class KeyWrapper
    {
        private final Object obj;

        public KeyWrapper(Object obj)
        {
            this.obj = obj;
        }

        public int hashCode()
        {
            return System.identityHashCode(obj);
        }

        public boolean equals(Object other)
        {
            return obj == ((KeyWrapper)other).obj;
        }

        public String toString()
        {
            return obj.toString();
        }
    }

    public IdentityMap()
    {
        map = new HashMap();
    }

    public int size()
    {
        return map.size();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public boolean containsKey(Object key)
    {
        return map.containsKey(new KeyWrapper(key));
    }

    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    public Object get(Object key)
    {
        return map.get(new KeyWrapper(key));
    }

    public Object put(Object key, Object value)
    {
        return map.put(new KeyWrapper(key), value);
    }

    public Object remove(Object key)
    {
        return map.remove(new KeyWrapper(key));
    }

    public void putAll(Map t)
    {
        throw new UnsupportedOperationException("unimplemented");
    }

    public void clear()
    {
        map.clear();
    }

    public int hashCode()
    {
        return map.hashCode();
    }

    public Set keySet()
    {
        return map.keySet();
    }

    public Collection values()
    {
        return map.values();
    }

    public Set entrySet()
    {
        return map.entrySet();
    }

    public Iterator iterator()
    {
        return map.keySet().iterator();
    }

    public boolean equals(Object o)
    {
        throw new RuntimeException("unimplemented");
    }

}
