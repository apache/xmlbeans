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

package org.apache.xmlbeans.impl.jam;

/**
 * <p>Interface implemented by JAM abstractions which can have
 * associated annotations (i.e. metadata).  This abstraction is
 * primarily useful in the case where annotation inheritance is
 * desired; the getParent() call can be used to climb the tree of
 * annoted objects until a desired annotation is found.  Note that
 * JAnnotations are themselves JElement, which is used to annotation
 * nesting (as expressed by JSR175 nested structs or javadoc'ed
 * name-value pairs).</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract interface JElement {

  /**
   * <p>Returns the metadata JAnnotations that are associated with
   * this abstraction.  Returns an empty array if there are no
   * annotations.</p>
   */
  public JAnnotation[] getAnnotations();

  /**
   * <p>Returns the annotation of the named annotation class.  The
   * named class must be loadable by the JClassLoader which loaded
   * this JElement.</p>
   *
   * NOTE: The following additional behavior is still supported
   * in the case where the annotation takes the form of javadoc tags,
   * but it is deprecated.
   *
   * <p>Returns the annotation on this abstraction of the given
   * annotation type.  which has the given
   * name.  If more than one such annotation exists, returns the first
   * one in document order.  If none exists, returns null.</p>
   *
   * <p>This method also provides a convenient way to access nested
   * metadata by using an '@' delimiter.  For example, take a method
   * with a 'foo' annotation, which in turn has a 'bar'
   * sub-annotation.  The javadoc-style annotation for this would like
   * <pre>
   * @foo bar=myvalue
   * </pre>
   *
   * <p> The JAnnotation for 'bar' could be accessed directly via
   * myMethod.getAnnotation("foo@bar").  Note that it might still
   * return null.</p>
   *
   */
  public JAnnotation getAnnotation(String qualifiedAnnotationClassname);

  /**
   * <p>Returns the annotation on this abstraction of the given
   * annotation type.</p>
   */
  //public JAnnotation getAnnotation(JAnnotationDefinition annType);




  /**
   * <p>Returns the parent of this abstraction, or null if this
   * annotation represents a root abstraction (i.e. a JPackage).  The
   * JElement hierarchy looks like this:</p>
   *
   * <pre>
   *     JPackage
   *       JClass
   *         JConstructor
   *         JField
   *         JMethod
   *           JParameter
   *         JProperty
   *         JClass (inner class)...
   * </pre>
   *
   * <p>Additionally, any of the abstractions above may in turn have
   * child JAnnotations, which may themselves have child
   * JAnnotations.</p>
   */
  public JElement getParent();

  /**
   * <p>Returns the comments associated with this abstraction.
   * Returns an empty array if there are no comments.</p>
   */
  public JComment[] getComments();

  /**
   * <p>Returns a simple name of this abstraction.  The exact format
   * of the name depends on the particular abstraction (see javadoc).
   * Please refer to the JAM package documentation for more details on
   * naming conventions.</p>
   */
  public String getSimpleName();

  /**
   * <p>Returns a fully-qualified name for this abstraction.  The
   * exact format of the name depends on the particular abstraction.
   * Please refer to the JAM package documentation for more details on
   * naming conventions.</p>
   */
  public String getQualifiedName();

  /**
   * Returns an object describing the source file position of this
   * element, or null if the position is unknown on not applicable.
   */
  public JSourcePosition getSourcePosition();


  // ========================================================================
  // Deprecated methods

  /**
   * <p>Returns the set of annotations associated with this
   * abstraction which have the given name.  Returns an empty array if
   * there are no such annotations.</p>
   *
   * @deprecated I don't think we should encourage people to support
   * multiple annotations with the same name as that does not work once
   * we get to 175-land.
   */
  public JAnnotation[] getAnnotations(String named);

}
