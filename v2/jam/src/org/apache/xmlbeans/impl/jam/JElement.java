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

import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;

/**
 * <p>The root of the java type object model.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
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
  public void accept(JVisitor visitor);


  /**
   * <p>This is not something you want to mess with.  It's here only for the
   * benefit of some JAM implementations which need a handle back to the
   * actual implementation-specific object which is being proxied by this
   * JElement.</p>
   * @return
   */
  public Object getArtifact();
}
