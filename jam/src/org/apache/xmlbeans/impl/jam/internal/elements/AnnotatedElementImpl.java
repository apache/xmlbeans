package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MComment;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class AnnotatedElementImpl extends ElementImpl
  implements MAnnotatedElement
 {

  // ========================================================================
  // Variables

  private Map mName2Annotation = null;
  private MComment mComment = null;
  private List mAllAnnotations = null;

  // ========================================================================
  // Constructors

  protected AnnotatedElementImpl(ElementContext ctx) { super(ctx); }

  protected AnnotatedElementImpl(ElementImpl parent) { super(parent); }

  // ========================================================================
  // JAnnotatedElement implementation

  public JAnnotation[] getAnnotations() {
    return getMutableAnnotations();
  }

  public JAnnotation getAnnotation(Class proxyClass) {
    return getMutableAnnotation(proxyClass.getName());
  }

  public JAnnotation getAnnotation(String named) {
    return getMutableAnnotation(named);
  }

  public JAnnotationValue getAnnotationValue(String valueId) {
    if (mName2Annotation == null) return null;
    valueId = valueId.trim();

    int delim = valueId.indexOf('@');
    if (delim == -1 || delim == valueId.length()-1) {
      JAnnotation ann = getAnnotation(valueId);
      if (ann == null) return null;
      return ann.getValue(JAnnotation.SINGLE_VALUE_NAME);
    } else {
      JAnnotation ann = getAnnotation(valueId.substring(0,delim));
      if (ann == null) return null;

      return ann.getValue(valueId.substring(delim+1));
    }
  }


  public Object getAnnotationProxy(Class proxyClass) {
    return getEditableProxy(proxyClass);
  }

  public JComment getComment() { return getMutableComment(); }

  /**
   * @deprecated
   */
  public JAnnotation[] getAllJavadocTags() {
    if (mAllAnnotations == null) return NO_ANNOTATION;
    JAnnotation[] out = new JAnnotation[mAllAnnotations.size()];
    mAllAnnotations.toArray(out);
    return out;
  }

  /*
  public JAnnotation[] getAllJavadocTags(String named) {
    //FIXME this impl is quite gross
    if (mAllAnnotations == null) return NO_ANNOTATION;
    List list = new ArrayList();
    for(int i=0; i<mAllAnnotations.size(); i++) {
      JAnnotation j = (JAnnotation)mAllAnnotations.get(i);
      if (j.getSimpleName().equals(named)) {
        list.add(j);
      }
    }
    JAnnotation[] out = new JAnnotation[list.size()];
    list.toArray(out);
    return out;
  }
  */

  // ========================================================================
  // MAnnotatedElement implementation

  public AnnotationProxy getEditableProxy(Class proxyClass) {
    if (mName2Annotation == null) return null;
    MAnnotation out = getMutableAnnotation(proxyClass.getName());
    return (out == null) ? null : (AnnotationProxy)out.getProxy();
  }

  public void removeAnnotation(MAnnotation ann) {
    if (mName2Annotation != null) mName2Annotation.values().remove(ann);
  }

  public MAnnotation[] getMutableAnnotations() {
    if (mName2Annotation == null) return new MAnnotation[0];
    MAnnotation[] out = new MAnnotation[mName2Annotation.values().size()];
    mName2Annotation.values().toArray(out);
    return out;
  }

  public MAnnotation getMutableAnnotation(String named) {
    if (mName2Annotation == null) return null;
    named = named.trim();
    return (MAnnotation)mName2Annotation.get(named);
  }

  public MAnnotation findOrCreateAnnotation(String annotationName) {
    //ClassImpl.validateClassName(annotationName);
    MAnnotation ann = getMutableAnnotation(annotationName);
    if (ann != null) return ann;
    AnnotationProxy proxy = getContext().
      createAnnotationProxy(annotationName);
    ann = new AnnotationImpl(getContext(),proxy,annotationName);
    if (mName2Annotation == null) {
      mName2Annotation = new HashMap();
    }
    mName2Annotation.put(ann.getQualifiedName(),ann);
    return ann;
  }

  public MAnnotation addLiteralAnnotation(String annName) {
    if (annName == null) throw new IllegalArgumentException("null tagname");
    annName = annName.trim();
    // otherwise, we have to create an 'extra' one.  note this will only
    // happen when processing javadoc tags where more than one tag of a given
    // name appears in a given scope
    AnnotationProxy proxy = getContext().createAnnotationProxy(annName);
    MAnnotation ann = new AnnotationImpl(getContext(),proxy,annName);
    if (mAllAnnotations == null) mAllAnnotations = new ArrayList();
    mAllAnnotations.add(ann);

    // if one doesn't exist yet, then create the first one
    if (getMutableAnnotation(annName) == null) {
      if (mName2Annotation == null) mName2Annotation = new HashMap();
      mName2Annotation.put(annName,ann);
    }
    return ann;
  }

  public MComment getMutableComment() { return mComment; }

  public MComment createComment() { return mComment = new CommentImpl(this); }

  public void removeComment() { mComment = null; }

  // ========================================================================
  // Protected methods

  // these are exposed primarily for the benefit of PropertyImpl

  protected void addAnnotation(JAnnotation ann) {
    if (mName2Annotation == null) {
      mName2Annotation = new HashMap();
      mName2Annotation.put(ann.getQualifiedName(),ann);
    } else {
      if (mName2Annotation.get(ann.getQualifiedName()) == null) {
        mName2Annotation.put(ann.getQualifiedName(),ann);
      }
    }
    if (mAllAnnotations == null) mAllAnnotations = new ArrayList();
    mAllAnnotations.add(ann);
  }



  // ========================================================================
  // Old stuff

  /**
   * @deprecated this is a back door for xbeans.  do not use, will
   * be removed soon.
   */
  public MAnnotation addAnnotationForProxy(Class proxyClass,
                                           AnnotationProxy proxy)
  {
    //ClassImpl.validateClassName(annotationName);
    String annotationName = proxyClass.getName();
    MAnnotation ann = getMutableAnnotation(annotationName);
    if (ann != null) return ann;
    ann = new AnnotationImpl(getContext(),proxy,annotationName);
    if (mName2Annotation == null) {
      mName2Annotation = new HashMap();
    }
    mName2Annotation.put(ann.getQualifiedName(),ann);
    return ann;
  }
}