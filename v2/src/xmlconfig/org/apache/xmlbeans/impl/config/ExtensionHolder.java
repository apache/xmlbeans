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
 * Date: Apr 25, 2004
 */
package org.apache.xmlbeans.impl.config;

import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JClass;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * This class reprezents all the extensions for all xbean sets.
 * This class is the root of the structure that reprezents all the extensions.
 */
public class ExtensionHolder
{
    // these lists are expected to be quite small
    private List _interfaceExtensions;
    private List _prePostExtensions;

    ExtensionHolder()
    {
        _interfaceExtensions = new ArrayList();
        _prePostExtensions = new ArrayList();
    }

    void addInterfaceExtension(InterfaceExtension ext)
    {
        if (ext==null)
            return;

        _interfaceExtensions.add(ext);
    }

    void addPrePostExtension(PrePostExtension ext)
    {
        if (ext==null)
            return;

        _prePostExtensions.add(ext);
    }

    // this is used only for detecting method colisions of extending interfaces
    private static class MethodSignature
    {
        private String _intfName;  // Stored only for error output, does not influence the equals or hashCode
        private JMethod _method;
        private final int NOTINITIALIZED = -1;
        private int _hashCode = NOTINITIALIZED;
        private String _signature;

        MethodSignature(String intfName, JMethod method)
        {
            if (intfName==null || method==null)
                throw new IllegalArgumentException("Interface: " + intfName + " method: " + method);

            _intfName = intfName;
            _method = method;
            _hashCode = NOTINITIALIZED;
            _signature = null;
        }

        String getInterfaceName()
        {
            return _intfName;
        }

        String getSignature()
        {
            if (_signature==null)
                return _signature;

            String sig = "";
            JParameter[] paramTypes = _method.getParameters();
            for (int i = 0; i < paramTypes.length; i++)
            {
                JClass paramType = paramTypes[i].getType();
                sig += ( i==0 ? "" : " ," ) + paramType.getQualifiedName();
            }
            _signature = _method.getSimpleName() + "(" + sig + ")";
            return _signature;
        }

        public boolean equals(Object o)
        {
            if ( !(o instanceof MethodSignature))
                return false;

            MethodSignature ms = (MethodSignature)o;

            if (!ms._method.getSimpleName().equals(_method.getSimpleName()) )
                return false;

            JParameter[] params = _method.getParameters();
            JParameter[] msParams = _method.getParameters();

            if (msParams.length != params.length )
                return false;

            for (int i = 0; i < params.length; i++)
            {
                if (!msParams[i].getType().equals(params[i].getType()))
                    return false;
            }

            return true;
        }

        public int hashCode()
        {
            if (_hashCode!=NOTINITIALIZED)
                return _hashCode;

            int hash = _method.getSimpleName().hashCode();

            JParameter[] params = _method.getParameters();

            for (int i = 0; i < params.length; i++)
            {
                hash *= 19;
                hash += params[i].getType().hashCode();
            }

            _hashCode = hash;
            return _hashCode;
        }
    }

    void secondPhaseValidation()
    {
        // validate interface methods collisions
        Map methodSignatures = new HashMap();

        for (int i = 0; i < _interfaceExtensions.size(); i++)
        {
            InterfaceExtension interfaceExtension = (InterfaceExtension) _interfaceExtensions.get(i);
            for (int j = 0; j < interfaceExtension.getInterfaceMethodCount(); j++)
            {
                MethodSignature ms = new MethodSignature(interfaceExtension.getInterfaceName(),
                     interfaceExtension.getInterfaceMethod(j));

                if ( methodSignatures.containsKey(ms) )
                {
                    MethodSignature ms2 = (MethodSignature) methodSignatures.get(ms);
                    SchemaConfig.error("Colliding methods '" + ms.getSignature() + "' in interfaces " +
                        ms.getInterfaceName() + " and " + ms2.getInterfaceName() + ".", null);

                    return;
                }

                // store it into hashmap
                methodSignatures.put(ms, ms);
            }
        }

        // validate that PrePostExtension-s do not intersect
        for (int i = 0; i < _prePostExtensions.size() - 1; i++)
        {
            PrePostExtension a = (PrePostExtension) _prePostExtensions.get(i);
            for (int j = 1; j < _prePostExtensions.size(); j++)
            {
                PrePostExtension b = (PrePostExtension) _prePostExtensions.get(j);
                if (a.hasNameSetIntersection(b))
                    SchemaConfig.error("The applicable domain for handler '" + a.getHandlerNameForJavaSource() +
                        "' intersects with the one for '" + b.getHandlerNameForJavaSource() + "'.", null);
            }
        }
    }

    void normalize()
    {
        //todo: not yet used, useful for optimizing jar size in case of big interfaces and big schemas
        throw new RuntimeException("NYI");
        // this matters only for InterfaceExtension-s
    }

    public void verifyInterfaceNameCollisions(Set genedUsedNames)
    {
        for (int i = 0; i < _interfaceExtensions.size(); i++)
        {
            InterfaceExtension interfaceExtension = (InterfaceExtension) _interfaceExtensions.get(i);
            if ( genedUsedNames.contains(interfaceExtension.getInterfaceNameForJavaSource().toLowerCase()) )
            {
                SchemaConfig.error("Extension interface '" + interfaceExtension.getInterfaceNameForJavaSource() + "' creates a name collision with one of the generated interfaces or classes.", null);
            }
            String handlerClassName = interfaceExtension.getHandlerNameForJavaSource();
            if ( handlerClassName!=null &&
                genedUsedNames.contains(handlerClassName.toLowerCase()) )
            {
                SchemaConfig.error("Handler class '" + handlerClassName + "' creates a name collision with one of the generated interfaces or classes.", null);
            }
        }

        for (int i = 0; i < _prePostExtensions.size(); i++)
        {
            PrePostExtension prePostExtension = (PrePostExtension) _prePostExtensions.get(i);
            String handlerClassName = prePostExtension.getHandlerNameForJavaSource();
            if ( handlerClassName!=null &&
                genedUsedNames.contains(handlerClassName.toLowerCase()) )
            {
                SchemaConfig.error("Handler class '" + prePostExtension.getHandlerNameForJavaSource() + "' creates a name collision with one of the generated interfaces or classes.", null);
            }
        }
    }

    ExtensionHolder extensionHolderFor(String javaName)
    {
        for (int i = 0; i < _interfaceExtensions.size(); i++)
        {
            InterfaceExtension interfaceExtension = (InterfaceExtension) _interfaceExtensions.get(i);
            if (interfaceExtension.contains(javaName))
                return this;
        }

        for (int i = 0; i < _prePostExtensions.size(); i++)
        {
            PrePostExtension prePostExtension = (PrePostExtension) _prePostExtensions.get(i);
            if (prePostExtension.contains(javaName))
                return this;
        }

        return null;
    }

    public List getInterfaceExtensionsFor(String fullJavaName)
    {
        List result = new ArrayList();
        for (int i = 0; i < _interfaceExtensions.size(); i++)
        {
            InterfaceExtension intfExt = (InterfaceExtension) _interfaceExtensions.get(i);
            if (intfExt.contains(fullJavaName))
                result.add(intfExt);
        }
        return result;
    }

    public PrePostExtension getPrePostExtensionsFor(String fullJavaName)
    {
        for (int i = 0; i < _prePostExtensions.size(); i++)
        {
            PrePostExtension prePostExt = (PrePostExtension) _prePostExtensions.get(i);
            if (prePostExt.contains(fullJavaName))
                return prePostExt;
        }
        return null;
    }
}
