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

import java.util.Set;

/**
 * Represents a lattice of finite and infinite sets of QNames.
 * 
 * @see QNameSet
 */
public interface QNameSetSpecification
{
    /**
     * True if the set contains the given QName.
     *
     * Roughly equivalent to:
     *    (includedURIs() == null ?
     *           excludedURIs().contains(namespace) :
     *           includedURIs().contains(namespace)
     *    ) ?
     *        !excludedQNamesInIncludedURIs().contains(name) :
     *         includedQNamesInExcludedURIs().contains(name)
     */
    boolean contains(QName name);

    /**
     * True if the set is the set of all QNames.
     */
    boolean isAll();

    /**
     * True if the set is empty.
     */
    boolean isEmpty();

    /**
     * True if the parameter is a subset of this set.
     */ 
    boolean containsAll(QNameSetSpecification set);
    
    /**
     * True if is disjoint from the specified set.
     */
    boolean isDisjoint(QNameSetSpecification set);

    /**
     * Returns the intersection with another QNameSet.
     */
    QNameSet intersect(QNameSetSpecification set);

    /**
     * Returns the union with another QNameSet.
     */
    QNameSet union(QNameSetSpecification set);

    /**
     * Return the inverse of this QNameSet. That is the QNameSet which
     * contains all the QNames not contained in this set. In other words
     * for which set.contains(name) != set.inverse().contains(name) for
     * all names.
     */
    QNameSet inverse();

    /**
     * The finite set of namespace URIs that are almost completely excluded from
     * the set (that is, each namespace URI that included in the set with with
     * a finite number of QName exceptions). Null if the set of namespaceURIs
     * that are almost completely included is infinite.
     * <p>
     * Null (meaning almost all URIs excluded) if includedURIs() is non-null;
     * non-null otherwise.
     * <p>
     * The same set as inverse().includedURIs().
     */
    Set excludedURIs();

    /**
     * The finite set of namespace URIs that are almost completely included in
     * the set (that is, each namespace URI that included in the set with with
     * a finite number of QName exceptions). Null if the set of namespaceURIs
     * that are almost completely included is infinite.
     * <p>
     * Null (meaning almost all URIs included) if excludedURIs() is non-null;
     * non-null otherwise.
     * <p>
     * The same as inverse.excludedURIs().
     */
    Set includedURIs();

    /**
     * The finite set of QNames that are excluded from the set within namespaces
     * that are otherwise included. Should only contain QNames within namespace
     * that are within the set includedURIs() (or any URI, if includedURIs()
     * is null, which means that all URIs are almost completely included).
     * <p>
     * Never null.
     * <p>
     * The same set as inverse().includedQNames().
     */
    Set excludedQNamesInIncludedURIs();

    /**
     * The finite set of QNames that are included in the set within namespaces
     * that are otherwise excluded. Should only contain QNames within namespace
     * that are within the set excludedURIs() (or any URI, if excludedURIs()
     * is null, which means that all URIs are almost completely excluded).
     * <p>
     * Never null.
     * <p>
     * The same as inverse().excludedQNames().
     */
    Set includedQNamesInExcludedURIs();
}
