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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.SchemaType;
import javax.xml.namespace.QName;

/**
 * This gives the non default bindings to take control of TypeStoreUser
 * creation. It is set once per document and called each time instead of
 * TypeStoreUser.createElementUser() and createAttributeUser()
 */
public interface TypeStoreFactory
{
    /** A key to pass in for the XmlOptions to set the factory */
    public static final String KEY = "TypeStoreFactory";

    TypeStoreUser createElementUser(SchemaType parentType, QName name, QName xsiType);

    TypeStoreUser createAttributeUser(SchemaType parentType, QName name);
}
