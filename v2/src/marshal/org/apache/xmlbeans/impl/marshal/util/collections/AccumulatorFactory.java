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
                                                       initial_capacity);
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

        final boolean return_collection = !container_type.isArray();


        if (java.util.Collection.class.equals(container_type) ||
            java.util.ArrayList.class.equals(container_type) ||
            java.util.List.class.equals(container_type)) {
            return new ArrayListBasedObjectAccumulator(component_type,
                                                       initial_capacity,
                                                       return_collection);
        }


        if (java.util.Set.class.equals(container_type) ||
            java.util.HashSet.class.equals(container_type)) {
            return new HashSetBasedObjectAccumulator(component_type,
                                                     initial_capacity,
                                                     return_collection);
        }


        if (java.util.SortedSet.class.equals(container_type) ||
            java.util.TreeSet.class.equals(container_type)) {
            return new TreeSetBasedObjectAccumulator(component_type,
                                                     initial_capacity,
                                                     return_collection);
        }


        if (java.util.Vector.class.equals(container_type)) {
            return new VectorBasedObjectAccumulator(component_type,
                                                    initial_capacity,
                                                    return_collection);
        }

        if (java.util.Stack.class.equals(container_type)) {
            return new StackBasedObjectAccumulator(component_type,
                                                   initial_capacity,
                                                   return_collection);
        }

        if (java.util.LinkedList.class.equals(container_type)) {
            return new LinkedListBasedObjectAccumulator(component_type,
                                                        initial_capacity,
                                                        return_collection);
        }


        return new GenericCollectionObjectAccumulator(container_type,
                                                      component_type,
                                                      initial_capacity,
                                                      return_collection);


    }


}

