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
import org.apache.xmlbeans.impl.jam.JElementVisitor;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.editable.EElementVisitor;
import org.apache.xmlbeans.impl.jam.editable.EInvokable;
import org.apache.xmlbeans.impl.jam.editable.EParameter;
import org.apache.xmlbeans.impl.jam.internal.classrefs.DirectJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.UnqualifiedJClassRef;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class InvokableImpl extends MemberImpl implements EInvokable {

  // ========================================================================
  // Variables

  private List mExceptionClassRefs = null;
  private List mParameters = null;

  // ========================================================================
  // Constructors

  protected InvokableImpl(ClassImpl containingClass) {
    super(containingClass);
  }

  // ========================================================================
  // EConstructor implementation

  public void addException(JClass exceptionClass) {
    if (exceptionClass == null) {
      throw new IllegalArgumentException("null exception class");
    }
    if (mExceptionClassRefs == null) mExceptionClassRefs = new ArrayList();
    mExceptionClassRefs.add(DirectJClassRef.create(exceptionClass));
  }

  public void addException(String qcname) {
    if (qcname == null) throw new IllegalArgumentException("null qcname");
    if (mExceptionClassRefs == null) mExceptionClassRefs = new ArrayList();
    mExceptionClassRefs.add(QualifiedJClassRef.
                            create(qcname,(ClassImpl)getContainingClass()));
  }

  public void addUnqualifiedException(String ucname) {
    if (ucname == null) throw new IllegalArgumentException("null qcname");
    if (mExceptionClassRefs == null) mExceptionClassRefs = new ArrayList();
    mExceptionClassRefs.add(UnqualifiedJClassRef.
                            create(ucname,(ClassImpl)getContainingClass()));
  }

  public void removeException(String exceptionClassName) {
    if (exceptionClassName == null) {
      throw new IllegalArgumentException("null classname");
    }
    if (mExceptionClassRefs != null) {
      mExceptionClassRefs.remove(exceptionClassName);
    }
  }

  public void removeException(JClass exceptionClass) {
    removeException(exceptionClass.getQualifiedName());
  }

  public EParameter addNewParameter() {
    if (mParameters == null) mParameters = new ArrayList();
    EParameter param = new ParameterImpl(defaultName(mParameters.size()),
                                          this,"java.lang.Object");
    mParameters.add(param);
    return param;
  }

  public void removeParameter(EParameter parameter) {
    if (mParameters != null) mParameters.remove(parameter);
  }

  public EParameter[] getEditableParameters() {
    if (mParameters == null || mParameters.size() == 0) {
    return new EParameter[0];
    } else {
      EParameter[] out = new EParameter[mParameters.size()];
      mParameters.toArray(out);
      return out;
    }
  }

  // ========================================================================
  // EElement implementation

  public void acceptAndWalk(EElementVisitor visitor) {
    accept(visitor);
    acceptAndWalkAll(visitor,getEditableParameters());
    visitAnnotations(visitor);
  }


  // ========================================================================
  // JInvokable implementation

  public JParameter[] getParameters() {
    return getEditableParameters();
  }

  public JClass[] getExceptionTypes() {
    if (mExceptionClassRefs == null || mExceptionClassRefs.size() == 0) {
      return new JClass[0];
    }
    JClass[] out = new JClass[mExceptionClassRefs.size()];
    for(int i=0; i<out.length; i++) {
      out[i] = ((JClassRef)mExceptionClassRefs.get(i)).getRefClass();
    }
    return out;
  }

  public String getQualifiedName() {
    //REVIEW this probably needs more thought
    StringWriter out = new StringWriter();
    out.write(getContainingClass().getQualifiedName());
    out.write('.');
    out.write(getSimpleName());
    out.write('(');
    JParameter[] params = getParameters();
    for(int i=0; i<params.length; i++) {
      out.write(params[i].getType().getQualifiedName());
      if (i<params.length-1) out.write(", ");
    }
    out.write(')');
    return out.toString();
  }

  // ========================================================================
  // JElement implementation

  public void acceptAndWalk(JElementVisitor visitor) {
    accept(visitor);
    acceptAndWalkAll(visitor,getEditableParameters());
    visitAnnotations(visitor);
  }

  // ========================================================================
  // Other public methods

  //FIXME this is here only so the parser can be lazy - please remove it
  public void setUnqualifiedThrows(List classnames) {
    if (classnames == null || classnames.size() == 0) {
      mExceptionClassRefs= null;
      return;
    }
    mExceptionClassRefs = new ArrayList(classnames.size());
    for(int i=0; i<classnames.size(); i++) {
      mExceptionClassRefs.add(UnqualifiedJClassRef.create
                              ((String)classnames.get(i),
                               (ClassImpl)getContainingClass()));
    }
  }
}