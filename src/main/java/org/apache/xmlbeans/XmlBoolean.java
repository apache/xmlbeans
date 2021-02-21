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
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#boolean">xs:boolean</a> type.
 * <p>
 * Naturally, convertible to Java boolean.
 */
public interface XmlBoolean extends XmlAnySimpleType {
    XmlObjectFactory<XmlBoolean> Factory = new XmlObjectFactory<>("_BI_boolean");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_boolean");

    /**
     * Returns this value as a boolean
     */
    boolean getBooleanValue();

    /**
     * Sets this value as a boolean
     */
    void setBooleanValue(boolean v);
}

