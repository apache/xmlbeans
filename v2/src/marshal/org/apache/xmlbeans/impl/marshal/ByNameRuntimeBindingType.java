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
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;
import org.apache.xmlbeans.impl.marshal.util.collections.Accumulator;
import org.apache.xmlbeans.impl.marshal.util.collections.AccumulatorFactory;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;


final class ByNameRuntimeBindingType
    extends AttributeRuntimeBindingType
{
    private final ElementQNameProperty[] elementProperties;
    private final boolean hasMulti;  //has any multi properties

    //DO NOT CALL THIS CONSTRUCTOR, use the RuntimeTypeFactory
    ByNameRuntimeBindingType(ByNameBean btype)
        throws XmlException
    {
        super(btype);

        final Class java_type = getJavaType();
        if (java_type.isPrimitive() || java_type.isArray()) {
            final String msg = "invalid ByNameBean java type: " + java_type +
                " found in " + btype;
            throw new XmlException(msg);
        }

        int elem_prop_cnt = 0;
        boolean has_multi = false;
        final Collection type_props = getQNameProperties();
        for (Iterator itr = type_props.iterator(); itr.hasNext();) {
            QNameProperty p =
                (QNameProperty)itr.next();
            if (p.isAttribute()) continue;
            if (p.isMultiple()) has_multi = true;
            elem_prop_cnt++;
        }

        elementProperties = new ElementQNameProperty[elem_prop_cnt];
        hasMulti = has_multi;
    }


    public Object getObjectFromIntermediate(Object inter)
    {
        if (hasMulti()) {
            UResultHolder res = (UResultHolder)inter;
            return res.getValue();
        } else {
            return inter;
        }
    }


    protected Collection getQNameProperties()
    {
        ByNameBean narrowed_type = (ByNameBean)getBindingType();
        return narrowed_type.getProperties();
    }


    protected void initElementProperty(QNameProperty prop,
                                       int elem_idx,
                                       RuntimeBindingTypeTable typeTable,
                                       BindingLoader loader,
                                       RuntimeTypeFactory rttFactory)
        throws XmlException
    {
        elementProperties[elem_idx] =
            new ElementQNameProperty(elem_idx, getJavaType(), hasMulti(), prop,
                                     this, typeTable, loader, rttFactory);
    }

    protected Object createIntermediary(UnmarshalResult context)
    {
        if (hasMulti) {
            return new UResultHolder(this);
        } else {
            return ClassLoadingUtils.newInstance(getJavaType());
        }
    }

    protected Object getFinalObjectFromIntermediary(Object retval,
                                                    UnmarshalResult context)
        throws XmlException
    {
        if (hasMulti) {
            UResultHolder rh = (UResultHolder)retval;
            return rh.getFinalValue();
        } else {
            return retval;
        }
    }

    RuntimeBindingProperty getElementProperty(int index)
    {
        return elementProperties[index];
    }

    //TODO: optimize this linear scan
    RuntimeBindingProperty getMatchingElementProperty(String uri,
                                                      String localname)
    {
        for (int i = 0, len = elementProperties.length; i < len; i++) {
            final ElementQNameProperty prop = elementProperties[i];

            if (doesPropMatch(uri, localname, prop))
                return prop;
        }
        return null;
    }


    private static boolean doesPropMatch(String uri,
                                         String localname,
                                         QNamePropertyBase prop)
    {
        assert localname != null;

        final QName qn = prop.getQName();

        return UnmarshalResult.doesElementMatch(qn, localname, uri);
    }

    public int getElementPropertyCount()
    {
        return elementProperties.length;
    }

    protected boolean hasMulti()
    {
        return hasMulti;
    }


    protected static final class ElementQNameProperty
        extends QNamePropertyBase
    {
        protected final int propertyIndex;

        ElementQNameProperty(int property_index,
                             Class beanClass,
                             boolean bean_has_multis,
                             QNameProperty prop,
                             IntermediateResolver intermediateResolver,
                             RuntimeBindingTypeTable typeTable,
                             BindingLoader loader,
                             RuntimeTypeFactory rttFactory)
            throws XmlException
        {
            super(beanClass, bean_has_multis,
                  prop, intermediateResolver, typeTable, loader, rttFactory);
            propertyIndex = property_index;
            assert !prop.isAttribute();
        }


        public void fill(final Object inter, final Object prop_obj)
            throws XmlException
        {
            //means xsi:nil was true but we're a primtive.
            //schema should have nillable="false" so this
            //is a validation problem
            if (prop_obj == null && runtimeBindingType.isJavaPrimitive())
                return;

            if (beanHasMulti) {
                final UResultHolder rh = (UResultHolder)inter;

                if (isMultiple()) {
                    rh.addItem(propertyIndex, prop_obj);
                } else {
                    ReflectionUtils.invokeMethod(rh.getValue(), setMethod,
                                 new Object[]{prop_obj});
                }
            } else {
                ReflectionUtils.invokeMethod(inter, setMethod, new Object[]{prop_obj});
            }
        }

    }


    protected static final class UResultHolder
    {
        private final ByNameRuntimeBindingType runtimeBindingType;
        private final Object value;
        private Accumulator[] accumulators;

        UResultHolder(ByNameRuntimeBindingType type)
        {
            runtimeBindingType = type;
            value = ClassLoadingUtils.newInstance(type.getJavaType());
        }


        Object getFinalValue() throws XmlException
        {
            if (accumulators != null) {
                final QNamePropertyBase[] props = runtimeBindingType.elementProperties;
                for (int i = 0, len = accumulators.length; i < len; i++) {
                    final Accumulator accum = accumulators[i];
                    if (accum != null) {
                        final QNamePropertyBase prop = props[i];
                        prop.fillCollection(value, accum.getFinalArray());
                    }
                }
            }
            return value;
        }

        void addItem(int elem_idx, Object value)
        {
            initAccumulator(elem_idx);
            accumulators[elem_idx].append(value);
        }

        private void initAccumulator(int elem_idx)
        {
            Accumulator[] accs = accumulators;
            if (accs == null) {
                accs = new Accumulator[runtimeBindingType.getElementPropertyCount()];
                accumulators = accs;
            }
            if (accs[elem_idx] == null) {
                final QNamePropertyBase p = runtimeBindingType.elementProperties[elem_idx];
                accs[elem_idx] =
                    AccumulatorFactory.createAccumulator(p.propertyClass,
                                                         p.collectionElementClass);
            }
        }

        public Object getValue()
        {
            return value;
        }

    }


}
