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

//FIXME rename to JAnnotationDefinition

/**
 * Represents the definition of an Annotation.
 *
 * Note that JAnnotationDeclaration is a JClass.  In the most typical case,
 * this class is simply a representation of a java.lang.Annotation class,
 * although this should not be assumed.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JAnnotationDeclaration extends JClass {

  //NOTE the delcarations are also returned as JMethods

  /**
   * Note that the objects in the array returned by this method are exactly
   * the same as those returned by getMethods().
   */
  public JAnnotationMemberDeclaration[] getMemberDeclarations();
}