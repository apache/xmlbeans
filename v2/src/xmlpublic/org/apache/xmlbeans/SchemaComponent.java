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
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans;

import javax.xml.namespace.QName;

/**
 * Represents a global Schema Component.  That is, a type, element, attribute,
 * model group, attribute group, or identity constraint.
 * <p>
 * Note that not all types, elements, and attributes are global; local
 * types, element, and attributes do not appear in the global lookup table.
 * Also note that other information items such as particles, facets, and
 * so on are not globally indexed, so are not SchemaComponents.
 * 
 * @see SchemaType
 * @see SchemaGlobalElement
 * @see SchemaGlobalAttribute
 * @see SchemaAttributeGroup
 * @see SchemaModelGroup
 * @see SchemaIdentityConstraint
 */
public interface SchemaComponent
{
    /** A type definition.  See {@link #getComponentType} */
    static final int TYPE = 0;
    /** An element definition.  See {@link #getComponentType} */
    static final int ELEMENT = 1;
    /** An attribute definition.  See {@link #getComponentType} */
    static final int ATTRIBUTE = 3;
    /** An attribute group definition.  See {@link #getComponentType} */
    static final int ATTRIBUTE_GROUP = 4;
    /** An identity constraint definition.  See {@link #getComponentType} */
    static final int IDENTITY_CONSTRAINT = 5;
    /** A model group definition.  See {@link #getComponentType} */
    static final int MODEL_GROUP = 6;
    /** A notation definition.  See {@link #getComponentType} */
    static final int NOTATION = 7;

    /**
     * Returns the type code for the schema object, either {@link #TYPE},
     * {@link #ELEMENT}, {@link #ATTRIBUTE}, {@link #ATTRIBUTE_GROUP},
     * {@link #MODEL_GROUP}, {@link #IDENTITY_CONSTRAINT}, or {@link #NOTATION}.
     */
    int getComponentType();

    /**
     * Returns the typesystem within which this component definition resides
     */
    SchemaTypeSystem getTypeSystem();

    /**
     * The name of the schema component
     */
    QName getName();

    /**
     * A lazy reference to a component. Used by SchemaTypeLoaders to
     * avoid loading components until they are actually needed.
     * 
     * @exclude
     */
    public static abstract class Ref
    {
        protected Ref(SchemaComponent schemaComponent)
            { _schemaComponent = schemaComponent; }

        protected Ref(SchemaTypeSystem schemaTypeSystem, String handle)
            { assert(handle != null); _schemaTypeSystem = schemaTypeSystem; _handle = handle; }

        private SchemaComponent _schemaComponent;
        private SchemaTypeSystem _schemaTypeSystem;
        public String _handle;

        public abstract int getComponentType();

        public final SchemaTypeSystem getTypeSystem()
            { return _schemaTypeSystem; }

        public final SchemaComponent getComponent()
        {
            if (_schemaComponent == null && _handle != null)
                _schemaComponent = _schemaTypeSystem.resolveHandle(_handle);

            return _schemaComponent;
        }
    }
    
    /**
     * Used for on-demand loading of schema components.
     */ 
    public Ref getComponentRef();
}
