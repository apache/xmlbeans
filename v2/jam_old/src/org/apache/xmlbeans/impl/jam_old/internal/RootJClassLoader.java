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

package org.apache.xmlbeans.impl.jam_old.internal;

import org.apache.xmlbeans.impl.jam_old.JClassLoader;
import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JPackage;
import org.apache.xmlbeans.impl.jam_old.JAnnotationLoader;

import java.util.Map;
import java.util.HashMap;

/**
 * Singleton classloader which should be at the root of every JClassLoader
 * chain.  Handles primtives, void, and returns an UnresolvedJClass in the
 * worst case.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class RootJClassLoader implements JClassLoader {

  // ========================================================================
  // Static initializer

  private static final Map mFd2Class = new HashMap();

  static {
    for(int i=0; i<PrimitiveJClass.PRIMITIVES.length; i++) {
      mFd2Class.put(PrimitiveJClass.PRIMITIVES[i][0],
                    PrimitiveJClass.getPrimitiveClassForName(
                            (String)PrimitiveJClass.PRIMITIVES[i][0]));
    }
    mFd2Class.put("void",VoidJClass.getInstance());
  }


  // ========================================================================
  // Constructors

  public RootJClassLoader() {}

  // ========================================================================
  // JClassLoader implementation

  public JClass loadClass(String fd) {
    if (fd == null) throw new IllegalArgumentException("null fd");
    fd = fd.trim();
    // check cache first
    JClass out = (JClass)mFd2Class.get(fd);
    if (out != null) return out;
    return new UnresolvedJClass(fd);
  }

  public JPackage getPackage(String name) {
    return new JPackageImpl(name);
  }

  public JAnnotationLoader getAnnotationLoader() { return null; }

  public JClassLoader getParent() {
    return null;
  }
}