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

package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.editable.EClass;

/**
 * A JClassBuilder which delegate to a list of JClassBuilders.  When requested
 * to build a new JClass, it will try each builder on the list until
 * one of them is able to build the class.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class CompositeJClassBuilder implements JClassBuilder {

  // ========================================================================
  // Variables

  private JClassBuilder[] mServices;

  // ========================================================================
  // Constructors

  public CompositeJClassBuilder(JClassBuilder[] services) {
    if (services == null) throw new IllegalArgumentException("null services");
    mServices = services;
  }

  // ========================================================================
  // JClassBuilder implementation

  public JClass buildJClass(String qualifiedName, JClassLoader loader) {
    JClass out = null;
    for(int i=0; i<mServices.length; i++) {
      out = mServices[i].buildJClass(qualifiedName,loader);
      if (out != null) return out;
    }
    return null;
  }

  public boolean populateClass(EClass clazz) {
    throw new IllegalStateException();
  }

}