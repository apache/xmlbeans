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
import org.apache.xmlbeans.impl.jam.*;

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

  // ========================================================================
  // Constructors

  protected EElementImpl() {}

  protected EElementImpl(String simpleName, JClassLoader loader) {
    if (simpleName == null) throw new IllegalArgumentException("null name");
    mSimpleName = simpleName;
    if (loader == null) throw new IllegalArgumentException("null loader");
    mClassLoader = loader;
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


  //FIXME
  public JAnnotation[] getAnnotations() {
    return new JAnnotation[0];
  }

  //FIXME
  public JAnnotation[] getAnnotations(String named) {
    return new JAnnotation[0];
  }

  public JAnnotation getAnnotation(String named) {
    return null;
  }

  public JElement getParent() {
    return null;
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

  public void removeAnnotation(EAnnotation ann) {
  }

  public EAnnotation[] getEditableAnnotations() {
    return null;
  }

  public EAnnotation getEditableAnnotation(String named) {
    return null;
  }

  public EAnnotation[] getEditableAnnotations(String named) {
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


}