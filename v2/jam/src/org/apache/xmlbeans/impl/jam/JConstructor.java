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
 * <p>Represents a constructor of a java class.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JConstructor extends JInvokable {

  /**
   * <p>Returns a qualied name for this method as specified by
   * <code>java.lang.reflect.Constructor.toString()</code>:</p>
   *
   * <p><i>Returns a string describing this Constructor. The string is formatted
   * as the constructor access modifiers, if any, followed by the
   * fully-qualified name of the declaring class, followed by a parenthesized,
   * comma-separated list of the constructor's formal parameter types.
   * For example:<i></p>
   *
   * <p><i>public java.util.Hashtable(int,float)</i></p>
   *
   * <p><i>The only possible modifiers for constructors are the access modifiers
   * public, protected or private. Only one of these may appear, or none if
   * the constructor has default (package) access.</i></p>
   */
  public String getQualifiedName();


}
