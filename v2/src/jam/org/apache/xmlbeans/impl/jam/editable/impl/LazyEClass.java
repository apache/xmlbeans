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

import org.apache.xmlbeans.impl.jam.provider.EClassBuilder;
import org.apache.xmlbeans.impl.jam.*;

/**
 * Implementation of JClass which can be instantiated for a given class name
 * without having to interact with any source or classfile artifacts.  Only
 * when the user requests something substantive about the java type will
 * this impl request a given EClassBuilder to populate it, which in turn
 * might cause a source file to be parsed.  This allows the caller to deal
 * with JClass objects without having to parse anything for classes which
 * they aren't interested in.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class LazyEClass extends EClassImpl {

  // ========================================================================
  // Variables

  private EClassBuilder mBuilder = null;
  private boolean mIsIntialized = false;
  private boolean mIsPopulated = true;

  // ========================================================================
  // Constructors

  public LazyEClass(String packageName,
                    String className,
                    JClassLoader loader,
                    EClassBuilder builder) {
    super(packageName,className,loader);
    mBuilder = builder;
  }

  // ========================================================================
  // JElement implementation

  public JSourcePosition getSourcePosition() {
    checkInitialized();
    return super.getSourcePosition();
  }

  // ========================================================================
  // JMember implementation

  public JClass getContainingClass() {
    checkInitialized();
    return super.getContainingClass();
  }

  // ========================================================================
  // JClass implementation

  public boolean isUnresolved() {
    checkInitialized();
    return !mIsPopulated;//yuck
  }

  public JClass getSuperclass() {
    checkInitialized();
    return super.getSuperclass();
  }

  public JClass[] getInterfaces() {
    checkInitialized();
    return super.getInterfaces();
  }

  public JField[] getFields() {
    checkInitialized();
    return super.getFields();
  }

  public JField[] getDeclaredFields() {
    checkInitialized();
    return super.getDeclaredFields();
  }

  public JMethod[] getMethods() {
    checkInitialized();
    return super.getMethods();
  }

  public JMethod[] getDeclaredMethods() {
    checkInitialized();
    return super.getDeclaredMethods();
  }

  public JConstructor[] getConstructors() {
    checkInitialized();
    return super.getConstructors();
  }

  public JProperty[] getProperties() {
    checkInitialized();
    return super.getProperties();
  }

  public boolean isInterface() {
    checkInitialized();
    return super.isInterface();
  }

  public int getModifiers() {
    checkInitialized();
    return super.getModifiers();
  }

  public boolean isAssignableFrom(JClass clazz) {
    checkInitialized();
    return super.isAssignableFrom(clazz);
  }

  public JClass[] getClasses() {
    checkInitialized();
    return super.getClasses();
  }

  public JPackage[] getImportedPackages() {
    checkInitialized();
    return super.getImportedPackages();
  }

  public JClass[] getImportedClasses() {
    checkInitialized();
    return super.getImportedClasses();
  }

  // ========================================================================
  // Private methods

  private void checkInitialized() {
    if (mIsIntialized) return;
    mIsPopulated = mBuilder.populateClass(this);
    mIsIntialized = true;
  }
}