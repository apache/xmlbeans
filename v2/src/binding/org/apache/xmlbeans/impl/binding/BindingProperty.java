/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding;

import org.apache.xmlbeans.SchemaType;

public abstract class BindingProperty
{
    private JavaName tJava;
    protected XmlName tXml;
    private String getter;
    private String setter;
    private String field;
    private JavaName collection;
    
    /**
     * This kind of constructor is used when making a new one out of the blue.
     * 
     * Subclasses should call super(..) when defining constructors that init new BindingTypes.
     */ 
    protected BindingProperty()
    {
    }
    
    /**
     * This constructor loads an instance from an XML file
     * 
     * Subclasses should have ctors of the same signature and call super(..) first.
     */ 
    protected BindingProperty(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty node)
    {
        this.tJava = JavaName.forString(node.getJavatype());
        this.tXml = XmlName.forString(node.getXmlcomponent());
        this.getter = node.getGetter();
        this.setter = node.getSetter();
        this.field = node.getField();
        String collection = node.getCollection();
        if (collection != null)
            this.collection = JavaName.forString(collection);
    }
    
    /**
     * This function copies an instance back out to the relevant part of the XML file.
     * 
     * Subclasses should override and call super.write first.
     */ 
    protected org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty node)
    {
        node = (org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty)node.changeType(kinds.typeForClass(this.getClass()));
        
        node.setJavatype(tJava.toString());
        node.setXmlcomponent(tXml.toString());
        if (getFieldName() != null)
            node.setField(getFieldName());
        if (getGetterName() != null)
            node.setGetter(getGetterName());
        if (getSetterName() != null)
            node.setSetter(getSetterName());
        if (getCollectionClass() != null)
            node.setCollection(getCollectionClass().toString());
        return node;
    }
    
    public boolean isField()
    {
        return field != null;
    }
    
    public BindingType getBindingType(BindingLoader loader)
    {
        return loader.getBindingType(tJava, tXml);
    }
    
    public void setBindingType(BindingType bType)
    {
        this.tJava = bType.getJavaName();
        this.tXml = bType.getXmlName();
    }
    
    public String getGetterName()
    {
        return isField() ? null : getter;
    }
    
    public void setGetterName(String getter)
    {
        this.getter = getter;
    }
    
    public boolean hasSetter()
    {
        return !isField() && setter != null;
    }
    
    public String getSetterName()
    {
        return isField() ? null : setter;
    }
    
    public void setSetterName(String setter)
    {
        this.setter = setter; 
    }
    
    public String getFieldName()
    {
        return field;
    }
    
    public void setFieldName(String field)
    {
        this.field = field;
    }
    
    public JavaName getCollectionClass()
    {
        return collection;
    }
    
    public void setCollectionClass(JavaName jName)
    {
        collection = jName;
    }
    
    /* REGISTRY OF SUBCLASSES */
    
    private static final Class[] ctorArgs = new Class[] {org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty.class};
    
    public static BindingProperty forNode(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty node)
    {
        try
        {
            Class clazz = kinds.classForType(node.schemaType());
            return (BindingProperty)clazz.getConstructor(ctorArgs).newInstance(new Object[] {node});
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
        if (!BindingProperty.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Classes must inherit from BindingProperty");
        if (!org.apache.xmlbeans.x2003.x09.bindingConfig.BindingProperty.type.isAssignableFrom(type))
            throw new IllegalArgumentException("Schema types must inherit from binding-property");
        kinds.registerClassAndType(clazz, type);
    }
    
    static
    {
        registerClassAndType(QNameProperty.class, org.apache.xmlbeans.x2003.x09.bindingConfig.QnameProperty.type);
        registerClassAndType(ParticleProperty.class, org.apache.xmlbeans.x2003.x09.bindingConfig.ParticleProperty.type);
    }
}
