package org.apache.xmlbeans.impl.jam.editable;

import org.apache.xmlbeans.impl.jam.JAnnotatedElement;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface EAnnotatedElement extends EElement, JAnnotatedElement {

  /**
   * <p>Creates an annotation to expose the metadata in a javadoc tag.
   * You should subsequently call <code>setMemberValue()</code> on
   * the returned object to populate the annotation's values.</p>
   *
   * @param tagName name of the javadoc tag to be represented.
   * @return
   */
  public EAnnotation addAnnotationForTag(String tagName);

  /**
   * <p>Creates an annotation to expose the metadata in a javadoc tag.</p>
   *
   * @param tagName
   * @param tagContents
   * @return
   */
  public EAnnotation addAnnotationForTag(String tagName, String tagContents);

  //DOCME
  public EAnnotation addAnnotationForInstance(/*Annotation*/ Object jsr175annotationInstance);

  //DOCME
  public EAnnotation addAnnotationForType(String jsr175annotationClassname);

  //DOCME actually not entirely clear we ever want to be able to do this
  public EAnnotation addAnnotationForProxy(AnnotationProxy proxy);


  //DOCME
  public EAnnotation getEditableAnnotation(Class proxyClass);

  //DOCME
  public EAnnotation getEditableAnnotation(String tagnameOr175typename);


  //DOCME
  public EComment getEditableComment();

  //DOCME
  public EComment createComment();

  //DOCME
  public void removeComment();
}
