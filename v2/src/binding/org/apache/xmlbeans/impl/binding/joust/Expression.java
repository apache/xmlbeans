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

package org.apache.xmlbeans.impl.binding.joust;

/**
 * Provides a handle to a java expression in the generated code.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface Expression {

  /**
   * This value is provided by and used only by implementations of
   * JavaOutputStream.  The memento is simply a place for the implementation
   * to hang a reference to some domain object that the Expression actually
   * represents.
   */
  public Object getMemento();
  //REVIEW maybe we don't need to expose memento at all - just let them
  //implement it however they want and cast down to get the info.  Often
  //as not, they may just want to implement toString().

  /**
   * Provides a textual representation of the expression.  This should
   * only be used for logging or debugging purposes.
   */
  //  public String getLabel();   //not clear to me this is worthwhile
}
