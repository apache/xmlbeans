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
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JamClassLoader;

public class InterfaceExtension
{
    private NameSet _xbeanSet;
    private JClass _interface;
    private JClass _delegateToClass;
    private String _delegateToClassName;
    private JMethod[] _interfaceMethods;
    private JMethod[] _delegateToMethods;

    static InterfaceExtension newInstance(JamClassLoader loader, NameSet xbeanSet, Extensionconfig.Interface intfXO)
    {
        InterfaceExtension result = new InterfaceExtension();

        result._xbeanSet = xbeanSet;
        result._interface = validateInterface(loader, intfXO.getName(), intfXO);

        if (result._interface == null)
        {
            SchemaConfig.error("Interface '" + intfXO.getStaticHandler() + "' not found.", intfXO);
            return null;
        }

        result._delegateToClassName = intfXO.getStaticHandler();
        result._delegateToClass = validateClass(loader, result._delegateToClassName, intfXO);

        if ( result._delegateToClass==null ) // no HandlerClass
        {
            SchemaConfig.warning("Handler class '" + intfXO.getStaticHandler() + "' not found on classpath, skip validation.", intfXO);
            return result;
        }

        if (!result.validateMethods(intfXO))
            return null;

        return result;
    }

    private static JClass validateInterface(JamClassLoader loader, String intfStr, XmlObject loc)
    {
        return validateJava(loader, intfStr, true, loc);
    }

    static JClass validateClass(JamClassLoader loader, String clsStr, XmlObject loc)
    {
        return validateJava(loader, clsStr, false, loc);
    }

    static JClass validateJava(JamClassLoader loader, String clsStr, boolean isInterface, XmlObject loc)
    {
        if (loader==null)
            return null;

        final String ent = isInterface ? "Interface" : "Class";
        JClass cls = loader.loadClass(clsStr);

        if (cls==null)
        {
            SchemaConfig.error(ent + " '" + clsStr + "' not found.", loc);
            return null;
        }

        if ( (isInterface && !cls.isInterface()) ||
                (!isInterface && cls.isInterface()))
        {
            SchemaConfig.error("'" + clsStr + "' must be " +
                (isInterface ? "an interface" : "a class") + ".", loc);
        }

        if (!cls.isPublic())
        {
            SchemaConfig.error(ent + " '" + clsStr + "' is not public.", loc);
        }

        return cls;
    }

    private boolean validateMethods(XmlObject loc)
    {
        assert _delegateToClass != null : "Delegate to class handler expected.";
        boolean valid = true;

        _interfaceMethods = _interface.getMethods();
        _delegateToMethods = new JMethod[_interfaceMethods.length];

        for (int i = 0; i < _interfaceMethods.length; i++)
        {
            valid &= validateMethod(i, _interfaceMethods[i], loc);
        }

        return valid;
    }

    private boolean validateMethod(int index, JMethod method, XmlObject loc)
    {
        String methodName = method.getSimpleName();
        JParameter[] params = method.getParameters();
        JClass returnType = method.getReturnType();

        JClass[] delegateParams = new JClass[params.length+1];
        delegateParams[0] = returnType.forName("org.apache.xmlbeans.XmlObject");
        for (int i = 1; i < delegateParams.length; i++)
        {
            delegateParams[i] = params[i-1].getType();
        }

        JMethod handlerMethod = null;
        handlerMethod = getMethod(_delegateToClass, methodName, delegateParams);
        if (handlerMethod==null)
        {
            SchemaConfig.error("Handler class '" + _delegateToClass.getQualifiedName() + "' does not contain method " + methodName + "(" + listTypes(delegateParams) + ")", loc);
            return false;
        }

        // check for throws exceptions
        JClass[] intfExceptions = method.getExceptionTypes();
        JClass[] delegateExceptions = handlerMethod.getExceptionTypes();
        if ( delegateExceptions.length!=intfExceptions.length )
        {
            SchemaConfig.error("Handler method '" + _delegateToClass.getQualifiedName() + "." + methodName + "(" + listTypes(delegateParams) +
                ")' must declare the same exceptions as the interface method '" + _interface.getQualifiedName() + "." + methodName + "(" + listTypes(params), loc);
            return false;
        }

        for (int i = 0; i < delegateExceptions.length; i++)
        {
            if ( delegateExceptions[i]!=intfExceptions[i] )
            {
                SchemaConfig.error("Handler method '" + _delegateToClass.getQualifiedName() + "." + methodName + "(" + listTypes(delegateParams) +
                    ")' must declare the same exceptions as the interface method '" + _interface.getQualifiedName() + "." + methodName + "(" + listTypes(params), loc);
                return false;
            }
        }

        if (!handlerMethod.isPublic() || !handlerMethod.isStatic())
        {
            SchemaConfig.error("Method '" + _delegateToClass.getQualifiedName() + "." + methodName + "(" + listTypes(delegateParams) + ")' must be declared public and static.", loc);
            return false;
        }

        if (!returnType.equals(handlerMethod.getReturnType()))
        {
            SchemaConfig.error("Return type for method '" + handlerMethod.getReturnType() + " " + _delegateToClass.getQualifiedName() +
                    "." + methodName + "(" + listTypes(delegateParams) + ")' does not match the return type of the interface method :'" + returnType + "'.", loc);
            return false;
        }

        _delegateToMethods[index] = method;

        return true;
    }

    static JMethod getMethod(JClass cls, String name, JClass[] paramTypes)
    {
        JMethod[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            JMethod method = methods[i];
            if (!name.equals(method.getSimpleName()))
                continue;

            JParameter[] mParams = method.getParameters();
            for (int j = 0; j < mParams.length; j++)
            {
                JParameter mParam = mParams[j];
                if (!mParam.getType().equals(paramTypes[j]))
                    continue;
            }

            return method;
        }
        return null;
    }

    private static String listTypes(JClass[] types)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < types.length; i++)
        {
            JClass type = types[i];
            if (i>0)
                result.append(", ");
            result.append(emitType(type));
        }
        return result.toString();
    }

    private static String listTypes(JParameter[] params)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < params.length; i++)
        {
            JClass type = params[i].getType();
            if (i>0)
                result.append(", ");
            result.append(emitType(type));
        }
        return result.toString();
    }

    public static String emitType(JClass cls)
    {
        if (cls.isArrayType())
            return emitType(cls.getArrayComponentType()) + "[]";
        else
            return cls.getQualifiedName().replace('$', '.');
    }

    /* public getters */
    public boolean contains(String fullJavaName)
    {
        return _xbeanSet.contains(fullJavaName);
    }

    public String getInterfaceName()
    {
        return _interface.getSimpleName();
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
        return _interfaceMethods[methodIndex].getSimpleName();
    }

    public JMethod getInterfaceMethod(int methodIndex)
    {
        return _interfaceMethods[methodIndex];
    }

    public String getInterfaceMethodDecl(int methodIndex)
    {
        StringBuffer sb = new StringBuffer();
        JMethod m = _interfaceMethods[methodIndex];
        JParameter[] params = m.getParameters();

        for (int i = 0; i < params.length; i++)
        {
            JClass paramType = params[i].getType();
            sb.append( i==0 ? "" : ", " );
            sb.append( emitType(paramType) + " p" + i);
        }

        StringBuffer exceptions = new StringBuffer();
        JClass[] excClasses = m.getExceptionTypes();

        for (int i=0; i<excClasses.length; i++)
            exceptions.append((i==0 ? " throws " : ", ") + emitType(excClasses[i]));

        return "public " + emitType(m.getReturnType()) + " " + m.getSimpleName() + "(" + sb.toString() + ")" + exceptions.toString();
    }

    public String getInterfaceMethodImpl(int methodIndex)
    {
	    // use the methods from the interface for gen the call to the handler
        StringBuffer sb = new StringBuffer();

        if (!_interfaceMethods[methodIndex].getReturnType().forName("void").equals(_interfaceMethods[methodIndex].getReturnType()))
            sb.append("return ");

        sb.append(_delegateToClassName + "." + _delegateToMethods[methodIndex].getSimpleName() + "(this");

        int paramCount = _interfaceMethods[methodIndex].getParameters().length;
        for (int i=0; i<paramCount; i++)
        {
            sb.append(", p" + i);
        }

        sb.append(");");
        return sb.toString();
    }
}
