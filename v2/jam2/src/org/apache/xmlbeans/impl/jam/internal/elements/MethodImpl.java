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
import org.apache.xmlbeans.impl.jam.visitor.ElementVisitor;
import org.apache.xmlbeans.impl.jam.editable.EMethod;
import org.apache.xmlbeans.impl.jam.internal.classrefs.DirectJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.UnqualifiedJClassRef;

import java.lang.reflect.Modifier;

/**
 * <p>Standard implementation of EMethod.  It's probably bad inheritance to
 * extend ConstructorImpl, but it's convenient and no one should ever care
 * since this is a private class; there is no inheritance between method and
 * constructor in the public API.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class MethodImpl extends InvokableImpl implements EMethod {

  // ========================================================================
  // Variables

  private JClassRef mReturnTypeRef = null;

  // ========================================================================
  // Constructors

  /*package*/ MethodImpl(String simpleName, ClassImpl containingClass) {
    super(containingClass);
    setSimpleName(simpleName);
  }

  // ========================================================================
  // EMethod implementation

  public void setReturnType(String className) {
    mReturnTypeRef = QualifiedJClassRef.create
            (className,(ClassImpl)getContainingClass());
  }

  public void setUnqualifiedReturnType(String unqualifiedTypeName) {
    mReturnTypeRef = UnqualifiedJClassRef.create
            (unqualifiedTypeName,(ClassImpl)getContainingClass());
  }

  public void setReturnType(JClass c) {
    mReturnTypeRef = DirectJClassRef.create(c);
  }

  // ========================================================================
  // JMethod implementation

  public JClass getReturnType() {
    if (mReturnTypeRef == null) {
      return getClassLoader().loadClass("void");
    } else {
      return mReturnTypeRef.getRefClass();
    }
  }

  public boolean isFinal() {
    return Modifier.isFinal(getModifiers());
  }

  public boolean isStatic() {
    return Modifier.isStatic(getModifiers());
  }

  public boolean isAbstract() {
    return Modifier.isAbstract(getModifiers());
  }

  public boolean isNative() {
    return Modifier.isNative(getModifiers());
  }

  public boolean isSynchronized() {
    return Modifier.isSynchronized(getModifiers());
  }

  // ========================================================================
  // JElement implementation

  public void accept(ElementVisitor visitor) {
    visitor.visit(this);
  }

}