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

import org.apache.xmlbeans.impl.jam.editable.EMember;
import org.apache.xmlbeans.impl.jam.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class EMemberImpl extends EElementImpl implements EMember {

  // ========================================================================
  // Variables

  private int mModifiers = 0;
  private EClassImpl mContainingClass = null;
  private List mComments = null;

  // ========================================================================
  // Constructors

  protected EMemberImpl() {}

  protected EMemberImpl(String simpleName, EClassImpl containingClass) {
    super(simpleName,containingClass.getClassLoader());
    if (containingClass == null) {
      throw new IllegalArgumentException("null class");
    }
    mContainingClass = containingClass;
  }

  protected EMemberImpl(String simpleName, JClassLoader loader) {
    super(simpleName,loader);
  }

  // ========================================================================
  // JMember implementation

  public JClass getContainingClass() {
    return mContainingClass;
  }

  public int getModifiers() {
    return mModifiers;
  }

  public boolean isPackagePrivate() {
    return !isPrivate() && !isPublic() && !isProtected();
  }

  public boolean isPrivate() {
    return Modifier.isPrivate(mModifiers);
  }

  public boolean isProtected() {
    return Modifier.isProtected(mModifiers);
  }

  public boolean isPublic() {
    return Modifier.isPublic(mModifiers);
  }

  // ========================================================================
  // EMember implementation

  public void setModifiers(int modifiers) {
    mModifiers = modifiers;
  }

  public void addComment(String comment) {
    if (mComments == null) mComments = new ArrayList();
    mComments.add(comment);
  }

  // ========================================================================
  // Package methods

  /*package*/ void setContainingClass(EClassImpl c) {
    mContainingClass = c;
  }

}