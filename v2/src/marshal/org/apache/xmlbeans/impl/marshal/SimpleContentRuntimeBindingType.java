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

public class SimpleContentRuntimeBindingType
    extends AttributeRuntimeBindingType
{
    private SimpleContentRuntimeProperty simpleTypeProperty;

    public SimpleContentRuntimeBindingType(SimpleContentBean simpleContentBean)
        throws XmlException
    {
        super(simpleContentBean);
    }

    //prepare internal data structures for use
    public final void initialize(RuntimeBindingTypeTable typeTable,
                                 BindingLoader loader,
                                 RuntimeTypeFactory rttFactory)
        throws XmlException
    {
        super.initialize(typeTable, loader, rttFactory);
        SimpleContentBean scb = getSimpleContentBean();
        final SimpleContentProperty simpleContentProperty =
            scb.getSimpleContentProperty();
        simpleTypeProperty =
            new SimpleContentRuntimeProperty(getJavaType(),
                                             simpleContentProperty, this,
                                             typeTable, loader, rttFactory);
    }

    private SimpleContentBean getSimpleContentBean()
    {
        return (SimpleContentBean)getBindingType();
    }

    protected void initElementProperty(QNameProperty prop,
                                       int elem_idx,
                                       RuntimeBindingTypeTable typeTable,
                                       BindingLoader loader,
                                       RuntimeTypeFactory rttFactory)
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

    public int getElementPropertyCount()
    {
        return 0;
    }

    protected boolean hasMulti()
    {
        return false;
    }

    public RuntimeBindingProperty getSimpleContentProperty()
    {
        return simpleTypeProperty;
    }

    private static final class SimpleContentRuntimeProperty
        extends RuntimePropertyBase
    {

        SimpleContentRuntimeProperty(Class beanClass,
                                     BindingProperty prop,
                                     IntermediateResolver intermediateResolver,
                                     RuntimeBindingTypeTable typeTable,
                                     BindingLoader loader,
                                     RuntimeTypeFactory rttFactory)
            throws XmlException
        {
            super(beanClass, prop, intermediateResolver, typeTable, loader, rttFactory);

        }

        public QName getName()
        {
            throw new AssertionError("prop has no name by design");
        }

        public boolean isMultiple()
        {
            return false;
        }

        public boolean isNillable()
        {
            return false;
        }

        public String getLexicalDefault()
        {
            return null;
        }
    }
}
