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

import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;

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
  private String mQualifiedName = null;


  // ========================================================================
  // Constructors

  /*package*/ AnnotationImpl(ElementContext ctx,
                             AnnotationProxy proxy,
                             String qualifiedName) {
    super(ctx);
    if (proxy == null) throw new IllegalArgumentException("null proxy");
    if (qualifiedName == null) throw new IllegalArgumentException("null qn");
    mProxy = proxy;
    // review maybe this should just be the behavior in the default impl
    // of getSimpleName().
    setSimpleName(qualifiedName.substring(qualifiedName.lastIndexOf('.')+1));
    mQualifiedName = qualifiedName;
  }

  // ========================================================================
  // JAnnotation implementation

  public Object getProxy() { return mProxy; }

  public JAnnotationValue[] getValues() { return mProxy.getValues(); }

  public JAnnotationValue getValue(String name) {
    return mProxy.getValue(name);
  }

  public Object getAnnotationInstance() { return mAnnotationInstance; }

  // ========================================================================
  // MAnnotation implementation

  public void setAnnotationInstance(Object o) {
    mAnnotationInstance = o;
  }

  public void setSimpleValue(String name, Object value, JClass type) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (type == null) throw new IllegalArgumentException("null type");
    if (value == null) throw new IllegalArgumentException("null value");
    mProxy.setValue(name,value,type);
  }

  public MAnnotation createNestedValue(String name, String annTypeName) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (annTypeName == null) throw new IllegalArgumentException("null typename");
    AnnotationProxy p = getContext().createAnnotationProxy(annTypeName);
    AnnotationImpl out = new AnnotationImpl(getContext(),p,annTypeName);
    JClass type  = getContext().getClassLoader().loadClass(annTypeName);
    mProxy.setValue(name,out,type);
    return out;
  }

  public MAnnotation[] createNestedValueArray(String name,
                                              String annComponentTypeName,
                                              int dimensions) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (annComponentTypeName == null) throw new IllegalArgumentException("null typename");
    if (dimensions < 0) throw new IllegalArgumentException("dimensions = "+dimensions);
    MAnnotation[] out = new MAnnotation[dimensions];
    for(int i=0; i<out.length; i++) {
      AnnotationProxy p = getContext().createAnnotationProxy(annComponentTypeName);
      out[i] = new AnnotationImpl(getContext(),p,annComponentTypeName);
    }
    JClass type  = getContext().getClassLoader().loadClass("[L"+annComponentTypeName+";");
    mProxy.setValue(name,out,type);
    return out;
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() { return mQualifiedName; }

  public void accept(MVisitor visitor) { visitor.visit(this); }

  public void accept(JVisitor visitor) { visitor.visit(this); }


}