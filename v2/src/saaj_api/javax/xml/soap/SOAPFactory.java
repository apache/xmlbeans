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

/**
 * <code>SOAPFactory</code> is a factory for creating various objects
 * that exist in the SOAP XML tree.
 *
 * <code>SOAPFactory</code> can be
 * used to create XML fragments that will eventually end up in the
 * SOAP part. These fragments can be inserted as children of the
 * <code>SOAPHeaderElement</code> or <code>SOAPBodyElement</code> or
 * <code>SOAPEnvelope</code>.
 *
 * <code>SOAPFactory</code> also has methods to create
 * <code>javax.xml.soap.Detail</code> objects as well as
 * <code>java.xml.soap.Name</code> objects.
 *
 */
public abstract class SOAPFactory {

    public SOAPFactory() {}

    /**
     * Create a <code>SOAPElement</code> object initialized with the
     * given <code>Name</code> object.
     *
     * @param name a <code>Name</code> object with the XML name for
     *        the new element
     * @return  the new <code>SOAPElement</code> object that was
     *    created
     * @throws SOAPException if there is an error in creating the
     *       <code>SOAPElement</code> object
     */
    public abstract SOAPElement createElement(Name name) throws SOAPException;

    /**
     * Create a <code>SOAPElement</code> object initialized with the
     * given local name.
     *
     * @param localName a <code>String</code> giving the local name for
     *       the new element
     * @return the new <code>SOAPElement</code> object that was
     *    created
     * @throws SOAPException if there is an error in creating the
     *       <code>SOAPElement</code> object
     */
    public abstract SOAPElement createElement(String localName) throws SOAPException;

    /**
     * Create a new <code>SOAPElement</code> object with the given
     * local name, prefix and uri.
     *
     * @param localName a <code>String</code> giving the local name
     *            for the new element
     * @param prefix the prefix for this <code>SOAPElement</code>
     * @param uri a <code>String</code> giving the URI of the
     *      namespace to which the new element belongs
     * @return the new <code>SOAPElement</code> object that was
     *    created
     * @throws SOAPException if there is an error in creating the
     *      <code>SOAPElement</code> object
     */
    public abstract SOAPElement createElement(String localName, String prefix, String uri)
        throws SOAPException;

    /**
     * Creates a new <code>Detail</code> object which serves as a container
     * for <code>DetailEntry</code> objects.
     * <p>
     * This factory method creates <code>Detail</code> objects for use in
     * situations where it is not practical to use the <code>SOAPFault</code>
     * abstraction.
     *
     * @return a <code>Detail</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Detail createDetail() throws SOAPException;

    /**
     * Creates a new <code>Name</code> object initialized with the
     * given local name, namespace prefix, and namespace URI.
     * <p>
     * This factory method creates <code>Name</code> objects for use in
     * situations where it is not practical to use the <code>SOAPEnvelope</code>
     * abstraction.
     *
     * @param localName a <code>String</code> giving the local name
     * @param prefix a <code>String</code> giving the prefix of the namespace
     * @param uri a <code>String</code> giving the URI of the namespace
     * @return a <code>Name</code> object initialized with the given
     *   local name, namespace prefix, and namespace URI
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Name createName(String localName, String prefix, String uri)
        throws SOAPException;

    /**
     * Creates a new <code>Name</code> object initialized with the
     * given local name.
     * <p>
     * This factory method creates <code>Name</code> objects for use in
     * situations where it is not practical to use the <code>SOAPEnvelope</code>
     * abstraction.
     *
     * @param localName a <code>String</code> giving the local name
     * @return a <code>Name</code> object initialized with the given
     *    local name
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Name createName(String localName) throws SOAPException;

    /**
     * Creates a new instance of <code>SOAPFactory</code>.
     *
     * @return a new instance of a <code>SOAPFactory</code>
     * @throws SOAPException if there was an error creating the
     *       default <code>SOAPFactory</code>
     */
    public static SOAPFactory newInstance() throws SOAPException {

        try {
            return (SOAPFactory) FactoryFinder.find(SF_PROPERTY, DEFAULT_SF);
        } catch (Exception exception) {
            throw new SOAPException("Unable to create SOAP Factory: "
                                    + exception.getMessage());
        }
    }

    private static final String SF_PROPERTY = "javax.xml.soap.SOAPFactory";

    private static final String DEFAULT_SF =
        "org.apache.axis.soap.SOAPFactoryImpl";
}
