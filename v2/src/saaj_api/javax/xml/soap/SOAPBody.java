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

import org.w3c.dom.Document;

import java.util.Locale;

/**
 * An object that represents the contents of the SOAP body
 * element in a SOAP message. A SOAP body element consists of XML data
 * that affects the way the application-specific content is processed.
 * <P>
 * A <code>SOAPBody</code> object contains <code>SOAPBodyElement</code>
 * objects, which have the content for the SOAP body.
 * A <code>SOAPFault</code> object, which carries status and/or
 * error information, is an example of a <code>SOAPBodyElement</code> object.
 * @see SOAPFault SOAPFault
 */
public interface SOAPBody extends SOAPElement {

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to
     * this <code>SOAPBody</code> object.
     * @return the new <code>SOAPFault</code> object
     * @throws  SOAPException if there is a SOAP error
     */
    public abstract SOAPFault addFault() throws SOAPException;

    /**
     * Indicates whether a <code>SOAPFault</code> object exists in
     * this <code>SOAPBody</code> object.
     * @return <code>true</code> if a <code>SOAPFault</code> object exists in
     *     this <code>SOAPBody</code> object; <code>false</code>
     *     otherwise
     */
    public abstract boolean hasFault();

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     * object.
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code>
     *    object
     */
    public abstract SOAPFault getFault();

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the
     * specified name and adds it to this <code>SOAPBody</code> object.
     * @param name a <code>Name</code> object with the name for the new
     *   <code>SOAPBodyElement</code> object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws SOAPException  if a SOAP error occurs
     */
    public abstract SOAPBodyElement addBodyElement(Name name)
        throws SOAPException;

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this
     * <code>SOAPBody</code> object. The new <code>SOAPFault</code> will have a
     * <code>faultcode</code> element that is set to the <code>faultCode</code>
     * parameter and a <code>faultstring</code> set to <code>faultstring</code>
     * and localized to <code>locale</code>.
     *
     * @param faultCode a <code>Name</code> object giving the fault code to be
     *              set; must be one of the fault codes defined in the SOAP 1.1
     *              specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the
     *              fault
     * @param locale a <code>Locale</code> object indicating the native language
     *              of the <ocde>faultString</code>
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException  if there is a SOAP error
     */
    public abstract SOAPFault addFault(Name faultCode,
                                       String faultString,
                                       Locale locale) throws SOAPException;

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this
     * <code>SOAPBody</code> object. The new <code>SOAPFault</code> will have a
     * <code>faultcode</code> element that is set to the <code>faultCode</code>
     * parameter and a <code>faultstring</code> set to <code>faultstring</code>.
     *
     * @param faultCode a <code>Name</code> object giving the fault code to be
     *              set; must be one of the fault codes defined in the SOAP 1.1
     *              specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the
     *              fault
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException  if there is a SOAP error
     */
    public abstract SOAPFault addFault(Name faultCode, String faultString) throws SOAPException;

    /**
     * Adds the root node of the DOM <code>Document</code> to this
     * <code>SOAPBody</code> object.
     * <p>
     * Calling this method invalidates the <code>document</code> parameter. The
     * client application should discard all references to this
     * <code>Document</code> and its contents upon calling
     * <code>addDocument</code>. The behavior of an application that continues
     * to use such references is undefined.
     *
     * @param document the <code>Document</code> object whose root node will be
     *              added to this <code>SOAPBody</code>
     * @return the <code>SOAPBodyElement</code> that represents the root node
     *              that was added
     * @throws SOAPException if the <code>Document</code> cannot be added
     */
    public abstract SOAPBodyElement addDocument(Document document) throws SOAPException;
    }
