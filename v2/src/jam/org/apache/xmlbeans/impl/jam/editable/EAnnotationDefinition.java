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

import org.apache.xmlbeans.impl.jam.JAnnotationDefinition;
import org.apache.xmlbeans.impl.jam.JClass;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EAnnotationDefinition extends
        JAnnotationDefinition, EElement {

  /**
   * Creates a new member in this Annotation definition and returns the
   * result.
   *
   * @param type JClass representing the type of the new member.
   * @param name A name for the new member.
   * @param dflt A default value for the new member.  Primitives should be
   * wrapped in java.lang wrappers, e.g. java.lang.Integer.
   *
   * @return The newly-added EAnnotation.
   */
  public EAnnotationMemberDefinition addNewMemberDefinition(JClass type,
                                                            String name,
                                                            Object dflt);

  /**
   *
   * @param memberDef
   */
  public void removeMemberDefinition(EAnnotationMemberDefinition memberDef);
}