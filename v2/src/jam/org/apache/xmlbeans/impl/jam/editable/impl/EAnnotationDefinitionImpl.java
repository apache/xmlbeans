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
package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.EAnnotationDefinition;
import org.apache.xmlbeans.impl.jam.editable.EAnnotationMemberDefinition;
import org.apache.xmlbeans.impl.jam.editable.EMethod;
import org.apache.xmlbeans.impl.jam.JAnnotationMemberDefinition;
import org.apache.xmlbeans.impl.jam.JClassLoader;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EAnnotationDefinitionImpl extends EClassImpl
        implements EAnnotationDefinition {

  // ========================================================================
  // Constructors

  public EAnnotationDefinitionImpl(String packageName,
                                   String simpleName,
                                   JClassLoader classLoader) {
    super(packageName,simpleName,classLoader);
  }

  // ========================================================================
  // EAnnotation implementation

  public EAnnotationMemberDefinition addNewMemberDefinition() {
    EAnnotationMemberDefinition out =
            new EAnnotationMemberDefinitionImpl("unnamed",//FIXME
                                                this);
    super.addMethod(out);
    return out;
  }

  public void removeMemberDefinition(EAnnotationMemberDefinition memberDef) {
    super.removeMethod(memberDef);
  }

  // ========================================================================
  // JAnnotation implementation

  public JAnnotationMemberDefinition[] getMemberDefinitions() {
    return new JAnnotationMemberDefinition[0];
  }

  // ========================================================================
  // EMethod implementation

  /**
   * This really shouldn't be called, but if it is, we want to make sure
   * that the method really is an annotation member.
   */
  public EMethod addNewMethod(String name) {
    EMethod out = addNewMemberDefinition();
    out.setSimpleName(name);
    return out;
  }


}
