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
import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.MethodName;
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.marshal.util.collections.Accumulator;
import org.apache.xmlbeans.impl.marshal.util.collections.AccumulatorFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;


final class ByNameRuntimeBindingType
    extends RuntimeBindingType
{
    private final ByNameBean byNameBean;
    private final Property[] attributeProperties;
    private final Property[] elementProperties;
    private final boolean hasMulti;  //has any multi properties
    private final boolean hasDefaultAttributes;  //has any attributes with defaults

    //DO NOT CALL THIS CONSTRUCTOR, use the RuntimeTypeFactory
    ByNameRuntimeBindingType(ByNameBean btype)
        throws XmlException
    {
        super(btype);
        byNameBean = btype;

        final Class java_type = getJavaType();
        if (java_type.isPrimitive() || java_type.isArray()) {
            final String msg = "invalid ByNameBean java type: " + java_type +
                " found in " + btype;
            throw new XmlException(msg);
        }

        int elem_prop_cnt = 0;
        int att_prop_cnt = 0;
        boolean has_multi = false;
        boolean has_attribute_defaults = false;
        final Collection type_props = btype.getProperties();
        for (Iterator itr = type_props.iterator(); itr.hasNext();) {
            QNameProperty p = (QNameProperty)itr.next();
            if (p.isMultiple()) has_multi = true;
            if (p.isAttribute()) {
                att_prop_cnt++;
                if (p.getDefault() != null) {
                    has_attribute_defaults = true;
                }
            } else {
                elem_prop_cnt++;
            }
        }

        attributeProperties = new Property[att_prop_cnt];
        elementProperties = new Property[elem_prop_cnt];
        hasMulti = has_multi;
        hasDefaultAttributes = has_attribute_defaults;
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader loader,
                           RuntimeTypeFactory rttFactory)
        throws XmlException
    {
        int att_idx = 0;
        int elem_idx = 0;
        for (Iterator itr = byNameBean.getProperties().iterator(); itr.hasNext();) {
            QNameProperty bprop = (QNameProperty)itr.next();
            final boolean is_att = bprop.isAttribute();

            final Property prop = new Property(is_att ? att_idx : elem_idx,
                                               getJavaType(), hasMulti, bprop,
                                               typeTable, loader, rttFactory);
            if (is_att)
                attributeProperties[att_idx++] = prop;
            else
                elementProperties[elem_idx++] = prop;

        }
    }

    Object createIntermediary(UnmarshalResult context)
    {
        if (hasMulti) {
            return new UResultHolder(this);
        } else {
            return ClassLoadingUtils.newInstance(getJavaType());
        }
    }

    Object getFinalObjectFromIntermediary(Object retval,
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

    RuntimeBindingProperty getAttributeProperty(int index)
    {
        return attributeProperties[index];
    }

    //TODO: optimize this linear scan
    RuntimeBindingProperty getMatchingElementProperty(String uri,
                                                      String localname)
    {
        for (int i = 0, len = elementProperties.length; i < len; i++) {
            final Property prop = elementProperties[i];

            if (doesPropMatch(uri, localname, prop))
                return prop;
        }
        return null;
    }

    //TODO: optimize this linear scan
    RuntimeBindingProperty getMatchingAttributeProperty(String uri,
                                                        String localname,
                                                        UnmarshalResult context)
    {
        for (int i = 0, len = attributeProperties.length; i < len; i++) {
            final Property prop = attributeProperties[i];

            if (doesPropMatch(uri, localname, prop)) {
                if (hasDefaultAttributes && (prop.defaultValue != null)) {
                    context.attributePresent(i);
                }
                return prop;
            }
        }
        return null;
    }

    private static boolean doesPropMatch(String uri,
                                         String localname,
                                         Property prop)
    {
        assert localname != null;

        final QName qn = prop.getQName();

        if (qn.getLocalPart().equals(localname)) {
            //QNames always uses "" for no namespace, but the incoming uri
            //might use null or "".
            return qn.getNamespaceURI().equals(uri == null ? "" : uri);
        }
        return false;
    }

    public int getElementPropertyCount()
    {
        return elementProperties.length;
    }

    public int getAttributePropertyCount()
    {
        return attributeProperties.length;
    }

    public void fillDefaultAttributes(Object inter, UnmarshalResult context)
        throws XmlException
    {
        if (!hasDefaultAttributes) return;

        for (int aidx = 0, alen = attributeProperties.length; aidx < alen; aidx++) {
            final Property p = attributeProperties[aidx];

            if (p.defaultValue == null) continue;
            if (context.isAttributePresent(aidx)) continue;

            p.fillDefaultValue(inter);
        }
    }


    private static final class Property implements RuntimeBindingProperty
    {
        private final int propertyIndex;
        private final Class beanClass;
        private final boolean beanHasMulti;          //consider a subclass
        private final QNameProperty bindingProperty;
        private final RuntimeBindingType runtimeBindingType;
        private final Class propertyClass;
        private final Class collectionElementClass; //null for non collections
        private final TypeUnmarshaller unmarshaller;
        private final TypeMarshaller marshaller; // used only for simple types
        private final Method getMethod;
        private final Method setMethod;
        private final Method issetMethod;
        private final Object defaultValue;

        private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

        Property(int property_index,
                 Class beanClass,
                 boolean bean_has_multis,
                 QNameProperty prop,
                 RuntimeBindingTypeTable typeTable,
                 BindingLoader loader,
                 RuntimeTypeFactory rttFactory)
            throws XmlException
        {
            if (prop.getQName() == null) {
                final String msg = "property " + property_index + " of " +
                    beanClass + " has no qname";
                throw new IllegalArgumentException(msg);
            }

            this.propertyIndex = property_index;
            this.beanClass = beanClass;
            this.beanHasMulti = bean_has_multis;
            this.bindingProperty = prop;
            this.unmarshaller = lookupUnmarshaller(prop, typeTable, loader);
            this.marshaller = lookupMarshaller(prop.getTypeName(), typeTable, loader);

            final BindingType binding_type = loader.getBindingType(prop.getTypeName());
            if (binding_type == null) {
                throw new XmlException("unable to load " + prop.getTypeName());
            }
            runtimeBindingType =
                rttFactory.createRuntimeType(binding_type, typeTable, loader);
            assert runtimeBindingType != null;

            propertyClass = getPropertyClass(prop, binding_type);
            collectionElementClass = getCollectionElementClass(prop, binding_type);
            getMethod = getGetterMethod(prop, beanClass);
            setMethod = getSetterMethod(prop, beanClass);
            issetMethod = getIssetterMethod(prop, beanClass);

            String def = bindingProperty.getDefault();
            if (def != null) {
                defaultValue = extractDefaultObject(def, binding_type,
                                                    typeTable, loader);
                if (!prop.isAttribute()) {
                    //TODO: deal with defaulting elements!
                    System.out.println("Default elements not supported: " + this);
                }
            } else {
                defaultValue = null;
            }
        }


        //REVIEW: find a shorter path to our goal.
        private static Object extractDefaultObject(String value,
                                                   BindingType bindingType,
                                                   RuntimeBindingTypeTable typeTable,
                                                   BindingLoader loader)
            throws XmlException
        {
            final String xmldoc = "<a>" + value + "</a>";
            try {
                final UnmarshallerImpl um = new UnmarshallerImpl(loader, typeTable);
                final StringReader sr = new StringReader(xmldoc);
                final XMLStreamReader reader =
                    um.getXmlInputFactory().createXMLStreamReader(sr);
                boolean ok =
                    MarshalStreamUtils.advanceToNextStartElement(reader);
                assert ok;
                final BindingTypeName btname = bindingType.getName();
                final Object obj =
                    um.unmarshalType(reader, btname.getXmlName().getQName(),
                                     btname.getJavaName().toString());
                reader.close();
                sr.close();
                return obj;
            }
            catch (XMLStreamException e) {
                throw new XmlException(e);
            }
        }

        private Class getPropertyClass(QNameProperty prop, BindingType btype)
            throws XmlException
        {
            assert btype != null;

            final Class propertyClass;
            try {
                final ClassLoader our_cl = getClass().getClassLoader();
                final JavaTypeName collectionClass = prop.getCollectionClass();

                if (collectionClass == null) {
                    propertyClass = getJavaClass(btype, our_cl);
                } else {
                    final String col = collectionClass.toString();
                    propertyClass = ClassLoadingUtils.loadClass(col, our_cl);
                }
            }
            catch (ClassNotFoundException ex) {
                throw new XmlException(ex);
            }
            return propertyClass;
        }


        private Class getCollectionElementClass(QNameProperty prop,
                                                BindingType btype)
            throws XmlException
        {
            assert btype != null;

            try {
                final JavaTypeName collectionClass = prop.getCollectionClass();

                if (collectionClass == null) {
                    return null;
                } else {
                    final ClassLoader our_cl = getClass().getClassLoader();
                    return getJavaClass(btype, our_cl);
                }
            }
            catch (ClassNotFoundException ex) {
                throw new XmlException(ex);
            }
        }


        public BindingType getType()
        {
            return getRuntimeBindingType().getBindingType();
        }

        public RuntimeBindingType getRuntimeBindingType()
        {
            return runtimeBindingType;
        }

        public RuntimeBindingType getActualRuntimeType(Object property_value,
                                                       MarshalResult result)
            throws XmlException
        {
            return MarshalResult.findActualRuntimeType(property_value,
                                                       runtimeBindingType,
                                                       result);
        }

        public QName getName()
        {
            return bindingProperty.getQName();
        }

        private TypeUnmarshaller lookupUnmarshaller(BindingProperty prop,
                                                    RuntimeBindingTypeTable table,
                                                    BindingLoader loader)
            throws XmlException
        {
            assert prop != null;
            final BindingTypeName type_name = prop.getTypeName();
            assert type_name != null;
            final BindingType binding_type = loader.getBindingType(type_name);
            if (binding_type == null) {
                throw new XmlException("failed to load type: " + type_name);
            }

            TypeUnmarshaller um =
                table.getOrCreateTypeUnmarshaller(binding_type, loader);
            if (um == null) {
                throw new AssertionError("failed to get unmarshaller for " +
                                         type_name);
            }
            return um;
        }

        private TypeMarshaller lookupMarshaller(BindingTypeName type_name,
                                                RuntimeBindingTypeTable typeTable,
                                                BindingLoader loader)
            throws XmlException
        {
            final BindingType binding_type = loader.getBindingType(type_name);
            if (binding_type == null) {
                final String msg = "unable to load type for " + type_name;
                throw new XmlException(msg);
            }
            TypeMarshaller m = typeTable.getTypeMarshaller(binding_type);
            if (m != null) return m;

            if (binding_type instanceof SimpleBindingType) {
                SimpleBindingType stype = (SimpleBindingType)binding_type;

                final BindingTypeName asif_name = stype.getAsIfBindingTypeName();
                if (asif_name == null)
                    throw new XmlException("no asif for " + stype);

                return lookupMarshaller(asif_name, typeTable, loader);
            }

            return null;
        }


        public TypeUnmarshaller getTypeUnmarshaller(UnmarshalResult context)
            throws XmlException
        {
            //don't need any xsi stuff for attributes.
            if (bindingProperty.isAttribute()) return unmarshaller;

            final QName xsi_type = context.getXsiType();

            if (xsi_type != null) {
                TypeUnmarshaller typed_um = context.getTypeUnmarshaller(xsi_type);
                if (typed_um != null)
                    return typed_um;
                //reaching here means some problem with extracting the
                //marshaller for the xsi type, so just use the expected one
            }

            if (context.hasXsiNil())
                return NullUnmarshaller.getInstance();

            return unmarshaller;
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
                    invokeMethod(rh.getValue(), setMethod,
                                 new Object[]{prop_obj});
                }
            } else {
                invokeMethod(inter, setMethod, new Object[]{prop_obj});
            }
        }

        public void fillDefaultValue(Object inter)
            throws XmlException
        {
            assert (defaultValue != null);

            this.fill(inter, defaultValue);
        }

        public void fillCollection(final Object inter, final Object prop_obj)
            throws XmlException
        {
            assert isMultiple();
            invokeMethod(inter, setMethod, new Object[]{prop_obj});
        }

        public CharSequence getLexical(Object value, MarshalResult result)
            throws XmlException
        {
            assert value != null :
                "null value for " + bindingProperty + " class=" + beanClass;

            assert  result != null :
                "null value for " + bindingProperty + " class=" + beanClass;

            assert marshaller != null :
                "null marshaller for prop=" + bindingProperty + " class=" +
                beanClass + " propType=" + bindingProperty.getTypeName();

            return marshaller.print(value, result);
        }

        public Object getValue(Object parentObject, MarshalResult result)
            throws XmlException
        {
            assert parentObject != null;
            assert beanClass.isAssignableFrom(parentObject.getClass()) :
                parentObject.getClass() + " is not a " + beanClass;

            return invokeMethod(parentObject, getMethod, EMPTY_OBJECT_ARRAY);
        }

        public boolean isSet(Object parentObject, MarshalResult result)
            throws XmlException
        {
            if (issetMethod == null)
                return isSetFallback(parentObject, result);

            final Boolean isset =
                (Boolean)invokeMethod(parentObject, issetMethod,
                                      EMPTY_OBJECT_ARRAY);
            return isset.booleanValue();
        }

        private static Object invokeMethod(Object target,
                                           Method method,
                                           Object[] params)
            throws XmlException
        {
            try {
                return method.invoke(target, params);
            }
            catch (IllegalAccessException e) {
                throw new XmlException(e);
            }
            catch (IllegalArgumentException e) {
                throw new XmlException(e);
            }
            catch (InvocationTargetException e) {
                throw new XmlException(e);
            }
        }

        private boolean isSetFallback(Object parentObject, MarshalResult result)
            throws XmlException
        {
            //REVIEW: nillable is winning over minOccurs="0".  Is this correct?
            if (bindingProperty.isNillable())
                return true;

            Object val = getValue(parentObject, result);
            return (val != null);
        }

        public boolean isMultiple()
        {
            return bindingProperty.isMultiple();
        }

        public boolean isNillable()
        {
            return bindingProperty.isNillable();
        }

        private static Method getSetterMethod(QNameProperty binding_prop,
                                              Class beanClass)
            throws XmlException
        {
            if (!binding_prop.hasSetter()) return null;

            MethodName setterName = binding_prop.getSetterName();
            return getMethodOnClass(setterName, beanClass);
        }

        private static Method getIssetterMethod(QNameProperty binding_prop,
                                                Class clazz)
            throws XmlException
        {
            if (!binding_prop.hasIssetter())
                return null;

            Method isset_method =
                getMethodOnClass(binding_prop.getIssetterName(), clazz);

            if (!isset_method.getReturnType().equals(Boolean.TYPE)) {
                String msg = "invalid isset method: " + isset_method +
                    " -- return type must be boolean not " +
                    isset_method.getReturnType().getName();
                throw new XmlException(msg);
            }

            return isset_method;
        }


        private static Method getGetterMethod(QNameProperty binding_prop,
                                              Class beanClass)
            throws XmlException
        {
            MethodName getterName = binding_prop.getGetterName();
            return getMethodOnClass(getterName, beanClass);
        }


        private static Method getMethodOnClass(MethodName method_name,
                                               Class clazz)
            throws XmlException
        {
            try {
                return method_name.getMethodOn(clazz);
            }
            catch (NoSuchMethodException e) {
                throw new XmlException(e);
            }
            catch (SecurityException se) {
                throw new XmlException(se);
            }
            catch (ClassNotFoundException cnfe) {
                throw new XmlException(cnfe);
            }
        }


        QName getQName()
        {
            return bindingProperty.getQName();
        }


    }


    private static final class UResultHolder
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
                final Property[] props = runtimeBindingType.elementProperties;
                for (int i = 0, len = accumulators.length; i < len; i++) {
                    final Accumulator accum = accumulators[i];
                    if (accum != null) {
                        final Property prop = props[i];
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
                final Property p = runtimeBindingType.elementProperties[elem_idx];
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
