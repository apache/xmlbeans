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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.JavaInstanceFactory;


/**
 * A property that addresses an XML element or attribute by name
 * rather than by position.
 */
public class ParentInstanceFactory extends JavaInstanceFactory
{

    // ========================================================================
    // Variables
    private MethodName createObjectMethod;

    // ========================================================================
    // Constructors

    public ParentInstanceFactory()
    {
        super();
    }

    public ParentInstanceFactory(org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
    {
        super(node);
        org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory pifNode =
            (org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory)node;
        this.createObjectMethod =
            MethodName.create(pifNode.getCreateObjectMethod());
    }

    // ========================================================================
    // Public methods
    public MethodName getCreateObjectMethod()
    {
        return createObjectMethod;
    }

    public void setCreateObjectMethod(MethodName createObjectMethod)
    {
        this.createObjectMethod = createObjectMethod;
    }

    // ========================================================================
    // BindingType implementation

    /**
     * This function copies an instance back out to the relevant part of the XML file.
     *
     * Subclasses should override and call super.write first.
     */
    protected org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory write(org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
    {
        node = super.write(node);

        org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory pifNode =
            (org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory)node;

        if (createObjectMethod != null) {
            createObjectMethod.write(pifNode.addNewCreateObjectMethod());
        }

        return pifNode;
    }


}
