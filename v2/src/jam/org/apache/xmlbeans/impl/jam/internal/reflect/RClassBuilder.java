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

package org.apache.xmlbeans.impl.jam.internal.reflect;

import org.apache.xmlbeans.impl.jam.provider.JClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JPath;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class RClassBuilder implements JClassBuilder {

  // ========================================================================
  // Variables

  private ClassLoader mLoader;

  // ========================================================================
  // Factories

  public static RClassBuilder getSystemClassBuilder() {
    return new RClassBuilder(ClassLoader.getSystemClassLoader());
  }

  public static RClassBuilder getClassBuilderFor(ClassLoader cl) {
    return new RClassBuilder(cl);
  }

  // ========================================================================
  // Constructors

  private RClassBuilder(ClassLoader cl) {
    if (cl == null) throw new IllegalArgumentException("null cl");
    mLoader = cl;
  }

  // ========================================================================
  // Implements JClassBuilder

  public JClass buildJClass(String qualifiedName, JClassLoader loader) {
    try {
      return new RClass(mLoader.loadClass(qualifiedName),loader);
    } catch(ClassNotFoundException cnfe) {
      return null;
    }
  }
}