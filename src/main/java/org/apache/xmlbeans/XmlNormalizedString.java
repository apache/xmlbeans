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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#normalizedString">xs:normalizedString</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#string">xs:string</a>.
 * <p>
 * An normalizedString simply is a string where all the carriage return,
 * linefeed, and tab characters have been normalized (switched to) ordinary
 * space characters.  Use normalizedString for long strings to make them
 * insensitive to line breaking.  If you wish to often be insensitive to
 * runs of whitespace (as is often the case), use
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#token">xs:token</a>
 * (aka {@link XmlToken}) instead.
 * <p>
 * Convertible to {@link String}.  When obtaining the stringValue, the
 * whitespace-normalized value is returned.
 */
public interface XmlNormalizedString extends XmlString {
    XmlObjectFactory<XmlNormalizedString> Factory = new XmlObjectFactory<>("_BI_normalizedString");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();
}

