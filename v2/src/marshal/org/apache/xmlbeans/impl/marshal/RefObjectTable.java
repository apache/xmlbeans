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

import java.util.HashMap;
import java.util.Map;

final class RefObjectTable
{
    private final Map refTable = new HashMap();

    Object getObjectForRef(String ref)
    {
        assert ref != null;
        RefEntry e = getEntryForRef(ref);
        if (e == null) return null;
        return e.final_obj;
    }

    Object getInterForRef(String ref)
    {
        assert ref != null;

        RefEntry e = getEntryForRef(ref);
        if (e == null) return null;
        return e.inter;
    }

    RefEntry getEntryForRef(String ref)
    {
        assert ref != null;
        return (RefEntry)refTable.get(ref);
    }

    void putForRef(String ref, Object inter, Object actual_obj)
    {
        assert ref != null;
        refTable.put(ref, new RefEntry(inter, actual_obj));
    }

    void putObjectForRef(String ref, Object val)
    {
        assert ref != null;
        RefEntry e = (RefEntry)refTable.get(ref);
        assert e != null;
        assert e.inter != null;
        e.final_obj = val;
    }

    void putIntermediateForRef(String ref, Object inter)
    {
        assert ref != null;
        refTable.put(ref, new RefEntry(inter));
    }

    final static class RefEntry
    {
        RefEntry(Object inter, Object final_obj)
        {
            this.inter = inter;
            this.final_obj = final_obj;
        }

        RefEntry(Object inter)
        {
            this.inter = inter;
        }

        Object inter;
        Object final_obj;
    }
}
