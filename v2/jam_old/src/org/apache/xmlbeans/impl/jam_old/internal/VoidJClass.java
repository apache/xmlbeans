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

package org.apache.xmlbeans.impl.jam_old.internal;

import org.apache.xmlbeans.impl.jam_old.JClass;

/**
 * Singleton JClass impl for the void type.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class VoidJClass extends BuiltinJClass {

  // ========================================================================
  // Constants

  private static final String SIMPLE_NAME = "void";
  private static final String QUALIFIED_NAME = "void";

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

  private static final JClass INSTANCE = new VoidJClass();

  private VoidJClass() {
  }

  public static boolean isVoid(String fd) {
    return fd.equals(SIMPLE_NAME);
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

  public boolean isVoid() {
    return true;
  }

  public boolean isAssignableFrom(JClass c) {
    return false;
  }
}
