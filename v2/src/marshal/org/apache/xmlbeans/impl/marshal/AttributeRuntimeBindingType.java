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
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;

/**
 * base type for complex types with attributes (with QNameProperties)
 */
abstract class AttributeRuntimeBindingType
    extends RuntimeBindingType
    implements IntermediateResolver
{
    private final AttributeQNameProperty[] attributeProperties;
    private final boolean hasDefaultAttributes;  //has any attributes with defaults

    AttributeRuntimeBindingType(BindingType btype)
        throws XmlException
    {
        super(btype);

        final Class java_type = getJavaType();
        if (java_type.isPrimitive() || java_type.isArray()) {
            final String msg = "invalid ByNameBean java type: " + java_type +
                " found in " + btype;
            throw new XmlException(msg);
        }

        int att_prop_cnt = 0;
        boolean has_attribute_defaults = false;
        final Collection type_props = getQNameProperties();
        for (Iterator itr = type_props.iterator(); itr.hasNext();) {
            QNameProperty p =
                (QNameProperty)itr.next();
            if (p.isAttribute()) {
                att_prop_cnt++;
                if (p.getDefault() != null) {
                    has_attribute_defaults = true;
                }
            }
        }

        attributeProperties = new AttributeQNameProperty[att_prop_cnt];
        hasDefaultAttributes = has_attribute_defaults;
    }

    public Object getObjectFromIntermediate(Object inter)
    {
        return inter;
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader loader,
                           RuntimeTypeFactory rttFactory)
        throws XmlException
    {
        int att_idx = 0;
        int elem_idx = 0;
        Collection properties = getQNameProperties();
        for (Iterator itr = properties.iterator(); itr.hasNext();) {
            QNameProperty bprop = (QNameProperty)itr.next();
            final boolean is_att = bprop.isAttribute();

            if (is_att) {
                final AttributeQNameProperty aprop =
                    new AttributeQNameProperty(att_idx,
                                               getJavaType(), hasMulti(),
                                               bprop, this,
                                               typeTable, loader, rttFactory);
                initAttributeProperty(aprop, att_idx++);
            } else {
                initElementProperty(bprop, elem_idx++,
                                    typeTable, loader, rttFactory);
            }

        }
    }

    protected abstract void initElementProperty(final QNameProperty prop,
                                                int elem_idx,
                                                RuntimeBindingTypeTable typeTable,
                                                BindingLoader loader,
                                                RuntimeTypeFactory rttFactory)
        throws XmlException;

    private void initAttributeProperty(final AttributeQNameProperty prop,
                                       int att_idx)
    {
        attributeProperties[att_idx] = prop;
    }

    protected abstract Collection getQNameProperties();

    protected abstract Object createIntermediary(UnmarshalResult context);

    protected abstract Object getFinalObjectFromIntermediary(Object retval,
                                                             UnmarshalResult context)
        throws XmlException;


    final RuntimeBindingProperty getAttributeProperty(int index)
    {
        return attributeProperties[index];
    }

    //TODO: optimize this linear scan
    final RuntimeBindingProperty getMatchingAttributeProperty(String uri,
                                                              String localname,
                                                              UnmarshalResult context)
    {
        for (int i = 0, len = attributeProperties.length; i < len; i++) {
            final QNamePropertyBase prop = attributeProperties[i];

            if (doesPropMatch(uri, localname, prop)) {
                if (hasDefaultAttributes && (prop.typedDefaultValue != null)) {
                    context.attributePresent(i);
                }
                return prop;
            }
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

    public abstract int getElementPropertyCount();

    public final int getAttributePropertyCount()
    {
        return attributeProperties.length;
    }

    public final void fillDefaultAttributes(Object inter,
                                            UnmarshalResult context)
        throws XmlException
    {
        if (!hasDefaultAttributes) return;

        for (int aidx = 0, alen = attributeProperties.length; aidx < alen; aidx++) {
            final QNamePropertyBase p = attributeProperties[aidx];

            if (p.typedDefaultValue == null) continue;
            if (context.isAttributePresent(aidx)) continue;

            p.fillDefaultValue(inter);
        }
    }

    protected abstract boolean hasMulti();

    protected static final class AttributeQNameProperty
        extends QNamePropertyBase
    {
        AttributeQNameProperty(int property_index,
                               Class beanClass,
                               boolean bean_has_multis,
                               QNameProperty prop,
                               IntermediateResolver intermediateResolver,
                               RuntimeBindingTypeTable typeTable,
                               BindingLoader loader,
                               RuntimeTypeFactory rttFactory)
            throws XmlException
        {
            super(property_index, beanClass, bean_has_multis,
                  prop, intermediateResolver, typeTable, loader, rttFactory);
            assert prop.isAttribute();
        }


        public TypeUnmarshaller getTypeUnmarshaller(UnmarshalResult context)
            throws XmlException
        {
            assert bindingProperty.isAttribute();
            //don't need any xsi stuff for attributes.
            return unmarshaller;
        }
    }


    protected static abstract class QNamePropertyBase
        extends RuntimePropertyBase
    {
        //TODO: push index down to element subclass
        protected final int propertyIndex;
        protected final boolean beanHasMulti;          //consider a subclass
        protected final QNameProperty bindingProperty;
        protected final String lexicalDefaultValue;
        protected final Object typedDefaultValue;

        QNamePropertyBase(int property_index,
                          Class beanClass,
                          boolean bean_has_multis,
                          QNameProperty prop,
                          IntermediateResolver intermediateResolver,
                          RuntimeBindingTypeTable typeTable,
                          BindingLoader loader,
                          RuntimeTypeFactory rttFactory)
            throws XmlException
        {
            super(beanClass, prop, intermediateResolver, typeTable, loader, rttFactory);

            if (prop.getQName() == null) {
                final String msg = "property " + property_index + " of " +
                    beanClass + " has no qname";
                throw new IllegalArgumentException(msg);
            }

            this.propertyIndex = property_index;
            this.beanHasMulti = bean_has_multis;
            this.bindingProperty = prop;

            lexicalDefaultValue = bindingProperty.getDefault();
            if (lexicalDefaultValue != null) {
                typedDefaultValue = extractDefaultObject(lexicalDefaultValue,
                                                         runtimeBindingType.getBindingType(),
                                                         typeTable, loader);
            } else {
                typedDefaultValue = null;
            }
        }


        public final QName getName()
        {
            return bindingProperty.getQName();
        }


        public final void fillDefaultValue(Object inter)
            throws XmlException
        {
            assert (typedDefaultValue != null);

            this.fill(inter, typedDefaultValue);
        }

        public final void fillCollection(final Object inter,
                                         final Object prop_obj)
            throws XmlException
        {
            assert isMultiple();
            ReflectionUtils.invokeMethod(inter, setMethod, new Object[]{prop_obj});
        }

        public final boolean isMultiple()
        {
            return bindingProperty.isMultiple();
        }

        public final boolean isNillable()
        {
            return bindingProperty.isNillable();
        }

        public final String getLexicalDefault()
        {
            return lexicalDefaultValue;
        }

        final QName getQName()
        {
            return bindingProperty.getQName();
        }
    }


}
