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
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.ListArrayType;
import org.apache.xmlbeans.impl.marshal.util.collections.Accumulator;

import javax.xml.namespace.QName;
import java.lang.reflect.Array;

final class ListArrayRuntimeBindingType
    extends RuntimeBindingType
{
    private final ListArrayType listArrayType;

    private LAProperty itemProperty;

    ListArrayRuntimeBindingType(ListArrayType binding_type)
        throws XmlException
    {
        super(binding_type);
        listArrayType = binding_type;
    }

    void accept(RuntimeTypeVisitor visitor)
        throws XmlException
    {
        visitor.visit(this);
    }

    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader bindingLoader
                           )
        throws XmlException
    {
        final BindingTypeName item_type_name = listArrayType.getItemType();
        assert item_type_name != null;

        final BindingType item_type = bindingLoader.getBindingType(item_type_name);
        if (item_type == null) {
            final String msg = "unable to lookup " + item_type_name +
                " from type " + listArrayType;
            throw new XmlException(msg);
        }

        final RuntimeBindingType item_rtt =
            typeTable.createRuntimeType(item_type, bindingLoader);

        itemProperty = new LAProperty(this, item_rtt);
    }

    boolean hasElementChildren()
    {
        return false;
    }


    RuntimeBindingProperty getItemProperty()
    {
        assert itemProperty != null;
        return itemProperty;
    }

    private static final class LAProperty
        extends RuntimeBindingProperty
    {
        private final RuntimeBindingType itemType;

        LAProperty(RuntimeBindingType containing_type,
                   RuntimeBindingType item_type)
            throws XmlException
        {
            super(containing_type);

            itemType = item_type;

            assert item_type.getMarshaller() != null;
        }

        Class getItemClass()
        {
            return itemType.getJavaType();
        }

        RuntimeBindingType getRuntimeBindingType()
        {
            return itemType;
        }

        RuntimeBindingType getActualRuntimeType(Object property_value,
                                                MarshalResult result)
            throws XmlException
        {
            return result.determineRuntimeBindingType(itemType, property_value);
        }

        QName getName()
        {
            throw new UnsupportedOperationException("no name");
        }


        public void fill(Object inter, Object prop_obj)
            throws XmlException
        {
            Accumulator acc = (Accumulator)inter;
            acc.append(prop_obj);
        }

        Object getValue(Object parentObject, MarshalResult result)
            throws XmlException
        {
            return Array.get(parentObject, result.getCurrIndex());
        }

        boolean isSet(Object parentObject, MarshalResult result)
            throws XmlException
        {
            throw new AssertionError("UNIMP: TODO: FIXME");
            //if (itemType.isJavaPrimitive()) return true;

            //TODO: consider isSet for array elements?

            //return getValue(parentObject, result) != null;
        }

        boolean isMultiple()
        {
            return true;
        }

        boolean isNillable()
        {
            return false;
        }

        String getLexicalDefault()
        {
            return null;
        }

    }
}
