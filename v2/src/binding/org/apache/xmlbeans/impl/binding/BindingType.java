/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

import org.apache.xmlbeans.SchemaType;

public abstract class BindingType
{
    private JavaName jName;
    private XmlName xName;
    private boolean isXmlObj;
    
    /**
     * This kind of constructor is used when making a new one out of the blue.
     * 
     * Subclasses should call super(..) when defining constructors that init new BindingTypes.
     */ 
    protected BindingType(JavaName jName, XmlName xName, boolean isXmlObj)
    {
        this.jName = jName;
        this.xName = xName;
        this.isXmlObj = isXmlObj;
    }
    
    /**
     * This constructor loads an instance from an XML file
     * 
     * Subclasses should have ctors of the same signature and call super(..) first.
     */ 
    protected BindingType(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        this.jName = JavaName.forString(node.getJavatype());
        this.xName = XmlName.forString(node.getXmlcomponent());
        this.isXmlObj = node.getXmlobj();
    }
    
    /**
     * This function copies an instance back out to the relevant part of the XML file.
     * 
     * Subclasses should override and call super.write first.
     */ 
    protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        node = (org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType)node.changeType(kinds.typeForClass(this.getClass()));
        node.setJavatype(jName.toString());
        node.setXmlcomponent(xName.toString());
        node.setXmlobj(isXmlObj);
        return node;
    }
    
    public final JavaName getJavaName()
    {
        return jName;
    }
    
    public final XmlName getXmlName()
    {
        return xName;
    }
    
    public final boolean isXmlObject()
    {
        return isXmlObj;
    }
    
    
    /* REGISTRY OF SUBCLASSES */
    
    private static final Class[] ctorArgs = new Class[] {org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType.class};
    
    public static BindingType loadFromBindingTypeNode(BindingLoader bLoader, org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType node)
    {
        try
        {
            Class clazz = kinds.classForType(node.schemaType());
            return (BindingType)clazz.getConstructor(ctorArgs).newInstance(new Object[] {node});
        }
        catch (Exception e)
        {
            throw (IllegalStateException)new IllegalStateException("Cannot load class for " + node.schemaType() + ": should be registered.").initCause(e);
        }
    }
    
    /**
     * Should only be called by BindingFile, when loading up bindingtypes
     */
    static KindRegistry kinds = new KindRegistry();
    
    public static void registerClassAndType(Class clazz, SchemaType type)
    {
        if (!BindingType.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Classes must inherit from BindingType");
        if (!org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType.type.isAssignableFrom(type))
            throw new IllegalArgumentException("Schema types must inherit from binding-type");
        kinds.registerClassAndType(clazz, type);
    }
    
    static
    {
        registerClassAndType(JaxbBean.class, org.apache.xmlbeans.x2003.x09.bindingConfig.JaxbBean.type);
        registerClassAndType(ByNameBean.class, org.apache.xmlbeans.x2003.x09.bindingConfig.ByNameBean.type);
        registerClassAndType(SimpleBindingType.class, org.apache.xmlbeans.x2003.x09.bindingConfig.SimpleType.type);
    }

}
