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

import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Parameter;



/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocElementId implements ElementId {

  // ========================================================================
  // Variables

  private ProgramElementDoc mPed;
  private int mParameterNumber;

  // ========================================================================
  // Constructor

  public JavadocElementId(ProgramElementDoc ped) {
    if (ped == null) throw new IllegalArgumentException("null ped");
    mPed = ped;
  }

  public JavadocElementId(ExecutableMemberDoc member, int pnum) {
    if (member == null) throw new IllegalArgumentException("null param");
    mPed = member;
    mParameterNumber = pnum;
  }

  // ========================================================================
  // ElementId implementation

  public String getContainingClass() {
    return mPed.containingClass() != null ?
        mPed.containingClass().qualifiedName() : null;
  }

  public String getContainingPackage() {
    return mPed.containingPackage() != null ?
        mPed.containingPackage().name() : null;
  }

  public String getName() {
    return mPed.name();
  }

  public int getType() {
    if (mParameterNumber > -1) {
      return PARAMETER_TYPE;
    } else if (mPed.isField()) {
      return FIELD_TYPE;
    } else if (mPed.isMethod()) {
      return METHOD_TYPE;
    } else if (mPed.isConstructor()) {
      return CONSTRUCTOR_TYPE;
    } else if (mPed.isClass()) {
      return CLASS_TYPE;
    } else if (mPed.isPackagePrivate()) {
      return PACKAGE_TYPE;
    } else {
      throw new IllegalStateException();
    }
  }

  public String[] getSignature() {
    if (mPed instanceof ExecutableMemberDoc) {
      Parameter[] params = ((ExecutableMemberDoc)mPed).parameters();
      if (params == null || params.length == 0) return new String[0];
      String[] out = new String[params.length];
      for(int i=0; i<out.length; i++) out[i] = params[i].name();
      return out;
    } else {
      return null;
    }
  }

  public int getParameterNumber() { return mParameterNumber; }

  // ========================================================================
  // Package methods

  /*package*/ ProgramElementDoc getAnnotatedElement() { return mPed; }
}