package org.apache.xmlbeans.impl.jam;

/**
 * <p>Base abstraction for JElements which can carry annotations and comments.
 * The only JElements which cannot do this are JAnnotation and JComment.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract interface JAnnotatedElement extends JElement {

  // ========================================================================
  // Public methods

  /**
   * <p>Returns the metadata JAnnotations that are associated with
   * this abstraction.  Returns an empty array if there are no
   * annotations.</p>
   */
  public JAnnotation[] getAnnotations();

  /**
   * <p>Returns the JAnnotation which is being proxied by the given subclass
   * of TypedAnnotationProxyBase, or null if no such annotation exists.  If it
   * does exist, the <code>getProxy()</code> method on the returned
   * object is guaranteed to return be an instance of the proxyClass.</p>
   *
   * @throws IllegalArgumentException if the proxyClass parameter is null
   * or not a subclass of <code>TypedAnnotationProxyBase</code>.
   */
  public JAnnotation getAnnotation(Class proxyClass);

  /**
   * <p>Returns an instance of TypedAnnotationProxyBase on this elements for which the given
   * proxy class has been established.  This method is guaranteed to
   * return either an instance of the given proxyClass or null.</p>
   *
   * <p>This method is simply a convenient shorthand for
   * <code>getAnnotation(proxyClass).getProxy()</code>.</p>
   *
   * @throws IllegalArgumentException if the proxyClass parameter is null
   * or not a subclass of <code>AnnotationProxy</code>.
   */
  public Object getAnnotationProxy(Class proxyClass);

  /**
   * <p>Returns the annotation that represents the named 175 annotation
   * or javadoc tag on this elements.</p>
   */
  public JAnnotation getAnnotation(String tagnameProxynameOr175typename);


  /**
   * Shortcut method which returns a given annotation value.  The 'valueId'
   * should be a string of the format 'annotation-name@value-name'.  The
   * value-name may be ommitted; if it is, it defaults to
   * JAnntoation.SINGLE_MEMBER_VALUE.
   *
   * @param valueId
   * @return
   */
  public JAnnotationValue getAnnotationValue(String valueId);

  /**
   * <p>Returns the comment associated with this abstraction.
   * Returns null if it has no comment.</p>
   */
  public JComment getComment();


  /**
   * Use of this method is discouraged. DOCME
   */
  public JAnnotation[] getAllJavadocTags();


}
