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

import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.mutable.MElement;
import org.apache.xmlbeans.impl.jam.mutable.MSourcePosition;

/**
 * <p>Implementation of JElement and MElement.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class ElementImpl implements MElement {

  // ========================================================================
  // Constants

  // help reduce waste
  protected static final ElementImpl[] NO_NODE = new ElementImpl[0];
  protected static final ClassImpl[] NO_CLASS = new ClassImpl[0];
  protected static final FieldImpl[] NO_FIELD = new FieldImpl[0];
  protected static final ConstructorImpl[] NO_CONSTRUCTOR = new ConstructorImpl[0];
  protected static final MethodImpl[] NO_METHOD = new MethodImpl[0];
  protected static final ParameterImpl[] NO_PARAMETER = new ParameterImpl[0];
  protected static final JPackage[] NO_PACKAGE = new JPackage[0];//FIXME
  protected static final AnnotationImpl[] NO_ANNOTATION = new AnnotationImpl[0];

  protected static final CommentImpl[] NO_COMMENT = new CommentImpl[0];
  protected static final JProperty[] NO_PROPERTY = new JProperty[0];//FIXME

  // ========================================================================
  // Variables

  private ElementContext mContext;
  protected String mSimpleName;
  private MSourcePosition mPosition = null;
  private Object mArtifact = null;
  private ElementImpl mParent;

  // ========================================================================
  // Constructors

  protected ElementImpl(ElementImpl parent) {
    if (parent == null) throw new IllegalArgumentException("null ctx");
    if (parent == this) {
      throw new IllegalArgumentException("An element cannot be its own parent");
    }
    JElement check = parent.getParent();
    while(check != null) {
      if (check == this) throw new IllegalArgumentException("cycle detected");
      check = check.getParent();
    };
    mContext = parent.getContext();
    mParent = parent;
  }

  protected ElementImpl(ElementContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    mContext = ctx;
  }

  // ========================================================================
  // JElement implementation

  public final JElement getParent() { return mParent; }

  public String getSimpleName() { return mSimpleName; }

  public JSourcePosition getSourcePosition() { return mPosition; }

  public Object getArtifact() { return mArtifact; }

  // ========================================================================
  // MElement implementation

  public void setSimpleName(String name) {
    if (name == null) throw new IllegalArgumentException("null name");
    mSimpleName = name;
  }

  public MSourcePosition createSourcePosition() {
    return mPosition = new SourcePositionImpl();
  }

  public void removeSourcePosition() {
    mPosition = null;
  }

  public MSourcePosition getMutableSourcePosition() {
    return mPosition;
  }

  public void setArtifact(Object memento) {
    mArtifact = memento;
  }

  // ========================================================================
  // Public methods & JClass elements

  public JamClassLoader getClassLoader() {
    return mContext.getClassLoader();
  }

  public static String defaultName(int count) {
    return "unnamed_"+count;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ElementImpl)) return false;
    final ElementImpl eElement = (ElementImpl) o;
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
  // Other public methods

  public ElementContext getContext() { return mContext; }
}