/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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

    void initialize(RuntimeBindingTypeTable typeTable,
                    BindingLoader bindingLoader,
                    RuntimeTypeFactory rttFactory)
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
            rttFactory.createRuntimeType(item_type, typeTable, bindingLoader);

        itemProperty = new LAProperty(this, item_rtt, typeTable, bindingLoader);
    }


    RuntimeBindingProperty getItemProperty() {
        assert itemProperty != null;
        return itemProperty;
    }

    private static final class LAProperty
        extends RuntimeBindingProperty
    {
        private final RuntimeBindingType itemType;
        private final TypeMarshaller marshaller; // used only for simple types
        private final TypeUnmarshaller unmarshaller;

        LAProperty(RuntimeBindingType containing_type,
                   RuntimeBindingType item_type,
                   RuntimeBindingTypeTable type_table,
                   BindingLoader loader)
            throws XmlException
        {
            super(containing_type);

            itemType = item_type;

            final BindingType binding_type = item_type.getBindingType();
            marshaller =
                type_table.lookupMarshaller(binding_type, loader);
            unmarshaller =
                type_table.lookupUnmarshaller(binding_type, loader);

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
            return MarshalResult.findActualRuntimeType(property_value,
                                                       itemType,
                                                       result);
        }

        QName getName()
        {
            throw new UnsupportedOperationException("no name");
        }

        public TypeUnmarshaller getTypeUnmarshaller(UnmarshalResult context)
            throws XmlException
        {
            return context.determineTypeUnmarshaller(unmarshaller);
        }

        public void fill(Object inter, Object prop_obj)
            throws XmlException
        {
            Accumulator acc = (Accumulator)inter;
            acc.append(prop_obj);
        }

        //non simple type props can throw some runtime exception.
        CharSequence getLexical(Object value, MarshalResult result)
            throws XmlException
        {
            assert value != null;
            assert  result != null;
            assert marshaller != null;

            return marshaller.print(value, result);
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
