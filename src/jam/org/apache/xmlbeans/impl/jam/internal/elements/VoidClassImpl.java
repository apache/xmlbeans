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

package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.JClass;

/**
 * <p>Class implementation to represent the 'void' type.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public final class VoidClassImpl extends BuiltinClassImpl {

  // ========================================================================
  // Constants

  private static final String SIMPLE_NAME = "void";

// ========================================================================
  // Public static utilities

  public static boolean isVoid(String fd) {
    return fd.equals(SIMPLE_NAME);
  }

  // ========================================================================
  // Singleton

  public VoidClassImpl(ElementContext ctx) {
    super(ctx);
    super.reallySetSimpleName(SIMPLE_NAME);
  }

  // ========================================================================
  // JClass implementation

  public boolean isVoidType() { return true; }

  public boolean isAssignableFrom(JClass c) { return false; }
}
