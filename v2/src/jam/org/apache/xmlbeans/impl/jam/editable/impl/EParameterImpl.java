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

import org.apache.xmlbeans.impl.jam.editable.EParameter;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.JClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.DirectJClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.UnqualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JMember;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EParameterImpl extends EMemberImpl implements EParameter {

  // ========================================================================
  // Variables

  private JClassRef mTypeClassRef;

  // ========================================================================
  // Constructors

  // be careful with this one, should really only be used by the parser
  public EParameterImpl() {}

  public EParameterImpl(String simpleName,
                        EMemberImpl containingMember,
                        String typeName)
  {
    super(simpleName,(EClassImpl)containingMember.getContainingClass());
    setType(typeName);
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    return getContainingClass().getQualifiedName();//FIXME
  }

  // ========================================================================
  // EParameter implementation

  public void setType(String qcname) {
    if (qcname == null) throw new IllegalArgumentException("null typename");
    mTypeClassRef = QualifiedJClassRef.create
            (qcname,(EClassImpl)getContainingClass());
  }

  public void setType(JClass qcname) {
    if (qcname == null) throw new IllegalArgumentException("null qcname");
    mTypeClassRef = DirectJClassRef.create(qcname);
  }

  public void setUnqualifiedType(String ucname) {
    if (ucname == null) throw new IllegalArgumentException("null ucname");
    mTypeClassRef = UnqualifiedJClassRef.create
            (ucname,(EClassImpl)getContainingClass());
  }

  // ========================================================================
  // JParameter implementation

  public JClass getType() {
    return mTypeClassRef.getRefClass();
  }

  // ========================================================================
  // Package methods

  /*package*/ void setContainingMember(EMemberImpl member) {
    System.out.println("\n\n\n\nSSET CONTAINING MEMBER "+member);
    super.setContainingClass((EClassImpl)member.getContainingClass());
  }
}
