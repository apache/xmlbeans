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
package org.apache.xmlbeans.impl.binding.compile.internal.annotations;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class TypeTarget extends NamedTarget {

  // ========================================================================
  // Variables

  private List mTargetElements = null;
  private boolean mIgnoreInheritance = false;

  // ========================================================================
  // Constructors

  // type targets don't have to have a name - that means they want us to
  // generate one
  public TypeTarget() {}

  public TypeTarget(QName qn) {
    super(qn);
  }

  public TypeTarget(String ns, String local) {
    super(ns,local);
  }

  // ========================================================================
  // Accessors

  public TopLevelElementTarget[] getTopLevelElements() {
    if (mTargetElements == null) return null;
    TopLevelElementTarget[] out = new TopLevelElementTarget[mTargetElements.size()];
    mTargetElements.toArray(out);
    return out;
  }

  public boolean isIgnoreJavaInheritance() { return mIgnoreInheritance; }

  // ========================================================================
  // Mutators

  public void addNewTopLevelElement(QName qn) {
    if (mTargetElements == null) mTargetElements = new ArrayList();
    mTargetElements.add(new TopLevelElementTarget(qn));
  }

  public void setIgnoreJavaInheritance(boolean b) { mIgnoreInheritance = b; }

}
