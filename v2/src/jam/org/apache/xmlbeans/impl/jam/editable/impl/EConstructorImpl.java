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

import org.apache.xmlbeans.impl.jam.editable.EConstructor;
import org.apache.xmlbeans.impl.jam.editable.EParameter;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JParameter;

import java.util.List;
import java.util.ArrayList;
import java.io.StringWriter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EConstructorImpl extends EMemberImpl implements EConstructor {

  // ========================================================================
  // Variables

  private List mExceptionClassNames = null;
  private List mParameters = null;

  // ========================================================================
  // Constructors

  /*package*/ EConstructorImpl(JClass containingClass) {
    super(containingClass.getSimpleName(),containingClass);
  }

  protected EConstructorImpl(String methodName, JClass containingClass) {
    super(methodName,containingClass);
  }

  // ========================================================================
  // EConstructor implementation

  public void addException(JClass exceptionClass) {
    addException(exceptionClass.getQualifiedName());
  }

  public void addException(String exceptionClassName) {
    if (mExceptionClassNames == null) mExceptionClassNames = new ArrayList();
    mExceptionClassNames.add(exceptionClassName);
  }

  public void removeException(String exceptionClassName) {
    if (exceptionClassName == null) {
      throw new IllegalArgumentException("null classname");
    }
    if (mExceptionClassNames != null) {
      mExceptionClassNames.remove(exceptionClassName);
    }
  }

  public void removeException(JClass exceptionClass) {
    removeException(exceptionClass.getQualifiedName());
  }

  public EParameter addNewParameter(String typeName, String paramName) {
    EParameter param = new EParameterImpl(paramName,this,typeName);
    mParameters.add(param);
    return param;
  }

  public EParameter addNewParameter(JClass type, String name) {
    return addNewParameter(type.getQualifiedName(),name);
  }

  public void removeParameter(EParameter parameter) {
    if (mParameters != null) mParameters.remove(parameter);
  }

  public EParameter[] getEditableParameters() {
    return new EParameter[0];
  }

  // ========================================================================
  // JConstructor implementation

  public JParameter[] getParameters() {
    return getEditableParameters();
  }

  public JClass[] getExceptionTypes() {
    if (mExceptionClassNames == null || mExceptionClassNames.size() == 0) {
      return new JClass[0];
    }
    JClass[] out = new JClass[mExceptionClassNames.size()];
    for(int i=0; i<out.length; i++) {
      out[i] = getClassLoader().loadClass((String)mExceptionClassNames.get(i));
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
}