/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package javax.xml.soap;

import java.util.Iterator;

/**
 * An object representing the contents in a
 * <code>SOAPBody</code> object, the contents in a <code>SOAPHeader</code>
 * object, the content that can follow the <code>SOAPBody</code> object in a
 * <code>SOAPEnvelope</code> object, or what can follow the detail element
 * in a <code>SOAPFault</code> object. It is
 * the base class for all of the classes that represent the SOAP objects as
 * defined in the SOAP specification.
 */
public interface SOAPElement extends Node, org.w3c.dom.Element {

    /**
     * Creates a new <code>SOAPElement</code> object initialized with the
     * given <code>Name</code> object and adds the new element to this
     * <code>SOAPElement</code> object.
     * @param   name a <code>Name</code> object with the XML name for the
     *   new element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     <code>SOAPElement</code> object
     */
    public abstract SOAPElement addChildElement(Name name) throws SOAPException;

    /**
     * Creates a new <code>SOAPElement</code> object initialized with the
     * given <code>String</code> object and adds the new element to this
     * <code>SOAPElement</code> object.
     * @param   localName a <code>String</code> giving the local name for
     *     the element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     <code>SOAPElement</code> object
     */
    public abstract SOAPElement addChildElement(String localName)
        throws SOAPException;

    /**
     * Creates a new <code>SOAPElement</code> object initialized with the
     * specified local name and prefix and adds the new element to this
     * <code>SOAPElement</code> object.
     * @param   localName a <code>String</code> giving the local name for
     *   the new element
     * @param   prefix a <code>String</code> giving the namespace prefix for
     *   the new element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     <code>SOAPElement</code> object
     */
    public abstract SOAPElement addChildElement(String localName, String prefix)
        throws SOAPException;

    /**
     * Creates a new <code>SOAPElement</code> object initialized with the
     * specified local name, prefix, and URI and adds the new element to this
     * <code>SOAPElement</code> object.
     * @param   localName a <code>String</code> giving the local name for
     *   the new element
     * @param   prefix  a <code>String</code> giving the namespace prefix for
     *   the new element
     * @param   uri  a <code>String</code> giving the URI of the namespace
     *   to which the new element belongs
     * @return the new <code>SOAPElement</code> object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     <code>SOAPElement</code> object
     */
    public abstract SOAPElement addChildElement(
        String localName, String prefix, String uri) throws SOAPException;

    /**
     * Add a <code>SOAPElement</code> as a child of this
     * <code>SOAPElement</code> instance. The <code>SOAPElement</code>
     * is expected to be created by a
     * <code>SOAPElementFactory</code>. Callers should not rely on the
     * element instance being added as is into the XML
     * tree. Implementations could end up copying the content
     * of the <code>SOAPElement</code> passed into an instance of
     * a different <code>SOAPElement</code> implementation. For
     * instance if <code>addChildElement()</code> is called on a
     * <code>SOAPHeader</code>, <code>element</code> will be copied
     * into an instance of a <code>SOAPHeaderElement</code>.
     *
     * <P>The fragment rooted in <code>element</code> is either added
     * as a whole or not at all, if there was an error.
     *
     * <P>The fragment rooted in <code>element</code> cannot contain
     * elements named "Envelope", "Header" or "Body" and in the SOAP
     * namespace. Any namespace prefixes present in the fragment
     * should be fully resolved using appropriate namespace
     * declarations within the fragment itself.
     * @param   element the <code>SOAPElement</code> to be added as a
     *           new child
     * @return  an instance representing the new SOAP element that was
     *    actually added to the tree.
     * @throws  SOAPException if there was an error in adding this
     *                     element as a child
     */
    public abstract SOAPElement addChildElement(SOAPElement element)
        throws SOAPException;

    /**
     * Creates a new <code>Text</code> object initialized with the given
     * <code>String</code> and adds it to this <code>SOAPElement</code> object.
     * @param   text a <code>String</code> object with the textual content to be added
     * @return  the <code>SOAPElement</code> object into which
     *    the new <code>Text</code> object was inserted
     * @throws  SOAPException  if there is an error in creating the
     *               new <code>Text</code> object
     */
    public abstract SOAPElement addTextNode(String text) throws SOAPException;

    /**
     * Adds an attribute with the specified name and value to this
     * <code>SOAPElement</code> object.
     * <p>
     * @param   name a <code>Name</code> object with the name of the attribute
     * @param   value a <code>String</code> giving the value of the attribute
     * @return  the <code>SOAPElement</code> object into which the attribute was
     *    inserted
     * @throws  SOAPException  if there is an error in creating the
     *                     Attribute
     */
    public abstract SOAPElement addAttribute(Name name, String value)
        throws SOAPException;

    /**
     * Adds a namespace declaration with the specified prefix and URI to this
     * <code>SOAPElement</code> object.
     * <p>
     * @param   prefix a <code>String</code> giving the prefix of the namespace
     * @param  uri a <CODE>String</CODE> giving
     *     the prefix of the namespace
     * @return  the <code>SOAPElement</code> object into which this
     *     namespace declaration was inserted.
     * @throws  SOAPException  if there is an error in creating the
     *                     namespace
     */
    public abstract SOAPElement addNamespaceDeclaration(
        String prefix, String uri) throws SOAPException;

    /**
     * Returns the value of the attribute with the specified
     * name.
     * @param   name  a <CODE>Name</CODE> object with
     *     the name of the attribute
     * @return a <CODE>String</CODE> giving the value of the
     *     specified attribute
     */
    public abstract String getAttributeValue(Name name);

    /**
     * Returns an iterator over all of the attribute names in
     * this <CODE>SOAPElement</CODE> object. The iterator can be
     * used to get the attribute names, which can then be passed to
     * the method <CODE>getAttributeValue</CODE> to retrieve the
     * value of each attribute.
     * @return  an iterator over the names of the attributes
     */
    public abstract Iterator getAllAttributes();

    /**
     * Returns the URI of the namespace that has the given
     * prefix.
     *
     * @param prefix a <CODE>String</CODE> giving
     *     the prefix of the namespace for which to search
     * @return a <CODE>String</CODE> with the uri of the namespace
     *     that has the given prefix
     */
    public abstract String getNamespaceURI(String prefix);

    /**
     * Returns an iterator of namespace prefixes. The iterator
     * can be used to get the namespace prefixes, which can then be
     * passed to the method <CODE>getNamespaceURI</CODE> to retrieve
     * the URI of each namespace.
     * @return  an iterator over the namespace prefixes in this
     *     <CODE>SOAPElement</CODE> object
     */
    public abstract Iterator getNamespacePrefixes();

    /**
     * Returns the name of this <CODE>SOAPElement</CODE>
     * object.
     * @return  a <CODE>Name</CODE> object with the name of this
     *     <CODE>SOAPElement</CODE> object
     */
    public abstract Name getElementName();

    /**
     * Removes the attribute with the specified name.
     * @param   name  the <CODE>Name</CODE> object with
     *     the name of the attribute to be removed
     * @return <CODE>true</CODE> if the attribute was removed
     *     successfully; <CODE>false</CODE> if it was not
     */
    public abstract boolean removeAttribute(Name name);

    /**
     * Removes the namespace declaration corresponding to the
     * given prefix.
     * @param   prefix  a <CODE>String</CODE> giving
     *     the prefix for which to search
     * @return <CODE>true</CODE> if the namespace declaration was
     *     removed successfully; <CODE>false</CODE> if it was
     *     not
     */
    public abstract boolean removeNamespaceDeclaration(String prefix);

    /**
     * Returns an iterator over all the immediate content of
     * this element. This includes <CODE>Text</CODE> objects as well
     * as <CODE>SOAPElement</CODE> objects.
     * @return  an iterator with the content of this <CODE>
     *     SOAPElement</CODE> object
     */
    public abstract Iterator getChildElements();

    /**
     * Returns an iterator over all the child elements with the
     * specified name.
     * @param   name  a <CODE>Name</CODE> object with
     *     the name of the child elements to be returned
     * @return an <CODE>Iterator</CODE> object over all the elements
     *     in this <CODE>SOAPElement</CODE> object with the
     *     specified name
     */
    public abstract Iterator getChildElements(Name name);

    /**
     * Sets the encoding style for this <CODE>SOAPElement</CODE>
     * object to one specified.
     * @param   encodingStyle a <CODE>String</CODE>
     *     giving the encoding style
     * @throws  java.lang.IllegalArgumentException  if
     *     there was a problem in the encoding style being set.
     * @see #getEncodingStyle() getEncodingStyle()
     */
    public abstract void setEncodingStyle(String encodingStyle)
        throws SOAPException;

    /**
     * Returns the encoding style for this <CODE>
     * SOAPElement</CODE> object.
     * @return  a <CODE>String</CODE> giving the encoding style
     * @see #setEncodingStyle(java.lang.String) setEncodingStyle(java.lang.String)
     */
    public abstract String getEncodingStyle();

    /**
     * Detaches all children of this <code>SOAPElement</code>.
     * <p>
     * This method is useful for rolling back the construction of partially
     * completed <code>SOAPHeaders</code> and <code>SOAPBodys</code> in
     * reparation for sending a fault when an error condition is detected. It is
     * also useful for recycling portions of a document within a SOAP message.
     */
    public abstract void removeContents();

    /**
     * Returns an <code>Iterator</code> over the namespace prefix
     * <code>String</code>s visible to this element. The prefixes returned by
     * this iterator can be passed to the method <code>getNamespaceURI()</code>
     * to retrieve the URI of each namespace.
     *
     * @return an iterator over the namespace prefixes are within scope of this
     *              <code>SOAPElement</code> object
     */
    public abstract Iterator getVisibleNamespacePrefixes();
}
