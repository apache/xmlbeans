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
import java.util.Map;

/**
 * Represents an identity constraint definition.
 */ 
public interface SchemaIdentityConstraint extends SchemaComponent, SchemaAnnotated
{
    /**
     * Return the selector xpath as a string.
     */
    String getSelector();

    /**
     * Return a compiled xpath object for the selector.
     */
    Object getSelectorPath();

    /**
     * Return (a copy of) the xpaths for all the fields.
     */
    String[] getFields();

    /**
     * Return a compiled xpath object for the field.
     */
    Object getFieldPath(int index);

    /**
     * Return a read-only copy of the namespace map. This is the 
     * set of prefix to URI mappings that were in scope in the
     * schema at the point at which this constraint was declared
     */
    Map getNSMap();

    /** A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-key">xs:key</a> constraint.  See {@link #getConstraintCategory}. */
    public static final int CC_KEY = 1;
    /** A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-key">xs:keyRef</a> constraint.  See {@link #getConstraintCategory}. */
    public static final int CC_KEYREF = 2;
    /** A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-key">xs:unique</a> constraint.  See {@link #getConstraintCategory}. */
    public static final int CC_UNIQUE = 3;

    /**
     * Return the constraint category. Either {@link #CC_KEY}, {@link #CC_KEYREF},
     * or {@link #CC_UNIQUE}.
     */
    int getConstraintCategory();

    /**
     * Returns the key that a key ref refers to. Only valid for
     * keyrefs.
     */
    SchemaIdentityConstraint getReferencedKey();

    /**
     * Used to allow on-demand loading of identity constraints.
     * 
     * @exclude
     */
    public static final class Ref extends SchemaComponent.Ref
    {
        public Ref(SchemaIdentityConstraint idc)
            { super(idc); }

        public Ref(SchemaTypeSystem system, String handle)
            { super(system, handle); }

        public final int getComponentType()
            { return SchemaComponent.IDENTITY_CONSTRAINT; }

        public final SchemaIdentityConstraint get()
            { return (SchemaIdentityConstraint)getComponent(); }
    }
}
