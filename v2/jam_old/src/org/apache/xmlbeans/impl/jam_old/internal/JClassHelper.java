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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.xmlbeans.impl.jam_old.*;

/**
 * <p>Helper class containing methods to which JClass implementations
 * can delegate some tedious work.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JClassHelper {

  // ========================================================================
  // Variables

  private JClass mClass;
  private JField[] mFields = null;
  private JMethod[] mMethods = null;
  private JElement[] mChildren = null;
  private JElement mParent = null;

  // ========================================================================
  // Constructor

  public JClassHelper(JClass c) {
    if (c == null) throw new IllegalArgumentException();
    mClass = c;
  }

  // ========================================================================
  // Public methods

  public JElement getParent() {
    if (mParent == null) {
      mParent = mClass.getContainingClass();
      if (mParent == null) mParent = mClass.getContainingPackage();
    }
    return mParent;
  }

  /**
   * Returns all of the children of the given class as defined in the
   * javadocs for JClass.
   */
  public JElement[] getChildren() {
    if (mChildren == null) {
      List list = new ArrayList();
      // constructors
      JConstructor[] constructors = mClass.getConstructors();
      if (constructors != null) list.addAll(Arrays.asList(constructors));
      // methods
      JMethod[] methods = mClass.getDeclaredMethods();
      if (methods != null) list.addAll(Arrays.asList(methods));
      // fields
      JField[] fields = mClass.getDeclaredFields();
      if (fields != null) list.addAll(Arrays.asList(fields));
      // inner classes
      JClass[] inners = mClass.getClasses();
      if (inners != null) list.addAll(Arrays.asList(inners));
      //
      mChildren = new JElement[list.size()];
      list.toArray(mChildren);
    }
    return mChildren;
  }

  /**
   * Returns an array containing all of the fields declared by the
   * class and inherited from its superclasses.  This is done by
   * calling getDeclaredFields on each of the classes in the tree.
   * This method is useful for implementing JClass.getFields().
   */
  public JField[] getAllFields() {
    if (mFields == null) {
      List list = new ArrayList();
      addFieldsRecursively(mClass, list);
      mFields = new JField[list.size()];
      list.toArray(mFields);
    }
    return mFields;
  }

  /**
   * Returns an array containing all of the methods declared by the
   * class and inherited from its superclasses.  This is done by
   * calling getDeclaredMethods on each of the classes in the tree.
   * This method is useful for implementing JClass.getMethods().
   */
  public JMethod[] getAllMethods() {
    if (mMethods == null) {
      List list = new ArrayList();
      addMethodsRecursively(mClass, list);
      mMethods = new JMethod[list.size()];
      list.toArray(mMethods);
    }
    return mMethods;
  }

  /**
   * <p>Determines if the mClass is either the same as, or is a
   * superclass or superinterface of, the class or interface
   * represented by the arg. It returns true if so; otherwise it
   * returns false. If mClass represents a primitive type, this method
   * returns true only if arg is exactly the mClass.</p>
   */
  public boolean isAssignableFrom(JClass arg) {
    if (mClass.isPrimitive() || arg.isPrimitive()) {
      return mClass.equals(arg);
    }
    return isAssignableFromRecurse(arg);
  }

  // ========================================================================
  // Private methods

  private boolean isAssignableFromRecurse(JClass arg) {
    if (mClass.equals(arg)) return true;
    // check all of arg's implemented interfaces, recursively
    JClass[] interfaces = arg.getInterfaces();
    if (interfaces != null) {
      for (int i = 0; i < interfaces.length; i++) {
        if (isAssignableFromRecurse(interfaces[i])) return true;
      }
    }
    // check arg's superclass, recursively
    arg = arg.getSuperclass();
    if (arg != null) {
      if (isAssignableFromRecurse(arg)) return true;
    }
    return false;
  }

  private static void addFieldsRecursively(JClass clazz, Collection out) {
    JField[] fields = clazz.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) out.add(fields[i]);
    if (clazz.isInterface()) {
      JClass[] ints = clazz.getInterfaces();
      for (int i = 0; i < ints.length; i++) {
        addFieldsRecursively(ints[i], out);
      }
    } else {
      clazz = clazz.getSuperclass();
      if (clazz != null) addFieldsRecursively(clazz, out);
    }
  }

  private static void addMethodsRecursively(JClass clazz, Collection out) {
    JMethod[] methods = clazz.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) out.add(methods[i]);
    if (clazz.isInterface()) {
      JClass[] ints = clazz.getInterfaces();
      for (int i = 0; i < ints.length; i++) {
        addMethodsRecursively(ints[i], out);
      }
    } else {
      clazz = clazz.getSuperclass();
      if (clazz != null) addMethodsRecursively(clazz, out);
    }
  }
}



