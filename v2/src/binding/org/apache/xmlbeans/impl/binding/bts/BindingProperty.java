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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xml.xmlbeans.bindingConfig.JavaMethodName;

/**
 * Represents a property.  Every property corresponds to a
 * Java getter/setter or a field.  On the XML side, there
 * are different forms of properties, some which bind based
 * on sequencing, and others which bind based on name.
 */ 
public abstract class BindingProperty
{
    private BindingTypeName btName;
    private MethodName getter;
    private MethodName setter;
    private String field;
    private JavaTypeName collection;
    
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
    protected BindingProperty(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
    {
        this.btName = BindingTypeName.forPair(
                        JavaTypeName.forString(node.getJavatype()),
                        XmlTypeName.forString(node.getXmlcomponent()));
        this.getter = MethodName.create(node.getGetter());
        this.setter = MethodName.create(node.getSetter());
        this.field = node.getField();
        String collection = node.getCollection();
        if (collection != null)
            this.collection = JavaTypeName.forString(collection);
    }
    
    /**
     * This function copies an instance back out to the relevant part of the XML file.
     * 
     * Subclasses should override and call super.write first.
     */ 
    protected org.apache.xml.xmlbeans.bindingConfig.BindingProperty write(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
    {
        node = (org.apache.xml.xmlbeans.bindingConfig.BindingProperty)node.changeType(kinds.typeForClass(this.getClass()));
        
        node.setJavatype(btName.getJavaName().toString());
        node.setXmlcomponent(btName.getXmlName().toString());
        if (getFieldName() != null)
            node.setField(getFieldName());
        if (getGetterName() != null) {
          getGetterName().write(node.addNewGetter());
        }
        if (getSetterName() != null) {
          getSetterName().write(node.addNewSetter());
        }
        if (getCollectionClass() != null)
            node.setCollection(getCollectionClass().toString());
        return node;
    }
    
    public boolean isField()
    {
        return field != null;
    }
    
    public BindingTypeName getTypeName()
    {
        return btName;
    }
    
    public void setBindingType(BindingType bType)
    {
        btName = bType.getName();
    }
    
    public MethodName getGetterName()
    {
        return isField() ? null : getter;
    }
    
    public void setGetterName(MethodName mn)
    {
        getter = mn;
    }
    
    public boolean hasSetter()
    {
        return !isField() && setter != null;
    }
    
    public MethodName getSetterName()
    {
        return isField() ? null : setter;
    }
    
    public void setSetterName(MethodName mn)
    {
        setter = mn;
    }
    
    public String getFieldName()
    {
        return field;
    }
    
    public void setFieldName(String field)
    {
        this.field = field;
    }
    
    public JavaTypeName getCollectionClass()
    {
        return collection;
    }
    
    public void setCollectionClass(JavaTypeName jName)
    {
        collection = jName;
    }


    /* REGISTRY OF SUBCLASSES */
    
    private static final Class[] ctorArgs = new Class[] {org.apache.xml.xmlbeans.bindingConfig.BindingProperty.class};
    
    public static BindingProperty forNode(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
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
        if (!org.apache.xml.xmlbeans.bindingConfig.BindingProperty.type.isAssignableFrom(type))
            throw new IllegalArgumentException("Schema types must inherit from binding-property");
        kinds.registerClassAndType(clazz, type);
    }
    
    static
    {
        registerClassAndType(QNameProperty.class, org.apache.xml.xmlbeans.bindingConfig.QnameProperty.type);
        registerClassAndType(ParticleProperty.class, org.apache.xml.xmlbeans.bindingConfig.ParticleProperty.type);
    }

    public String toString()
    {
        return getClass().getName() + " [" + getGetterName().getSimpleName() + "]";
    }
}
