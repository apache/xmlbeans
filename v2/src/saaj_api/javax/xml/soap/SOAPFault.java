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

import java.util.Locale;

/**
 * An element in the <CODE>SOAPBody</CODE> object that contains
 *   error and/or status information. This information may relate to
 *   errors in the <CODE>SOAPMessage</CODE> object or to problems
 *   that are not related to the content in the message itself.
 *   Problems not related to the message itself are generally errors
 *   in processing, such as the inability to communicate with an
 *   upstream server.
 *   <P>
 *   The <CODE>SOAPFault</CODE> interface provides methods for
 *   retrieving the information contained in a <CODE>
 *   SOAPFault</CODE> object and for setting the fault code, the
 *   fault actor, and a string describing the fault. A fault code is
 *   one of the codes defined in the SOAP 1.1 specification that
 *   describe the fault. An actor is an intermediate recipient to
 *   whom a message was routed. The message path may include one or
 *   more actors, or, if no actors are specified, the message goes
 *   only to the default actor, which is the final intended
 *   recipient.
 */
public interface SOAPFault extends SOAPBodyElement {

    /**
     * Sets this <CODE>SOAPFault</CODE> object with the given
     *   fault code.
     *
     *   <P>Fault codes, which given information about the fault,
     *   are defined in the SOAP 1.1 specification.</P>
     * @param   faultCode a <CODE>String</CODE> giving
     *     the fault code to be set; must be one of the fault codes
     *     defined in the SOAP 1.1 specification
     * @throws  SOAPException if there was an error in
     *     adding the <CODE>faultCode</CODE> to the underlying XML
     *     tree.
     * @see #getFaultCode() getFaultCode()
     */
    public abstract void setFaultCode(String faultCode) throws SOAPException;

    /**
     * Gets the fault code for this <CODE>SOAPFault</CODE>
     * object.
     * @return a <CODE>String</CODE> with the fault code
     * @see #setFaultCode(java.lang.String) setFaultCode(java.lang.String)
     */
    public abstract String getFaultCode();

    /**
     *  Sets this <CODE>SOAPFault</CODE> object with the given
     *   fault actor.
     *
     *   <P>The fault actor is the recipient in the message path who
     *   caused the fault to happen.</P>
     * @param   faultActor a <CODE>String</CODE>
     *     identifying the actor that caused this <CODE>
     *     SOAPFault</CODE> object
     * @throws  SOAPException  if there was an error in
     *     adding the <CODE>faultActor</CODE> to the underlying XML
     *     tree.
     * @see #getFaultActor() getFaultActor()
     */
    public abstract void setFaultActor(String faultActor) throws SOAPException;

    /**
     * Gets the fault actor for this <CODE>SOAPFault</CODE>
     * object.
     * @return  a <CODE>String</CODE> giving the actor in the message
     *     path that caused this <CODE>SOAPFault</CODE> object
     * @see #setFaultActor(java.lang.String) setFaultActor(java.lang.String)
     */
    public abstract String getFaultActor();

    /**
     * Sets the fault string for this <CODE>SOAPFault</CODE>
     * object to the given string.
     *
     * @param faultString a <CODE>String</CODE>
     *     giving an explanation of the fault
     * @throws  SOAPException  if there was an error in
     *     adding the <CODE>faultString</CODE> to the underlying XML
     *     tree.
     * @see #getFaultString() getFaultString()
     */
    public abstract void setFaultString(String faultString)
        throws SOAPException;

    /**
     * Gets the fault string for this <CODE>SOAPFault</CODE>
     * object.
     * @return a <CODE>String</CODE> giving an explanation of the
     *     fault
     */
    public abstract String getFaultString();

    /**
     * Returns the detail element for this <CODE>SOAPFault</CODE>
     *   object.
     *
     *   <P>A <CODE>Detail</CODE> object carries
     *   application-specific error information related to <CODE>
     *   SOAPBodyElement</CODE> objects.</P>
     * @return  a <CODE>Detail</CODE> object with
     *     application-specific error information
     */
    public abstract Detail getDetail();

    /**
     * Creates a <CODE>Detail</CODE> object and sets it as the
     *   <CODE>Detail</CODE> object for this <CODE>SOAPFault</CODE>
     *   object.
     *
     *   <P>It is illegal to add a detail when the fault already
     *   contains a detail. Therefore, this method should be called
     *   only after the existing detail has been removed.</P>
     * @return the new <CODE>Detail</CODE> object
     * @throws  SOAPException  if this
     *     <CODE>SOAPFault</CODE> object already contains a valid
     *     <CODE>Detail</CODE> object
     */
    public abstract Detail addDetail() throws SOAPException;

    /**
     * Sets this <code>SOAPFault</code> object with the given fault code.
     *
     * Fault codes, which give information about the fault, are defined in the
     * SOAP 1.1 specification. A fault code is mandatory and must be of type
     * <code>QName</code>. This method provides a convenient way to set a fault
     * code. For example,
     *
     * <pre>
     SOAPEnvelope se = ...;
     // Create a qualified name in the SOAP namespace with a localName
     // of "Client".  Note that prefix parameter is optional and is null
     // here which causes the implementation to use an appropriate prefix.
     Name qname = se.createName("Client", null,
     SOAPConstants.URI_NS_SOAP_ENVELOPE);
     SOAPFault fault = ...;
     fault.setFaultCode(qname);
     *
     * It is preferable to use this method over setFaultCode(String).
     *
     * @param name a <code>Name</code> object giving the fault code to be set.
     *              It must be namespace qualified.
     * @throws SOAPException if there was an error in adding the
     *              <code>faultcode</code> element to the underlying XML tree
     */
    public abstract void setFaultCode(Name name) throws SOAPException;

    /**
     * Gets the mandatory SOAP 1.1 fault code for this <code>SOAPFault</code>
     * object as a SAAJ <code>Name</code> object. The SOAP 1.1 specification
     * requires the value of the "faultcode" element to be of type QName. This
     * method returns the content of the element as a QName in the form of a
     * SAAJ <code>Name</code> object. This method should be used instead of the
     * <code>getFaultCode()</code> method since it allows applications to easily
     * access the namespace name without additional parsing.
     * <p>
     * In the future, a QName object version of this method may also be added.
     * @return a <code>Name</code> representing the faultcode
     */
    public abstract Name getFaultCodeAsName();

    /**
     * Sets the fault string for this <code>SOAPFault</code> object to the given
     * string and localized to the given locale.
     *
     * @param faultString       a <code>String</code> giving an explanation of
     *              the fault
     * @param locale            a <code>Locale</code> object indicating the
     *              native language of the <code>faultString</code>
     * @throws SOAPException    if there was an error in adding the
     *              <code>faultString</code> to the underlying XML tree
     */
    public abstract void setFaultString(String faultString, Locale locale) throws SOAPException;

    /**
     * Returns the optional detail element for this <code>SOAPFault</code>
     * object.
     *
     * @return a <code>Locale</code> object indicating the native language of
     *              the fault string or <code>null</code> if no locale was
     *              specified
     */
    public abstract Locale getFaultStringLocale();
}
