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

import java.lang.reflect.Modifier;

import org.apache.xmlbeans.impl.jam_old.JMember;
import org.apache.xmlbeans.impl.jam_old.JElement;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BaseJMember extends BaseJElement implements JMember {

  // ========================================================================
  // Constructors

  protected BaseJMember() {}

  // ========================================================================
  // Partial JElement implementation

  public JElement getParent() { return getContainingClass(); }

  // ========================================================================
  // Partial JMember implementation

  public boolean isFinal() { return Modifier.isFinal(getModifiers()); }

  public boolean isAbstract() { return Modifier.isAbstract(getModifiers()); }

  public boolean isProtected() { return Modifier.isProtected(getModifiers()); }

  public boolean isPublic() { return Modifier.isPublic(getModifiers()); }

  public boolean isPrivate() { return Modifier.isProtected(getModifiers()); }

  public boolean isStatic() { return Modifier.isStatic(getModifiers()); }

  //only for JMethod impls
  public boolean isNative() { return Modifier.isNative(getModifiers()); }

  //only for JMethod impls
  public boolean isSynchronized() { return Modifier.isSynchronized(getModifiers()); }

  //only for JField impls
  public boolean isVolatile() { return Modifier.isVolatile(getModifiers()); }

  //only for JField impls
  public boolean isTransient() { return Modifier.isTransient(getModifiers()); }

  public boolean isPackagePrivate() {
    return !isPublic() && !isProtected() && !isPrivate();
  }

  //FIXME this is gross.  implement it properly and move up to BaseJElement,
  //probably should make it final as well.
  public String getQualifiedName() {
    if (getParent() == null) {
      return getSimpleName();
    } else {
      return getParent().getQualifiedName() + "." + getSimpleName();
    }
  }
}
