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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.SchemaType;

import java.util.Map;
import java.util.HashMap;


/* package protected */

/**
 * Note that this is NOT a generic binding registry for users' binding; this is
 * a registry that is used internally just to manage binding between
 * XBeans for BindingTypes and BindingType wrapper classes.  The reason
 * for this mechanism is that XMLBeans binding is NOT YET powerful enough
 * to bind the various kinds of BindingTypes directly to schema yet.
 *
 * However, in the future, we hope to make it powerful enough to do so,
 * so that most of the hand-coded binding betwen Java and XML can go away.
 * This class, and all the mechanisms that use it, should go away too.
 */
class KindRegistry {

  // ========================================================================
  // Variables

  private Map registryClassFromType = new HashMap();
  private Map registryTypeFromClass = new HashMap();

  // ========================================================================
  // Package methods

  synchronized void registerClassAndType(Class bindingTypeClass, SchemaType bindingTypeSchemaType) {
    registryClassFromType.put(bindingTypeSchemaType, bindingTypeClass);
    registryTypeFromClass.put(bindingTypeClass, bindingTypeSchemaType);
  }

  synchronized Class classForType(SchemaType type) {
    return (Class) registryClassFromType.get(type);
  }

  synchronized SchemaType typeForClass(Class clazz) {
    return (SchemaType) registryTypeFromClass.get(clazz);
  }
}
