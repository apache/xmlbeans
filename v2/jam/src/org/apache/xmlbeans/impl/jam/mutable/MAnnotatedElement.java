package org.apache.xmlbeans.impl.jam.mutable;

import org.apache.xmlbeans.impl.jam.JAnnotatedElement;
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;


/**
 * <p>Mutable version of JAnnotatedElement.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface MAnnotatedElement extends MElement, JAnnotatedElement {

  /**
   * <p>Creates an annotation to expose the metadata in a javadoc tag.
   * You should subsequently call <code>setMemberValue()</code> on
   * the returned object to populate the annotation's values.</p>
   *
   * @param tagName name of the javadoc tag to be represented.
   * @return
   */
  public MAnnotation addAnnotationForTag(String tagName);

  /**
   * <p>Creates an annotation to expose the metadata in a javadoc tag.</p>
   *
   * @param tagName
   * @param tagContents
   * @return
   */
  public MAnnotation addAnnotationForTag(String tagName, String tagContents);

  //DOCME
  public MAnnotation addAnnotationForInstance(/*Annotation*/ Object jsr175annotationInstance);

  //DOCME
  public MAnnotation addAnnotationForType(String jsr175annotationClassname);

  //DOCME actually not entirely clear we ever want to be able to do this
  public MAnnotation addAnnotationForProxy(AnnotationProxy proxy);


  //DOCME
  public MAnnotation getEditableAnnotation(Class proxyClass);

  //DOCME
  public MAnnotation getEditableAnnotation(String tagnameOr175typename);


  //DOCME
  public MComment getEditableComment();

  //DOCME
  public MComment createComment();

  //DOCME
  public void removeComment();
}
