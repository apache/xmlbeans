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
package org.apache.xmlbeans.impl.jam_old.editable.impl;

import org.apache.xmlbeans.impl.jam_old.editable.EAnnotationMemberDefinition;
import org.apache.xmlbeans.impl.jam_old.JClass;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EAnnotationMemberDefinitionImpl extends EMethodImpl
        implements EAnnotationMemberDefinition
 {

  // ========================================================================
  // Variables

  private Object mDefaultValue = null;

  // ========================================================================
  // Constructors

  public EAnnotationMemberDefinitionImpl(String simpleName,
                                         EClassImpl containingClass)
  {
    super(simpleName,containingClass);
  }

  // ========================================================================
  // EAnnotationMemberDefinition implementation

  public Object getDefaultValue() {
    return mDefaultValue;
  }

  public void setDefaultValue(Object o) {
    mDefaultValue = o;
  }

  public JClass getType() {
    return super.getReturnType();
  }

  public void setType(String qualifiedTypeName) {
    super.setReturnType(qualifiedTypeName);
  }

  public void setUnqualifiedType(String unqualifiedTypeName) {
    super.setUnqualifiedReturnType(unqualifiedTypeName);
  }

  public void setType(JClass c) {
    super.setReturnType(c);
  }
}