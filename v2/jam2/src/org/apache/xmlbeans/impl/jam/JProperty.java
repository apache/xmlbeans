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
 * <p>This is a helper class which provides java.beans-like
 * functionality in the JAM framework.

 extension to the JAM framework that encapsulates the notion of a
 * javabean properties.  This class can be used to get similar
 * functionality in JAM that is provided by the java.beans.*
 * packge.</p>
 *
 * <p>An array of JProperties for a given JClass can be retrieved
 * using the getProperties factory method.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JProperty { //is it really an AnnotatedElement?

  // ========================================================================
  // Public methods

  /**
   * Returns a JClass which represents the type of this property.
   */
  public JClass getType();

  /**
   * Returns a JMethod which represents the setter for this property.
   * Returns null if this property is read-only.
   */
  public JMethod getSetter();

  /**
   * Returns a JMethod which represents the getter for this property.
   * Returns null if this property is write-only.
   */
  public JMethod getGetter();
}
