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

package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.JClass;

/**
 * Singleton JClass impl for java.lang.Object.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class ObjectJClass extends BuiltinJClass {

  // ========================================================================
  // Constants

  private static final String SIMPLE_NAME = "Object";
  private static final String QUALIFIED_NAME = "java.lang.Object";

  // ========================================================================
  // Singleton

  /**
   * Returns a JClass representing the named primitive type.  The name
   * parameter can be a simple type name (e.g. 'int') or a field
   * descriptor (e.g. 'I').
   */
  public static JClass getInstance() {
    return INSTANCE;
  }

  private static final JClass INSTANCE = new ObjectJClass();

  private ObjectJClass() {
  }

  // ========================================================================
  // JElement implementation

  public String getSimpleName() {
    return SIMPLE_NAME;
  }

  public String getQualifiedName() {
    return QUALIFIED_NAME;
  }

  public String getFieldDescriptor() {
    return QUALIFIED_NAME;
  }

  // ========================================================================
  // JClass implementation

  public boolean isAssignableFrom(JClass c) {
    return true;
  }

  public boolean isObject() {
    return true;
  }
}
