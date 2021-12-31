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

import java.io.IOException;
import java.io.InputStream;

/**
 * <P>A factory for creating {@code SOAPMessage} objects.</P>
 *
 *   <P>A JAXM client performs the following steps to create a
 *   message.</P>
 *
 *   <UL>
 *     <LI>
 *       Creates a {@code MessageFactory} object from a {@code
 *       ProviderConnection} object ({@code con} in the
 *       following line of code). The {@code String} passed to
 *       the {@code createMessageFactory} method is the name of
 *       of a messaging profile, which must be the URL for the
 *       schema.
 * <PRE>
 *      MessageFactory mf = con.createMessageFactory(schemaURL);
 * </PRE>
 *     </LI>
 *
 *     <LI>
 *       Calls the method {@code createMessage} on the {@code
 *       MessageFactory} object. All messages produced by this
 *       {@code MessageFactory} object will have the header
 *       information appropriate for the messaging profile that was
 *       specified when the {@code MessageFactory} object was
 *       created.
 * <PRE>
 *      SOAPMessage m = mf.createMessage();
 * </PRE>
 *     </LI>
 *   </UL>
 *   It is also possible to create a {@code MessageFactory}
 *   object using the method {@code newInstance}, as shown in
 *   the following line of code.
 * <PRE>
 *      MessageFactory mf = MessageFactory.newInstance();
 * </PRE>
 *   A standalone client (a client that is not running in a
 *   container) can use the {@code newInstance} method to
 *   create a {@code MessageFactory} object.
 *
 *   <P>All {@code MessageFactory} objects, regardless of how
 *   they are created, will produce {@code SOAPMessage} objects
 *   that have the following elements by default:</P>
 *
 *   <UL>
 *     <LI>A {@code SOAPPart} object</LI>
 *
 *     <LI>A {@code SOAPEnvelope} object</LI>
 *
 *     <LI>A {@code SOAPBody} object</LI>
 *
 *     <LI>A {@code SOAPHeader} object</LI>
 *   </UL>
 *   If a {@code MessageFactory} object was created using a
 *   {@code ProviderConnection} object, which means that it was
 *   initialized with a specified profile, it will produce messages
 *   that also come prepopulated with additional entries in the
 *   {@code SOAPHeader} object and the {@code SOAPBody}
 *   object. The content of a new {@code SOAPMessage} object
 *   depends on which of the two {@code MessageFactory} methods
 *   is used to create it.
 *
 *   <UL>
 *     <LI>{@code createMessage()} -- message has no
 *     content<BR>
 *      This is the method clients would normally use to create a
 *     request message.</LI>
 *
 *     <LI>{@code createMessage(MimeHeaders,
 *     java.io.InputStream)} -- message has content from the
 *     {@code InputStream} object and headers from the {@code
 *     MimeHeaders} object<BR>
 *      This method can be used internally by a service
 *     implementation to create a message that is a response to a
 *     request.</LI>
 *   </UL>
 */
public abstract class MessageFactory {

    // fixme: this should be protected as the class is abstract.
    /** Create a new MessageFactory. */
    public MessageFactory() {}

    /**
     * Creates a new {@code MessageFactory} object that is
     * an instance of the default implementation.
     * @return a new {@code MessageFactory} object
     * @throws  SOAPException  if there was an error in
     *     creating the default implementation of the {@code
     *     MessageFactory}
     */
    public static MessageFactory newInstance() throws SOAPException {

        try {
            return (MessageFactory) FactoryFinder.find(MESSAGE_FACTORY_PROPERTY,
                                                       DEFAULT_MESSAGE_FACTORY);
        } catch (Exception exception) {
            throw new SOAPException(
                "Unable to create message factory for SOAP: "
                + exception.getMessage());
        }
    }

    /**
     * Creates a new {@code SOAPMessage} object with the
     *   default {@code SOAPPart}, {@code SOAPEnvelope},
     *   {@code SOAPBody}, and {@code SOAPHeader} objects.
     *   Profile-specific message factories can choose to
     *   prepopulate the {@code SOAPMessage} object with
     *   profile-specific headers.
     *
     *   <P>Content can be added to this message's {@code
     *   SOAPPart} object, and the message can be sent "as is"
     *   when a message containing only a SOAP part is sufficient.
     *   Otherwise, the {@code SOAPMessage} object needs to
     *   create one or more {@code AttachmentPart} objects and
     *   add them to itself. Any content that is not in XML format
     *   must be in an {@code AttachmentPart} object.</P>
     * @return  a new {@code SOAPMessage} object
     * @throws  SOAPException if a SOAP error occurs
     */
    public abstract SOAPMessage createMessage() throws SOAPException;

    /**
     * Internalizes the contents of the given {@code
     * InputStream} object into a new {@code SOAPMessage}
     * object and returns the {@code SOAPMessage} object.
     * @param   mimeheaders    the transport-specific headers
     *     passed to the message in a transport-independent fashion
     *     for creation of the message
     * @param   inputstream    the {@code InputStream} object
     *     that contains the data for a message
     * @return a new {@code SOAPMessage} object containing the
     *     data from the given {@code InputStream} object
     * @throws  IOException    if there is a
     *     problem in reading data from the input stream
     * @throws  SOAPException  if the message is invalid
     */
    public abstract SOAPMessage createMessage(
        MimeHeaders mimeheaders, InputStream inputstream)
            throws IOException, SOAPException;

    private static final String DEFAULT_MESSAGE_FACTORY =
        "org.apache.axis.soap.MessageFactoryImpl";

    private static final String MESSAGE_FACTORY_PROPERTY =
        "javax.xml.soap.MessageFactory";
}
