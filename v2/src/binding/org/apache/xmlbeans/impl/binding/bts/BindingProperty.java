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

import org.apache.xmlbeans.SchemaType;

/**
 * Represents a property.  Every property corresponds to a
 * Java getter/setter or a field.  On the XML side, there
 * are different forms of properties, some which bind based
 * on sequencing, and others which bind based on name.
 */
public abstract class BindingProperty
{

    // ========================================================================
    // Variables

    private BindingTypeName btName;
    private MethodName getter;
    private MethodName setter;
    private MethodName issetter;
    private String field;
    private JavaTypeName collection;
    protected JavaInstanceFactory javaInstanceFactory;

    // ========================================================================
    // Constructors

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
        this.issetter = MethodName.create(node.getIssetter());
        this.field = node.getField();
        String collection = node.getCollection();
        if (collection != null)
            this.collection = JavaTypeName.forString(collection);

        final org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory factory =
            node.getFactory();
        if (factory != null) {
            javaInstanceFactory = JavaInstanceFactory.forNode(factory);
        }

    }

    // ========================================================================
    // Protected methods

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

        if (javaInstanceFactory != null) {
            org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory jif_node =
                node.addNewFactory();
            javaInstanceFactory.write(jif_node);
        }

        return node;
    }

    // ========================================================================
    // Public methods

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

    public boolean hasIssetter()
    {
        return !isField() && issetter != null;
    }

    public MethodName getIssetterName()
    {
        return isField() ? null : issetter;
    }

    public void setIssetterName(MethodName mn)
    {
        issetter = mn;
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

    // ========================================================================
    // Static initialization

    /* REGISTRY OF SUBCLASSES */

    private static final Class[] ctorArgs = new Class[]{org.apache.xml.xmlbeans.bindingConfig.BindingProperty.class};

    public static BindingProperty forNode(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node)
    {
        try {
            Class clazz = kinds.classForType(node.schemaType());
            return (BindingProperty)clazz.getConstructor(ctorArgs).newInstance(new Object[]{node});
        }
        catch (Exception e) {
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
        registerClassAndType(SimpleContentProperty.class, org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty.type);
        registerClassAndType(ParticleProperty.class, org.apache.xml.xmlbeans.bindingConfig.ParticleProperty.type);
        registerClassAndType(GenericXmlProperty.class, org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty.type);
    }

    public String toString()
    {
        return getClass().getName() + " [" + getGetterName().getSimpleName() + "]";
    }

    public JavaInstanceFactory getJavaInstanceFactory()
    {
        return javaInstanceFactory;
    }

    public void setJavaInstanceFactory(JavaInstanceFactory javaInstanceFactory)
    {
        this.javaInstanceFactory = javaInstanceFactory;
    }
}
