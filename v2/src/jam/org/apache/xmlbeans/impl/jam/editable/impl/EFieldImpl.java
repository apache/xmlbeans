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

package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.EField;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.JClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.UnqualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.DirectJClassRef;
import org.apache.xmlbeans.impl.jam.JClass;

import java.lang.reflect.Modifier;

/**
 * Standard implementation of EField.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EFieldImpl extends EMemberImpl implements EField {

  // ========================================================================
  // Variables

  private JClassRef mTypeClassRef;

  // ========================================================================
  // Constructors

  /*package*/ EFieldImpl(String simpleName,
                         EClassImpl containingClass,
                         String qualifiedTypeClassName) {
    super(simpleName,containingClass);
    mTypeClassRef = QualifiedJClassRef.create
            (qualifiedTypeClassName,containingClass);
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    //REVIEW
    return getContainingClass().getQualifiedName()+"."+getSimpleName();
  }

  // ========================================================================
  // EField implementation

  public void setType(JClass type) {
    if (type == null) throw new IllegalArgumentException("null type");
    mTypeClassRef = DirectJClassRef.create(type);
  }

  public void setType(String qcname) {
    if (qcname == null) throw new IllegalArgumentException("null qcname");
    mTypeClassRef = QualifiedJClassRef.create
            (qcname,(EClassImpl)getContainingClass());
  }

  public void setUnqualifiedType(String ucname) {
    if (ucname == null) throw new IllegalArgumentException("null ucname");
    mTypeClassRef = UnqualifiedJClassRef.create
            (ucname,(EClassImpl)getContainingClass());
  }

  // ========================================================================
  // JField implementation

  public JClass getType() {
    if (mTypeClassRef == null) throw new IllegalStateException();
    return mTypeClassRef.getRefClass();
  }

  public boolean isFinal() {
    return Modifier.isFinal(getModifiers());
  }

  public boolean isStatic() {
    return Modifier.isStatic(getModifiers());
  }

  public boolean isVolatile() {
    return Modifier.isVolatile(getModifiers());
  }

  public boolean isTransient() {
    return Modifier.isTransient(getModifiers());
  }
}