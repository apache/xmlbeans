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

package org.apache.xmlbeans.impl.jam.internal.reflect;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JField;

/**
 * Reflection-backed implementation of JClass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
/*package*/ final class RField extends RMember implements JField {

  // ========================================================================
  // Variables

  private Field mField;

  // ========================================================================
  // Constructors
  
  public RField(Field f, JClassLoader loader) {
    super(f,loader);
    mField = f;
  }

  // ========================================================================
  // JField implementation

  public JClass getType() { 
    return RClassLoader.getClassFor(mField.getType(),mLoader);
  }

  public boolean isVolatile() { 
    return Modifier.isVolatile(mField.getModifiers());
  }

  public boolean isTransient() { 
    return Modifier.isTransient(mField.getModifiers());
  }
}
