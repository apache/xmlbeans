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
package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.*;
import org.apache.xmlbeans.impl.jam.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.lang.reflect.Modifier;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EClassImpl extends EMemberImpl implements EClass {

  // ========================================================================
  // Variables

  private String mPackageName = null;

  // name of the class we extend, or null
  private String mSuperClassName = null;
  // list of names of interfaces we implement, or null
  private ArrayList mInterfaceNames = null;

  private ArrayList mFields = null;
  private ArrayList mMethods = null;
  private ArrayList mConstructors = null;

  // are we an interface or a class?
  private boolean mIsInterface = false;

  // ========================================================================
  // Constructors

  public EClassImpl(String packageName,
                    String simpleName,
                    JClassLoader classLoader) {
    super(simpleName,classLoader);
    mPackageName = packageName;
  }

  // ========================================================================
  // JClass implementation

  public JPackage getContainingPackage() {
    return getClassLoader().getPackage(mPackageName);
  }

  public JClass getSuperclass() {
    if (mSuperClassName == null) {
      return null;
    } else {
      return getClassLoader().loadClass(mSuperClassName);
    }
  }

  public JClass[] getInterfaces() {
    if (mInterfaceNames == null || mInterfaceNames.size() == 0) {
      return new JClass[0];
    } else {
      JClass[] out = new JClass[mInterfaceNames.size()];
      for(int i=0; i<out.length; i++) {
        out[i] = getClassLoader().loadClass((String)mInterfaceNames.get(i));
      }
      return out;
    }
  }

  public JField[] getFields() {
    List list = new ArrayList();
    addFieldsRecursively(this, list);
    JField[] out = new JField[list.size()];
    list.toArray(out);
    return out;
  }

  public JField[] getDeclaredFields() {
    return getEditableFields();
  }

  public JMethod[] getMethods() {
    List list = new ArrayList();
    addMethodsRecursively(this, list);
    JMethod[] out = new JMethod[list.size()];
    list.toArray(out);
    return out;
  }

  public JMethod[] getDeclaredMethods() {
    return getEditableMethods();
  }

  public JConstructor[] getConstructors() {
    return getEditableConstructors();
  }

  public JProperty[] getProperties() {
    throw new IllegalStateException("NYI");//FIXME
  }

  public boolean isInterface() {
    return mIsInterface;
  }

  public boolean isFinal() {
    return Modifier.isFinal(getModifiers());
  }

  public boolean isStatic() {
    return Modifier.isStatic(getModifiers());
  }

  public boolean isAbstract() {
    return Modifier.isAbstract(getModifiers());
  }

  public boolean isAssignableFrom(JClass arg) {
    if (isPrimitive() || arg.isPrimitive()) {
      return this.equals(arg);
    }
    return isAssignableFromRecursively(arg);
  }

  public JClass[] getClasses() {
    return new JClass[0];
  }

  public String getFieldDescriptor() {
    return mPackageName+"."+getSimpleName();
  }

  public JClass forName(String name) {
    return getClassLoader().loadClass(name);
  }

  public JPackage[] getImportedPackages() {
    return new JPackage[0];//FIXME
  }

  public JClass[] getImportedClasses() {
    return new JClass[0];//FIXME
  }

  // ========================================================================
  // Dumb JClass implementation

  public boolean isPrimitive() {
    return false;
  }

  public Class getPrimitiveClass() {
    return null;
  }

  public boolean isVoid() {
    return false;
  }

  public boolean isObject() {
    return false;
  }

  public boolean isArray() {
    return false;
  }

  public JClass getArrayComponentType() {
    return null;
  }

  public int getArrayDimensions() {
    return 0;
  }

  public boolean isUnresolved() {
    return false;
  }

  // ========================================================================
  // EClass implementation

  public void setSuperclass(String className) {
    mSuperClassName = className;
  }

  public void setSuperclass(JClass clazz) {
    if (clazz == null) {
      mSuperClassName = null;
    } else {
      setSuperclass(clazz.getQualifiedName());
    }
  }

  public void addInterface(JClass interf) {
    if (interf == null) throw new IllegalArgumentException("null interf");
    addInterface(interf.getQualifiedName());
  }

  public void addInterface(String className) {
    if (mInterfaceNames == null) mInterfaceNames = new ArrayList();
    mInterfaceNames.add(className);
  }

  public void removeInterface(JClass interf) {
    if (interf == null) throw new IllegalArgumentException("null interf");
    removeInterface(interf.getQualifiedName());
  }

  public void removeInterface(String className) {
    if (className == null) throw new IllegalArgumentException("null classname");
    if (mInterfaceNames == null) return;
    mInterfaceNames.remove(className);
  }

  public EConstructor addNewConstructor() {
    if (mConstructors == null) mConstructors = new ArrayList();
    EConstructor out = new EConstructorImpl(this);
    mConstructors.add(out);
    return out;
  }

  public void removeConstructor(EConstructor constr) {
    if (mConstructors == null) return;
    mConstructors.remove(constr);
  }

  public EConstructor[] getEditableConstructors() {
    if (mConstructors == null || mConstructors.size() == 0) {
      return new EConstructor[0];
    }
    EConstructor[] out = new EConstructor[mConstructors.size()];
    mConstructors.toArray(out);
    return out;
  }

  public EField addNewField(String typeName, String name) {
    if (typeName == null) throw new IllegalArgumentException("null typeName");
    if (name == null) throw new IllegalArgumentException("null name");
    if (mFields == null) mFields = new ArrayList();
    EField out = new EFieldImpl(name,this,typeName);
    mFields.add(out);
    return out;
  }

  public EField addNewField(JClass type, String name) {
    if (type == null) throw new IllegalArgumentException("null type");
    if (name == null) throw new IllegalArgumentException("null name");
    return addNewField(type.getQualifiedName(),name);
  }

  public void removeField(EField field) {
    if (mFields == null) return;
    mFields.remove(field);
  }

  public EField[] getEditableFields() {
    if (mFields == null || mFields.size() == 0) {
      return new EField[0];
    }
    EField[] out = new EField[mFields.size()];
    mFields.toArray(out);
    return out;
  }

  public EMethod addNewMethod(String name) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (mMethods == null) mMethods = new ArrayList();
    EMethod out = new EMethodImpl(name,this);
    mMethods.add(out);
    return out;
  }

  public void removeMethod(EMethod method) {
    if (mMethods == null) return;
    mMethods.remove(method);
  }

  public EMethod[] getEditableMethods() {
    if (mMethods == null || mMethods.size() == 0) {
      return new EMethod[0];
    }
    EMethod[] out = new EMethod[mMethods.size()];
    mMethods.toArray(out);
    return out;
  }

 // ========================================================================
  // Private methods

  private boolean isAssignableFromRecursively(JClass arg) {
    if (this.equals(arg)) return true;
    // check all of arg's implemented interfaces, recursively
    JClass[] interfaces = arg.getInterfaces();
    if (interfaces != null) {
      for (int i = 0; i < interfaces.length; i++) {
        if (isAssignableFromRecursively(interfaces[i])) return true;
      }
    }
    // check arg's superclass, recursively
    arg = arg.getSuperclass();
    if (arg != null) {
      if (isAssignableFromRecursively(arg)) return true;
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

  public String getQualifiedName() {
    return mPackageName+ '.' +getSimpleName();
  }
}
