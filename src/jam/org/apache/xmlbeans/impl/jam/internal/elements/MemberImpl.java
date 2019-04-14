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
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.impl.jam.JMember;
import org.apache.xmlbeans.impl.jam.mutable.MMember;

import java.lang.reflect.Modifier;

/**
 * <p>Implementation of JMember and EMenber.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class MemberImpl extends AnnotatedElementImpl implements MMember {

  // ========================================================================
  // Variables

  private int mModifiers = 0;

  // ========================================================================
  // Constructors

  protected MemberImpl(ElementImpl parent) {
    super(parent);
  }

  protected MemberImpl(ElementContext ctx) {
    super(ctx);
  }

  // ========================================================================
  // JMember implementation

  public JClass getContainingClass() {
    JElement p = getParent();
    //FIXME very gross
    if (p instanceof JClass) return (JClass)p;
    if (p instanceof JMember) return ((JMember)p).getContainingClass();
    return null;
  }

  public int getModifiers() { return mModifiers; }

  public boolean isPackagePrivate() {
    return !isPrivate() && !isPublic() && !isProtected();
  }

  public boolean isPrivate() { return Modifier.isPrivate(getModifiers()); }

  public boolean isProtected() { return Modifier.isProtected(getModifiers()); }

  public boolean isPublic() { return Modifier.isPublic(getModifiers()); }

  // ========================================================================
  // MMember implementation

  public void setModifiers(int modifiers) { mModifiers = modifiers; }

}