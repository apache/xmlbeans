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
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.JavaName;
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;

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

    ByNameRuntimeBindingType(ByNameBean btype)
    {
        byNameBean = btype;
        try {
            javaClass = getJavaClass(btype, getClass().getClassLoader());
        }
        catch (ClassNotFoundException e) {
            final String msg = "failed to load " + btype.getName().getJavaName();
            throw (RuntimeException)(new RuntimeException(msg).initCause(e));
        }

        properties = new Property[btype.getProperties().size()];
    }

    //prepare internal data structures for use
    public void initialize(RuntimeBindingTypeTable typeTable,
                           BindingLoader loader)
    {
        int idx = 0;
        for (Iterator itr = byNameBean.getProperties().iterator(); itr.hasNext();) {
            QNameProperty bprop = (QNameProperty)itr.next();
            Property prop = new Property(javaClass, bprop, typeTable, loader);
            properties[idx++] = prop;
        }
    }

    public Object createIntermediary(UnmarshalContext context)
    {
        return ClassLoadingUtils.newInstance(javaClass);
    }

    private static Class getJavaClass(BindingType btype, ClassLoader backup)
        throws ClassNotFoundException
    {
        final JavaName javaName = btype.getName().getJavaName();
        String jclass = javaName.toString();
        return ClassLoadingUtils.loadClass(jclass, backup);
    }

    public Object getFinalObjectFromIntermediary(Object retval,
                                                 UnmarshalContext context)
    {
        return retval;
    }

    public BindingType getType()
    {
        return byNameBean;
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


    private static final class Property implements RuntimeBindingProperty
    {
        private final Class beanClass;
        private final QNameProperty bindingProperty;
        private final BindingType bindingType;
        private final TypeUnmarshaller unmarshaller;
        private final TypeMarshaller marshaller; // used only for simple types
        private final Class propertyClass;
        private final Method getMethod;
        private final Method setMethod;
        private final boolean javaPrimitive;
        private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
        private static final Class[] EMPTY_CLASS_ARRAY = new Class[]{};

        Property(Class beanClass,
                 QNameProperty prop,
                 RuntimeBindingTypeTable typeTable,
                 BindingLoader loader)
        {
            this.beanClass = beanClass;
            this.bindingProperty = prop;
            this.unmarshaller = lookupUnmarshaller(prop, typeTable, loader);
            this.marshaller = lookupMarshaller(prop, typeTable, loader);
            this.bindingType = loader.getBindingType(prop.getTypeName());
            try {
                this.propertyClass = getJavaClass(bindingType, getClass().getClassLoader());
            }
            catch (ClassNotFoundException e) {
                final String msg = "error loading " + bindingType.getName().getJavaName();
                throw (RuntimeException)(new RuntimeException(msg).initCause(e));
            }

            getMethod = getGetterMethod(prop, beanClass);
            setMethod = getSetterMethod(prop, beanClass, propertyClass);
            javaPrimitive = propertyClass.isPrimitive();
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
            final BindingType bindingType =
                bindingLoader.getBindingType(prop.getTypeName());
            TypeUnmarshaller um = typeTable.getTypeUnmarshaller(bindingType);
            if (um == null) {
                throw new AssertionError("failed to get unmarshaller for " + prop);
            }
            return um;
        }

        private TypeMarshaller lookupMarshaller(BindingProperty prop,
                                                RuntimeBindingTypeTable typeTable,
                                                BindingLoader bindingLoader)
        {
            final BindingType bindingType =
                bindingLoader.getBindingType(prop.getTypeName());
            TypeMarshaller m = typeTable.getTypeMarshaller(bindingType);
            return m;
        }


        public TypeUnmarshaller getTypeUnmarshaller(UnmarshalContext context)
        {
            final QName xsi_type = context.getXsiType();

            if (xsi_type == null)
                return unmarshaller;
            else if (xsi_type == UnmarshalContext.XSI_NIL_MARKER)
                return NullUnmarshaller.getInstance();

            return context.getTypeUnmarshaller(xsi_type);
        }

        public void fill(Object inter, Object prop_obj)
        {
            //means xsi:nil was true but we're a primtive.
            //schema should have nillable="false" so this
            //is a validation problems
            if (prop_obj == null && javaPrimitive)
                return;

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
        public CharSequence getLexical(Object value, MarshalContext context)
        {
            assert marshaller != null : "no marhsaller for " + bindingProperty;
            return marshaller.print(value, context);
        }

        public Object getValue(Object parentObject, MarshalContext context)
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
        public boolean isSet(Object parentObject, MarshalContext context)
        {
            if (bindingProperty.isNillable())
                return true;

            Object val = getValue(parentObject, context);
            return (val != null);
        }

        private static Method getSetterMethod(QNameProperty binding_prop,
                                              Class beanClass,
                                              Class propClass)
        {
            String setter = binding_prop.getSetterName();
            try {
                final Method set_method =
                    beanClass.getMethod(setter, new Class[]{propClass});
                return set_method;
            }
            catch (NoSuchMethodException e) {
                throw new XmlRuntimeException(e);
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
            }
        }


        private static Method getGetterMethod(QNameProperty binding_prop,
                                              Class beanClass)
        {
            String getter = binding_prop.getGetterName();
            try {
                final Method get_method =
                    beanClass.getMethod(getter, EMPTY_CLASS_ARRAY);
                return get_method;
            }
            catch (NoSuchMethodException e) {
                throw new XmlRuntimeException(e);
            }
            catch (SecurityException e) {
                throw new XmlRuntimeException(e);
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

}
