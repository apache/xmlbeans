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

package org.apache.xmlbeans.impl.marshal;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

final class ObjectRefTable
{
    private final IdentityHashMap table = new IdentityHashMap();
    private boolean haveMultiplyRefdObj = false;
    private int idcnt;


    public boolean hasMultiplyRefdObjects()
    {
        return haveMultiplyRefdObj;
    }

    public Iterator getMultipleRefTableEntries()
    {
        return new MultiRefIterator();
    }

    private final class MultiRefIterator
        implements Iterator
    {
        private final Iterator base_itr = table.entrySet().iterator();
        private Value nextValue = null;

        MultiRefIterator()
        {
            updateNext();
        }

        public boolean hasNext()
        {
            return (nextValue != null);
        }

        public Object next()
        {
            final Value retval = this.nextValue;


            assert retval.getCnt() > 1;


            updateNext();

            assert (nextValue == null || nextValue.getCnt() > 1);

            return retval;
        }

        private void updateNext()
        {
            while (base_itr.hasNext()) {
                final Map.Entry map_entry = (Map.Entry)base_itr.next();
                final ObjectRefTable.Value val =
                    (ObjectRefTable.Value)map_entry.getValue();
                if (val.cnt > 1) {
                    nextValue = val;
                    return;
                }
            }
            nextValue = null;
        }


        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

    //returns new count
    public int incrementRefCount(final Object keyobj,
                                 RuntimeBindingProperty property)
    {
        if (keyobj == null) return 0;

        Value val = (Value)table.get(keyobj);
        if (val == null) {
            val = new Value(keyobj, ++idcnt, property);
            table.put(keyobj, val);
        } else {
            assert val.cnt > 0;
            haveMultiplyRefdObj = true;
            //System.out.println("MULTI: " + System.identityHashCode(keyobj) + ": " + keyobj.getClass().getName());
        }

        assert table.get(keyobj) == val;

        return (++val.cnt);
    }

    public int getRefCount(Object obj)
    {
        if (obj == null) return 0;

        Value val = (Value)table.get(obj);
        if (val == null) {
            return 0;
        }
        return val.cnt;
    }


    /**
     * Returns -1 if ref count for obj is <= 1
     *
     * @param obj
     * @return
     */
    public int getId(Object obj)
    {
        int retval = -1;

        if (obj != null) {
            Value val = (Value)table.get(obj);
            if (val != null && val.cnt > 1) {
                retval = val.id;
            }
        }
        return retval;
    }


    public static final class Value
    {
        final Object object;
        final int id;
        final RuntimeBindingProperty prop;
        int cnt;

        public int getId()
        {
            return id;
        }

        public int getCnt()
        {
            return cnt;
        }

        public RuntimeBindingProperty getProp()
        {
            return prop;
        }

        public Value(Object obj, int id, RuntimeBindingProperty prop)
        {
            this.object = obj;
            this.id = id;
            this.prop = prop;
        }

    }
}
