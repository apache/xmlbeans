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

import javax.xml.namespace.QName;

final class RuntimeGlobalProperty
    extends RuntimeBindingProperty
{
    private final QName rootElement;
    private final RuntimeBindingType runtimeBindingType;

    public RuntimeGlobalProperty(QName rootElement,
                                 RuntimeBindingType runtimeBindingType)
    {
        //We can pass null becuase the global element properties never have
        //a parent object, so this resolve should never be needed.
        //If it is somehow used we'll find out thanks to NPE.
        super(null);
        this.rootElement = rootElement;
        this.runtimeBindingType = runtimeBindingType;
    }

    public RuntimeBindingType getRuntimeBindingType()
    {
        return runtimeBindingType;
    }

    public RuntimeBindingType getActualRuntimeType(Object parentObject,
                                                   MarshalResult result)
        throws XmlException
    {
        return result.determineRuntimeBindingType(runtimeBindingType,
                                                  parentObject);
    }

    public QName getName()
    {
        return rootElement;
    }

    public void fill(Object inter, Object prop_obj)
        throws XmlException
    {
        throw new UnsupportedOperationException();
    }

    public Object getValue(Object parent_obj, MarshalResult result)
        throws XmlException
    {
        throw new AssertionError("UNIMP: " + this);
    }

    public boolean isSet(Object parentObject, MarshalResult result)
        throws XmlException
    {
        return true;
    }

    public boolean isMultiple()
    {
        return false;
    }

    public boolean isNillable()
    {
        //TODO & FIXME: we need the real information from the schema here
        return true;
    }

    public String getLexicalDefault()
    {
        throw new AssertionError("UNIMP");
    }

}
