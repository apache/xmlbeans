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


    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader loader
                           )
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
                    new AttributeQNameProperty(
                        getJavaType(), hasMulti(),
                        bprop, this,
                        typeTable, loader);
                initAttributeProperty(aprop, att_idx++);
            } else {
                initElementProperty(bprop, elem_idx++,
                                    typeTable, loader
                );
            }

        }
    }

    protected abstract void initElementProperty(final QNameProperty prop,
                                                int elem_idx,
                                                RuntimeBindingTypeTable typeTable,
                                                BindingLoader loader)
        throws XmlException;

    private void initAttributeProperty(final AttributeQNameProperty prop,
                                       int att_idx)
    {
        attributeProperties[att_idx] = prop;
    }

    protected abstract Collection getQNameProperties();

    //some subclass will certainly need to override this
    protected Object createIntermediary(UnmarshalResult context,
                                        Object actual_object)
    {
        return actual_object;
    }

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
            final QNameRuntimeProperty prop = attributeProperties[i];

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
                                         QNameRuntimeProperty prop)
    {
        assert localname != null;

        final QName qn = prop.getQName();

        return UnmarshalResult.doesElementMatch(qn, localname, uri);
    }

    abstract int getElementPropertyCount();

    final int getAttributePropertyCount()
    {
        return attributeProperties.length;
    }

    final void fillDefaultAttributes(Object inter,
                                     UnmarshalResult context)
        throws XmlException
    {
        if (!hasDefaultAttributes) return;

        for (int aidx = 0, alen = attributeProperties.length; aidx < alen; aidx++) {
            final QNameRuntimeProperty p = attributeProperties[aidx];

            if (p.typedDefaultValue == null) continue;
            if (context.isAttributePresent(aidx)) continue;

            p.fillDefaultValue(inter);
        }
    }

    protected abstract boolean hasMulti();

    protected static final class AttributeQNameProperty
        extends QNameRuntimeProperty
    {
        AttributeQNameProperty(Class beanClass,
                               boolean bean_has_multis,
                               QNameProperty prop,
                               RuntimeBindingType containing_type,
                               RuntimeBindingTypeTable typeTable,
                               BindingLoader loader)
            throws XmlException
        {
            super(beanClass, bean_has_multis,
                  prop, containing_type, typeTable, loader
            );
            assert prop.isAttribute();
        }

    }


    protected static abstract class QNameRuntimeProperty
        extends BeanRuntimeProperty
    {
        protected final boolean beanHasMulti;          //consider a subclass
        protected final QNameProperty bindingProperty;
        protected final String lexicalDefaultValue;
        protected final Object typedDefaultValue;

        QNameRuntimeProperty(Class beanClass,
                             boolean bean_has_multis,
                             QNameProperty prop,
                             RuntimeBindingType containing_type,
                             RuntimeBindingTypeTable typeTable,
                             BindingLoader loader)
            throws XmlException
        {
            super(beanClass, prop, containing_type, typeTable, loader
            );

            if (prop.getQName() == null) {
                final String msg = "property " + prop + " of " +
                    beanClass + " has no qname";
                throw new IllegalArgumentException(msg);
            }

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


        final QName getName()
        {
            return bindingProperty.getQName();
        }


        final void fillDefaultValue(Object inter)
            throws XmlException
        {
            assert (typedDefaultValue != null);

            this.fill(inter, typedDefaultValue);
        }

        final void fillCollection(final Object inter,
                                  final Object prop_obj)
            throws XmlException
        {
            assert isMultiple();
            setValue(inter, prop_obj);
        }

        final boolean isMultiple()
        {
            return bindingProperty.isMultiple();
        }

        final boolean isNillable()
        {
            return bindingProperty.isNillable();
        }

        final String getLexicalDefault()
        {
            return lexicalDefaultValue;
        }

        final QName getQName()
        {
            return bindingProperty.getQName();
        }
    }


}
