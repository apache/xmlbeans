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

package org.apache.xmlbeans.impl.jam.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.impl.jam.JSourcePosition;


/**
 * <p>Generic implementation of JPackage/InternalJPackage that is good
 * enough for all cases, right now.  This might change if we ever wrap
 * a model which natively supports package-level annotation.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JPackageImpl extends BaseJElement implements InternalJPackage {

  // ========================================================================
  // Variables

  private List mRootClasses = null;
  private String mName;
  private JElement mParent = null;

  // ========================================================================
  // Constructors

  public JPackageImpl(String name) {
    mName = name;
  }

  // ========================================================================
  // InternalJPackage implementation

  public void setParent(JElement parent) {
    mParent = parent;
  }

  /**
   * <p>Need this so we can tell arbitrary packages that an arbitrary
   * class is their child.  See JRootImpl constructor.</p>
   */
  public void addClass(JClass clazz) {
    if (mRootClasses == null) mRootClasses = new ArrayList();
    mRootClasses.add(clazz);
  }


  // ========================================================================
  // JElement implementation

  /**
   * The parent of a package is always the root.
   */
  public JElement getParent() {
    return mParent;
  }

  public JElement[] getChildren() {
    return getClasses();
  }

  public String getSimpleName() {
    int lastDot = mName.lastIndexOf('.');
    if (lastDot == -1) return mName;
    return mName.substring(lastDot + 1);
  }

  public String getQualifiedName() {
    return mName;
  }

  //REVIEW maybe we should try to find package.html and use that as
  //source position?
  public JSourcePosition getSourcePosition() {
    return null;
  }

  // ========================================================================
  // JPackage implementation

  public JClass[] getClasses() {
    JClass[] out = new JClass[mRootClasses.size()];
    mRootClasses.toArray(out);
    return out;
  }

  // ========================================================================
  // BaseJElement implementation

  // no support for local package-level comments or annotations

  protected void getLocalAnnotations(Collection out) {
  }

  protected void getLocalComments(Collection out) {
  }

}
