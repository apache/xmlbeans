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
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;

import javax.xml.namespace.QName;

/**
 * what we need to know about a binding type at runtime.
 * No marshalling state should be stored here.
 * This object will be shared by many threads
 */
abstract class RuntimeBindingType
{
    private final BindingType bindingType;
    private final Class javaClass;
    private final boolean javaPrimitive;

    RuntimeBindingType(BindingType binding_type)
        throws XmlException
    {
        bindingType = binding_type;

        try {
            javaClass = getJavaClass(binding_type, getClass().getClassLoader());

        }
        catch (ClassNotFoundException e) {
            final String msg = "failed to load " +
                binding_type.getName().getJavaName();
            throw new XmlException(msg, e);
        }

        javaPrimitive = javaClass.isPrimitive();
    }


    final BindingType getBindingType()
    {
        return bindingType;
    }

    /**
     * prepare internal data structures for use
     *
     * @param typeTable
     * @param bindingLoader
     */
    abstract void initialize(RuntimeBindingTypeTable typeTable,
                             BindingLoader bindingLoader,
                             RuntimeTypeFactory rttFactory)
        throws XmlException;

    final Class getJavaType()
    {
        return javaClass;
    }

    final boolean isJavaPrimitive()
    {
        return javaPrimitive;
    }

    protected static Class getJavaClass(BindingType btype, ClassLoader backup)
        throws ClassNotFoundException
    {
        final JavaTypeName javaName = btype.getName().getJavaName();
        String jclass = javaName.toString();
        return ClassLoadingUtils.loadClass(jclass, backup);
    }

    protected QName getSchemaTypeName()
    {
        return getBindingType().getName().getXmlName().getQName();
    }

}
