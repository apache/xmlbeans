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

import org.apache.xmlbeans.impl.jam.editable.EElement;
import org.apache.xmlbeans.impl.jam.editable.ESourcePosition;
import org.apache.xmlbeans.impl.jam.editable.EAnnotation;
import org.apache.xmlbeans.impl.jam.editable.EElementVisitor;
import org.apache.xmlbeans.impl.jam.*;

import java.util.List;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class EElementImpl implements EElement {

  // ========================================================================
  // Variables

  private String mSimpleName;
  private ESourcePosition mPosition = null;
  private JClassLoader mClassLoader;
  private List mAnnotations = null;

  // ========================================================================
  // Constructors

  protected EElementImpl() {}

  protected EElementImpl(String simpleName, JClassLoader loader) {
    if (simpleName == null) throw new IllegalArgumentException("null name");
    mSimpleName = simpleName;
    if (loader == null) throw new IllegalArgumentException("null loader");
    mClassLoader = loader;
  }

  public JElement getParent() {
    throw new IllegalStateException("NYI");//FIXME
  }

  // ========================================================================
  // JElement implementation

  public String getSimpleName() {
    return mSimpleName;
  }

  public JSourcePosition getSourcePosition() {
    return mPosition;
  }

  //FIXME
  public JComment[] getComments() {
    return new JComment[0];
  }

  public JAnnotation[] getAnnotations() {
    return getEditableAnnotations();
  }

  public JAnnotation[] getAnnotations(String named) {
    return getAnnotations(); //FIXME remove this method please
  }

  public JAnnotation getAnnotation(String named) {
    return getEditableAnnotation(named);
  }

  // ========================================================================
  // EElement implementation

  public void setSimpleName(String name) {
    mSimpleName = name;
  }

  public EAnnotation createAnnotation() {
    return null;
  }

  public ESourcePosition createSourcePosition() {
    return mPosition = new ESourcePositionImpl();
  }

  public void removeSourcePosition() {
    mPosition = null;
  }

  public ESourcePosition getEditableSourcePosition() {
    return mPosition;
  }

  public EAnnotation addNewAnnotation() {
    return null;
  }

  public void removeAnnotation(EAnnotation ann) {
  }

  public EAnnotation[] getEditableAnnotations() {
    if (mAnnotations == null) return new EAnnotation[0];
    EAnnotation[] out = new EAnnotation[mAnnotations.size()];
    mAnnotations.toArray(out);
    return out;
  }

  public EAnnotation getEditableAnnotation(String named) {
    if (mAnnotations == null) return null;
    //FIXME this is fairly gross
    for(int i=0; i<mAnnotations.size(); i++) {
      EAnnotation out = (EAnnotation)mAnnotations.get(i);
      if (out.getSimpleName().equals(named)) return out;
    }
    return null;
  }

  // ========================================================================
  // Public methods & JClass impl

  public JClassLoader getClassLoader() {
    return mClassLoader;
  }

  public static String defaultName(int count) {
    return "unnamed_"+count;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EElementImpl)) return false;
    final EElementImpl eElement = (EElementImpl) o;
    String qn = getQualifiedName();
    if (qn == null) return false;
    String oqn = eElement.getQualifiedName();
    if (oqn == null) return false;
    return qn.equals(oqn);
  }

  public int hashCode() {
    String qn = getQualifiedName();
    return (qn == null) ? 0 : qn.hashCode();
  }

  // ========================================================================
  // Protected methods

  protected void acceptAndWalkAll(EElementVisitor v, EElement[] elems) {
    for(int i=0; i<elems.length; i++) elems[i].acceptAndWalk(v);
  }


}