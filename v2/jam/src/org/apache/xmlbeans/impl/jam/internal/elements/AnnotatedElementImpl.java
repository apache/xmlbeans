package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
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

  private List mAllJavadocTags = null;

  // ========================================================================
  // Constructors

  protected AnnotatedElementImpl(ElementContext ctx) {
    super(ctx);
  }

  protected AnnotatedElementImpl(ElementImpl parent) {
    super(parent);
  }

  // ========================================================================
  // JAnnotatedElement implementation

  public JAnnotation[] getAnnotations() {
    return getMutableAnnotations();
  }

  public JAnnotation getAnnotation(Class proxyClass) {
    return getMutableAnnotation(proxyClass);
  }

  public JAnnotation getAnnotation(String named) {
    return getMutableAnnotation(named);
  }

  public JAnnotationValue getAnnotationValue(String valueName) {
    if (mName2Annotation == null) return null;
    int delim = valueName.indexOf('@');
    if (delim == -1) {
      throw new IllegalArgumentException("value identifiers must include an '@'"+
                                         valueName);
    }
    JAnnotation out = getAnnotation(valueName.substring(0,delim));
    return out.getValue(valueName.substring(delim+1));
  }


  public Object getAnnotationProxy(Class proxyClass) {
    return getEditableProxy(proxyClass);
  }

  public JComment getComment() { return getMutableComment(); }

  public JAnnotation[] getAllJavadocTags() {
    if (mAllJavadocTags == null) return NO_ANNOTATION;
    JAnnotation[] out = new JAnnotation[mAllJavadocTags.size()];
    mAllJavadocTags.toArray(out);
    return out;
  }

  /*
  public JAnnotation[] getAllJavadocTags(String named) {
    //FIXME this impl is quite gross
    if (mAllJavadocTags == null) return NO_ANNOTATION;
    List list = new ArrayList();
    for(int i=0; i<mAllJavadocTags.size(); i++) {
      JAnnotation j = (JAnnotation)mAllJavadocTags.get(i);
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
    return (MAnnotation)mName2Annotation.get(named);
  }

  public MAnnotation getMutableAnnotation(Class proxyClass) {
    if (mName2Annotation == null) return null;
    return (MAnnotation)mName2Annotation.get(proxyClass.getName());
  }

  public MAnnotation addAnnotationForTag(String tagName) {
    {
      // looks like we need to maintain a full list no matter what
      AnnotationProxy proxy = getContext().createProxyForTag(tagName);
      MAnnotation ann = new AnnotationImpl(getContext(),proxy,tagName);
      ann.setArtifact("@"+tagName);
      addJavadocAnnotation(ann);
    }
    MAnnotation out = getMutableAnnotation(tagName);
    if (out != null) {
      //REVIEW this is a weird case.  we'll just go with it for now.
    } else {
      AnnotationProxy proxy = getContext().createProxyForTag(tagName);
      out = new AnnotationImpl(getContext(),proxy,tagName);
      addAnnotation(out);
    }
    return out;
  }

  public MAnnotation addAnnotationForTag(String tagName, String tagContents) {
    {
      // looks like we need to maintain a full list no matter what
      AnnotationProxy proxy = getContext().createProxyForTag(tagName);
      proxy.initFromJavadocTag(tagContents);
      MAnnotation ann = new AnnotationImpl(getContext(),proxy,tagName);
      ann.setArtifact("@"+tagName+" "+tagContents);
      addJavadocAnnotation(ann);
    }
    MAnnotation out = getMutableAnnotation(tagName);
    if (out != null) {
      //REVIEW this is a weird case where they add the same thing twice.
      // we'll just go with it for now.
      out.getMutableProxy().initFromJavadocTag(tagContents);
    } else {
      AnnotationProxy proxy = getContext().createProxyForTag(tagName);
      proxy.initFromJavadocTag(tagContents);
      out = new AnnotationImpl(getContext(),proxy,tagName);
      out.setArtifact(tagContents);
      addAnnotation(out);
    }
    return out;
  }

  public MAnnotation addAnnotationForInstance(/*Annotation*/ Object jsr175annotationInstance) {
    if (jsr175annotationInstance == null) {
      throw new IllegalArgumentException("null instance");
    }
    String typename = getAnnotationTypeFor(jsr175annotationInstance);
    MAnnotation ann = getMutableAnnotation(typename);
    if (ann != null) {
      ann.setAnnotationInstance(jsr175annotationInstance);
      ann.getMutableProxy().initFromAnnotationInstance(jsr175annotationInstance);
      //REVIEW this is a weird case where they add another instance
      // of the same annotation type.  We'll just go with it for now,
      // but we might want to throw an exception here, not sure.
    } else {
      AnnotationProxy proxy = getContext().createProxyForAnnotationType
        (getAnnotationTypeFor(jsr175annotationInstance));
      proxy.initFromAnnotationInstance(jsr175annotationInstance);
      ann = new AnnotationImpl(getContext(),proxy,typename);
      ann.setAnnotationInstance(jsr175annotationInstance);
      setArtifact(jsr175annotationInstance);
      addAnnotation(ann);
    }
    return ann;
  }

  public MAnnotation addAnnotationForType(String jsr175annotationClassname) {
    ClassImpl.validateClassName(jsr175annotationClassname);
    MAnnotation ann = getMutableAnnotation(jsr175annotationClassname);
    if (ann != null) return ann; //REVIEW weird case again
    AnnotationProxy proxy = getContext().
      createProxyForAnnotationType(jsr175annotationClassname);
    ann = new AnnotationImpl(getContext(),proxy,jsr175annotationClassname);
    addAnnotation(ann);
    return ann;
  }

  public MAnnotation addAnnotationForProxy(AnnotationProxy proxy) {
    if (proxy == null) throw new IllegalArgumentException("null proxy");
    String name = proxy.getClass().getName();
    MAnnotation ann = getMutableAnnotation(name);
    if (ann != null) return ann; //REVIEW weird case yet again
    ann = new AnnotationImpl(getContext(),proxy,name);
    addAnnotation(ann);
    return ann;
  }


  public MComment getMutableComment() { return mComment; }

  public MComment createComment() { return mComment = new CommentImpl(this); }

  public void removeComment() { mComment = null; }

  // ========================================================================
  // Protected methods

  // these are exposed primarily for the benefit of PropertyImpl

  protected void addAnnotation(JAnnotation ann) {
    if (mName2Annotation == null) mName2Annotation = new HashMap();
    mName2Annotation.put(ann.getSimpleName(),ann);
  }

  protected void addJavadocAnnotation(JAnnotation ann) {
    if (mAllJavadocTags == null) mAllJavadocTags = new ArrayList();
    mAllJavadocTags.add(ann);
  }

  // ========================================================================
  // Private methods

  private String getAnnotationTypeFor(/*Annotation*/ Object annotationInstance) {
    //FIXME this may be broken, not sure yet what the class of an annotation
    // instance is.  we may need to climb the type tree.
    return annotationInstance.getClass().getName();
  }
}