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
/**
 * Author: Cezar Andrei ( cezar.andrei at bea.com )
 * Date: Mar 25, 2004
 */
package org.apache.xmlbeans.impl.config;

import org.apache.xml.xmlbeans.x2004.x02.xbean.config.Extensionconfig;
import org.apache.xmlbeans.XmlObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InterfaceExtension
{
    private NameSet _xbeanSet;
    private Class _interface;
    private Class _delegateToClass;
    private String _delegateToClassName;
    private Method[] _interfaceMethods;
    private Method[] _delegateToMethods;

    static InterfaceExtension newInstance(NameSet xbeanSet, Extensionconfig.Interface intfXO)
    {
        InterfaceExtension result = new InterfaceExtension();

        result._xbeanSet = xbeanSet;
        result._interface = validateInterface(intfXO.getName(), intfXO);

        if (result._interface == null)
        {
            SchemaConfig.error("Interface '" + intfXO.getStaticHandler() + "' not found.", intfXO);
            return null;
        }

        result._delegateToClassName = intfXO.getStaticHandler();
        result._delegateToClass = validateClass(result._delegateToClassName, intfXO);

        if ( result._delegateToClass==null ) // no HandlerClass
        {
            SchemaConfig.warning("Handler class '" + intfXO.getStaticHandler() + "' not found on classpath, skip validation.", intfXO);
            return result;
        }

        if (!result.validateMethods(intfXO))
            return null;

        return result;
    }

    private static Class validateInterface(String intfStr, XmlObject loc)
    {
        return validateJava(intfStr, true, loc);
    }

    static Class validateClass(String clsStr, XmlObject loc)
    {
        return validateJava(clsStr, false, loc);
    }

    static Class validateJava(String clsStr, boolean isInterface, XmlObject loc)
    {
        final String ent = isInterface ? "Interface" : "Class";
        try
        {
            Class cls = Class.forName(clsStr);
            if ( (isInterface && !cls.isInterface()) ||
                    (!isInterface && cls.isInterface()))
            {
                SchemaConfig.error("'" + clsStr + "' must be " +
                    (isInterface ? "an interface" : "a class") + ".", loc);
            }

            if (!Modifier.isPublic(cls.getModifiers()))
            {
                SchemaConfig.error(ent + " '" + clsStr + "' is not public.", loc);
            }

            return cls;
        }
        catch (ClassNotFoundException e)
        {
            SchemaConfig.error(ent + " '" + clsStr + "' not found.", loc);
            return null;
        }
    }

    private boolean validateMethods(XmlObject loc)
    {
        assert _delegateToClass != null : "Delegate to class handler expected.";
        boolean valid = true;

        _interfaceMethods = _interface.getMethods();
        _delegateToMethods = new Method[_interfaceMethods.length];

        for (int i = 0; i < _interfaceMethods.length; i++)
        {
            valid &= validateMethod(i, _interfaceMethods[i], loc);
        }

        return valid;
    }

    private boolean validateMethod(int index, Method method, XmlObject loc)
    {
        String methodName = method.getName();
        Class[] paramTypes = method.getParameterTypes();
        Class returnType = method.getReturnType();

        Class[] delegateParams = new Class[paramTypes.length+1];
        delegateParams[0] = XmlObject.class;
        for (int i = 1; i < delegateParams.length; i++)
        {
            delegateParams[i] = paramTypes[i-1];
        }

        Method handlerMethod = null;
        try
        {
            handlerMethod = _delegateToClass.getMethod(methodName, delegateParams);

            // check for throws exceptions
            Class[] intfExceptions = method.getExceptionTypes();
            Class[] delegateExceptions = handlerMethod.getExceptionTypes();
            if ( delegateExceptions.length!=intfExceptions.length )
            {
                SchemaConfig.error("Handler method '" + _delegateToClass.getName() + "." + methodName + "(" + listTypes(delegateParams) +
                    ")' must declare the same exceptions as the interface method '" + _interface.getName() + "." + methodName + "(" + listTypes(paramTypes), loc);
                return false;
            }

            for (int i = 0; i < delegateExceptions.length; i++)
            {
                if ( delegateExceptions[i]!=intfExceptions[i] )
                {
                    SchemaConfig.error("Handler method '" + _delegateToClass.getName() + "." + methodName + "(" + listTypes(delegateParams) +
                        ")' must declare the same exceptions as the interface method '" + _interface.getName() + "." + methodName + "(" + listTypes(paramTypes), loc);
                    return false;
                }
            }
        }
        catch (NoSuchMethodException e)
        {
            SchemaConfig.error("Handler class '" + _delegateToClass.getName() + "' does not contain method " + methodName + "(" + listTypes(delegateParams) + ")", loc);
            return false;
        }
        catch (SecurityException e)
        {
            SchemaConfig.error("Security violation for class '" + _interface.getName() + "' accesing method " + methodName + "(" + listTypes(delegateParams) + ")", loc);
            return false;
        }

        if (!Modifier.isPublic(handlerMethod.getModifiers()) || !Modifier.isStatic(handlerMethod.getModifiers()))
        {
            SchemaConfig.error("Method '" + _delegateToClass.getName() + "." + methodName + "(" + listTypes(delegateParams) + ")' must be declared public and static.", loc);
            return false;
        }

        if (!returnType.equals(handlerMethod.getReturnType()))
        {
            SchemaConfig.error("Return type for method '" + handlerMethod.getReturnType() + " " + _delegateToClass.getName() +
                    "." + methodName + "(" + listTypes(delegateParams) + ")' does not match the return type of the interface method :'" + returnType + "'.", loc);
            return false;
        }

        _delegateToMethods[index] = method;

        return true;
    }

    private static String listTypes(Class[] types)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < types.length; i++)
        {
            Class type = types[i];
            if (i>0)
                result.append(", ");
            result.append(emitType(type));
        }
        return result.toString();
    }

    public static String emitType(Class cls)
    {
        if (cls.isArray())
            return emitType(cls.getComponentType()) + "[]";
        else
            return cls.getName().replace('$', '.');
    }

    /* public getters */
    public boolean contains(String fullJavaName)
    {
        return _xbeanSet.contains(fullJavaName);
    }

    public String getInterfaceName()
    {
        return _interface.getName();
    }

    public String getInterfaceNameForJavaSource()
    {
        return emitType(_interface);
    }

    // used only for validation
    public String getHandlerNameForJavaSource()
    {
        if (_delegateToClass==null)
            return null;

        return emitType(_delegateToClass);
    }

    public int getInterfaceMethodCount()
    {
        return _interfaceMethods.length;
    }

    public String getInterfaceMethodName(int methodIndex)
    {
        return _interfaceMethods[methodIndex].getName();
    }

    public Method getInterfaceMethod(int methodIndex)
    {
        return _interfaceMethods[methodIndex];
    }

    public String getInterfaceMethodDecl(int methodIndex)
    {
        StringBuffer sb = new StringBuffer();
        Method m = _interfaceMethods[methodIndex];
        Class[] paramTypes = m.getParameterTypes();

        for (int i = 0; i < paramTypes.length; i++)
        {
            Class paramType = paramTypes[i];
            sb.append( i==0 ? "" : ", " );
            sb.append( emitType(paramType) + " p" + i);
        }

        StringBuffer exceptions = new StringBuffer();
        Class[] excClasses = m.getExceptionTypes();

        for (int i=0; i<excClasses.length; i++)
            exceptions.append((i==0 ? " throws " : ", ") + emitType(excClasses[i]));

        return "public " + emitType(m.getReturnType()) + " " + m.getName() + "(" + sb.toString() + ")" + exceptions.toString();
    }

    public String getInterfaceMethodImpl(int methodIndex)
    {
	// use the methods from the interface for gen the call to the handler
        StringBuffer sb = new StringBuffer();

        if (!void.class.equals(_interfaceMethods[methodIndex].getReturnType()))
            sb.append("return ");

        sb.append(_delegateToClassName + "." + _delegateToMethods[methodIndex].getName() + "(this");

        int paramCount = _interfaceMethods[methodIndex].getParameterTypes().length;
        for (int i=0; i<paramCount; i++)
        {
            sb.append(", p" + i);
        }

        sb.append(");");
        return sb.toString();
    }
}
