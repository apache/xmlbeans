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
package org.apache.xmlbeans.impl.jam.annogen.internal;

import org.apache.xmlbeans.impl.jam.annogen.provider.ElementId;
import org.apache.xmlbeans.impl.jam.JAnnotatedElement;
import org.apache.xmlbeans.impl.jam.JMember;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JConstructor;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JInvokable;
import org.apache.xmlbeans.impl.jam.JParameter;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JElementId implements ElementId {

  // ========================================================================
  // Variables

  private JAnnotatedElement mElement;

  // ========================================================================
  // Constructor

  public JElementId(JAnnotatedElement je) {
    if (je == null) throw new IllegalArgumentException("null je");
    mElement = je;
  }

  // ========================================================================
  // ElementId implementation

  public String getContainingClass() {
    if (mElement instanceof JMember) { //field, method, constructor
      return ((JMember)mElement).getContainingClass().getQualifiedName();
    } else if (mElement instanceof JClass) {
      JClass declaring = ((JClass)mElement).getContainingClass();
      return (declaring == null) ? null : declaring.getQualifiedName();
    } else if (mElement instanceof JPackage) {
      return null;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String getContainingPackage() {
    if (mElement instanceof JMember) { //field, method, constructor
      return ((JMember)mElement).
        getContainingClass().getContainingPackage().getQualifiedName();
    } else if (mElement instanceof JClass) {
      return ((JClass)mElement).getContainingPackage().getQualifiedName();
    } else if (mElement instanceof JPackage) {
      return null;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String getName() {
    return mElement.getQualifiedName(); //FIXME
  }

  public int getType() {
    if (mElement instanceof JField) {
      return FIELD_TYPE;
    } else if (mElement instanceof JMethod) {
      return METHOD_TYPE;
    } else if (mElement instanceof JConstructor) {
      return CONSTRUCTOR_TYPE;
    } else if (mElement instanceof JClass) {
      return CLASS_TYPE;
    } else if (mElement instanceof JPackage) {
      return PACKAGE_TYPE;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String[] getSignature() {
    if (mElement instanceof JInvokable) {
      JParameter[] params = ((JInvokable)mElement).getParameters();
      String[] out = new String[params.length];
      for(int i=0; i<params.length; i++) {
        out[i] = params[i].getType().getQualifiedName();
      }
      return out;
    } else {
      return null;
    }
  }

  public int getParameterNumber() {
    throw new IllegalStateException("NYI"); //FIXME
  }

  // ========================================================================
  // For internal use only

  public JAnnotatedElement getElement() { return mElement; }


}