/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

public class JaxbBean extends BindingType
{
    Map partProps = new LinkedHashMap(); // XmlName -> prop (particles)
    Map eltProps = new LinkedHashMap(); // QName -> prop (elts)
    Map attProps = new LinkedHashMap(); // QName -> prop (attrs)
    
    public JaxbBean(JavaName jName, XmlName xName)
    {
        super(jName, xName, false);
    }

    public JaxbBean(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        super(node);
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.JaxbBean jbNode = (org.apache.xmlbeans.x2003.x09.bindingConfig.JaxbBean)node;
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.ParticleProperty[] ppropArray = jbNode.getParticlePropertyArray();
        for (int i = 0; i < ppropArray.length; i++)
        {
            addProperty(BindingProperty.forNode(ppropArray[i]));
        }
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty[] qpropArray = jbNode.getQnamePropertyArray();
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
    protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        org.apache.xmlbeans.x2003.x09.bindingConfig.JaxbBean jbNode = (org.apache.xmlbeans.x2003.x09.bindingConfig.JaxbBean)super.write(node);
        for (Iterator i = getProperties().iterator(); i.hasNext(); )
        {
            BindingProperty bProp = (BindingProperty)i.next();
            if (bProp instanceof ParticleProperty)
            {
                org.apache.xmlbeans.x2003.x09.bindingConfig.ParticleProperty ppNode = jbNode.addNewParticleProperty();
                bProp.write(ppNode);
            }
            else
            {
                org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty qpNode = jbNode.addNewQnameProperty();
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
