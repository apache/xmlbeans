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

import java.math.BigInteger;

/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#integer">xs:integer</a> type.
 * One of the derived types based on <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#decimal">xs:decimal</a>.
 * <p>
 * This type should not be confused with <a target="_blank" href="http://www.w3.org/TR/xmlschema-2/#int">xs:int</a>
 * or Java {@link Integer}.  This type represents an arbitrary-precision integer with
 * any number of digits, while a Java int or an xs:int is a 32-bit finite-precision integer.
 * <p>
 * Convertible to a Java {@link BigInteger}.
 */
public interface XmlInteger extends XmlDecimal {
    XmlObjectFactory<XmlInteger> Factory = new XmlObjectFactory<>("_BI_integer");

    /**
     * The constant {@link SchemaType} object representing this schema type.
     */
    SchemaType type = Factory.getType();

    /**
     * Returns this value as a {@link BigInteger}
     */
    BigInteger getBigIntegerValue();

    /**
     * Sets this value as a {@link BigInteger}
     */
    void setBigIntegerValue(BigInteger bi);
}

