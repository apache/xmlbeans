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
 * A factory for creating {@code SOAPConnection} objects. Implementation of
 * this class is optional. If {@code SOAPConnectionFactory.newInstance()}
 * throws an {@code UnsupportedOperationException} then the implementation
 * does not support the SAAJ communication infrastructure. Otherwise
 * {@code SOAPConnection} objects can be created by calling
 * {@code createConnection()} on the newly created
 * {@code SOAPConnectionFactory} object.
 */
public abstract class SOAPConnectionFactory {

    public SOAPConnectionFactory() {}

    /**
     * Creates an instance of the default {@code
     * SOAPConnectionFactory} object.
     * @return a new instance of a default {@code
     *     SOAPConnectionFactory} object
     * @throws  SOAPException  if there was an error creating
     *     the {@code SOAPConnectionFactory}
     * @throws UnsupportedOperationException  if newInstance is not supported.
     */
    public static SOAPConnectionFactory newInstance()
            throws SOAPException, UnsupportedOperationException {

        try {
            return (SOAPConnectionFactory) FactoryFinder.find(SF_PROPERTY,
                    DEFAULT_SOAP_CONNECTION_FACTORY);
        } catch (Exception exception) {
            throw new SOAPException("Unable to create SOAP connection factory: "
                                    + exception.getMessage());
        }
    }

    /**
     * Create a new {@code SOAPConnection}.
     * @return the new {@code SOAPConnection} object.
     * @throws  SOAPException if there was an exception
     *     creating the {@code SOAPConnection} object.
     */
    public abstract SOAPConnection createConnection() throws SOAPException;

    private static final String DEFAULT_SOAP_CONNECTION_FACTORY =
        "org.apache.axis.soap.SOAPConnectionFactoryImpl";

    private static final String SF_PROPERTY =
        "javax.xml.soap.SOAPConnectionFactory";
}
