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


public final class AccumulatorFactory
{

    public static Accumulator createAccumulator(Class component_type)
    {
        return createAccumulator(component_type,
                                 Accumulator.DEFAULT_INITIAL_CAPACITY);
    }


    public static Accumulator createAccumulator(Class component_type,
                                                int initial_capacity)
    {
        if (component_type.isPrimitive()) {
            return createPrimtiveAccumulator(component_type, initial_capacity);
        } else if (String.class.equals(component_type)) {
            return new StringList(initial_capacity);
        } else {
            return new ArrayListBasedObjectAccumulator(component_type,
                                                       initial_capacity,
                                                       false);
        }
    }

    private static Accumulator createPrimtiveAccumulator(Class component_type,
                                                         int initial_capacity)
    {
        if (int.class.equals(component_type)) {
            return new IntList(initial_capacity);
        } else if (short.class.equals(component_type)) {
            return new ShortList(initial_capacity);
        } else if (long.class.equals(component_type)) {
            return new LongList(initial_capacity);
        } else if (float.class.equals(component_type)) {
            return new FloatList(initial_capacity);
        } else if (double.class.equals(component_type)) {
            return new DoubleList(initial_capacity);
        } else if (byte.class.equals(component_type)) {
            return new ByteList(initial_capacity);
        } else if (boolean.class.equals(component_type)) {
            return new BooleanList(initial_capacity);
        } else if (char.class.equals(component_type)) {
            return new CharList(initial_capacity);
        } else {
            throw new AssertionError("unknown primitive type: " + component_type);
        }
    }

    public static Accumulator createAccumulator(Class container_type,
                                                Class component_type,
                                                int initial_capacity)
    {
        if (container_type.isArray()) {
            assert (container_type.getComponentType().isAssignableFrom(component_type));
            return createAccumulator(component_type, initial_capacity);
        } else if (java.util.Collection.class.isAssignableFrom(container_type)) {
            return createCollectionAccumulator(container_type,
                                               component_type,
                                               initial_capacity);
        } else {
            throw new AssertionError("unsupported container type: " + container_type);
        }
    }

    public static Accumulator createAccumulator(Class container_type,
                                                Class component_type)
    {
        return createAccumulator(container_type, component_type,
                                 Accumulator.DEFAULT_INITIAL_CAPACITY);
    }

    private static Accumulator createCollectionAccumulator(Class container_type,
                                                           Class component_type,
                                                           int initial_capacity)
    {
        assert !container_type.isArray();


        if (java.util.Collection.class.equals(container_type) ||
            java.util.ArrayList.class.equals(container_type) ||
            java.util.List.class.equals(container_type)) {
            return new ArrayListBasedObjectAccumulator(component_type,
                                                       initial_capacity);
        }


        if (java.util.Set.class.equals(container_type) ||
            java.util.HashSet.class.equals(container_type)) {
            return new HashSetBasedObjectAccumulator(component_type,
                                                     initial_capacity);
        }


        if (java.util.SortedSet.class.equals(container_type) ||
            java.util.TreeSet.class.equals(container_type)) {
            return new TreeSetBasedObjectAccumulator(component_type,
                                                     initial_capacity);
        }


        if (java.util.Vector.class.equals(container_type)) {
            return new VectorBasedObjectAccumulator(component_type,
                                                    initial_capacity);
        }

        if (java.util.Stack.class.equals(container_type)) {
            return new StackBasedObjectAccumulator(component_type,
                                                   initial_capacity);
        }

        if (java.util.LinkedList.class.equals(container_type)) {
            return new LinkedListBasedObjectAccumulator(component_type,
                                                        initial_capacity);
        }

        return new GenericCollectionObjectAccumulator(container_type,
                                                      component_type);
    }


}

