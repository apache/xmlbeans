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

import org.apache.xmlbeans.impl.schema.XmlObjectFactory;

/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#unsignedByte">xs:unsignedByte</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * Verified to be in the range 0..255 when validating.
 * <p>
 * As suggested by JAXB, convertible to Java short.
 */
public interface XmlUnsignedByte extends XmlUnsignedShort {
    XmlObjectFactory<XmlUnsignedByte> Factory = new XmlObjectFactory<>("_BI_unsignedByte");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();

    /**
     * Returns this value as a short
     */
    short getShortValue();

    /**
     * Sets this value as a short
     */
    void setShortValue(short s);
}

