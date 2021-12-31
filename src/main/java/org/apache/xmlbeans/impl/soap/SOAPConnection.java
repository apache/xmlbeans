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

/**
 * A point-to-point connection that a client can use for sending messages
 * directly to a remote party (represented by a URL, for instance).
 * <p>
 * A client can obtain a {@code SOAPConnection} object simply by
 * calling the following static method.
 * <pre>
 *
 *      SOAPConnection con = SOAPConnection.newInstance();
 * </pre>
 * A {@code SOAPConnection} object can be used to send messages
 * directly to a URL following the request/response paradigm.  That is,
 * messages are sent using the method {@code call}, which sends the
 * message and then waits until it gets a reply.
 */
public abstract class SOAPConnection {

    public SOAPConnection() {}

    /**
     * Sends the given message to the specified endpoint and
     * blocks until it has returned the response.
     * @param request the {@code SOAPMessage}
     *     object to be sent
     * @param endpoint an {@code Object} that identifies
     *            where the message should be sent. It is required to
     *            support Objects of type
     *            {@code java.lang.String},
     *            {@code java.net.URL}, and when JAXM is present
     *            {@code javax.xml.messaging.URLEndpoint}
     * @return the {@code SOAPMessage} object that is the
     *     response to the message that was sent
     * @throws  SOAPException if there is a SOAP error
     */
    public abstract SOAPMessage call(SOAPMessage request, Object endpoint)
        throws SOAPException;

    /**
     * Closes this {@code SOAPConnection} object.
     * @throws  SOAPException if there is a SOAP error
     */
    public abstract void close() throws SOAPException;
}
