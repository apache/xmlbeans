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
 * Represents a constructor of a java class.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JConstructor extends JMember {

  /**
   * <p>Returns representations of the parameters taken by this
   * constructor.  Returns an array of length 0 if the constructor
   * takes no parameters.</p>
   */
  public JParameter[] getParameters();

  /**
   * <p>Returns representations of the type of each of the exceptions
   * which can be thrown by this constructor.  Returns an array of
   * length 0 if the constructor throws no exceptions.</p>
   */
  public JClass[] getExceptionTypes();
}
