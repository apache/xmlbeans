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
 * {@code SOAPFactory} is a factory for creating various objects
 * that exist in the SOAP XML tree.
 *
 * {@code SOAPFactory} can be
 * used to create XML fragments that will eventually end up in the
 * SOAP part. These fragments can be inserted as children of the
 * {@code SOAPHeaderElement} or {@code SOAPBodyElement} or
 * {@code SOAPEnvelope}.
 *
 * {@code SOAPFactory} also has methods to create
 * {@code javax.xml.soap.Detail} objects as well as
 * {@code java.xml.soap.Name} objects.
 *
 */
public abstract class SOAPFactory {

    public SOAPFactory() {}

    /**
     * Create a {@code SOAPElement} object initialized with the
     * given {@code Name} object.
     *
     * @param name a {@code Name} object with the XML name for
     *        the new element
     * @return  the new {@code SOAPElement} object that was
     *    created
     * @throws SOAPException if there is an error in creating the
     *       {@code SOAPElement} object
     */
    public abstract SOAPElement createElement(Name name) throws SOAPException;

    /**
     * Create a {@code SOAPElement} object initialized with the
     * given local name.
     *
     * @param localName a {@code String} giving the local name for
     *       the new element
     * @return the new {@code SOAPElement} object that was
     *    created
     * @throws SOAPException if there is an error in creating the
     *       {@code SOAPElement} object
     */
    public abstract SOAPElement createElement(String localName) throws SOAPException;

    /**
     * Create a new {@code SOAPElement} object with the given
     * local name, prefix and uri.
     *
     * @param localName a {@code String} giving the local name
     *            for the new element
     * @param prefix the prefix for this {@code SOAPElement}
     * @param uri a {@code String} giving the URI of the
     *      namespace to which the new element belongs
     * @return the new {@code SOAPElement} object that was
     *    created
     * @throws SOAPException if there is an error in creating the
     *      {@code SOAPElement} object
     */
    public abstract SOAPElement createElement(String localName, String prefix, String uri)
        throws SOAPException;

    /**
     * Creates a new {@code Detail} object which serves as a container
     * for {@code DetailEntry} objects.
     * <p>
     * This factory method creates {@code Detail} objects for use in
     * situations where it is not practical to use the {@code SOAPFault}
     * abstraction.
     *
     * @return a {@code Detail} object
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Detail createDetail() throws SOAPException;

    /**
     * Creates a new {@code Name} object initialized with the
     * given local name, namespace prefix, and namespace URI.
     * <p>
     * This factory method creates {@code Name} objects for use in
     * situations where it is not practical to use the {@code SOAPEnvelope}
     * abstraction.
     *
     * @param localName a {@code String} giving the local name
     * @param prefix a {@code String} giving the prefix of the namespace
     * @param uri a {@code String} giving the URI of the namespace
     * @return a {@code Name} object initialized with the given
     *   local name, namespace prefix, and namespace URI
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Name createName(String localName, String prefix, String uri)
        throws SOAPException;

    /**
     * Creates a new {@code Name} object initialized with the
     * given local name.
     * <p>
     * This factory method creates {@code Name} objects for use in
     * situations where it is not practical to use the {@code SOAPEnvelope}
     * abstraction.
     *
     * @param localName a {@code String} giving the local name
     * @return a {@code Name} object initialized with the given
     *    local name
     * @throws SOAPException if there is a SOAP error
     */
    public abstract Name createName(String localName) throws SOAPException;

    /**
     * Creates a new instance of {@code SOAPFactory}.
     *
     * @return a new instance of a {@code SOAPFactory}
     * @throws SOAPException if there was an error creating the
     *       default {@code SOAPFactory}
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
