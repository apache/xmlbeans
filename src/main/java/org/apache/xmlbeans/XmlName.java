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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#Name">xs:Name</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#string">xs:string</a>.
 * <p>
 * This kind of string is the same as the lexical representation used for XML 1.0
 * element and attribute names, e.g., "pre:a-b.cdef".  It is just a physical
 * string, however, and it should NOT be confused with {@link XmlQName},
 * which is a logical combination of localname and namespace URI.
 * <p>
 * Convertible to {@link String}.
 */
public interface XmlName extends XmlToken {
    XmlObjectFactory<XmlName> Factory = new XmlObjectFactory<>("_BI_Name");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();
}

