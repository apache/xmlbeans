/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 7, 2003
 */
package org.apache.xmlbeans.impl.binding;

public class SimpleBindingType extends BindingType
{
    public SimpleBindingType(JavaName jName, XmlName xName, boolean isXmlObj)
    {
        super(jName, xName, isXmlObj);
    }

    public SimpleBindingType(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        super(node);
        org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleType stNode = (org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleType)node;
        asIfXmlType = XmlName.forString(stNode.getAsXml());
    }

    protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleType stNode = (org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleType)super.write(node);
        stNode.setAsXml(asIfXmlType.toString());
        return stNode;
    }

    private XmlName asIfXmlType;

    // typically the "as if" type is the closest base builtin type.
    public XmlName getAsIfXmlType()
    {
        return asIfXmlType;
    }

    public void setAsIfXmlType(XmlName asIfXmlType)
    {
        this.asIfXmlType = asIfXmlType;
    }
    
    // question: do we want an "as if Java type" as well?
    
    public BindingType getAsIfBindingType(BindingLoader loader)
    {
        return loader.getBindingType(getJavaName(), asIfXmlType);
    }
}
