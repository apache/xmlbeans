package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MComment;

import java.util.Map;
import java.util.HashMap;

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
      throw new IllegalArgumentException("invalid value identifier '"+valueName);
    }
    JAnnotation out = getAnnotation(valueName.substring(0,delim));
    return out.getValue(valueName.substring(delim+1));
  }


  public Object getAnnotationProxy(Class proxyClass) {
    return getEditableProxy(proxyClass);
  }

  public JComment getComment() { return getMutableComment(); }

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
    MAnnotation out = getMutableAnnotation(tagName);
    if (out != null) {
      //REVIEW this is a weird case.  we'll just go with it for now.
    } else {
      AnnotationProxy proxy = getContext().createProxyForTag(tagName);
      out = new AnnotationImpl(getContext(),proxy,tagName);

      getName2Annotation().put(tagName,out);
    }
    return out;
  }

  public MAnnotation addAnnotationForTag(String tagName, String tagContents) {
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
      getName2Annotation().put(tagName,out);
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
      //REVIEW this is an extremely weird case where they add another instance
      // of the same annotation type.  We'll just go with it for now,
      // but we might want to throw an exception here, not sure.
    } else {
      AnnotationProxy proxy = getContext().createProxyForAnnotationType
        (getAnnotationTypeFor(jsr175annotationInstance));
      proxy.initFromAnnotationInstance(jsr175annotationInstance);
      ann = new AnnotationImpl(getContext(),proxy,typename);
      setArtifact(jsr175annotationInstance);
      getName2Annotation().put(typename,ann);
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
    getName2Annotation().put(jsr175annotationClassname,ann);
    return ann;
  }

  public MAnnotation addAnnotationForProxy(AnnotationProxy proxy) {
    if (proxy == null) throw new IllegalArgumentException("null proxy");
    String name = proxy.getClass().getName();
    MAnnotation ann = getMutableAnnotation(name);
    if (ann != null) return ann; //REVIEW weird case yet again
    ann = new AnnotationImpl(getContext(),proxy,name);
    getName2Annotation().put(name,ann);
    return ann;
  }


  public MComment getMutableComment() { return mComment; }

  public MComment createComment() { return mComment = new CommentImpl(this); }

  public void removeComment() { mComment = null; }

  // ========================================================================
  // Protect methods

  protected void visitAnnotations(MVisitor visitor) {
    MAnnotation[] anns = getMutableAnnotations();
    for(int i=0; i<anns.length; i++) visitor.visit(anns[i]);
    if (mComment != null) visitor.visit(mComment);
  }

  // ========================================================================
  // Private methods


  private Map getName2Annotation() {
    if (mName2Annotation == null) mName2Annotation = new HashMap();
    return mName2Annotation;
  }

  private String getAnnotationTypeFor(/*Annotation*/ Object annotationInstance) {
    //FIXME this may be broken, not sure yet what the class of an annotation
    // instance is.  we may need to climb the type tree.
    return annotationInstance.getClass().getName();
  }
}