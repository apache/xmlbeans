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
import org.apache.xmlbeans.impl.binding.bts.BindingType;

import javax.xml.namespace.QName;

final class RuntimeGlobalProperty
    implements RuntimeBindingProperty
{
    private final QName rootElement;
    private final RuntimeBindingType runtimeBindingType;

    public RuntimeGlobalProperty(QName rootElement,
                                 RuntimeBindingType runtimeBindingType)
    {
        this.rootElement = rootElement;
        this.runtimeBindingType = runtimeBindingType;
    }

    private BindingType getType()
    {
        return runtimeBindingType.getBindingType();
    }

    public RuntimeBindingType getRuntimeBindingType()
    {
        return runtimeBindingType;
    }

    public RuntimeBindingType getActualRuntimeType(Object parentObject,
                                                   MarshalResult result)
        throws XmlException
    {
        return MarshalResult.findActualRuntimeType(parentObject,
                                                   runtimeBindingType,
                                                   result);
    }

    public QName getName()
    {
        return rootElement;
    }

    public TypeUnmarshaller getTypeUnmarshaller(UnmarshalResult context)
    {
        throw new UnsupportedOperationException();
    }

    public void fill(Object inter, Object prop_obj)
        throws XmlException
    {
        throw new UnsupportedOperationException();
    }

    //non simple type props can throw some runtime exception.
    public CharSequence getLexical(Object parent, MarshalResult result)
        throws XmlException
    {
        //TODO: polymorphism checks

        final BindingType type = getType();

        final TypeMarshaller tm =
            result.getTypeTable().getTypeMarshaller(type);

        if (tm == null) {
            throw new XmlException("lookup failed for " + type);
        }

        final CharSequence retval = tm.print(parent, result);
        return retval;
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
