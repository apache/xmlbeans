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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.JaxrpcEnumType;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;

import java.lang.reflect.Method;

final class JaxrpcEnumRuntimeBindingType
    extends RuntimeBindingType
{
    private final JaxrpcEnumType jaxrpcEnumType;
    private ItemInfo itemInfo;

    JaxrpcEnumRuntimeBindingType(JaxrpcEnumType type)
        throws XmlException
    {
        super(type);
        jaxrpcEnumType = type;
    }

    void initialize(RuntimeBindingTypeTable typeTable,
                    BindingLoader bindingLoader,
                    RuntimeTypeFactory rttFactory)
        throws XmlException
    {
        itemInfo = new ItemInfo(jaxrpcEnumType, getJavaType(), typeTable,
                                bindingLoader, rttFactory);
    }

    CharSequence print(Object value,
                       MarshalResult result)
        throws XmlException
    {
        if (itemInfo.hasToXmlMethod()) {
            return (String)ReflectionUtils.invokeMethod(value,
                                                        itemInfo.getToXmlMethod());
        } else {
            final Object simple_content = extractValue(value);
            return itemInfo.getItemMarshaller().print(simple_content, result);
        }
    }

    private Object extractValue(Object value)
        throws XmlException
    {
        return ReflectionUtils.invokeMethod(value,
                                            itemInfo.getGetValueMethod());
    }

    TypeUnmarshaller getItemUnmarshaller()
    {
        return itemInfo.getItemUnmarshaller();
    }

    Object fromValue(Object itemValue) throws XmlException
    {
        return ReflectionUtils.invokeMethod(null, itemInfo.getFromValueMethod(),
                                            new Object[]{itemValue});
    }


    private static final class ItemInfo
    {
        private final TypeMarshaller itemMarshaller;
        private final TypeUnmarshaller itemUnmarshaller;
        private final Method getValueMethod;
        private final Method fromValueMethod;
        private final Method toXmlMethod;

        ItemInfo(JaxrpcEnumType jaxrpcEnumType,
                 Class enum_java_class,
                 RuntimeBindingTypeTable typeTable,
                 BindingLoader loader,
                 RuntimeTypeFactory rttFactory)
            throws XmlException
        {
            final BindingTypeName base_name = jaxrpcEnumType.getBaseTypeName();
            assert base_name != null;
            final BindingType item_type = loader.getBindingType(base_name);
            if (item_type == null) {
                final String msg = "unable to load type" + item_type +
                    " for " + jaxrpcEnumType;
                throw new XmlException(msg);
            }


            itemMarshaller = typeTable.lookupMarshaller(item_type, loader);
            if (itemMarshaller == null) {
                String m = "unable to locate marshaller for " + item_type;
                throw new XmlException(m);
            }
            itemUnmarshaller =
                typeTable.getOrCreateTypeUnmarshaller(item_type, loader);
            assert itemUnmarshaller != null;

            fromValueMethod =
                ReflectionUtils.getMethodOnClass(jaxrpcEnumType.getFromValueMethod(),
                                                 enum_java_class);
            if (!ReflectionUtils.isMethodStatic(fromValueMethod)) {
                String e = "fromValue method must be static.  invalid " +
                    "method: " + fromValueMethod + " in type " + jaxrpcEnumType;
                throw new XmlException(e);
            }

            getValueMethod =
                ReflectionUtils.getMethodOnClass(jaxrpcEnumType.getGetValueMethod(),
                                                 enum_java_class);
            toXmlMethod =
                ReflectionUtils.getMethodOnClass(jaxrpcEnumType.getToXMLMethod(),
                                                 enum_java_class);

            //final sanity checks
            final RuntimeBindingType itemType =
                rttFactory.createRuntimeType(item_type, typeTable, loader);

            final Class[] parms = fromValueMethod.getParameterTypes();
            if (parms.length != 1) {
                throw new XmlException("invalid fromValue method, must have" +
                                       " one parameter: " + fromValueMethod +
                                       " for type " + jaxrpcEnumType);
            }
            if (!parms[0].isAssignableFrom(itemType.getJavaType())) {
                String m =
                    "invalid fromValue method:" + fromValueMethod +
                    " --  type mismatch between: " + parms[0] +
                    " and " + itemType.getJavaType() +
                    " for type " + jaxrpcEnumType;
                throw new XmlException(m);
            }
        }

        TypeMarshaller getItemMarshaller()
        {
            return itemMarshaller;
        }

        TypeUnmarshaller getItemUnmarshaller()
        {
            return itemUnmarshaller;
        }

        Method getGetValueMethod()
        {
            return getValueMethod;
        }

        Method getFromValueMethod()
        {
            return fromValueMethod;
        }

        Method getToXmlMethod()
        {
            return toXmlMethod;
        }

        boolean hasToXmlMethod()
        {
            return toXmlMethod != null;
        }

    }

}
