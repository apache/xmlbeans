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
import java.util.Collection;


public abstract class ObjectAccumulator
    implements Accumulator
{
    private final Class componentType;
    private final boolean returnCollectionForArray;
    protected final Collection store;


    public ObjectAccumulator(Class component_type,
                             int initial_capacity,
                             boolean return_collection)
    {
        if (!return_collection) {
            if (component_type.isPrimitive()) {
                throw new AssertionError("odd: " + component_type);
            }
        }

        componentType = component_type;
        returnCollectionForArray = return_collection;

        store = createNewStore(initial_capacity);
        assert store != null;
    }

    public ObjectAccumulator(Class component_type,
                             int initial_capacity)
    {
        this(component_type, initial_capacity, false);
    }

    protected abstract Collection createNewStore(int capacity);

    public ObjectAccumulator(Class component_type)
    {
        this(component_type, Accumulator.DEFAULT_INITIAL_CAPACITY);
    }

    public void append(Object o)
    {
//        if (o != null && !componentType.isAssignableFrom(o.getClass())) {
//            String msg = "Invalid type: " + o.getClass().getName() +
//                " expecting: " + componentType.getName();
//            throw new IllegalArgumentException(msg);
//        }

        store.add(o);
    }

    public final Object getFinalArray()
    {
        if (returnCollectionForArray) {
            return store;
        } else {
            Object[] out = (Object[])Array.newInstance(componentType, store.size());
            return store.toArray(out);
        }
    }

    // use with caution
    public final Collection getStore()
    {
        return store;
    }

}

