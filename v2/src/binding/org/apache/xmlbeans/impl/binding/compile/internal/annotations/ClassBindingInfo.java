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
package org.apache.xmlbeans.impl.binding.compile.internal.annotations;

import org.apache.xmlbeans.impl.jam.annotation.TypedAnnotationProxyBase;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ClassBindingInfo extends TypedAnnotationProxyBase {

  // ========================================================================
  // Variables

  private TypeTarget mDefaultSchemaTypeTarget = null;
  private boolean mExclude = false;

  // ========================================================================
  // Public methods

  public TypeTarget getDefaultTargetType() {
    return mDefaultSchemaTypeTarget;
  }

  public boolean isExclude() { return mExclude; }

  // ========================================================================
  // Mutators

  public void setDefaultTargetType(TypeTarget stt) {
    mDefaultSchemaTypeTarget = stt;
  }

  public void setExclude(boolean b) { mExclude = b; }



}
