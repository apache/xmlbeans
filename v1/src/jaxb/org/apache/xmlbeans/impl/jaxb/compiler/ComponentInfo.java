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

package org.apache.xmlbeans.impl.jaxb.compiler;

import org.apache.xmlbeans.SchemaType;
import java.lang.reflect.Constructor;

public abstract class ComponentInfo
{
    private String _fullJavaIntfName;
    private String _fullJavaImplName;
    private String _shortJavaIntfName;
    private String _shortJavaImplName;
    private String _baseIntfName;
    private String _baseImplName;
    private int _superCtrArgCount;

    // runtime support
    private volatile boolean     _implNotAvailable;
    private volatile Class       _javaImplClass;
    private volatile Constructor _javaImplConstructor;

    public String getFullJavaIntfName() {
        return _fullJavaIntfName;
    }

    public String getShortJavaIntfName() {
        return _shortJavaIntfName;
    }

    public void setFullJavaIntfName(String intfName) {
        _fullJavaIntfName = intfName;

        int index = Math.max(_fullJavaIntfName.lastIndexOf('$'),
                             _fullJavaIntfName.lastIndexOf('.')) + 1;

        _shortJavaIntfName = _fullJavaIntfName.substring(index);
    }

    public String getFullJavaImplName() {
        return _fullJavaImplName;
    }

    public String getShortJavaImplName() {
        return _shortJavaImplName;
    }

    public void setFullJavaImplName(String implName) {
        _fullJavaImplName = implName;

        int index = Math.max(_fullJavaImplName.lastIndexOf('$'),
                             _fullJavaImplName.lastIndexOf('.')) + 1;

        _shortJavaImplName = _fullJavaImplName.substring(index);
    }

    public void setBaseIntfName(String baseType)
    {
        _baseIntfName = baseType;
    }

    public String getBaseIntfName()
    {
        return _baseIntfName;
    }

    public void setBaseImplName(String basetype)
    {
        _baseImplName = basetype;
    }

    public String getBaseImplName()
    {
        return _baseImplName;
    }

    public int getSuperCtrArgCount() {
        return _superCtrArgCount;
    }

    public void setSuperCtrArgCount(int count) {
        _superCtrArgCount = count;
    }

    public Class getJavaImplClass(ClassLoader cl) {
        if (_implNotAvailable)
            return null;

        if (_javaImplClass == null)
        {
            try {
                if (getFullJavaImplName() != null)
                    _javaImplClass = Class.forName(getFullJavaImplName(), false, cl);
                else
                    _implNotAvailable = true;
            }
            catch (ClassNotFoundException e) {
                _implNotAvailable = true;
            }
        }

        return _javaImplClass;
    }

    public Constructor getJavaImplConstructor(ClassLoader cl)
    {
        if (_javaImplConstructor == null && !_implNotAvailable)
        {
            final Class impl = getJavaImplClass(cl);
            if (impl == null) return null;
            try
            {
                _javaImplConstructor = impl.getConstructor(null);
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }

        return _javaImplConstructor;

    }
}
