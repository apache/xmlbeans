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

/**
 * Object which can load representations of a java.lang.Class.
 * Analagous to a java.lang.ClassLoader.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JClassLoader {

  /**
   * <p>Returns a representation of the named class.  If the class is
   * not under the same root as this JElement root, a representation
   * of it will be synthesized via reflection (see note in class
   * comments).  The 'named' parameter must be a fully-qualified class
   * name in the classfile 'Field Descriptor' format, a simple
   * primitive name (e.g. 'long' or 'int'), or 'void'.</p>
   *
   * <p>A note regarding fully-qualified class names: if you're
   * looking up a non-array type by name, you can just pass the
   * regular, fully-qualified name.  If you're looking up an array
   * type, you need to use the 'Field Descriptor' format as described
   * in secion 4.3.2 of the VM spec.  This is the same name format
   * that is returned by JClass.getFieldDescriptor.</p>
   *
   * <p>Inner classes cannot be loaded with this method.  To load an inner
   * class, you must load the outer class first and then call getClasses().
   * </p>
   *
   * <p>Note that this method always returns some JClass - it never
   * returns null or throws ClassNotFoundException.  If neither a
   * sourcefile not a classfile could be located for the named class,
   * a stubbed-out JClass will be returned with the isUnresolved()
   * flag set to true.  This JClass will have a name (as determined by
   * the given descriptor), but no other information about it will be
   * available.</p>
   *
   * @throws IllegalArgumentException if the parameter is null or not
   * a valid class name.
   */
  public JClass loadClass(String fieldDescriptor);

  /**
   * Returns a representation of a package having the given name.

   * @throws IllegalArgumentException if the parameter is null or not
   * a valid package name.
   */
  public JPackage getPackage(String qualifiedPackageName);

  /**
   * Returns the JAnnotationLoader which should be used for retrieving
   * supplemental annotations
   */
  public JAnnotationLoader getAnnotationLoader();

  /**
   * Returns this JClassLoaders's parent.
   */
  public JClassLoader getParent();
}