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
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingType;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.WrappedArrayType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeVisitor;
import org.apache.xmlbeans.impl.binding.bts.SimpleDocumentBinding;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * caching factory for runtime binding types
 */

final class RuntimeTypeFactory
{
    //concurrent hashMap allows us to do hash lookups outside of any sync blocks,
    //and successful lookups involve no locking, which should be
    //99% of the cases in any sort of long running process
    private final Map initedTypeMap = new ConcurrentReaderHashMap();

    private final Map tempTypeMap = new HashMap();

    //access to this object must be inside a synchronized block.
    private final TypeVisitor typeVisitor = new TypeVisitor();


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
                rtype.initialize(type_table, binding_loader, this);
                initedTypeMap.put(type, rtype);
                tempTypeMap.remove(type); // save some memory.
            }
        }
        assert rtype != null;
        return rtype;
    }


    //overloaded, more strongly typed versions of createRuntimeType.
    //the idea being that this class maintains the matching of the
    //two type hiearchies and all casting is done here.
    public WrappedArrayRuntimeBindingType createRuntimeType(WrappedArrayType type,
                                                            RuntimeBindingTypeTable type_table,
                                                            BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, type_table, binding_loader);
        return (WrappedArrayRuntimeBindingType)rtt;
    }

    public BuiltinRuntimeBindingType createRuntimeType(BuiltinBindingType type,
                                                       RuntimeBindingTypeTable type_table,
                                                       BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, type_table, binding_loader);
        return (BuiltinRuntimeBindingType)rtt;
    }

    public SimpleRuntimeBindingType createRuntimeType(SimpleBindingType type,
                                                      RuntimeBindingTypeTable type_table,
                                                      BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, type_table, binding_loader);
        return (SimpleRuntimeBindingType)rtt;
    }


    public ByNameRuntimeBindingType createRuntimeType(ByNameBean type,
                                                      RuntimeBindingTypeTable type_table,
                                                      BindingLoader binding_loader)
        throws XmlException
    {
        final RuntimeBindingType rtt =
            createRuntimeTypeInternal(type, type_table, binding_loader);
        return (ByNameRuntimeBindingType)rtt;
    }


    //avoids a cast to deal with overloaded methods
    private RuntimeBindingType createRuntimeTypeInternal(BindingType type,
                                                         RuntimeBindingTypeTable type_table,
                                                         BindingLoader binding_loader)
        throws XmlException
    {
        return createRuntimeType(type, type_table, binding_loader);
    }


    private RuntimeBindingType allocateType(BindingType type)
        throws XmlException
    {
        type.accept(typeVisitor);
        return typeVisitor.getRuntimeBindingType();
    }

    private static final class TypeVisitor
        implements BindingTypeVisitor
    {
        private RuntimeBindingType runtimeBindingType;

        public RuntimeBindingType getRuntimeBindingType()
        {
            return runtimeBindingType;
        }

        public void visit(BuiltinBindingType builtinBindingType)
            throws XmlException
        {
            runtimeBindingType = new BuiltinRuntimeBindingType(builtinBindingType);
        }

        public void visit(ByNameBean byNameBean)
            throws XmlException
        {
            runtimeBindingType = new ByNameRuntimeBindingType(byNameBean);
        }

        public void visit(SimpleBindingType simpleBindingType)
            throws XmlException
        {
            runtimeBindingType = new SimpleRuntimeBindingType(simpleBindingType);
        }

        public void visit(SimpleDocumentBinding simpleDocumentBinding)
            throws XmlException
        {
            throw new AssertionError("not valid here: " + simpleDocumentBinding);
        }

        public void visit(WrappedArrayType wrappedArrayType)
            throws XmlException
        {
            runtimeBindingType = new WrappedArrayRuntimeBindingType(wrappedArrayType);
        }


    }

}
