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

import java.math.BigInteger;

import javax.xml.namespace.QName;

/**
 * Represents a Schema particle definition.
 * <p>
 * The content model of a complex type is a tree of particles.  Each
 * particle is either an {@link #ALL}, {@link #CHOICE}, {@link #SEQUENCE},
 * {@link #ELEMENT}, or {@link #WILDCARD}.
 * All, choice and sequence particles are groups that can have child
 * particles; elements and wildcards are always leaves of the particle tree.
 * <p>
 * The tree of particles available on a schema type is minimized, that
 * is, it already has removed "pointless" particles such as empty
 * sequences, nonrepeating sequences with only one item, and so on.
 * (<a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#cos-particle-restrict">Pointless particles</a>
 * are defined precisely in the XML Schema specification.)
 * 
 * @see SchemaType#getContentModel
 */
public interface SchemaParticle
{
    /**
     * Returns the particle type ({@link #ALL}, {@link #CHOICE},
     * {@link #SEQUENCE}, {@link #ELEMENT}, or {@link #WILDCARD}). 
     */ 
    int getParticleType();
    
    /**
     * An <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-contentModel">xs:all</a> group.
     * See {@link #getParticleType}.
     */ 
    static final int ALL = 1;
    /**
     * A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-contentModel">xs:choice</a> group.
     * See {@link #getParticleType}.
     */ 
    static final int CHOICE = 2;
    /**
     * A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-contentModel">xs:sequence</a> group.
     * See {@link #getParticleType}.
     */ 
    static final int SEQUENCE = 3;
    /**
     * An <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-element">xs:element</a> particle.
     * This code means the particle can be coerced to {@link SchemaLocalElement}.
     * See {@link #getParticleType}.
     */ 
    static final int ELEMENT = 4;
    /**
     * An <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-openness">xs:any</a> particle,
     * also known as an element wildcard.
     * See {@link #getParticleType}.
     */ 
    static final int WILDCARD = 5;

    /**
     * Returns the minOccurs value for this particle.
     * If it's not specified explicitly, this returns BigInteger.ONE.
     */
    BigInteger getMinOccurs();

    /**
     * Returns the maxOccurs value for this particle, or null if it
     * is unbounded.
     * If it's not specified explicitly, this returns BigInteger.ONE.
     */
    BigInteger getMaxOccurs();

    /**
     * Returns the minOccurs value, pegged to a 32-bit int for
     * convenience of a validating state machine that doesn't count
     * higher than MAX_INT anyway.
     */
    public int getIntMinOccurs();

    /**
     * Returns the maxOccurs value, pegged to a 32-bit int for
     * convenience of a validating state machine that doesn't count
     * higher than MAX_INT anyway. Unbounded is given as MAX_INT.
     */
    public int getIntMaxOccurs();


    /**
     * One if minOccurs == maxOccurs == 1.
     */
    boolean isSingleton();

    /**
     * Applies to sequence, choice, and all particles only: returns an array
     * of all the particle children in order.
     */
    SchemaParticle[] getParticleChildren();

    /**
     * Another way to access the particle children.
     */
    SchemaParticle getParticleChild(int i);

    /**
     * The number of children.
     */
    int countOfParticleChild();

    /**
     * True if this particle can start with the given element
     * (taking into account the structure of all child particles
     * of course).
     */
    boolean canStartWithElement(QName name);

    /**
     * Returns the QNameSet of element names that can be
     * accepted at the beginning of this particle.
     */
    QNameSet acceptedStartNames();

    /**
     * True if this particle can be skipped (taking into account
     * both the minOcurs as well as the structure of all the
     * child particles)
     */
    boolean isSkippable();

    /**
     * For wildcards, returns a QNameSet representing the wildcard.
     */
    QNameSet getWildcardSet();

    /**
     * For wildcards, returns the processing code ({@link #STRICT}, {@link #LAX}, {@link #SKIP}).
     */
    int getWildcardProcess();

    /** <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Wildcard_details">Strict wildcard</a> processing. See {@link #getWildcardProcess} */
    static final int STRICT = 1;
    /** <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Wildcard_details">Lax wildcard</a> processing. See {@link #getWildcardProcess} */
    static final int LAX = 2;
    /** <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Wildcard_details">Skip wildcard</a> processing. See {@link #getWildcardProcess} */
    static final int SKIP = 3;

    /**
     * For elements only: the QName for the element use.
     * May be unqualified version of referenced element's name.
     */
    QName getName();
    
    /**
     * For elements only: returns the type of the element.
     */
    SchemaType getType();

    /**
     * For elements only: true if nillable.
     */
    boolean isNillable();

    /**
     * For elements only: returns the default (or fixed) text value
     */
    String getDefaultText();
    
    /**
     * For elements only: returns the default (or fixed) strongly-typed value
     */
    XmlAnySimpleType getDefaultValue();

    /**
     * For elements only: True if has default. If isFixed, then isDefault is always true.
     */
    boolean isDefault();

    /**
     * For elements only: true if is fixed value.
     */
    boolean isFixed();
    
}
