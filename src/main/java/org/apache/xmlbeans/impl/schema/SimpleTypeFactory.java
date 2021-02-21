/*   Copyright 2017, 2018 The Apache Software Foundation
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

import org.apache.xmlbeans.SchemaTypeSystem;

@SuppressWarnings("unchecked")
public class SimpleTypeFactory<T> extends ElementFactory<T> {
    public SimpleTypeFactory(SchemaTypeSystem typeSystem, String typeHandle) {
        super(typeSystem, typeHandle);
    }

    public T newValue(java.lang.Object obj) {
        return (T) getType().newValue(obj);
    }
}
