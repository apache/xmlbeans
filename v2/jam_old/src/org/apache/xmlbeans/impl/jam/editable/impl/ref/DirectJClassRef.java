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
package org.apache.xmlbeans.impl.jam.editable.impl.ref;

import org.apache.xmlbeans.impl.jam.JClass;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DirectJClassRef implements JClassRef {

  // ========================================================================
  // Factory

  public static JClassRef create(JClass clazz) {
    // we normally can expect that most JClass impls will simply implement
    // JClassRef directly
    if (clazz instanceof JClassRef) return (JClassRef)clazz;
    return new DirectJClassRef(clazz);
  }

  // ========================================================================
  // Variables

  private JClass mClass;

  // ========================================================================
  // Constructors

  private DirectJClassRef(JClass clazz) {
    if (clazz == null) throw new IllegalArgumentException("null clazz");
    mClass = clazz;
  }

  // ========================================================================
  // JClassRef implementation

  public JClass getRefClass() {
    return mClass;
  }

  public String getQualifiedName() {
    return mClass.getQualifiedName();
  }
}
