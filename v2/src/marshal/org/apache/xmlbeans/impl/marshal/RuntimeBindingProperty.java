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
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.JavaInstanceFactory;
import org.apache.xmlbeans.impl.binding.bts.ParentInstanceFactory;
import org.apache.xmlbeans.impl.binding.bts.MethodName;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;


abstract class RuntimeBindingProperty
{
    protected final RuntimeBindingType containingType;

    //TODO: when we have other types of factories,
    //this will need to more general
    private final Method parentFactoryMethod;
    private final boolean parentFactoryMethodTakesClassArg;

    protected RuntimeBindingProperty(RuntimeBindingType containingType)
    {
        this.containingType = containingType;
        parentFactoryMethod = null;
        parentFactoryMethodTakesClassArg = false;
    }

    protected RuntimeBindingProperty(BindingProperty prop,
                                     RuntimeBindingType containingType)
        throws XmlException
    {
        this.containingType = containingType;
        final JavaInstanceFactory jif = prop.getJavaInstanceFactory();
        if (jif == null) {
            parentFactoryMethod = null;
            parentFactoryMethodTakesClassArg = false;
        } else {
            if (jif instanceof ParentInstanceFactory) {
                ParentInstanceFactory pif = (ParentInstanceFactory)jif;
                final MethodName create_method = pif.getCreateObjectMethod();
                final Class container_class = containingType.getJavaType();
                parentFactoryMethod =
                    ReflectionUtils.getMethodOnClass(create_method,
                                                     container_class);
                Class[] param_types = parentFactoryMethod.getParameterTypes();
                if (param_types.length > 1) {
                    String msg = "too many args for parent factory method: " +
                        parentFactoryMethod;
                    throw new XmlException(msg);
                }
                if (param_types.length == 1 && !Class.class.equals(param_types[0])) {
                    String msg = "arg must be java.lang.Class for " +
                        "parent factory method: " + parentFactoryMethod;
                    throw new XmlException(msg);
                }

                parentFactoryMethodTakesClassArg = param_types.length > 0;
            } else {
                throw new AssertionError("FACTORY UNIMP: " + jif);
            }
        }
    }

    abstract RuntimeBindingType getRuntimeBindingType();

    abstract RuntimeBindingType getActualRuntimeType(Object property_value,
                                                     MarshalResult result)
        throws XmlException;

    abstract QName getName();

    //non simple type props can throw an exception
    abstract CharSequence getLexical(Object parent, MarshalResult result)
        throws XmlException;

    abstract Object getValue(Object parentObject, MarshalResult result)
        throws XmlException;

    abstract boolean isSet(Object parentObject, MarshalResult result)
        throws XmlException;

    abstract boolean isMultiple();

    abstract boolean isNillable();

    /**
     * returns null if this property has no default
     *
     * @return
     */
    abstract String getLexicalDefault();

    final void extractAndFillAttributeProp(UnmarshalResult context,
                                           Object inter)
        throws XmlException
    {
        final TypeUnmarshaller um = this.getTypeUnmarshaller(context);
        assert um != null;

        try {
            final Object this_val;
            if (hasFactory()) {
                final Object actual_obj =
                    containingType.getObjectFromIntermediate(inter);
                if (parentFactoryMethodTakesClassArg) {
                    this_val =
                        createObjectViaFactory(actual_obj,
                                               getRuntimeBindingType().getJavaType());
                } else {
                    this_val = createObjectViaFactory(actual_obj);
                }
                um.unmarshalAttribute(this_val, context);
            } else {
                this_val = um.unmarshalAttribute(context);
            }
            fill(inter, this_val);
        }
        catch (InvalidLexicalValueException ilve) {
            String msg = "invalid value for " + this.getName() +
                ": " + ilve.getMessage();
            context.addError(msg, ilve.getLocation());
        }
    }

    final void extractAndFillElementProp(final UnmarshalResult context,
                                         Object inter)
        throws XmlException
    {
        try {
            final String lexical_default = this.getLexicalDefault();
            if (lexical_default != null) {
                context.setNextElementDefault(lexical_default);
            }
            final Object this_val;
            if (hasFactory()) {
                final TypeUnmarshaller um;
                final Class actual_prop_class;
                final QName xsi_type = context.getXsiType();
                if (xsi_type == null) {
                    //REVIEW: we're doing a little extra work here
                    um = this.getTypeUnmarshaller(context);
                    actual_prop_class =
                        this.getRuntimeBindingType().getJavaType();
                } else {
                    final BindingType actual_binding_type =
                        context.lookupBindingType(xsi_type);
                    if (actual_binding_type != null) {
                        um = context.getTypeUnmarshaller(actual_binding_type);
                        actual_prop_class =
                            context.getRuntimeType(actual_binding_type).getJavaType();
                    } else {
                        um = this.getTypeUnmarshaller(context);
                        actual_prop_class =
                            this.getRuntimeBindingType().getJavaType();
                    }
                }
                if (um == null) {
                    //there was a big problem looking up the type,
                    //so just skip this element
                    context.skipElement();
                    return;
                }
                final Object actual_obj =
                    containingType.getObjectFromIntermediate(inter);
                if (parentFactoryMethodTakesClassArg) {
                    this_val = createObjectViaFactory(actual_obj,
                                                      actual_prop_class);
                } else {
                    //TODO: avoid some of the lookups if we know we have this kind
                    //of factory
                    this_val = createObjectViaFactory(actual_obj);
                }
                um.unmarshal(this_val, context);
            } else {
                this_val = this.getTypeUnmarshaller(context).unmarshal(context);
            }
            fill(inter, this_val);
        }
        catch (InvalidLexicalValueException ilve) {
            //unlike attributes, the error has been added to the context
            //already via BaseSimpleTypeConveter...
        }
    }

    private Object createObjectViaFactory(Object parent,
                                          Class actual_prop_class)
        throws XmlException
    {
        assert parent != null;
        assert parentFactoryMethod != null;
        assert parentFactoryMethodTakesClassArg;

        return ReflectionUtils.invokeMethod(parent,
                                            parentFactoryMethod,
                                            new Object[]{actual_prop_class});
    }

    private Object createObjectViaFactory(Object parent)
        throws XmlException
    {
        assert parent != null;
        assert parentFactoryMethod != null;
        assert !parentFactoryMethodTakesClassArg;

        return ReflectionUtils.invokeMethod(parent,
                                            parentFactoryMethod,
                                            null);
    }

    //these methods should be used only by this type and subclasses
    protected abstract void fill(Object inter, Object prop_obj)
        throws XmlException;

    protected abstract TypeUnmarshaller getTypeUnmarshaller(UnmarshalResult context)
        throws XmlException;


    protected boolean hasFactory()
    {
        return parentFactoryMethod != null;
    }


}
