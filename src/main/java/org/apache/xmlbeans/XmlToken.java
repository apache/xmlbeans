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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#token">xs:token</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#string">xs:string</a>.
 * <p>
 * A token is XML's best representation for a "whitespace insensitive string."
 * All carriage returns, linefeeds, and tabs are converted to ordinary space
 * characters (as with <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#normalizedString">xs:normalizedString</a>),
 * and furthermore, all contiguous runs of space are collapsed to single spaces,
 * and leading and trailing spaces are trimmed.
 * <p>
 * If you want <code>"&nbsp;&nbsp;high&nbsp;&nbsp;priority&nbsp;&nbsp;"</code>
 * to be equivalent to <code>"high priority"</code>, you should consider
 * using xs:token or a subtype of xs:token.
 * <p>
 * When the {@link #getStringValue()} is obtained from an XmlToken, the normalized,
 * trimmed, whitespace collapsed value is returned.
 * <p>
 * Convertible to {@link String}.
 */
public interface XmlToken extends XmlNormalizedString {
    XmlObjectFactory<XmlToken> Factory = new XmlObjectFactory<>("_BI_token");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();
}

