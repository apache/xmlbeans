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

import org.apache.xmlbeans.XmlRuntimeException;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;


final class ByNameRuntimeBindingType
    implements RuntimeBindingType
{
    private final ByNameBean byNameBean;
    private final Property[] properties;
    private final Class javaClass;
    private final boolean hasMulti;  //has any multi properties

    //is this a subtype of something besides the ultimate parent type?
    //(XmlObject or java.lang.Object, though only the latter
    //is currently considered)
    private final boolean isSubType;


    ByNameRuntimeBindingType(ByNameBean btype)
    {
        byNameBean = btype;
        try {
            javaClass = getJavaClass(btype, getClass().getClassLoader());
        }
        catch (ClassNotFoundException e) {
            final String msg = "failed to load " + btype.getName().getJavaName();
            throw new XmlRuntimeException(msg, e);
        }

        properties = new Property[btype.getProperties().size()];
        hasMulti = hasMulti(btype);

        isSubType = determineIsSubType(javaClass);
    }

    private static boolean determineIsSubType(Class javaClass)
    {
        int cnt = 0;
        for (Class p = javaClass.getSuperclass(); p != null; p = p.getSuperclass()) {
            if (cnt > 0) return true;
            cnt++;
        }
        return false;
    }

    private static boolean hasMulti(ByNameBean btype)
    {
        for (Iterator itr = btype.getProperties().iterator(); itr.hasNext();) {
            QNameProperty bprop = (QNameProperty)itr.next();
            if (bprop.isMultiple())
                return true;
        }
        return false;
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader loader)
    {
        int idx = 0;
        for (Iterator itr = byNameBean.getProperties().iterator(); itr.hasNext();) {
            QNameProperty bprop = (QNameProperty)itr.next();
            Property prop = new Property(idx, javaClass, hasMulti, bprop, typeTable, loader);
            properties[idx++] = prop;
        }
    }

    Object createIntermediary(UnmarshallerImpl context)
    {
        if (hasMulti) {
            return new UResultHolder(this);
        } else {
            return ClassLoadingUtils.newInstance(javaClass);
        }
    }

    Object getFinalObjectFromIntermediary(Object retval,
                                          UnmarshallerImpl context)
    {
        if (hasMulti) {
            UResultHolder rh = (UResultHolder)retval;
            return rh.getFinalValue();
        } else {
            return retval;
        }
    }

    private static Class getJavaClass(BindingType btype, ClassLoader backup)
        throws ClassNotFoundException
    {
        final JavaTypeName javaName = btype.getName().getJavaName();
        String jclass = javaName.toString();
        return ClassLoadingUtils.loadClass(jclass, backup);
    }


    RuntimeBindingProperty getProperty(int index)
    {
        return properties[index];
    }

    //TODO: optimize this linear scan
    RuntimeBindingProperty getMatchingElementProperty(String uri,
                                                      String localname)
    {
        for (int i = 0, len = properties.length; i < len; i++) {
            final Property prop = properties[i];
            if (prop.isAttribute()) continue;

            QName qn = prop.getQName();
            if (qn.getLocalPart().equals(localname) &&
                qn.getNamespaceURI().equals(uri)) {
                return prop;
            }
        }
        return null;
    }

    //TODO: optimize this linear scan
    RuntimeBindingProperty getMatchingAttributeProperty(String uri,
                                                        String localname)
    {
        for (int i = 0, len = properties.length; i < len; i++) {
            final Property prop = properties[i];
            if (!prop.isAttribute()) continue;

            QName qn = prop.getQName();
            if (qn.getLocalPart().equals(localname) &&
                qn.getNamespaceURI().equals(uri)) {
                return prop;
            }
        }
        return null;
    }

    public int getPropertyCount()
    {
        return properties.length;
    }

    public boolean isSubType()
    {
        return isSubType;
    }

    public QName getSchemaTypeName()
    {
        return byNameBean.getName().getXmlName().getQName();
    }


    private static final class Property implements RuntimeBindingProperty
    {
        private final int propertyIndex;
        private final Class beanClass;
        private final boolean beanHasMulti;          //consider a subclass
        private final QNameProperty bindingProperty;
        private final BindingType bindingType;
        private final Class propertyClass;
        private final Class collectionElementClass; //null for non collections
        private final TypeUnmarshaller unmarshaller;
        private final TypeMarshaller marshaller; // used only for simple types
        private final Method getMethod;
        private final Method setMethod;
        private final boolean javaPrimitive;

        private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

        Property(int property_index,
                 Class beanClass,
                 boolean bean_has_multis,
                 QNameProperty prop,
                 RuntimeBindingTypeTable typeTable,
                 BindingLoader loader)
        {
            this.propertyIndex = property_index;
            this.beanClass = beanClass;
            this.beanHasMulti = bean_has_multis;
            this.bindingProperty = prop;
            this.unmarshaller = lookupUnmarshaller(prop, typeTable, loader);
            this.marshaller = lookupMarshaller(prop, typeTable, loader);
            this.bindingType = loader.getBindingType(prop.getTypeName());
            propertyClass = getPropertyClass(prop, bindingType);
            collectionElementClass = getCollectionElementClass(prop, bindingType);
            getMethod = getGetterMethod(prop, beanClass);
            setMethod = getSetterMethod(prop, beanClass);
            javaPrimitive = propertyClass.isPrimitive();
        }

        private Class getPropertyClass(QNameProperty prop, BindingType btype)
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
                final String s = "error loading " +
                    btype.getName().getJavaName();
                throw (RuntimeException)(new RuntimeException(s).initCause(ex));
            }
            return propertyClass;
        }


        private Class getCollectionElementClass(QNameProperty prop,
                                                BindingType btype)
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
                final String s = "error loading " +
                    btype.getName().getJavaName();
                throw (RuntimeException)(new RuntimeException(s).initCause(ex));
            }
        }


        public BindingType getType()
        {
            return bindingType;
        }

        public QName getName()
        {
            return bindingProperty.getQName();
        }

        private TypeUnmarshaller lookupUnmarshaller(BindingProperty prop,
                                                    RuntimeBindingTypeTable typeTable,
                                                    BindingLoader bindingLoader)
        {
            assert prop != null;
            final BindingTypeName type_name = prop.getTypeName();
            assert type_name != null;
            final BindingType binding_type =
                bindingLoader.getBindingType(type_name);
            if (binding_type == null) {
                throw new XmlRuntimeException("failed to load type: " +
                                              type_name);
            }

            TypeUnmarshaller um = typeTable.getTypeUnmarshaller(binding_type);
            if (um == null) {
                throw new AssertionError("failed to get unmarshaller for " +
                                         type_name);
            }
            return um;
        }

        private TypeMarshaller lookupMarshaller(BindingProperty prop,
                                                RuntimeBindingTypeTable typeTable,
                                                BindingLoader loader)
        {
            final BindingType bindingType =
                loader.getBindingType(prop.getTypeName());
            TypeMarshaller m = typeTable.getTypeMarshaller(bindingType);

            if (m == null) {
                if (bindingType instanceof SimpleBindingType) {
                    SimpleBindingType stype = (SimpleBindingType)bindingType;

                    //let's try using the as if type
                    final BindingTypeName asif_name = stype.getAsIfBindingTypeName();
                    assert asif_name != null : "no asif for " + stype;
                    BindingType asif = loader.getBindingType(asif_name);
                    if (asif == null) {
                        throw new AssertionError("unable to get asif type" +
                                                 " for " + asif_name);
                    }
                    m = typeTable.getTypeMarshaller(asif);

                    if (m == null) {
                        final String msg = "asif type marshaller not found" +
                            " for" + stype + " asif=" + asif;
                        throw new AssertionError(msg);
                    }
                }
            }

            return m;
        }


        public TypeUnmarshaller getTypeUnmarshaller(UnmarshallerImpl context)
        {
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
        {
            //means xsi:nil was true but we're a primtive.
            //schema should have nillable="false" so this
            //is a validation problems
            if (prop_obj == null && javaPrimitive)
                return;

            try {
                if (beanHasMulti) {
                    final UResultHolder rh = (UResultHolder)inter;

                    if (isMultiple()) {
                        rh.addItem(propertyIndex, prop_obj);
                    } else {
                        setMethod.invoke(rh.getValue(), new Object[]{prop_obj});
                    }
                } else {
                    setMethod.invoke(inter, new Object[]{prop_obj});
                }
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
            }
            catch (IllegalAccessException e) {
                throw new XmlRuntimeException(e);
            }
            catch (InvocationTargetException e) {
                throw new XmlRuntimeException(e);
            }
        }

        public void fillCollection(final Object inter, final Object prop_obj)
        {
            assert isMultiple();
            try {
                setMethod.invoke(inter, new Object[]{prop_obj});
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
            }
            catch (IllegalAccessException e) {
                throw new XmlRuntimeException(e);
            }
            catch (InvocationTargetException e) {
                throw new XmlRuntimeException(e);
            }
        }

        //non simple type props can throw some runtime exception.
        public CharSequence getLexical(Object value, MarshallerImpl context)
        {

            //TODO: after marshalling table is refactored
            //turn these into assertions   zieg Dec 19 2003.

            if (value == null) {
                throw new AssertionError("null value for " + bindingProperty +
                                         " class=" + beanClass);
            }

            if (context == null) {
                throw new AssertionError("null value for " + bindingProperty +
                                         " class=" + beanClass);
            }

            if (marshaller == null) {
                String msg = "null marshaller for prop=" + bindingProperty +
                    " class=" + beanClass + " propType=" +
                    bindingProperty.getTypeName();
                throw new AssertionError(msg);
            }

            return marshaller.print(value, context);
        }

        public Object getValue(Object parentObject, MarshallerImpl context)
        {
            assert parentObject != null;
            assert beanClass.isAssignableFrom(parentObject.getClass()) :
                parentObject.getClass() + " is not a " + beanClass;
            try {
                return getMethod.invoke(parentObject, EMPTY_OBJECT_ARRAY);
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
            }
            catch (IllegalAccessException e) {
                throw new XmlRuntimeException(e);
            }
            catch (InvocationTargetException e) {
                throw new XmlRuntimeException(e);
            }
        }

        //TODO: check isSet methods
        public boolean isSet(Object parentObject, MarshallerImpl context)
        {
            if (bindingProperty.isNillable())
                return true;

            Object val = getValue(parentObject, context);
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
        {
            MethodName setterName = binding_prop.getSetterName();
            try {
                final Method set_method = setterName.getMethodOn(beanClass);
                return set_method;
            }
            catch (NoSuchMethodException e) {
                throw new XmlRuntimeException(e);
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
            }
            catch (ClassNotFoundException cnfe) {
                throw new XmlRuntimeException(cnfe);
            }
        }


        private static Method getGetterMethod(QNameProperty binding_prop,
                                              Class beanClass)
        {
            MethodName getterName = binding_prop.getGetterName();
            try {
                final Method get_method =
                    getterName.getMethodOn(beanClass);
                return get_method;
            }
            catch (NoSuchMethodException e) {
                throw new XmlRuntimeException(e);
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
            }
            catch (ClassNotFoundException cnfe) {
                throw new XmlRuntimeException(cnfe);//should never happen
            }
        }


        public boolean isAttribute()
        {
            return bindingProperty.isAttribute();
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
            value = ClassLoadingUtils.newInstance(type.javaClass);
        }


        Object getFinalValue()
        {
            if (accumulators != null) {
                final Property[] props = runtimeBindingType.properties;
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

        void addItem(int property_index, Object value)
        {
            initAccumulator(property_index);
            accumulators[property_index].append(value);
        }

        private void initAccumulator(int property_index)
        {
            Accumulator[] accs = accumulators;
            if (accs == null) {
                accs = new Accumulator[runtimeBindingType.getPropertyCount()];
                accumulators = accs;
            }
            if (accs[property_index] == null) {
                final Property p = runtimeBindingType.properties[property_index];
                accs[property_index] =
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
