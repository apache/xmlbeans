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
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.binding.bts.SimpleContentBean;
import org.apache.xmlbeans.impl.binding.bts.SimpleContentProperty;

import javax.xml.namespace.QName;
import java.util.Collection;

class SimpleContentRuntimeBindingType
    extends AttributeRuntimeBindingType
{
    private SimpleContentRuntimeProperty simpleTypeProperty;

    SimpleContentRuntimeBindingType(SimpleContentBean simpleContentBean)
        throws XmlException
    {
        super(simpleContentBean);
    }

    void accept(RuntimeTypeVisitor visitor)
        throws XmlException
    {
        visitor.visit(this);
    }

    //prepare internal data structures for use
    public final void initialize(RuntimeBindingTypeTable typeTable,
                                 BindingLoader loader)
        throws XmlException
    {
        super.initialize(typeTable, loader);
        SimpleContentBean scb = getSimpleContentBean();
        final SimpleContentProperty simpleContentProperty =
            scb.getSimpleContentProperty();
        simpleTypeProperty =
            new SimpleContentRuntimeProperty(getJavaType(),
                                             simpleContentProperty, this,
                                             typeTable, loader);
    }

    boolean hasElementChildren()
    {
        return false;
    }

    private SimpleContentBean getSimpleContentBean()
    {
        return (SimpleContentBean)getBindingType();
    }

    protected void initElementProperty(QNameProperty prop,
                                       int elem_idx,
                                       RuntimeBindingTypeTable typeTable,
                                       BindingLoader loader
                                       )
        throws XmlException
    {
        throw new AssertionError("invalid property for this type: " + prop);
    }

    protected Collection getQNameProperties()
    {
        SimpleContentBean narrowed_type = (SimpleContentBean)getBindingType();
        return narrowed_type.getAttributeProperties();
    }

    protected Object createIntermediary(UnmarshalResult context)
    {
        return ClassLoadingUtils.newInstance(getJavaType());
    }

    protected Object getFinalObjectFromIntermediary(Object retval,
                                                    UnmarshalResult context)
        throws XmlException
    {
        return retval;
    }

    int getElementPropertyCount()
    {
        return 0;
    }

    protected boolean hasMulti()
    {
        return false;
    }

    RuntimeBindingProperty getSimpleContentProperty()
    {
        return simpleTypeProperty;
    }

    private static final class SimpleContentRuntimeProperty
        extends BeanRuntimeProperty
    {

        SimpleContentRuntimeProperty(Class beanClass,
                                     BindingProperty prop,
                                     RuntimeBindingType containing_type,
                                     RuntimeBindingTypeTable typeTable,
                                     BindingLoader loader)
            throws XmlException
        {
            super(beanClass, prop, containing_type, typeTable, loader);
        }

        QName getName()
        {
            throw new AssertionError("prop has no name by design");
        }

        boolean isMultiple()
        {
            return false;
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
