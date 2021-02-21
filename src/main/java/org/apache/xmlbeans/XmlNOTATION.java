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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#NOTATION">xs:NOTATION</a> type.
 * <p>
 * NOTATIONs are a mechanism in XML Schema that is provided
 * for partial backward compatibility with NOTATIONs in DTDs.  XML Schema
 * users should almost never have any reason to use this data type.
 * <p>
 * Convertible to {@link String}.
 */
public interface XmlNOTATION extends XmlAnySimpleType {
    XmlObjectFactory<XmlNOTATION> Factory = new XmlObjectFactory<>("_BI_NOTATION");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();
}

