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
import org.apache.xmlbeans.impl.jam.mutable.MMethod;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.QualifiedJClassRef;

/**
 * <p>Implementation of JProperty.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class PropertyImpl extends AnnotatedElementImpl implements JProperty {

  // ========================================================================
  // Variables

  private String mName;
  private JMethod mGetter, mSetter;
  private JClassRef mTypeRef;

  // ========================================================================
  // Constructor

  /**
   * <p>You'll usually want to use the getProperties() factory method
   * instead of constructing JProperties yourself.  This constructor
   * is exposed just in case the default rules in the factory method
   * for identifying properties are insufficient for some use
   * case.</p>
   *
   */
  public PropertyImpl(String name,
                      JMethod getter,
                      JMethod setter,
                      String qualifiedTypeName)
  {
    super((ElementImpl)
      ((getter != null) ? getter.getParent() : setter.getParent()));
    //FIXME should do more validation on the arguments
    mName = name;
    mGetter = getter;
    mSetter = setter;
    ClassImpl cont = (ClassImpl)((getter != null) ?
      getter.getContainingClass() : setter.getContainingClass());
    mTypeRef = QualifiedJClassRef.create(qualifiedTypeName,cont);
    initAnnotations();
  }

  // ========================================================================
  // Public methods

  /**
   * Returns a JClass which represents the type of this property.
   */
  public JClass getType() { return mTypeRef.getRefClass(); }

  /**
   * Returns the simple name of this property.  For example, for a
   * property manifest by getFoo() and setFoo(), this will return
   * 'foo'.
   */
  public String getSimpleName() { return mName; }


  /**
   * Returns the simple name of this property.  For example, for a
   * property manifest by getFoo() and setFoo(), this will return
   * 'foo'.
   */
  public String getQualifiedName() {
    return getParent().getQualifiedName()+"."+getSimpleName(); //REVIEW
  }

  /**
   * Returns a JMethod which represents the setter for this property.
   * Returns null if this property is read-only.
   */
  public JMethod getSetter() { return mSetter; }

  /**
   * Returns a JMethod which represents the getter for this property.
   * Returns null if this property is write-only.
   */
  public JMethod getGetter() { return mGetter; }

  // ========================================================================
  // JElement implementation

  /**
   * Returns all of the annotations on the getter and/or the setter
   * methods.
   */
  public JAnnotation[] getAnnotations() {
    return combine((mGetter == null) ?
                   ElementImpl.NO_ANNOTATION : mGetter.getAnnotations(),
                   (mSetter == null) ?
                   ElementImpl.NO_ANNOTATION : mSetter.getAnnotations());
  }

  /**
   * Returns annotations with the given name that are found on this
   * property's getter and/or setter.

  public JAnnotation[] getAnnotations(String named) {
    return combine((mGetter == null) ?
                   BaseJElement.NO_ANNOTATION : mGetter.getAnnotations(named),
                   (mSetter == null) ?
                   BaseJElement.NO_ANNOTATION : mSetter.getAnnotations(named));
  }
   */

  //internal use only
  public void setSetter(JMethod method) { mSetter = method; }

  //internal use only
  public void setGetter(JMethod method) { mGetter = method; }

  /**
   * Returns the first annotation with the given name that is found on
   * this property's getter and/or setters.
   */
  public JAnnotation getAnnotation(String named) {
    JAnnotation out = (mGetter != null) ? mGetter.getAnnotation(named) : null;
    if (out != null) return out;
    return (mSetter != null) ? mSetter.getAnnotation(named) : null;
  }

  public JComment getComment() {
    //REVIEW do we want to somehow merge the comments?
    if (mGetter != null)  return mGetter.getComment();
    if (mSetter != null)  return mSetter.getComment();
    return null;
  }

  public JSourcePosition getSourcePosition() {
    return mGetter != null ?
      mGetter.getSourcePosition() : mSetter.getSourcePosition();
  }

  public void accept(JVisitor visitor) {
    if (mGetter != null) visitor.visit(mGetter);
    if (mSetter != null) visitor.visit(mSetter);
  }

  // ========================================================================
  // Object implementation

  public String toString() { return getQualifiedName(); }

  // ========================================================================
  // Private methods

  private void initAnnotations() {
    if (mSetter != null) {
      JAnnotation[] anns = mSetter.getAnnotations();
      for(int i=0; i<anns.length; i++) super.addAnnotation(anns[i]);
      anns = mSetter.getAllJavadocTags();
      for(int i=0; i<anns.length; i++) super.addAnnotation(anns[i]);
    }
    if (mGetter != null) {
      JAnnotation[] anns = mGetter.getAnnotations();
      for(int i=0; i<anns.length; i++) super.addAnnotation(anns[i]);
      anns = mGetter.getAllJavadocTags();
      for(int i=0; i<anns.length; i++) super.addAnnotation(anns[i]);
    }
  }


  /**
   * Returns an array that is the union of the two arrays of
   * anotations.
   */
  private JAnnotation[] combine(JAnnotation[] a, JAnnotation[] b) {
    if (a.length == 0) return b;
    if (b.length == 0) return a;
    JAnnotation[] out = new JAnnotation[a.length+b.length];
    System.arraycopy(a,0,out,0,a.length);
    System.arraycopy(b,0,out,a.length,b.length);
    return out;
  }

  /**
   * Returns an array that is the union of the two arrays of
   * anotations.
   */
  private JComment[] combine(JComment[] a, JComment[] b) {
    if (a.length == 0) return b;
    if (b.length == 0) return a;
    JComment[] out = new JComment[a.length+b.length];
    System.arraycopy(a,0,out,0,a.length);
    System.arraycopy(b,0,out,a.length,b.length);
    return out;
  }

  public void accept(MVisitor visitor) {
    //review this is kinda broken
    if (mGetter != null) visitor.visit((MMethod)mGetter);
    if (mSetter != null) visitor.visit((MMethod)mSetter);
  }

}
