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
import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.JavaInstanceFactory;
import org.apache.xmlbeans.impl.binding.bts.MethodName;
import org.apache.xmlbeans.impl.binding.bts.ParentInstanceFactory;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.marshal.util.ReflectionUtils;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;


abstract class RuntimeBindingProperty
{
    private final RuntimeBindingType containingType;

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


    //TODO: consider inlining this method since every impl is the same
    //or moving it to the type?
    abstract RuntimeBindingType getActualRuntimeType(Object property_value,
                                                     MarshalResult result)
        throws XmlException;


    abstract QName getName();


    protected final Object createIntermediary(Object parent_inter,
                                              RuntimeBindingType actual_rtt,
                                              UnmarshalResult context)
        throws XmlException
    {
        if (hasFactory()) {
            final Object obj = createObjectViaFactory(parent_inter, actual_rtt);
            return actual_rtt.createIntermediary(context, obj);
        } else {
            return actual_rtt.createIntermediary(context);
        }
    }


    //non simple type props can throw an exception
    final CharSequence getLexical(Object value,
                                  MarshalResult result)
        throws XmlException
    {
        assert value != null :
            "null value for " + getName();

        assert  result != null;

        final RuntimeBindingType actualRuntimeType =
            getActualRuntimeType(value, result);

        assert actualRuntimeType.getMarshaller() != null :
            "null marshaller for prop=" + getName() +
            " propType=" + actualRuntimeType;

        return actualRuntimeType.getMarshaller().print(value, result);
    }

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
        final RuntimeBindingType rtt = getRuntimeBindingType();
        final TypeUnmarshaller um = rtt.getUnmarshaller();
        assert um != null;

        try {
            final Object this_val;
            if (hasFactory()) {
                this_val = createObjectViaFactory(inter, rtt);
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


    protected abstract void fill(Object inter, Object prop_obj)
        throws XmlException;


    protected void fillPlaceholder(Object inter)
    {
        throw new UnsupportedOperationException("not used.  this=" + this);
    }

    //non index properties can return unsupported...
    protected int getSize(Object inter)
    {
        throw new UnsupportedOperationException("this=" + this);
    }

    //non index properties can return unsupported...
    protected void fill(Object final_obj, int index, Object prop_val)
    {
        throw new UnsupportedOperationException("this=" + this);
    }


    protected final boolean hasFactory()
    {
        return parentFactoryMethod != null;
    }

    private Object createObjectViaFactory(Object inter,
                                          final RuntimeBindingType actual_rtt)
        throws XmlException
    {
        final Object actual_obj =
            containingType.getObjectFromIntermediate(inter);

        assert actual_obj != null;
        assert parentFactoryMethod != null;


        final Object[] params;
        if (parentFactoryMethodTakesClassArg) {
            params = new Object[]{actual_rtt.getJavaType()};
        } else {
            params = null;
        }

        return ReflectionUtils.invokeMethod(actual_obj,
                                            parentFactoryMethod,
                                            params);

    }

    public String toString()
    {
        return "RuntimeBindingProperty{" +
            "bindingType=" + this.getRuntimeBindingType().getBindingType() +
            "containingType=" + containingType +
            "}";
    }


}
