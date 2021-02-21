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

package org.apache.xmlbeans.impl.values;

import java.util.AbstractList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JavaListObject<T> extends AbstractList<T> {
    private final Function<Integer,T> getter;
    private final BiConsumer<Integer,T> setter;
    private final BiConsumer<Integer,T> adder;
    private final Consumer<Integer> remover;
    private final Supplier<Integer> sizer;

    public JavaListObject(
        Function<Integer,T> getter,
        BiConsumer<Integer,T> setter,
        BiConsumer<Integer,T> adder,
        Consumer<Integer> remover,
        Supplier<Integer> sizer
    ) {
        this.getter = getter;
        this.setter = setter;
        this.adder = adder;
        this.remover = remover;
        this.sizer = sizer;
    }


    @Override
    public T get(int index) {
        if (getter == null) {
            throw new IllegalStateException("XmlBean generated using partial methods - no getter method available");
        }
        return getter.apply(index);
    }

    @Override
    public T set(int index, T element) {
        if (setter == null) {
            throw new IllegalStateException("XmlBean generated using partial methods - no setter method available");
        }
        T old = get(index);
        setter.accept(index, element);
        return old;
    }

    @Override
    public void add(int index, T t) {
        if (adder == null) {
            throw new IllegalStateException("XmlBean generated using partial methods - no add method available");
        }
        adder.accept(index, t);
    }

    @Override
    public T remove(int index) {
        if (remover == null) {
            throw new IllegalStateException("XmlBean generated using partial methods - no remove method available");
        }
        T old = get(index);
        remover.accept(index);
        return old;
    }

    @Override
    public int size() {
        if (sizer == null) {
            throw new IllegalStateException("XmlBean generated using partial methods - no size-of method available");
        }
        return sizer.get();
    }
}
