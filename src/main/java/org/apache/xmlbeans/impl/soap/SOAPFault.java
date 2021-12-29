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

import java.util.Locale;

/**
 * An element in the {@code SOAPBody} object that contains
 *   error and/or status information. This information may relate to
 *   errors in the {@code SOAPMessage} object or to problems
 *   that are not related to the content in the message itself.
 *   Problems not related to the message itself are generally errors
 *   in processing, such as the inability to communicate with an
 *   upstream server.
 *   <P>
 *   The {@code SOAPFault} interface provides methods for
 *   retrieving the information contained in a {@code
 *   SOAPFault} object and for setting the fault code, the
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
     * Sets this {@code SOAPFault} object with the given
     *   fault code.
     *
     *   <P>Fault codes, which given information about the fault,
     *   are defined in the SOAP 1.1 specification.</P>
     * @param   faultCode a {@code String} giving
     *     the fault code to be set; must be one of the fault codes
     *     defined in the SOAP 1.1 specification
     * @throws  SOAPException if there was an error in
     *     adding the {@code faultCode} to the underlying XML
     *     tree.
     * @see #getFaultCode() getFaultCode()
     */
    void setFaultCode(String faultCode) throws SOAPException;

    /**
     * Gets the fault code for this {@code SOAPFault}
     * object.
     * @return a {@code String} with the fault code
     * @see #setFaultCode(java.lang.String) setFaultCode(java.lang.String)
     */
    String getFaultCode();

    /**
     *  Sets this {@code SOAPFault} object with the given
     *   fault actor.
     *
     *   <P>The fault actor is the recipient in the message path who
     *   caused the fault to happen.</P>
     * @param   faultActor a {@code String}
     *     identifying the actor that caused this {@code
     *     SOAPFault} object
     * @throws  SOAPException  if there was an error in
     *     adding the {@code faultActor} to the underlying XML
     *     tree.
     * @see #getFaultActor() getFaultActor()
     */
    void setFaultActor(String faultActor) throws SOAPException;

    /**
     * Gets the fault actor for this {@code SOAPFault}
     * object.
     * @return  a {@code String} giving the actor in the message
     *     path that caused this {@code SOAPFault} object
     * @see #setFaultActor(java.lang.String) setFaultActor(java.lang.String)
     */
    String getFaultActor();

    /**
     * Sets the fault string for this {@code SOAPFault}
     * object to the given string.
     *
     * @param faultString a {@code String}
     *     giving an explanation of the fault
     * @throws  SOAPException  if there was an error in
     *     adding the {@code faultString} to the underlying XML
     *     tree.
     * @see #getFaultString() getFaultString()
     */
    void setFaultString(String faultString)
        throws SOAPException;

    /**
     * Gets the fault string for this {@code SOAPFault}
     * object.
     * @return a {@code String} giving an explanation of the
     *     fault
     */
    String getFaultString();

    /**
     * Returns the detail element for this {@code SOAPFault}
     *   object.
     *
     *   <P>A {@code Detail} object carries
     *   application-specific error information related to {@code
     *   SOAPBodyElement} objects.</P>
     * @return  a {@code Detail} object with
     *     application-specific error information
     */
    Detail getDetail();

    /**
     * Creates a {@code Detail} object and sets it as the
     *   {@code Detail} object for this {@code SOAPFault}
     *   object.
     *
     *   <P>It is illegal to add a detail when the fault already
     *   contains a detail. Therefore, this method should be called
     *   only after the existing detail has been removed.</P>
     * @return the new {@code Detail} object
     * @throws  SOAPException  if this
     *     {@code SOAPFault} object already contains a valid
     *     {@code Detail} object
     */
    Detail addDetail() throws SOAPException;

    /**
     * Sets this {@code SOAPFault} object with the given fault code.
     *
     * Fault codes, which give information about the fault, are defined in the
     * SOAP 1.1 specification. A fault code is mandatory and must be of type
     * {@code QName}. This method provides a convenient way to set a fault
     * code. For example,
     *
     * <pre>{@code
     * SOAPEnvelope se = ...;
     * // Create a qualified name in the SOAP namespace with a localName
     * // of "Client".  Note that prefix parameter is optional and is null
     * // here which causes the implementation to use an appropriate prefix.
     * Name qname = se.createName("Client", null,
     * SOAPConstants.URI_NS_SOAP_ENVELOPE);
     * SOAPFault fault = ...;
     * fault.setFaultCode(qname);
     * }</pre>
     *
     * It is preferable to use this method over setFaultCode(String).
     *
     * @param name a {@code Name} object giving the fault code to be set.
     *              It must be namespace qualified.
     * @throws SOAPException if there was an error in adding the
     *              {@code faultcode} element to the underlying XML tree
     */
    void setFaultCode(Name name) throws SOAPException;

    /**
     * Gets the mandatory SOAP 1.1 fault code for this {@code SOAPFault}
     * object as a SAAJ {@code Name} object. The SOAP 1.1 specification
     * requires the value of the "faultcode" element to be of type QName. This
     * method returns the content of the element as a QName in the form of a
     * SAAJ {@code Name} object. This method should be used instead of the
     * {@code getFaultCode()} method since it allows applications to easily
     * access the namespace name without additional parsing.
     * <p>
     * In the future, a QName object version of this method may also be added.
     * @return a {@code Name} representing the faultcode
     */
    Name getFaultCodeAsName();

    /**
     * Sets the fault string for this {@code SOAPFault} object to the given
     * string and localized to the given locale.
     *
     * @param faultString       a {@code String} giving an explanation of
     *              the fault
     * @param locale            a {@code Locale} object indicating the
     *              native language of the {@code faultString}
     * @throws SOAPException    if there was an error in adding the
     *              {@code faultString} to the underlying XML tree
     */
    void setFaultString(String faultString, Locale locale) throws SOAPException;

    /**
     * Returns the optional detail element for this {@code SOAPFault}
     * object.
     *
     * @return a {@code Locale} object indicating the native language of
     *              the fault string or {@code null} if no locale was
     *              specified
     */
    Locale getFaultStringLocale();
}
