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

/**
 * Defines an object which can create an instance of JClass for a named
 * java class.  This interface is a key part of the boilerplate JProvider
 * implementation provided by BaseJProvider; to write a new JAM provider,
 * all you really have to do is implement this interface.
 *
 * @author Patrick Calahan <pcal@bea.com>
 * @deprecated
 */
public interface JClassBuilder {

  /**
   * Instantiates and returns a new JClass for the java class of the given
   * name.  If the JClassBuilder has no knowledge about the named class,
   * this method should return null.
   *
   * The given JClassLoader must be used for resolving all type references
   * by the returned JClass instance.  The loader's JAnnotationLoader should
   * also be respected for loading annotations.
   *
   * Note that unlike a JClassLoader.loadClass(), this method should always
   * return a new instance of the JClass.  (The builtin JClassLoader machinery
   * implementations will handle caching as appropriate).
   *
   * @param qualifiedClassname name of JClass to instantiate
   * @param loader loader for resolving referenced types
   * @return new JClass, or null if the class is not known
   */
  public JClass buildJClass(String qualifiedClassname, JClassLoader loader);

}