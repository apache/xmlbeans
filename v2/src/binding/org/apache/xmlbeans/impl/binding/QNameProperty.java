/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

import javax.xml.namespace.QName;

public class QNameProperty extends BindingProperty
{
    private QName theName;
    private boolean isAttribute;
    private boolean isMultiple;
    private boolean isOptional;
    private boolean isNillable;
    
    public QNameProperty(BindingLoader bFile)
    {
        super(bFile);
    }

    public QNameProperty(BindingLoader bFile, org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty node)
    {
        super(bFile, node);
        org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty qpNode =
                (org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty)node;
        theName = qpNode.getQname();
        isAttribute = qpNode.getAttribute();
        isMultiple = qpNode.getMultiple();
        isNillable = qpNode.getNillable();
        isOptional = qpNode.getOptional();
    }
    
    /**
     * This function copies an instance back out to the relevant part of the XML file.
     * 
     * Subclasses should override and call super.write first.
     */ 
    protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty node)
    {
        node = super.write(node);
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty qpNode =
                (org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty)node;
        
        qpNode.setQname(theName);
        if (isAttribute)
            qpNode.setAttribute(true);
        if (isMultiple)
            qpNode.setMultiple(true);
        if (isOptional)
            qpNode.setOptional(true);
        if (isNillable)
            qpNode.setNillable(true);
        return qpNode;
    }

    public QName getQName()
    {
        return theName;
    }
    
    public void setQName(QName theName)
    {
        this.theName = theName;
    }

    public boolean isAttribute()
    {
        return isAttribute;
    }

    public void setAttribute(boolean attribute)
    {
        isAttribute = attribute;
    }

    public boolean isMultiple()
    {
        return isMultiple;
    }

    public void setMultiple(boolean multiple)
    {
        isMultiple = multiple;
    }

    public boolean isOptional()
    {
        return isOptional;
    }

    public void setOptional(boolean optional)
    {
        isOptional = optional;
    }

    public boolean isNillable()
    {
        return isNillable;
    }

    public void setNillable(boolean nillable)
    {
        isNillable = nillable;
    }
}
