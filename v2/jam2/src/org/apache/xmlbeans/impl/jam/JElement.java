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

import org.apache.xmlbeans.impl.jam.visitor.ElementVisitor;

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
   * elements, or null if the position is unknown on not applicable.
   */
  public JSourcePosition getSourcePosition();


  /**
   * <p>Accepts the given visitor.</p>
   */
  public void accept(ElementVisitor visitor);

  /**
   * <p>Calls accept() with the given visitor, and then recursively calls
   * acceptAndWalk for each of our component elements2, if any.  Calling this
   * on
   * an EClass will cause the EClass to accept the visitor, and then
   * all of it's declared fields constructors, and methods.  The parameter
   * for each constructor and method will also in turn be accepted.
   * Any annotations for each of these elements types will also be visited
   * after their other children have been visited.  Note that inherited
   * members are never visited, nor are referenced classes (e.g. referenced
   * via inheritance or member types).  </p>
   *
   * <table border='1'>
   * <tr><td><b>Element</b></td><td><b>Sub-elements traversal</b></td></tr>
   * <tr><td>Package       </td><td>Classes, Annotations, Comments</td></tr>
   * <tr><td>Class         </td><td>Fields, Constructors, Methods, Annotations, Comments</td></tr>
   * <tr><td>Field         </td><td>Annotations, Comments</td></tr>
   * <tr><td>Constructor   </td><td>Parameters, Annotations, Comments</td></tr>
   * <tr><td>Method        </td><td>Parameters, Annotations, Comments</td></tr>
   * <tr><td>Parameter     </td><td>[none]</td></tr>
   * <tr><td>Comment       </td><td>[none]</td></tr>
   * </table>
   *
   * </p>
   */
  public void acceptAndWalk(ElementVisitor visitor);

  /**
   * <p>This is not something you want to mess with.  It's here only for the
   * benefit of some JAM implementations which need a handle back to the
   * actual implementation-specific object which is being proxied by this
   * JElement.</p>
   * @return
   */
  public Object getArtifact();
}
