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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * An Unmarshaller is used to unmarshal xml documents into Java objects.
 * The object is not thread safe and should not be shared
 * amonst threads.  It should however be shared across different invocations of
 * unmarshalType() for a given document.  The Unmarshaller maintains a cache of
 * what objects have been deserialized, and will maintain this cache across
 * invocations of unmarshalType
 */
public interface SoapUnmarshaller
{

    /**
     * unmarshal an xml instance of a given schema type
     *
     * No attention is paid to the actual tag on which the reader is positioned.
     * It is only the contents that matter
     * (including attributes on that start tag).
     *
     *
     * PRECONDITIONS:
     * reader.isStartElement() must return true
     *
     * POSTCONDITIONS:
     * reader will be positioned immediately after the end element
     * corresponding to the start element from the precondition
     *
     * <p>Use the <em>options</em> parameter to specify the following:</p>
     *
     * <ul>
     * <li>A collection instance that should be used as an error listener during
     * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
     * </ul>
     *
     *
     * @param schemaType
     * @param javaType the java type in the format returned by Class.getName()
     * @return
     * @throws XmlException
     */
    Object unmarshalType(XMLStreamReader reader,
                         QName schemaType,
                         String javaType,
                         XmlOptions options)
        throws XmlException;

}
