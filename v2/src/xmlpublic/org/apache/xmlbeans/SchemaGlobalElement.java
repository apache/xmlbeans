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
 * Represents a global element definition.
 * 
 * @see SchemaTypeLoader#findElement
 */
public interface SchemaGlobalElement extends SchemaLocalElement, SchemaComponent
{
    /**
     * The name of the source .xsd file within which this attribute was defined
     */ 
    String getSourceName();

    /**
     * Set of QNames for elements that are the members of the
     * substitution group for which this element is the head,
     * not including this element.
     */
    QName[] substitutionGroupMembers();

    /**
     * The element that is the head of this element's substitution
     * group, or <code>null</code> if this element is not a member
     * of a substitution group.
     */
    SchemaGlobalElement substitutionGroup();

    /**
     * True if using this element as the head of a substitution
     * group for a substitution via type extension is prohibited.
     * If both finalExtension and finalRestriction are true, this
     * element cannot be head of a substitution group.
     * Sensible only for global elements.
     */
    public boolean finalExtension();

    /**
     * True if using this element as the head of a substitution
     * group for a substitution via type restriction is prohibited.
     * If both finalExtension and finalRestriction are true, this
     * element cannot be head of a substitution group.
     * Sensible only for global elements.
     */
    public boolean finalRestriction();

    /**
     * Used to allow on-demand loading of elements.
     * 
     * @exclude
     */
    public final static class Ref extends SchemaComponent.Ref
    {
        public Ref(SchemaGlobalElement element)
            { super(element); }

        public Ref(SchemaTypeSystem system, String handle)
            { super(system, handle); }

        public final int getComponentType()
            { return SchemaComponent.ELEMENT; }

        public final SchemaGlobalElement get()
            { return (SchemaGlobalElement)getComponent(); }
    }

    /**
     * Retruns a SchemaGlobalElement.Ref pointing to this element itself.
     */
    public Ref getRef();


}
