/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
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