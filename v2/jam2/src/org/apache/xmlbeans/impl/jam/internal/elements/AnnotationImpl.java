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

import org.apache.xmlbeans.impl.jam.JElementVisitor;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.annotation.ValueMap;
import org.apache.xmlbeans.impl.jam.editable.EAnnotation;
import org.apache.xmlbeans.impl.jam.editable.EElementVisitor;

/**
 * <p>Standard implementation of AnnotationImpl.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class AnnotationImpl extends ElementImpl implements EAnnotation {

  // ========================================================================
  // Variables

  private AnnotationProxy mProxy;

  // ========================================================================
  // Constructors

  /*package*/ AnnotationImpl(ElementContext ctx, AnnotationProxy proxy,
                             String simplename) {
    super(ctx);
    if (proxy == null) throw new IllegalArgumentException("null proxy");
    mProxy = proxy;
    setSimpleName(simplename);
  }

  // ========================================================================
  // JAnnotation implementation

  public Object getProxy() {
    return mProxy;
  }

  public ValueMap getValues() {
    return mProxy.getValueMap();
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    return mProxy.getClass().getName(); //FIXME
  }

  public void accept(JElementVisitor visitor) {
    visitor.visit(this);
  }

  public void acceptAndWalk(JElementVisitor visitor) {
    visitor.visit(this);
  }

  // ========================================================================
  // EElement implementation

  public void accept(EElementVisitor visitor) {
    visitor.visit(this);
  }

  public void acceptAndWalk(EElementVisitor visitor) {
    accept(visitor);
  }

  public AnnotationProxy getEditableProxy() {
    return mProxy;
  }

}