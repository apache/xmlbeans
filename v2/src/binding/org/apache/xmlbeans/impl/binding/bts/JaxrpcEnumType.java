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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.XmlException;

/**
 * A representation of the JAX-RPC 1.1 rules for binding a
 * schema enumeration into java
 */
public class JaxrpcEnumType extends BindingType
{

    // ========================================================================
    // Variables

    private BindingTypeName baseType;
    private MethodName getValueMethod;
    private MethodName fromValueMethod;
    private MethodName fromStringMethod;
    private MethodName toXMLMethod;

    // ========================================================================
    // Constants

    public final static MethodName DEFAULT_GET_VALUE = MethodName.create("getValue");
    public final static MethodName DEFAULT_FROM_VALUE = MethodName.create("fromValue");
    public final static MethodName DEFAULT_FROM_STRING = MethodName.create("fromString");
    public final static MethodName DEFAULT_TO_XML = MethodName.create("toXML");

    // ========================================================================
    // Constructors

    public JaxrpcEnumType(BindingTypeName btName)
    {
        super(btName);
    }

    public JaxrpcEnumType(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        this((org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType)node);
    }

    public JaxrpcEnumType(org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType node)
    {
        super(node);

        this.baseType = BindingTypeName.forPair(
            JavaTypeName.forString(node.getBaseJavatype()),
            XmlTypeName.forString(node.getBaseXmlcomponent()));

        this.getValueMethod = MethodName.create(node.getGetValueMethod());
        this.fromValueMethod = MethodName.create(node.getFromValueMethod());
        this.fromStringMethod = MethodName.create(node.getFromStringMethod());

        org.apache.xml.xmlbeans.bindingConfig.JavaMethodName toxml_method =
            node.getToXMLMethod();
        if (toxml_method != null) {
            this.toXMLMethod = MethodName.create(toxml_method);
        }
    }


    protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType jnode =
            (org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType)super.write(node);

        if (baseType != null) {
            jnode.setBaseJavatype(baseType.getJavaName().toString());
            jnode.setBaseXmlcomponent(baseType.getXmlName().toString());
        }

        if (getValueMethod != null) {
            getValueMethod.write(jnode.addNewGetValueMethod());
        }

        if (fromValueMethod != null) {
            fromValueMethod.write(jnode.addNewFromValueMethod());
        }

        if (toXMLMethod != null) {
            toXMLMethod.write(jnode.addNewToXMLMethod());
        }

        if (fromStringMethod != null) {
            fromStringMethod.write(jnode.addNewFromStringMethod());
        }

        return jnode;
    }

    public void accept(BindingTypeVisitor visitor) throws XmlException
    {
        visitor.visit(this);
    }


    // ========================================================================
    // Public methods

    public BindingTypeName getBaseTypeName()
    {
        return baseType;
    }

    public void setBaseType(BindingType bType)
    {
        baseType = bType.getName();
    }

    public MethodName getGetValueMethod()
    {
        return getValueMethod;
    }

    public void setGetValueMethod(MethodName getValueMethod)
    {
        this.getValueMethod = getValueMethod;
    }

    public MethodName getFromValueMethod()
    {
        return fromValueMethod;
    }

    public void setFromValueMethod(MethodName fromValueMethod)
    {
        this.fromValueMethod = fromValueMethod;
    }

    public MethodName getFromStringMethod()
    {
        return fromStringMethod;
    }

    public void setFromStringMethod(MethodName fromStringMethod)
    {
        this.fromStringMethod = fromStringMethod;
    }

    public MethodName getToXMLMethod()
    {
        return toXMLMethod;
    }

    public void setToXMLMethod(MethodName toXMLMethod)
    {
        this.toXMLMethod = toXMLMethod;
    }

}
