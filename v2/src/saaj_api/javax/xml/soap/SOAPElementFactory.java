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
 * <P><CODE>SOAPElementFactory</CODE> is a factory for XML
 * fragments that will eventually end up in the SOAP part. These
 * fragments can be inserted as children of the <CODE>
 * SOAPHeader</CODE> or <CODE>SOAPBody</CODE> or <CODE>
 * SOAPEnvelope</CODE>.</P>
 *
 * <P>Elements created using this factory do not have the
 * properties of an element that lives inside a SOAP header
 * document. These elements are copied into the XML document tree
 * when they are inserted.</P>
 * @deprecated - Use javax.xml.soap.SOAPFactory for creating SOAPElements.
 * @see SOAPFactory SOAPFactory
 */
public class SOAPElementFactory {

    /**
     * Create a new <code>SOAPElementFactory from a <code>SOAPFactory</code>.
     *
     * @param soapfactory  the <code>SOAPFactory</code> to use
     */
    private SOAPElementFactory(SOAPFactory soapfactory) {
        sf = soapfactory;
    }

    /**
     * Create a <CODE>SOAPElement</CODE> object initialized with
     * the given <CODE>Name</CODE> object.
     * @param   name a <CODE>Name</CODE> object with
     *     the XML name for the new element
     * @return the new <CODE>SOAPElement</CODE> object that was
     *     created
     * @throws  SOAPException if there is an error in
     *     creating the <CODE>SOAPElement</CODE> object
     * @deprecated Use javax.xml.soap.SOAPFactory.createElement(javax.xml.soap.Name) instead
     * @see SOAPFactory#createElement(javax.xml.soap.Name) SOAPFactory.createElement(javax.xml.soap.Name)
     */
    public SOAPElement create(Name name) throws SOAPException {
        return sf.createElement(name);
    }

    /**
     * Create a <CODE>SOAPElement</CODE> object initialized with
     * the given local name.
     * @param   localName a <CODE>String</CODE> giving
     *     the local name for the new element
     * @return the new <CODE>SOAPElement</CODE> object that was
     *     created
     * @throws  SOAPException if there is an error in
     *     creating the <CODE>SOAPElement</CODE> object
     * @deprecated Use javax.xml.soap.SOAPFactory.createElement(String localName) instead
     * @see SOAPFactory#createElement(java.lang.String) SOAPFactory.createElement(java.lang.String)
     */
    public SOAPElement create(String localName) throws SOAPException {
        return sf.createElement(localName);
    }

    /**
     * Create a new <CODE>SOAPElement</CODE> object with the
     * given local name, prefix and uri.
     * @param   localName a <CODE>String</CODE> giving
     *     the local name for the new element
     * @param   prefix the prefix for this <CODE>
     *     SOAPElement</CODE>
     * @param   uri a <CODE>String</CODE> giving the
     *     URI of the namespace to which the new element
     *     belongs
     * @return the new <CODE>SOAPElement</CODE> object that was
     *     created
     * @throws  SOAPException if there is an error in
     *     creating the <CODE>SOAPElement</CODE> object
     * @deprecated Use javax.xml.soap.SOAPFactory.createElement(String localName, String prefix, String uri) instead
     * @see SOAPFactory#createElement(java.lang.String, java.lang.String, java.lang.String) SOAPFactory.createElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public SOAPElement create(String localName, String prefix, String uri)
            throws SOAPException {
        return sf.createElement(localName, prefix, uri);
    }

    /**
     * Creates a new instance of <CODE>SOAPElementFactory</CODE>.
     *
     * @return a new instance of a <CODE>
     *     SOAPElementFactory</CODE>
     * @throws  SOAPException if there was an error creating
     *     the default <CODE>SOAPElementFactory
     * @deprecated
     */
    public static SOAPElementFactory newInstance() throws SOAPException {

        try {
            return new SOAPElementFactory(SOAPFactory.newInstance());
        } catch (Exception exception) {
            throw new SOAPException("Unable to create SOAP Element Factory: "
                                    + exception.getMessage());
        }
    }

    private SOAPFactory sf;
}
