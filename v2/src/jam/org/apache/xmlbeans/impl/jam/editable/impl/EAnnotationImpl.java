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

import org.apache.xmlbeans.impl.jam.editable.*;
import org.apache.xmlbeans.impl.jam.*;

import java.util.ArrayList;

/**
 * <p>Standard implementation of EAnnotationImpl.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EAnnotationImpl extends EMemberImpl implements EAnnotation {

  // ========================================================================
  // Variables

  private ArrayList mMembers = null;
  private Object m175Annotation = null;
  private String mJavadocText = null;
  private EAnnotationDefinition mDefinition = null;

  // ========================================================================
  // Constructors

  public EAnnotationImpl() {}

  public EAnnotationImpl(String simpleName, JClassLoader loader) {
    super(simpleName,loader);
  }

  // ========================================================================
  // EAnnotation implementation

  public EAnnotationMember addNewMember() {
    if (mMembers == null) mMembers = new ArrayList();
    EAnnotationMemberImpl out =
            new EAnnotationMemberImpl(defaultName(mMembers.size()),
                                      getClassLoader());
    mMembers.add(out);
    return out;
  }

  public EAnnotationMember[] getEditableMembers() {
    if (mMembers == null) return new EAnnotationMember[0];
    EAnnotationMember[] out = new EAnnotationMember[mMembers.size()];
    mMembers.toArray(out);
    return out;
  }

  public EAnnotationMember getEditableMember(String named) {
    if (mMembers == null) return null;
    EAnnotationMember out;
    for(int i=0; i<mMembers.size(); i++) {
      out = (EAnnotationMember)mMembers.get(i);
      if (named.equals(out.getName())) return out;
    }
    return null;
  }

  public void setDefinition(JAnnotationDefinition jad) {
  }

  public void setDefinition(String qualifiedClassName) {
  }

  public void setDefinitionUnqualified(String unqualifiedClassName) {
  }

  public void setAnnotationObject(Object o) {
    m175Annotation = o;
  }

  public void setJavadocText(String text) {
    mJavadocText = text;
  }

  // ========================================================================
  // JAnnotation implementation

  public String getName() {
    return super.getSimpleName();
  }

  public JAnnotationMember[] getMembers() {
    return getEditableMembers();
  }

  public JAnnotationMember getMember(String named) {
   return getEditableMember(named);
  }

  public JAnnotationDefinition getDefinition() {
    return mDefinition;
  }

  public Object getAnnotationObject() {
    return m175Annotation;
  }

  public String getJavadocText() {
    return mJavadocText;
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    return null;
  }

  // ========================================================================
  // Deprecated JAnnotation implementation

  public JAnnotation[] getAnnotations() {
    return null;
  }

  public JAnnotation[] getAnnotations(String named) {
    return null;
  }

  public JAnnotation getAnnotation(String named) {
    return null;
  }

  public String getStringValue() {
    return null;
  }

  public int getIntValue() {
    return 0;
  }

  public boolean getBooleanValue() {
    return false;
  }

  public long getLongValue() {
    return 0;
  }

  public short getShortValue() {
    return 0;
  }

  public double getDoubleValue() {
    return 0;
  }

  public byte getByteValue() {
    return 0;
  }

}
