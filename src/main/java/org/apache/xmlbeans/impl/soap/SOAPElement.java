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

package org.apache.xmlbeans.impl.soap;

import java.util.Iterator;

/**
 * An object representing the contents in a
 * {@code SOAPBody} object, the contents in a {@code SOAPHeader}
 * object, the content that can follow the {@code SOAPBody} object in a
 * {@code SOAPEnvelope} object, or what can follow the detail element
 * in a {@code SOAPFault} object. It is
 * the base class for all of the classes that represent the SOAP objects as
 * defined in the SOAP specification.
 */
public interface SOAPElement extends Node, org.w3c.dom.Element {

    /**
     * Creates a new {@code SOAPElement} object initialized with the
     * given {@code Name} object and adds the new element to this
     * {@code SOAPElement} object.
     * @param   name a {@code Name} object with the XML name for the
     *   new element
     * @return the new {@code SOAPElement} object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     {@code SOAPElement} object
     */
    SOAPElement addChildElement(Name name) throws SOAPException;

    /**
     * Creates a new {@code SOAPElement} object initialized with the
     * given {@code String} object and adds the new element to this
     * {@code SOAPElement} object.
     * @param   localName a {@code String} giving the local name for
     *     the element
     * @return the new {@code SOAPElement} object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     {@code SOAPElement} object
     */
    SOAPElement addChildElement(String localName)
        throws SOAPException;

    /**
     * Creates a new {@code SOAPElement} object initialized with the
     * specified local name and prefix and adds the new element to this
     * {@code SOAPElement} object.
     * @param   localName a {@code String} giving the local name for
     *   the new element
     * @param   prefix a {@code String} giving the namespace prefix for
     *   the new element
     * @return the new {@code SOAPElement} object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     {@code SOAPElement} object
     */
    SOAPElement addChildElement(String localName, String prefix)
        throws SOAPException;

    /**
     * Creates a new {@code SOAPElement} object initialized with the
     * specified local name, prefix, and URI and adds the new element to this
     * {@code SOAPElement} object.
     * @param   localName a {@code String} giving the local name for
     *   the new element
     * @param   prefix  a {@code String} giving the namespace prefix for
     *   the new element
     * @param   uri  a {@code String} giving the URI of the namespace
     *   to which the new element belongs
     * @return the new {@code SOAPElement} object that was created
     * @throws  SOAPException  if there is an error in creating the
     *                     {@code SOAPElement} object
     */
    SOAPElement addChildElement(
        String localName, String prefix, String uri) throws SOAPException;

    /**
     * Add a {@code SOAPElement} as a child of this
     * {@code SOAPElement} instance. The {@code SOAPElement}
     * is expected to be created by a
     * {@code SOAPElementFactory}. Callers should not rely on the
     * element instance being added as is into the XML
     * tree. Implementations could end up copying the content
     * of the {@code SOAPElement} passed into an instance of
     * a different {@code SOAPElement} implementation. For
     * instance if {@code addChildElement()} is called on a
     * {@code SOAPHeader}, {@code element} will be copied
     * into an instance of a {@code SOAPHeaderElement}.
     *
     * <P>The fragment rooted in {@code element} is either added
     * as a whole or not at all, if there was an error.
     *
     * <P>The fragment rooted in {@code element} cannot contain
     * elements named "Envelope", "Header" or "Body" and in the SOAP
     * namespace. Any namespace prefixes present in the fragment
     * should be fully resolved using appropriate namespace
     * declarations within the fragment itself.
     * @param   element the {@code SOAPElement} to be added as a
     *           new child
     * @return  an instance representing the new SOAP element that was
     *    actually added to the tree.
     * @throws  SOAPException if there was an error in adding this
     *                     element as a child
     */
    SOAPElement addChildElement(SOAPElement element)
        throws SOAPException;

    /**
     * Creates a new {@code Text} object initialized with the given
     * {@code String} and adds it to this {@code SOAPElement} object.
     * @param   text a {@code String} object with the textual content to be added
     * @return  the {@code SOAPElement} object into which
     *    the new {@code Text} object was inserted
     * @throws  SOAPException  if there is an error in creating the
     *               new {@code Text} object
     */
    SOAPElement addTextNode(String text) throws SOAPException;

    /**
     * Adds an attribute with the specified name and value to this
     * {@code SOAPElement} object.
     * <p>
     * @param   name a {@code Name} object with the name of the attribute
     * @param   value a {@code String} giving the value of the attribute
     * @return  the {@code SOAPElement} object into which the attribute was
     *    inserted
     * @throws  SOAPException  if there is an error in creating the
     *                     Attribute
     */
    SOAPElement addAttribute(Name name, String value)
        throws SOAPException;

    /**
     * Adds a namespace declaration with the specified prefix and URI to this
     * {@code SOAPElement} object.
     * <p>
     * @param   prefix a {@code String} giving the prefix of the namespace
     * @param  uri a {@code String} giving
     *     the prefix of the namespace
     * @return  the {@code SOAPElement} object into which this
     *     namespace declaration was inserted.
     * @throws  SOAPException  if there is an error in creating the
     *                     namespace
     */
    SOAPElement addNamespaceDeclaration(
        String prefix, String uri) throws SOAPException;

    /**
     * Returns the value of the attribute with the specified
     * name.
     * @param   name  a {@code Name} object with
     *     the name of the attribute
     * @return a {@code String} giving the value of the
     *     specified attribute
     */
    String getAttributeValue(Name name);

    /**
     * Returns an iterator over all of the attribute names in
     * this {@code SOAPElement} object. The iterator can be
     * used to get the attribute names, which can then be passed to
     * the method {@code getAttributeValue} to retrieve the
     * value of each attribute.
     * @return  an iterator over the names of the attributes
     */
    public abstract Iterator<Name> getAllAttributes();

    /**
     * Returns the URI of the namespace that has the given
     * prefix.
     *
     * @param prefix a {@code String} giving
     *     the prefix of the namespace for which to search
     * @return a {@code String} with the uri of the namespace
     *     that has the given prefix
     */
    String getNamespaceURI(String prefix);

    /**
     * Returns an iterator of namespace prefixes. The iterator
     * can be used to get the namespace prefixes, which can then be
     * passed to the method {@code getNamespaceURI} to retrieve
     * the URI of each namespace.
     * @return  an iterator over the namespace prefixes in this
     *     {@code SOAPElement} object
     */
    Iterator getNamespacePrefixes();

    /**
     * Returns the name of this {@code SOAPElement}
     * object.
     * @return  a {@code Name} object with the name of this
     *     {@code SOAPElement} object
     */
    Name getElementName();

    /**
     * Removes the attribute with the specified name.
     * @param   name  the {@code Name} object with
     *     the name of the attribute to be removed
     * @return {@code true} if the attribute was removed
     *     successfully; {@code false} if it was not
     */
    boolean removeAttribute(Name name);

    /**
     * Removes the namespace declaration corresponding to the
     * given prefix.
     * @param   prefix  a {@code String} giving
     *     the prefix for which to search
     * @return {@code true} if the namespace declaration was
     *     removed successfully; {@code false} if it was
     *     not
     */
    boolean removeNamespaceDeclaration(String prefix);

    /**
     * Returns an iterator over all the immediate content of
     * this element. This includes {@code Text} objects as well
     * as {@code SOAPElement} objects.
     * @return  an iterator with the content of this {@code
     *     SOAPElement} object
     */
    Iterator getChildElements();

    /**
     * Returns an iterator over all the child elements with the
     * specified name.
     * @param   name  a {@code Name} object with
     *     the name of the child elements to be returned
     * @return an {@code Iterator} object over all the elements
     *     in this {@code SOAPElement} object with the
     *     specified name
     */
    Iterator getChildElements(Name name);

    /**
     * Sets the encoding style for this {@code SOAPElement}
     * object to one specified.
     * @param   encodingStyle a {@code String}
     *     giving the encoding style
     * @throws  java.lang.IllegalArgumentException  if
     *     there was a problem in the encoding style being set.
     * @see #getEncodingStyle() getEncodingStyle()
     */
    void setEncodingStyle(String encodingStyle)
        throws SOAPException;

    /**
     * Returns the encoding style for this {@code
     * SOAPElement} object.
     * @return  a {@code String} giving the encoding style
     * @see #setEncodingStyle(java.lang.String) setEncodingStyle(java.lang.String)
     */
    String getEncodingStyle();

    /**
     * Detaches all children of this {@code SOAPElement}.
     * <p>
     * This method is useful for rolling back the construction of partially
     * completed {@code SOAPHeaders} and {@code SOAPBodys} in
     * reparation for sending a fault when an error condition is detected. It is
     * also useful for recycling portions of a document within a SOAP message.
     */
    void removeContents();

    /**
     * Returns an {@code Iterator} over the namespace prefix
     * {@code String}s visible to this element. The prefixes returned by
     * this iterator can be passed to the method {@code getNamespaceURI()}
     * to retrieve the URI of each namespace.
     *
     * @return an iterator over the namespace prefixes are within scope of this
     *              {@code SOAPElement} object
     */
    Iterator getVisibleNamespacePrefixes();
}
