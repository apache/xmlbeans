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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * caching factory for runtime binding types
 */

final class RuntimeTypeFactory
{
    //concurrent hashMap allows us to do hash lookups outside of any sync blocks,
    //and successful lookups  involve no locking, which should be
    //99% of the cases in any sort of long running process
    private final Map initedTypeMap = new ConcurrentReaderHashMap();


    private final Map tempTypeMap = new HashMap();

    RuntimeTypeFactory()
    {
    }

    public RuntimeBindingType createRuntimeType(BindingType type,
                                                RuntimeBindingTypeTable type_table,
                                                BindingLoader binding_loader)
        throws XmlException
    {
        RuntimeBindingType rtype = (RuntimeBindingType)initedTypeMap.get(type);
        if (rtype != null) return rtype;

        //safe but slow creation of new type.
        synchronized (this) {
            rtype = (RuntimeBindingType)tempTypeMap.get(type);
            if (rtype == null) {
                rtype = allocateType(type);
                tempTypeMap.put(type, rtype);
                rtype.initialize(type_table, binding_loader);
                initedTypeMap.put(type, rtype);
                tempTypeMap.remove(type); // save some memory.
            }
        }
        assert rtype != null;
        return rtype;
    }

    private static RuntimeBindingType allocateType(BindingType type)
        throws XmlException
    {
        //TODO: fix instanceof nastiness
        if (type instanceof ByNameBean) {
            return new ByNameRuntimeBindingType((ByNameBean)type);
        }

        throw new AssertionError("unknown type: " + type);
    }

}
