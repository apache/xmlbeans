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

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

/**
 * A simpel-content binding is one that connects XML and Java based on the
 * QNames of XML attributes, and a special property to represent
 * the simple content of a complexType who's content type is a simple type.
 */
public class SimpleContentBean extends BindingType
{

    // ========================================================================
    // Variables

    private SimpleContentProperty simpleContentProperty;
    private Map attProps = new LinkedHashMap(); // QName -> prop (attrs)

    // ========================================================================
    // Constructors

    public SimpleContentBean(BindingTypeName btName)
    {
        super(btName);
    }

    public SimpleContentBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        super(node);

        org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean simpleContentBean =
            (org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean)node;
        org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] propArray =
            simpleContentBean.getAttributePropertyArray();

        for (int i = 0; i < propArray.length; i++) {
            addProperty((QNameProperty)BindingProperty.forNode(propArray[i]));
        }

        final org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty scp =
            simpleContentBean.getSimpleContentProperty();

        final SimpleContentProperty bprop =
            (SimpleContentProperty)SimpleContentProperty.forNode(scp);
        setSimpleContentProperty(bprop);
    }

    // ========================================================================
    // Public methods

    /**
     * Looks up a property by attribute name, null if no match.
     */
    public QNameProperty getPropertyForAttribute(QName name)
    {
        return (QNameProperty)attProps.get(name);
    }

    public Collection getAttributeProperties()
    {
        return Collections.unmodifiableCollection(attProps.values());
    }

    public SimpleContentProperty getSimpleContentProperty()
    {
        return simpleContentProperty;
    }

    public void setSimpleContentProperty(SimpleContentProperty simpleContentProperty)
    {
        this.simpleContentProperty = simpleContentProperty;
    }


    /**
     * Adds a new property
     */
    public void addProperty(QNameProperty newProp)
    {
        final boolean att = newProp.isAttribute();
        if (!att) {
            final String msg = "property must be an attribute: " + newProp;
            throw new IllegalArgumentException(msg);
        }
        if (attProps.containsKey(newProp.getQName()))
            throw new IllegalArgumentException("duplicate property: " + newProp);

        attProps.put(newProp.getQName(), newProp);
    }

    // ========================================================================
    // BindingType implementation

    /**
     * This function copies an instance back out to the relevant part of the XML file.
     *
     * Subclasses should override and call super.write first.
     */
    protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean bnNode =
            (org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean)super.write(node);

        if (simpleContentProperty == null) {
            throw new IllegalArgumentException("type must have a simple content property");
        }

        final org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty sc_prop =
            bnNode.addNewSimpleContentProperty();
        simpleContentProperty.write(sc_prop);

        for (Iterator i = attProps.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            QNameProperty qProp = (QNameProperty)e.getValue();
            org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = bnNode.addNewAttributeProperty();
            qProp.write(qpNode);
        }
        return bnNode;
    }

    public void accept(BindingTypeVisitor visitor) throws XmlException
    {
        visitor.visit(this);
    }


}
