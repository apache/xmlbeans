/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;

public class ByNameBean extends BindingType
{
    List props = new ArrayList(); // of QNameProperties
    Map eltProps = new HashMap(); // QName -> prop (elts)
    Map attProps = new HashMap(); // QName -> prop (attrs)
    
    public ByNameBean(BindingLoader bLoader, JavaName jName, XmlName xName, boolean isXmlObj)
    {
        super(bLoader, jName, xName, isXmlObj);
    }

    public ByNameBean(BindingLoader bFile, org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        super(bFile, node);
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty[] propArray =
           ((org.apache.xmlbeans.x2003.x09.bindingConfig.ByNameBean)node).getQnamePropertyArray();
        
        for (int i = 0; i < propArray.length; i++)
        {
            addProperty((QNameProperty)BindingProperty.forNode(getBindingLoader(), propArray[i]));
        }
    }
    
    /**
     * This function copies an instance back out to the relevant part of the XML file.
     * 
     * Subclasses should override and call super.write first.
     */ 
    protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        org.apache.xmlbeans.x2003.x09.bindingConfig.ByNameBean bnNode = (org.apache.xmlbeans.x2003.x09.bindingConfig.ByNameBean)super.write(node);
        for (Iterator i = props.iterator(); i.hasNext(); )
        {
            QNameProperty qProp = (QNameProperty)i.next();
            org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty qpNode = bnNode.addNewQnameProperty();
            qProp.write(qpNode);
        }
        return bnNode;
    }
    
    /**
     * Returns an unmodifiable collection of QNameProperty objects.
     */ 
    public Collection getProperties()
    {
        return Collections.unmodifiableCollection(props);
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
    public void addProperty(QNameProperty newProp)
    {
        if (newProp.isAttribute() ? attProps.containsKey(newProp.getQName()) : eltProps.containsKey(newProp.getQName()))
            throw new IllegalArgumentException();
        
        props.add(newProp);
        if (newProp.isAttribute())
            attProps.put(newProp.getQName(), newProp);
        else
            eltProps.put(newProp.getQName(), newProp);
    }
}
