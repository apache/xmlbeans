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

import java.util.*;

public class PackageInfo 
{
    private final String _packageName;
    private String _indexClassName;

    /** A list of the components in this package that will need a factory method */
    private final List _components = new ArrayList();

    public PackageInfo(String packageName)
    {
        _packageName = packageName;
    }

    public String getPackageName()
    {
        return _packageName;
    }

    public void addFactoryComponent(ComponentInfo ci)
    {
        _components.add(ci);
    }

    public List factoryComponents()
    {
        return _components;
    }

    public void setIndexClassName(String indexClassName)
    {
        _indexClassName = indexClassName;
    }

    public String getIndexClassName()
    {
        return _indexClassName;
    }

}

