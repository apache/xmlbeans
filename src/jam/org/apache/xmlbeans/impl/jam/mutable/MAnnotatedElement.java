package org.apache.xmlbeans.impl.jam.mutable;

import org.apache.xmlbeans.impl.jam.JAnnotatedElement;


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


  
  public MAnnotation[] getMutableAnnotations();

  //DOCME
  public MAnnotation getMutableAnnotation(String named);



  //DOCME
  public MAnnotation addLiteralAnnotation(String annotationName);

//  public MAnnotation[] getLiteralMutableAnnotations();

//  public MAnnotation[] getLiteralMutableAnnotations(String named);


  //DOCME
  public MComment getMutableComment();

  //DOCME
  public MComment createComment();

  //DOCME
  public void removeComment();

}
