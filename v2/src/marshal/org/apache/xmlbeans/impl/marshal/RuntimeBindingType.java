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
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.lang.reflect.Method;

/**
 * what we need to know about a binding type at runtime.
 * No marshalling state should be stored here.
 * This object will be shared by many threads
 */
abstract class RuntimeBindingType
{
    private final BindingType bindingType;
    private final Class javaClass;
    private final boolean javaPrimitive;
    private final boolean javaFinal;
    private TypeMarshaller marshaller;
    private TypeUnmarshaller unmarshaller;

    RuntimeBindingType(BindingType binding_type)
        throws XmlException
    {
        this(binding_type, null, null);
    }

    RuntimeBindingType(BindingType binding_type,
                       TypeMarshaller m,
                       TypeUnmarshaller um)
        throws XmlException
    {
        bindingType = binding_type;

        try {
            javaClass = getJavaClass(binding_type, getClass().getClassLoader());
        }
        catch (ClassNotFoundException e) {
            ClassLoader context_cl =
                Thread.currentThread().getContextClassLoader();
            final String msg = "failed to load " +
                binding_type.getName().getJavaName() + " from " + context_cl;
            throw new XmlException(msg, e);
        }

        javaPrimitive = javaClass.isPrimitive();
        javaFinal = ReflectionUtils.isClassFinal(javaClass);
        unmarshaller = um;
        marshaller = m;
    }

    Object getObjectFromIntermediate(Object inter)
    {
        return inter;
    }

    final BindingType getBindingType()
    {
        return bindingType;
    }

    /**
     * prepare internal data structures for use
     *
     * @param typeTable
     * @param bindingLoader
     */
    protected abstract void initialize(RuntimeBindingTypeTable typeTable,
                                       BindingLoader bindingLoader)
        throws XmlException;

    /**
     * prepare internal data structures for use
     *
     * @param typeTable
     * @param bindingLoader
     */
    final void external_initialize(RuntimeBindingTypeTable typeTable,
                                   BindingLoader bindingLoader)
        throws XmlException
    {
        this.initialize(typeTable, bindingLoader);

        if (marshaller == null)
            marshaller = typeTable.createMarshaller(bindingType, bindingLoader);

        if (bindingType instanceof SimpleBindingType) {
            if (marshaller == null) {
                throw new AssertionError("null marshaller for " + bindingType);
            }
        }

        if (unmarshaller == null)
            unmarshaller = typeTable.createUnmarshaller(bindingType, bindingLoader);

        assert unmarshaller != null;
    }

    final Class getJavaType()
    {
        return javaClass;
    }

    final boolean isJavaPrimitive()
    {
        return javaPrimitive;
    }

    final boolean isJavaFinal()
    {
        return javaFinal;
    }

    final protected TypeUnmarshaller getUnmarshaller()
    {
        assert unmarshaller != null;
        return unmarshaller;
    }

    final protected TypeMarshaller getMarshaller()
    {
        return marshaller;
    }

    protected static Class getJavaClass(BindingType btype, ClassLoader backup)
        throws ClassNotFoundException
    {
        final JavaTypeName javaName = btype.getName().getJavaName();
        String jclass = javaName.toString();
        return ClassLoadingUtils.loadClass(jclass, backup);
    }

    protected QName getSchemaTypeName()
    {
        return getBindingType().getName().getXmlName().getQName();
    }

    //REVIEW: find a shorter path to our goal.
    protected static Object extractDefaultObject(String value,
                                                 BindingType bindingType,
                                                 RuntimeBindingTypeTable typeTable,
                                                 BindingLoader loader)
        throws XmlException
    {
        final String xmldoc = "<a>" + value + "</a>";
        try {
            final SchemaTypeLoaderProvider provider =
                UnusedSchemaTypeLoaderProvider.getInstance();
            final UnmarshallerImpl um =
                new UnmarshallerImpl(loader, typeTable, provider);
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
        catch (XmlRuntimeException re) {
            //TODO: improve error handling using custom error handler
            //esp nice to provide error info from config file
            final String msg = "invalid default value: " + value +
                " for type " + bindingType.getName();
            throw new XmlException(msg, re);
        }
        catch (XMLStreamException e) {
            throw new XmlException(e);
        }
    }

    public String toString()
    {
        return this.getClass().getName() +
            "{" +
            "bindingType=" + bindingType +
            "}";
    }


    protected abstract static class BeanRuntimeProperty
        extends RuntimeBindingProperty
    {
        protected final Class beanClass;
        protected final RuntimeBindingType containingType;
        protected final Method getMethod;
        protected final Method setMethod;
        protected final Method issetMethod;
        protected final RuntimeBindingType runtimeBindingType;
        protected final Class propertyClass;
        protected final Class collectionElementClass; //null for non collections

        BeanRuntimeProperty(Class beanClass,
                            BindingProperty prop,
                            RuntimeBindingType containingType,
                            RuntimeBindingTypeTable typeTable,
                            BindingLoader loader)
            throws XmlException
        {
            super(prop, containingType);
            this.beanClass = beanClass;
            this.containingType = containingType;
            final BindingTypeName type_name = prop.getTypeName();


            final BindingType binding_type = loader.getBindingType(type_name);
            if (binding_type == null) {
                throw new XmlException("unable to load " + type_name);
            }

            runtimeBindingType =
                typeTable.createRuntimeType(binding_type, loader);
            assert runtimeBindingType != null;

            propertyClass = getPropertyClass(prop, binding_type);
            collectionElementClass = getCollectionElementClass(prop, binding_type);
            getMethod = ReflectionUtils.getGetterMethod(prop, beanClass);
            setMethod = ReflectionUtils.getSetterMethod(prop, beanClass);
            issetMethod = ReflectionUtils.getIssetterMethod(prop, beanClass);

            //we may revisit whether this is an error
            if (getMethod == null) {
                String e = "no getter found for " + prop + " on " + beanClass;
                throw new XmlException(e);
            }

            //we no doubt will revisit whether this is an error, esp for exceptions
            if (setMethod == null) {
                String e = "no setter found for " + prop + " on " + beanClass;
                throw new XmlException(e);
            }

        }


        public void fill(final Object inter, final Object prop_obj)
            throws XmlException
        {
            Object inst = containingType.getObjectFromIntermediate(inter);
            ReflectionUtils.invokeMethod(inst, setMethod, new Object[]{prop_obj});
        }

        final Object getValue(Object parentObject, MarshalResult result)
            throws XmlException
        {
            assert parentObject != null;
            assert beanClass.isInstance(parentObject) :
                parentObject.getClass() + " is not a " + beanClass;

            return ReflectionUtils.invokeMethod(parentObject, getMethod);
        }

        protected Class getPropertyClass(BindingProperty prop, BindingType btype)
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

        protected Class getCollectionElementClass(BindingProperty prop,
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

        final RuntimeBindingType getRuntimeBindingType()
        {
            return runtimeBindingType;
        }

        final RuntimeBindingType getActualRuntimeType(Object property_value,
                                                      MarshalResult result)
            throws XmlException
        {
            return result.determineRuntimeBindingType(runtimeBindingType, property_value);
        }

        final boolean isSet(Object parentObject, MarshalResult result)
            throws XmlException
        {
            //TODO: can we just return true if this property is not optional?

            if (issetMethod == null)
                return isSetFallback(parentObject, result);

            final Boolean isset =
                (Boolean)ReflectionUtils.invokeMethod(parentObject, issetMethod);
            return isset.booleanValue();
        }

        private boolean isSetFallback(Object parentObject, MarshalResult result)
            throws XmlException
        {
            //REVIEW: nillable is winning over minOccurs="0".  Is this correct?
            if (isNillable()) return true;

            Object val = getValue(parentObject, result);
            return (val != null);
        }

        final CharSequence getLexical(Object value,
                                      MarshalResult result)
            throws XmlException
        {
            assert value != null :
                "null value for " + getName() + " class=" + beanClass;

            assert  result != null :
                "null value for " + getName() + " class=" + beanClass;

            assert runtimeBindingType.getMarshaller() != null :
                "null marshaller for prop=" + getName() + " java-type=" +
                beanClass + " propType=" + runtimeBindingType;

            return runtimeBindingType.getMarshaller().print(value, result);
        }

    }


}
