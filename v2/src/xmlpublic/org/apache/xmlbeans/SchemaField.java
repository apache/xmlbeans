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

import java.math.BigInteger;

/**
 * Represents an element or an attribute declaration.
 * 
 * @see SchemaType#getContainerField
 * @see SchemaLocalElement
 * @see SchemaLocalAttribute
 */ 
public interface SchemaField
{
    /**
     * Returns the form-unqualified-or-qualified name.
     */
    QName getName();

    /**
     * True if this use is an attribute
     */
    boolean isAttribute();

    /**
     * True if nillable; always false for attributes.
     */
    boolean isNillable();

    /**
     * Returns the type of this use.
     */
    SchemaType getType();

    /**
     * Returns the minOccurs value for this particle.
     * If it is not specified explicitly, this defaults to BigInteger.ONE.
     */
    BigInteger getMinOccurs();

    /**
     * Returns the maxOccurs value for this particle, or null if it
     * is unbounded.
     * If it is not specified explicitly, this defaults to BigInteger.ONE.
     */
    BigInteger getMaxOccurs();

    /**
     * The default value as plain text. See {@link #isDefault} and {@link #isFixed}.
     */
    String getDefaultText();
    
    /**
     * The default value as a strongly-typed value.  See {@link #isDefault} and {@link #isFixed}.
     */
    XmlAnySimpleType getDefaultValue();

    /**
     * True if a default is supplied. If {@link #isFixed}, then isDefault is always true.
     */
    boolean isDefault();

    /**
     * True if the value is fixed.
     */
    boolean isFixed();
}
