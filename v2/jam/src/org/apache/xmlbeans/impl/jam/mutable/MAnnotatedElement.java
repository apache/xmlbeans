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
   * Returns the annotation having the given name, creating it if it doesn't
   * exist.
   */
  public MAnnotation findOrCreateAnnotation(String annotationName);

  //DOCME
  public MAnnotation addAnnotation(String annotationName);


  public MAnnotation[] getMutableAnnotations();

  //DOCME
  public MAnnotation getMutableAnnotation(Class proxyClass);

  //DOCME
  public MAnnotation getMutableAnnotation(String tagnameOr175typename);


  //DOCME
  public MComment getMutableComment();

  //DOCME
  public MComment createComment();

  //DOCME
  public void removeComment();




  /**
   * @deprecated
   */
  public MAnnotation addAnnotationForTag(String tagName, String tagContents);



}
