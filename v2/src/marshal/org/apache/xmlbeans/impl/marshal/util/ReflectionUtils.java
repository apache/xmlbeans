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

package org.apache.xmlbeans.impl.marshal.util;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.MethodName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ReflectionUtils
{

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    public static Object invokeMethod(Object target,
                                      Method method,
                                      Object[] params)
        throws XmlException
    {
        try {
            return method.invoke(target, params);
        }
        catch (IllegalAccessException e) {
            throw new XmlException(e);
        }
        catch (IllegalArgumentException e) {
            throw new XmlException(e);
        }
        catch (InvocationTargetException e) {
            throw new XmlException(e);
        }
    }

    /**
     * invoke method with no arguments
     *
     * @param target
     * @param method
     * @return
     * @throws XmlException
     */
    public static Object invokeMethod(Object target,
                                      Method method)
        throws XmlException
    {
        return invokeMethod(target, method, EMPTY_OBJECT_ARRAY);
    }

    public static Method getSetterMethod(BindingProperty binding_prop,
                                         Class beanClass)
        throws XmlException
    {
        if (!binding_prop.hasSetter()) return null;

        MethodName setterName = binding_prop.getSetterName();
        return getMethodOnClass(setterName, beanClass);
    }

    public static Method getIssetterMethod(BindingProperty binding_prop,
                                           Class clazz)
        throws XmlException
    {
        if (!binding_prop.hasIssetter())
            return null;

        Method isset_method =
            getMethodOnClass(binding_prop.getIssetterName(), clazz);

        if (!isset_method.getReturnType().equals(Boolean.TYPE)) {
            String msg = "invalid isset method: " + isset_method +
                " -- return type must be boolean not " +
                isset_method.getReturnType().getName();
            throw new XmlException(msg);
        }

        return isset_method;
    }

    public static Method getGetterMethod(BindingProperty binding_prop,
                                         Class beanClass)
        throws XmlException
    {
        MethodName getterName = binding_prop.getGetterName();
        return getMethodOnClass(getterName, beanClass);
    }

    public static Method getMethodOnClass(MethodName method_name,
                                          Class clazz)
        throws XmlException
    {
        if (method_name == null) return null;
        
        try {
            return method_name.getMethodOn(clazz);
        }
        catch (NoSuchMethodException e) {
            throw new XmlException(e);
        }
        catch (SecurityException se) {
            throw new XmlException(se);
        }
        catch (ClassNotFoundException cnfe) {
            throw new XmlException(cnfe);
        }
    }

    public static boolean isClassFinal(Class javaClass)
    {
        final int modifiers = javaClass.getModifiers();
        return Modifier.isFinal(modifiers);
    }
}
