/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.jam.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.xmlbeans.impl.jam.*;

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



