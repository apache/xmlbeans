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
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.apache.xmlbeans.impl.jam.internal.classrefs.DirectJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.UnqualifiedJClassRef;

/**
 * <p>Implementation of JParameter and MParameter.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ParameterImpl extends MemberImpl implements MParameter {

  // ========================================================================
  // Variables

  private JClassRef mTypeClassRef;

  // ========================================================================
  // Constructors

  /*package*/ ParameterImpl(String simpleName,
                            InvokableImpl containingMember,
                            String typeName)
  {
    super(containingMember);
    setSimpleName(simpleName);
    setType(typeName);
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    return getContainingClass().getQualifiedName();//FIXME
  }

  // ========================================================================
  // MParameter implementation

  public void setType(String qcname) {
    if (qcname == null) throw new IllegalArgumentException("null typename");
    mTypeClassRef = QualifiedJClassRef.create
            (qcname,(ClassImpl)getContainingClass());
  }

  public void setType(JClass qcname) {
    if (qcname == null) throw new IllegalArgumentException("null qcname");
    mTypeClassRef = DirectJClassRef.create(qcname);
  }

  public void setUnqualifiedType(String ucname) {
    if (ucname == null) throw new IllegalArgumentException("null ucname");
    mTypeClassRef = UnqualifiedJClassRef.create
            (ucname,(ClassImpl)getContainingClass());
  }

  // ========================================================================
  // JParameter implementation

  public JClass getType() {
    return mTypeClassRef.getRefClass();
  }

  // ========================================================================
  // JElement implementation

  public void accept(MVisitor visitor) { visitor.visit(this); }

  public void accept(JVisitor visitor) { visitor.visit(this); }

}
