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
import org.apache.xmlbeans.impl.jam.mutable.MPackage;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Generic implementation of JPackage/InternalJPackage that is good
 * enough for all cases, right now.  This might change if we ever wrap
 * a model which natively supports package-level annotation.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class PackageImpl extends AnnotatedElementImpl implements MPackage {

  // ========================================================================
  // Variables

  private List mRootClasses = new ArrayList();
  private String mName;

  // ========================================================================
  // Constructors

  public PackageImpl(ElementContext ctx, String name) {
    super(ctx);
    mName = name;
    int lastDot = mName.lastIndexOf('.');
    setSimpleName((lastDot == -1) ? mName : mName.substring(lastDot + 1));
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() { return mName; }

  public void accept(ElementVisitor visitor) {
    visitor.visit(this);
  }

  public void acceptAndWalk(ElementVisitor visitor) {
    accept(visitor);
    acceptAndWalkAll(visitor,getClasses());
  }

  // ========================================================================
  // JPackage implementation

  public JClass[] getClasses() {
    JClass[] out = new JClass[mRootClasses.size()];
    mRootClasses.toArray(out);
    return out;
  }

}
