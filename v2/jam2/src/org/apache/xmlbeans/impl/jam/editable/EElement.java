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

package org.apache.xmlbeans.impl.jam.editable;

import org.apache.xmlbeans.impl.jam.JElement;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EElement extends JElement {

  public void setSimpleName(String name);

  public ESourcePosition createSourcePosition();

  public void removeSourcePosition();

  public ESourcePosition getEditableSourcePosition();

  /**
   * <p>Accepts the given visitor.</p>
   */
  public void accept(EElementVisitor visitor);

  /**
   * <p>Calls accept() with the given visitor, and then recursively calls
   * acceptAndWalk for all of our component elements2.  Calling this on
   * an EClass will cause the EClass to accept the visitor, and then
   * all of it's declared fields constructors, and methods.  The parameter
   * for each constructor and method will also in turn be accepted.
   * Any annotations for each of these elements types will also be visited
   * after their other children have been visited.  Note that inherited
   * members are never visited, nor are referenced classes (e.g. referenced
   * via inheritance or member types).  </p>
   *
   * Class
   *   Field
   *   Method
   *     Parameter
   *   Constructor
   *     Parameter
   *
   * </p>
   */
  public void acceptAndWalk(EElementVisitor visitor);


  public void setArtifact(Object o);
}