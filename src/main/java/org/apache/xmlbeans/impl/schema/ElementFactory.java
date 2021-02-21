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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlOptions;

@SuppressWarnings("unchecked")
public class ElementFactory<T> {
    private final SchemaType type;
    private final SchemaTypeSystem typeSystem;

    public ElementFactory(SchemaTypeSystem typeSystem, String typeHandle) {
        this.typeSystem = typeSystem;
        this.type = (SchemaType)typeSystem.resolveHandle(typeHandle);
    }

    public SchemaType getType() {
        return type;
    }

    public SchemaTypeSystem getTypeLoader() {
        return typeSystem;
    }

    public T newInstance() {
        return (T) getTypeLoader().newInstance(type, null);
    }

    public T newInstance(XmlOptions options) {
        return (T) getTypeLoader().newInstance(type, options);
    }
}
