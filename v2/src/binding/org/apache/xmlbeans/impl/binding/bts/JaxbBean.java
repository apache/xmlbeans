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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingProperty;
import org.apache.xmlbeans.impl.binding.bts.BindingType;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Represents a binding that can line up properties based on either
 * name (like ByNameBean) or position (i.e., Schema Particle), as
 * required by JAXB.
 */ 
public class JaxbBean extends BindingType
{
    Map partProps = new LinkedHashMap(); // XmlTypeName -> prop (particles)
    Map eltProps = new LinkedHashMap(); // QName -> prop (elts)
    Map attProps = new LinkedHashMap(); // QName -> prop (attrs)
    
    public JaxbBean(BindingTypeName btName)
    {
        super(btName);
    }

    public JaxbBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        super(node);
        
        org.apache.xml.xmlbeans.bindingConfig.JaxbBean jbNode = (org.apache.xml.xmlbeans.bindingConfig.JaxbBean)node;
        
        org.apache.xml.xmlbeans.bindingConfig.ParticleProperty[] ppropArray = jbNode.getParticlePropertyArray();
        for (int i = 0; i < ppropArray.length; i++)
        {
            addProperty(BindingProperty.forNode(ppropArray[i]));
        }
        
        org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] qpropArray = jbNode.getQnamePropertyArray();
        for (int i = 0; i < qpropArray.length; i++)
        {
            addProperty(BindingProperty.forNode(qpropArray[i]));
        }
    }
    
    
    /**
     * This function copies an instance back out to the relevant part of the XML file.
     * 
     * Subclasses should override and call super.write first.
     */ 
    protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        org.apache.xml.xmlbeans.bindingConfig.JaxbBean jbNode = (org.apache.xml.xmlbeans.bindingConfig.JaxbBean)super.write(node);
        for (Iterator i = getProperties().iterator(); i.hasNext(); )
        {
            BindingProperty bProp = (BindingProperty)i.next();
            if (bProp instanceof ParticleProperty)
            {
                org.apache.xml.xmlbeans.bindingConfig.ParticleProperty ppNode = jbNode.addNewParticleProperty();
                bProp.write(ppNode);
            }
            else
            {
                org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = jbNode.addNewQnameProperty();
                bProp.write(qpNode);
                
            }
        }
        return jbNode;
    }
    
    /**
     * Returns an unmodifiable collection of QNameProperty objects.
     */ 
    public Collection getProperties()
    {
        List result = new ArrayList();
        result.addAll(partProps.values());
        result.addAll(eltProps.values());
        result.addAll(attProps.values());
        return Collections.unmodifiableCollection(result);
    }
    
    /**
     * Looks up a property by attribute name, null if no match.
     */ 
    public QNameProperty getPropertyForAttribute(QName name)
    {
        return (QNameProperty)attProps.get(name);
    }
    
    /**
     * Looks up a property by element name, null if no match.
     */ 
    public QNameProperty getPropertyForElement(QName name)
    {
        return (QNameProperty)eltProps.get(name);
    }
    
    /**
     * Adds a new property
     */
    public void addProperty(BindingProperty newProp)
    {
        if (newProp instanceof ParticleProperty)
        {
            partProps.put(((ParticleProperty)newProp).getXmlName(), newProp);
        }
        else if (newProp instanceof QNameProperty)
        {
            QNameProperty qProp = (QNameProperty)newProp;
            if (qProp.isAttribute())
                attProps.put(qProp.getQName(), newProp);
            else
                eltProps.put(qProp.getQName(), newProp);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
}
