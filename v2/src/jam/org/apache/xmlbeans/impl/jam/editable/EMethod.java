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

import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JClass;

/**
 * Editable representation of a java method.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EMethod extends JMethod, EMember {

  /**
   * <p>Sets the type of this method's return value.  Null can be passed if
   * a 'void' return type is desired.</p>
   *
   * @throws IllegalArgumentException if the parameter is not a valid
   * java class name.
   */
  public void setReturnType(String qualifiedClassName);

  /**
   * <p>Sets the type of this method's return value.  Null may be passed if
   * a 'void' return type is desired.  This method is exactly equivalent to
   * calling setReturnType(jclass.getQualifiedName()).</p>
   */
  public void setReturnType(JClass c);

  /**
   * <p>Adds a declaration of a checked exception of the given type.</p>
   *
   * @throws IllegalArgumentException if the parameter is null or is not
   * a valid class name.
   */
  public void addException(String qualifiedClassName);

  /**
   * <p>Adds a declaration of a checked exception of the given type.</p>
   *
   * @throws IllegalArgumentException if the parameter is null or represents
   * a class which does not extend throwable.
   */
  public void addException(JClass exceptionClass);

  /**
   * Removes a declaration of a checked exception of the named class.  Does
   * nothing if no such declaration exists.
   *
   * @throws IllegalArgumentException if the parameter is null or is not
   * a valid class name.
   */
  public void removeException(String qualifiedClassName);

  /**
   * Removes a declaration of a checked exception of the given class.  Does
   * nothing if no such declaration exists.
   *
   * @throws IllegalArgumentException if the parameter is null.
   */
  public void removeException(JClass exceptionClass);

  /**
   * Creates a new parameter on this method with the given type and name.
   *
   * @throws IllegalArgumentException if either parameter is null, if
   * the type parameter represents 'void', or if the name parameter is not a
   * valid java identifier.
   */
  public EParameter addNewParameter(JClass type, String name);

  /**
   * Creates a new parameter on this method with the given type and name.
   *
   * @throws IllegalArgumentException if either parameter is null, if
   * type parameter is not a valid class name, or if the name parameter is
   * not a valid java identifier.
   */
  public EParameter addNewParameter(String qualifiedTypeName, String name);

  /**
   * Removes the given parameter.  Does nothing if the parameter is not
   * present on this method.
   *
   * @throws IllegalArgumentException if either parameter is null.
   */
  public void removeParameter(EParameter parameter);

  /**
   * Returns all of the parameters on this method, or an empty array if there
   * are none.  This is simply a more strongly-typed version of
   * getParameters().
   */
  public EParameter[] getEditableParameters();

}