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
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.JaxrpcEnumType;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

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

    void accept(RuntimeTypeVisitor visitor)
        throws XmlException
    {
        visitor.visit(this);
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader
                           )
        throws XmlException
    {
        itemInfo = new ItemInfo(jaxrpcEnumType, getJavaType(), typeTable,
                                bindingLoader);
    }

    boolean hasElementChildren()
    {
        return false;
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

    Object fromValue(Object itemValue, UnmarshalResult context)
        throws XmlException
    {
        assert itemValue != null;
        assert context != null;
        try {
            return ReflectionUtils.invokeMethod(null, itemInfo.getFromValueMethod(),
                                                new Object[]{itemValue});
        }
        catch (XmlException iae) {
            throw new InvalidLexicalValueException(iae, context.getLocation());
        }
    }


    private static final class ItemInfo
    {
        private final RuntimeBindingType itemType;
        private final Method getValueMethod;
        private final Method fromValueMethod;
        private final Method toXmlMethod;

        ItemInfo(JaxrpcEnumType jaxrpcEnumType,
                 Class enum_java_class,
                 RuntimeBindingTypeTable typeTable,
                 BindingLoader loader)
            throws XmlException
        {
            final BindingTypeName base_name = jaxrpcEnumType.getBaseTypeName();
            if (base_name == null) {
                throw new XmlException("null base type for " + jaxrpcEnumType);
            }
            final BindingType item_type = loader.getBindingType(base_name);
            if (item_type == null) {
                final String msg = "unable to load type " + base_name +
                    " for " + jaxrpcEnumType;
                throw new XmlException(msg);
            }

            itemType =
                typeTable.createRuntimeType(item_type, loader);

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
            TypeMarshaller itemMarshaller = itemType.getMarshaller();
            if (itemMarshaller == null) {
                String m = "unable to locate marshaller for " + item_type;
                throw new XmlException(m);
            }

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
            return itemType.getMarshaller();
        }

        TypeUnmarshaller getItemUnmarshaller()
        {
            return itemType.getUnmarshaller();
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
