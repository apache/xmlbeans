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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#string">xs:string</a> type.
 * <p>
 * A basic string in XML schema is not whitespace normalized.  If you
 * want your string type to be insensitive to variations in runs of
 * whitespace, consider using
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#token">xs:token</a>
 * (aka {@link XmlToken}) instead.
 * To forbid whitespace and permit just alphanumeric and other
 * common identifier characters consider
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#NMTOKEN">xs:NMTOKEN</a>
 * (aka {@link XmlNMTOKEN}) instead.
 * <p>
 * Convertible to {@link String}.
 */
public interface XmlString extends XmlAnySimpleType {
    XmlObjectFactory<XmlString> Factory = new XmlObjectFactory<>("_BI_string");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();
}

