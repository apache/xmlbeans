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
 Represents a description of a factory that is used to create java objects
 */
public abstract class JavaInstanceFactory
{

    // ========================================================================
    // Variables

    // ========================================================================
    // Constructors

    /**
     * This kind of constructor is used when making a new one out of the blue.
     *
     * Subclasses should call super(..) when defining constructors that init new JavaInstanceFactory
     */
    protected JavaInstanceFactory()
    {
    }

    /**
     * This constructor loads an instance from an XML file
     *
     * Subclasses should have ctors of the same signature and call super(..) first.
     */
    protected JavaInstanceFactory(org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
    {

    }

    // ========================================================================
    // Protected methods

    /**
     * This function copies an instance back out to the relevant part of the XML file.
     *
     * Subclasses should override and call super.write first.
     */
    protected org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory write(org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
    {
        node = (org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory)node.changeType(kinds.typeForClass(this.getClass()));

        return node;
    }

    // ========================================================================
    // Public methods


    // ========================================================================
    // Static initialization

    /* REGISTRY OF SUBCLASSES */

    private static final Class[] ctorArgs =
        new Class[]{org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory.class};

    public static JavaInstanceFactory forNode(org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory node)
    {
        assert node != null;
        try {
            Class clazz = kinds.classForType(node.schemaType());
            return (JavaInstanceFactory)clazz.getConstructor(ctorArgs).newInstance(new Object[]{node});
        }
        catch (Exception e) {
            String msg = "Cannot load class for " + node.schemaType() +
                ": should be registered.";
            throw (IllegalStateException)new IllegalStateException(msg).initCause(e);
        }
    }


    /**
     * Should only be called by BindingFile, when loading up bindingtypes
     */
    static KindRegistry kinds = new KindRegistry();

    public static void registerClassAndType(Class clazz, SchemaType type)
    {
        if (!JavaInstanceFactory.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Classes must inherit from JavaInstanceFactory: " + clazz);
        if (!org.apache.xml.xmlbeans.bindingConfig.JavaInstanceFactory.type.isAssignableFrom(type))
            throw new IllegalArgumentException("Schema types must inherit from java-instance-factory");
        kinds.registerClassAndType(clazz, type);
    }

    static
    {
        registerClassAndType(ParentInstanceFactory.class, org.apache.xml.xmlbeans.bindingConfig.ParentInstanceFactory.type);
    }

}
