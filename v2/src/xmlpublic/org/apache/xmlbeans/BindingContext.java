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

package org.apache.xmlbeans;


/**
 * A BindingContext contains the runtime information necessary to marshal
 * or unmarshal a set of types.
 *
 * The BindingContext is used to create Marshaller and Unmarshaller objects
 * that can be used to convert to and from java objects and xml documents.
 */
public interface BindingContext
{
    /**
     *
     * @return Unmarshaller object
     * @throws XmlException
     */
    Unmarshaller createUnmarshaller()
        throws XmlException;

    /**
     * @deprecated options are ignored and must be passed to Unmarshaller methods

     * Create an Unmarshaller object capable of unmarshalling types
     * known by this context
     *
     * @param options
     * @return
     * @throws XmlException
     */
    Unmarshaller createUnmarshaller(XmlOptions options)
        throws XmlException;

    /**
     * Create an Marshaller object capable of marshalling types
     * known by this context
     *
     * @return Marshaller object
     * @throws XmlException
     */
    Marshaller createMarshaller()
        throws XmlException;

    /**
     * @deprecated options are ignored and must be passed to Marshaller methods
     *
     * @param options
     * @return
     * @throws XmlException
     */
    Marshaller createMarshaller(XmlOptions options)
        throws XmlException;


    /**
     * Create an Marshaller object capable of marshalling types
     * known by this context
     *
     * @return Marshaller object
     * @throws XmlException
     */
    SoapMarshaller createSoapMarshaller(EncodingStyle encodingStyle)
        throws XmlException;


}
