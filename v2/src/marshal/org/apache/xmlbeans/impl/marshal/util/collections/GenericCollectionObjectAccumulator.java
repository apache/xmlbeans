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


public final class GenericCollectionObjectAccumulator
    implements Accumulator
{
    private final Class componentType;
    private final Collection container;

    public GenericCollectionObjectAccumulator(Class container_type,
                                              Class component_type)
    {
        assert Collection.class.isAssignableFrom(container_type);
        componentType = component_type;
        try {
            container = (Collection)container_type.newInstance();
        }
        catch (InstantiationException e) {
            throw (IllegalArgumentException)(new IllegalArgumentException()).initCause(e);
        }
        catch (IllegalAccessException e) {
            throw (IllegalArgumentException)(new IllegalArgumentException()).initCause(e);
        }
    }


    public void append(Object elem)
    {
        container.add(elem);
    }

    public void appendDefault()
    {
        append(null);
    }

    public void set(int index, Object value)
    {
        throw new UnsupportedOperationException("no indexed access");
    }

    public Object getFinalArray()
    {
        return container;
    }

}

