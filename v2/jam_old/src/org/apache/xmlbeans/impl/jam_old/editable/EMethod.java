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

package org.apache.xmlbeans.impl.jam_old.editable;

import org.apache.xmlbeans.impl.jam_old.JMethod;
import org.apache.xmlbeans.impl.jam_old.JClass;

/**
 * Editable representation of a java method.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EMethod extends JMethod, EInvokable {

  /**
   * <p>Sets the type of this method's return value.  Null can be passed if
   * a 'void' return type is desired.</p>
   *
   * @throws IllegalArgumentException if the parameter is not a valid
   * java class name.
   */
  public void setReturnType(String qualifiedTypeName);

  public void setUnqualifiedReturnType(String unqualifiedTypeName);

  /**
   * <p>Sets the type of this method's return value.  Null may be passed if
   * a 'void' return type is desired.  This method is exactly equivalent to
   * calling setReturnType(jclass.getQualifiedName()).</p>
   */
  public void setReturnType(JClass c);

}