/*   Copyright 2004-2018 The Apache Software Foundation
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

package org.apache.xmlbeans.impl.common;

public class XMLBeansConstants {
    public static final String PROPERTY_ENTITY_EXPANSION_LIMIT = "xmlbeans.entity.expansion.limit";
    public static final String PROPERTY_LOAD_DTD_GRAMMAR = "xmlbeans.load.dtd.grammar";
    public static final String PROPERTY_LOAD_EXTERNAL_DTD = "xmlbeans.load.external.dtd";
    public static final int DEFAULT_ENTITY_EXPANSION_LIMIT = 2048;
    public static final String XML_PROPERTY_ENTITY_EXPANSION_LIMIT = "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit";
    public static final String XML_PROPERTY_SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    public static final String FEATURE_LOAD_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
    public static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static int getEntityExpansionLimit() {
        return Integer.getInteger(PROPERTY_ENTITY_EXPANSION_LIMIT, DEFAULT_ENTITY_EXPANSION_LIMIT);
    }
    public static boolean isLoadDtdGrammar() {
        return Boolean.getBoolean(PROPERTY_LOAD_DTD_GRAMMAR);
    }
    public static boolean isLoadExternalDtd() {
        return Boolean.getBoolean(PROPERTY_LOAD_EXTERNAL_DTD);
    }
}
