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

import javax.xml.namespace.QName;


/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#QName">xs:QName</a> type.
 * <p>
 * A QName is the logical combination of an XML namespace URI and a localName.
 * Although in an XML instance document, a QName appears as "prefix:localName",
 * the logical value of a QName does NOT contain any information about the
 * prefix, only the namespace URI to which the prefix maps.  For example,
 * two QNames "a:hello" and "b:hello" are perfectly equivalent if "a:" in
 * the first instance maps to the same URI as "b:" in the second instance.
 * <p>
 * Convertible to {@link javax.xml.namespace.QName}.
 */
public interface XmlQName extends XmlAnySimpleType {
    XmlObjectFactory<XmlQName> Factory = new XmlObjectFactory<>("_BI_QName");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();

    /**
     * Returns this value as a {@link QName}
     */
    QName getQNameValue();

    /**
     * Sets this value as a {@link QName}
     */
    void setQNameValue(QName name);
}

