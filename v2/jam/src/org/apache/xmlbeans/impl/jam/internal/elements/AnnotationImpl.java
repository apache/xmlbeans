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

import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;

/**
 * <p>Standard implementation of AnnotationImpl.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public final class AnnotationImpl extends ElementImpl implements MAnnotation {

  // ========================================================================
  // Variables

  private AnnotationProxy mProxy;
  private Object mAnnotationInstance = null;

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

  public JAnnotationValue[] getValues() {
    return mProxy.getValues();
  }

  public JAnnotationValue getValue(String name) {
    return mProxy.getValue(name);
  }

  public Object getAnnotationInstance() {
    return mAnnotationInstance;
  }

  // ========================================================================
  // MAnnotation implementation

  public void setAnnotationInstance(Object o) {
    mAnnotationInstance = o;
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() {
    return mProxy.getClass().getName(); //FIXME
  }

  public void accept(MVisitor visitor) { visitor.visit(this); }

  public void accept(JVisitor visitor) { visitor.visit(this); }

  // ========================================================================
  // MElement implementation

  public AnnotationProxy getMutableProxy() {
    return mProxy;
  }

}