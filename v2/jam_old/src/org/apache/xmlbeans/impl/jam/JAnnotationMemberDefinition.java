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

//FIXME rename to JAnnotationMemberDefinition

/**
 * Note that the member definition is actually a JMethod.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JAnnotationMemberDefinition extends JMethod {

  //FIXME so, if the type is an annotation, what are they supposed to do?
  /**
   * Returns a JClass representing the type of this annotation memeber.
   * Note that it is entirely possible that the JClass returned by this
   * method will be a JAnnotationDefinition.  Note that getType() and
   * getReturnType() will return exactly the same value.
   *
   * @return
   */
  public JClass getType();

  /**
   * Returns an object representing the default value of this annotation
   * member.
   *
   * @return
   */
  public Object getDefaultValue();


  //REVIEW I don't want to expose this unless somebody really needs it
  //public JAnnotationDefinition getContainingAnnotationDefinition();
}