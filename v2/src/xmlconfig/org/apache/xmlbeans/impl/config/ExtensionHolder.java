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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.lang.reflect.Method;

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
        private Method _method;

        MethodSignature(String intfName, Method method)
        {
            if (intfName==null || method==null)
                throw new IllegalArgumentException("Interface: " + intfName + " method: " + method);

            _intfName = intfName;
            _method = method;
        }

        String getInterfaceName()
        {
            return _intfName;
        }

        String getSignature()
        {
            String sig = "";
            Class[] paramTypes = _method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++)
            {
                Class paramType = paramTypes[i];
                sig += ( i==0 ? "" : " ," ) + paramType.getName();
            }
            return _method.getName() + "(" + sig + ")";
        }

        public boolean equals(Object o)
        {
            if ( !(o instanceof MethodSignature))
                return false;

            MethodSignature ms = (MethodSignature)o;

            if (!ms._method.getName().equals(_method.getName()) )
                return false;

            Class[] paramTypes = _method.getParameterTypes();
            Class[] msParamTypes = _method.getParameterTypes();

            if (msParamTypes.length != paramTypes.length )
                return false;

            for (int i = 0; i < paramTypes.length; i++)
            {
                if (msParamTypes[i] != paramTypes[i])
                    return false;
            }

            return true;
        }

        public int hashCode()
        {
            int hash = _method.getName().hashCode();

            Class[] paramTypes = _method.getParameterTypes();

            for (int i = 0; i < paramTypes.length; i++)
            {
                hash *= 19;
                hash += paramTypes[i].hashCode();
            }

            return hash;
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
