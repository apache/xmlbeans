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


import java.util.Collection;
import java.util.LinkedList;


public final class LinkedListBasedObjectAccumulator
    extends ObjectAccumulator
{
    public LinkedListBasedObjectAccumulator(Class component_type,
                                            int initial_capacity,
                                            boolean return_collection)
    {
        super(component_type, initial_capacity, return_collection);
    }


    public LinkedListBasedObjectAccumulator(Class component_type,
                                            int initial_capacity)
    {
        super(component_type, initial_capacity);
    }

    public LinkedListBasedObjectAccumulator(Class component_type)
    {
        super(component_type);
    }

    protected Collection createNewStore(int capacity)
    {
        return new java.util.LinkedList();
    }

    public LinkedList getLinkedListStore()
    {
        assert (store instanceof LinkedList);
        return (LinkedList)store;
    }

}

