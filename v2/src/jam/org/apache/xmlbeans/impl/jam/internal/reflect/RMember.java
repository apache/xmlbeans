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

package org.apache.xmlbeans.impl.jam.internal.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.BaseJElement;

/**
 * Reflection-backed implementation of JMember.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
/*package*/ 
abstract class RMember extends BaseJElement implements JMember {

  // ========================================================================
  // Variables

  private Member mMember;
  protected JClassLoader mLoader;

  // ========================================================================
  // Constructors
  
  public RMember(Member m, JClassLoader loader) {
    mLoader = loader;
    mMember = m;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() {
    return RClassLoader.getClassFor(mMember.getDeclaringClass(),mLoader);
  }

  public JElement[] getChildren() { return null; }

  public String getSimpleName() { return mMember.getName(); }

  public String getQualifiedName() { return mMember.getName(); } //FIXME

  // ========================================================================
  // JMember implementation

  public boolean isFinal() { 
    return Modifier.isFinal(mMember.getModifiers());
  }

  public boolean isAbstract() { 
    return Modifier.isAbstract(mMember.getModifiers());
  }

  public boolean isProtected() { 
    return Modifier.isProtected(mMember.getModifiers());
  }

  public boolean isPublic() { 
    return Modifier.isPublic(mMember.getModifiers());
  }

  public boolean isPrivate() { 
    return Modifier.isPrivate(mMember.getModifiers());
  }

  public boolean isStatic() { 
    return Modifier.isStatic(mMember.getModifiers());
  }

  public boolean isPackagePrivate() {
    return !isPublic() && !isProtected() && !isPrivate();
  }

  public int getModifiers() { return mMember.getModifiers(); }

  /**
   * We're never going to know this.
   */
  public JSourcePosition getSourcePosition() { return null; }

  // ========================================================================
  // JMember implementation
  
  public JClass getContainingClass() {
    return RClassLoader.getClassFor(mMember.getDeclaringClass(),mLoader);
  }

  public boolean isSynthetic() { return false; }//FIXME?

  // ========================================================================
  // BaseJElement implementation

  /**
   * We can't implement this until JSR175 is here.
   */
  protected  void getLocalAnnotations(Collection out) {}

  /**
   * We can't ever implement this.
   */
  protected  void getLocalComments(Collection out) {}

}
