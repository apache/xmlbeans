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
import org.apache.xmlbeans.impl.jam.JPackage;

/**
 * <p>This is the JClass that is returned when a java type cannot be
 * resolved.  It has only a name.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public final class UnresolvedClassImpl extends BuiltinClassImpl {

  // ========================================================================
  // Variables

  private String mPackageName;

  // ========================================================================
  // Constructor

  public UnresolvedClassImpl(String packageName,
                             String simpleName,
                             ElementContext ctx) {
    super(ctx);
    if (packageName == null) throw new IllegalArgumentException("null pkg");
    mPackageName = packageName;
    reallySetSimpleName(simpleName);
  }

  // ========================================================================
  // JClass elements

  public String getQualifiedName() {
    return mPackageName+"."+getSimpleName();
  }

  public String getFieldDescriptor() { return getQualifiedName(); }

  public JPackage getContainingPackage() { return null; }

  public boolean isAssignableFrom(JClass c) { return false; }

  public boolean isUnresolvedType() { return true; }
}
