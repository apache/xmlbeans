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

import org.w3c.dom.Document;

import java.util.Locale;

/**
 * An object that represents the contents of the SOAP body
 * element in a SOAP message. A SOAP body element consists of XML data
 * that affects the way the application-specific content is processed.
 * <P>
 * A {@code SOAPBody} object contains {@code SOAPBodyElement}
 * objects, which have the content for the SOAP body.
 * A {@code SOAPFault} object, which carries status and/or
 * error information, is an example of a {@code SOAPBodyElement} object.
 * @see SOAPFault SOAPFault
 */
public interface SOAPBody extends SOAPElement {

    /**
     * Creates a new {@code SOAPFault} object and adds it to
     * this {@code SOAPBody} object.
     * @return the new {@code SOAPFault} object
     * @throws  SOAPException if there is a SOAP error
     */
    SOAPFault addFault() throws SOAPException;

    /**
     * Indicates whether a {@code SOAPFault} object exists in
     * this {@code SOAPBody} object.
     * @return {@code true} if a {@code SOAPFault} object exists in
     *     this {@code SOAPBody} object; {@code false}
     *     otherwise
     */
    boolean hasFault();

    /**
     * Returns the {@code SOAPFault} object in this {@code SOAPBody}
     * object.
     * @return the {@code SOAPFault} object in this {@code SOAPBody}
     *    object
     */
    SOAPFault getFault();

    /**
     * Creates a new {@code SOAPBodyElement} object with the
     * specified name and adds it to this {@code SOAPBody} object.
     * @param name a {@code Name} object with the name for the new
     *   {@code SOAPBodyElement} object
     * @return the new {@code SOAPBodyElement} object
     * @throws SOAPException  if a SOAP error occurs
     */
    SOAPBodyElement addBodyElement(Name name)
        throws SOAPException;

    /**
     * Creates a new {@code SOAPFault} object and adds it to this
     * {@code SOAPBody} object. The new {@code SOAPFault} will have a
     * {@code faultcode} element that is set to the {@code faultCode}
     * parameter and a {@code faultstring} set to {@code faultstring}
     * and localized to {@code locale}.
     *
     * @param faultCode a {@code Name} object giving the fault code to be
     *              set; must be one of the fault codes defined in the SOAP 1.1
     *              specification and of type QName
     * @param faultString a {@code String} giving an explanation of the
     *              fault
     * @param locale a {@code Locale} object indicating the native language
     *              of the {@code faultString}
     * @return the new {@code SOAPFault} object
     * @throws SOAPException  if there is a SOAP error
     */
    SOAPFault addFault(Name faultCode,
                       String faultString,
                       Locale locale) throws SOAPException;

    /**
     * Creates a new {@code SOAPFault} object and adds it to this
     * {@code SOAPBody} object. The new {@code SOAPFault} will have a
     * {@code faultcode} element that is set to the {@code faultCode}
     * parameter and a {@code faultstring} set to {@code faultstring}.
     *
     * @param faultCode a {@code Name} object giving the fault code to be
     *              set; must be one of the fault codes defined in the SOAP 1.1
     *              specification and of type QName
     * @param faultString a {@code String} giving an explanation of the
     *              fault
     * @return the new {@code SOAPFault} object
     * @throws SOAPException  if there is a SOAP error
     */
    SOAPFault addFault(Name faultCode, String faultString) throws SOAPException;

    /**
     * Adds the root node of the DOM {@code Document} to this
     * {@code SOAPBody} object.
     * <p>
     * Calling this method invalidates the {@code document} parameter. The
     * client application should discard all references to this
     * {@code Document} and its contents upon calling
     * {@code addDocument}. The behavior of an application that continues
     * to use such references is undefined.
     *
     * @param document the {@code Document} object whose root node will be
     *              added to this {@code SOAPBody}
     * @return the {@code SOAPBodyElement} that represents the root node
     *              that was added
     * @throws SOAPException if the {@code Document} cannot be added
     */
    SOAPBodyElement addDocument(Document document) throws SOAPException;
    }
