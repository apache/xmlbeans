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

import org.apache.xmlbeans.impl.jam.editable.EMethod;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.internal.VoidJClass;

import java.lang.reflect.Modifier;

/**
 * Standard implementation of EMethod.  It's probably bad inheritance to
 * extend EConstructorImpl, but it's convenient and no one should ever care
 * since this is a private class; there is no inheritance between method and
 * constructor in the public API.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EMethodImpl extends EConstructorImpl implements EMethod {


  // ========================================================================
  // Variables

  private String mReturnTypeName = null;

  // ========================================================================
  // Constructors

  public EMethodImpl(String simpleName, JClass containingClass) {
    super(simpleName,containingClass);
  }

  // ========================================================================
  // EMethod implementation

  public void setReturnType(String className) {
    mReturnTypeName = className;
  }

  public void setReturnType(JClass c) {
    setReturnType(c.getQualifiedName());
  }

  // ========================================================================
  // JMethod implementation

  public JClass getReturnType() {
    if (mReturnTypeName == null) {
      return VoidJClass.getInstance();
    } else {
      return getClassLoader().loadClass(mReturnTypeName);
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
}