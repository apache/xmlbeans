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

import org.apache.xmlbeans.impl.jam.editable.*;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.JClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.JClassRefContext;
import org.apache.xmlbeans.impl.jam.editable.impl.ref.UnqualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.*;

import java.util.*;
import java.lang.reflect.Modifier;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EClassImpl extends EMemberImpl
        implements EClass, JClassRef, JClassRefContext
 {

  // ========================================================================
  // Variables

  private boolean mIsUnresolved = false;
  private String mPackageName = null;

  // name of the class we extend, or null
  private JClassRef mSuperClassRef = null;
  // list of names of interfaces we implement, or null
  private ArrayList mInterfaceRefs = null;

  private ArrayList mFields = null;
  private ArrayList mMethods = null;
  private ArrayList mConstructors = null;

  // are we an interface or a class?
  private boolean mIsInterface = false;

  private String[] mImports = null;

  // FIXME implement this - we should only create one UnqualifiedJClassRef
  // for each unqualified name so as to avoid resolving them over and over.
  //private Map mName2Uqref = null;

  // ========================================================================
  // Constructors

  public EClassImpl(String packageName,
                    String simpleName,
                    JClassLoader classLoader,
                    String[] importSpecs) {
    super(simpleName,classLoader);
    mPackageName = packageName;
    mImports = importSpecs;
  }

  // ========================================================================
  // JClass implementation

  public JPackage getContainingPackage() {
    return getClassLoader().getPackage(mPackageName);
  }

  public JClass getSuperclass() {
    if (mSuperClassRef == null) {
      return null;
    } else {
      return mSuperClassRef.getRefClass();
    }
  }

  public JClass[] getInterfaces() {
    if (mInterfaceRefs == null || mInterfaceRefs.size() == 0) {
      return new JClass[0];
    } else {
      JClass[] out = new JClass[mInterfaceRefs.size()];
      for(int i=0; i<out.length; i++) {
        out[i] = ((JClassRef)mInterfaceRefs.get(i)).getRefClass();
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
      return getQualifiedName().equals(arg.getQualifiedName());
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
    return mIsUnresolved;
  }

  // ========================================================================
  // EClass implementation

  public void setSuperclass(String qualifiedClassName) {
    mSuperClassRef = QualifiedJClassRef.create(qualifiedClassName, this);
  }

  public void setSuperclassUnqualified(String unqualifiedClassName) {
    mSuperClassRef = UnqualifiedJClassRef.create(unqualifiedClassName,this);
  }

  public void setSuperclass(JClass clazz) {
    if (clazz == null) {
      mSuperClassRef = null;
    } else {
      setSuperclass(clazz.getQualifiedName());
    }
  }

  public void addInterface(JClass interf) {
    if (interf == null) throw new IllegalArgumentException("null interf");
    addInterface(interf.getQualifiedName());
  }

  public void addInterface(String qcname) {
    if (mInterfaceRefs == null) mInterfaceRefs = new ArrayList();
    mInterfaceRefs.add(QualifiedJClassRef.create(qcname,this));
  }

  public void addInterfaceUnqualified(String ucname) {
    if (mInterfaceRefs == null) mInterfaceRefs = new ArrayList();
    mInterfaceRefs.add(UnqualifiedJClassRef.create(ucname,this));
  }

  public void removeInterface(JClass interf) {
    if (interf == null) throw new IllegalArgumentException("null interf");
    removeInterface(interf.getQualifiedName());
  }

  public void removeInterface(String qcname) {
    //REVIEW this is quite inefficient, but maybe it doesnt matter so much
    if (qcname == null) throw new IllegalArgumentException("null classname");
    if (mInterfaceRefs == null) return;
    for(int i=0; i<mInterfaceRefs.size(); i++) {
      if (qcname.equals
              (((JClassRef)mInterfaceRefs.get(i)).getQualifiedName())) {
        mInterfaceRefs.remove(i);
      }
    }
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

  public EField addNewField() {
    if (mFields == null) mFields = new ArrayList();
    EField out = new EFieldImpl(defaultName(mFields.size()),
                                this,"java.lang.Object");
    mFields.add(out);
    return out;
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

  public EMethod addNewMethod() {
    if (mMethods == null) mMethods = new ArrayList();
    EMethod out = new EMethodImpl(defaultName(mMethods.size()),this);
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

  public void setIsInterface(boolean b) {
    mIsInterface = b;
  }

  public void setIsUnresolved(boolean b) {
    mIsUnresolved = b;
  }

  public String getQualifiedName() {
    return mPackageName+ '.' +getSimpleName();
  }


  // ========================================================================
  // JClassRef implementation

  public JClass getRefClass() {
    return this;
  }

  // ========================================================================
  // JClassRefContext implementation

  public String getPackageName() {
    return mPackageName;
  }

  public String[] getImportSpecs() {
    if (mImports == null) return new String[0];
    return mImports;
  }

  // ========================================================================
  // Protected methods

  /**
   * Provided for the benefit of EAnnotationDefinitionImpl.
   */
  protected void addMethod(JMethod m) {
    if (mMethods == null) mMethods = new ArrayList();
    mMethods.add(m);
  }

  // ========================================================================
  // Private methods

  private boolean isAssignableFromRecursively(JClass arg) {
    if (this.getQualifiedName().equals(arg.getQualifiedName())) return true;
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

  public void accept(EElementVisitor visitor) {
    visitor.visit(this);
  }

  public void acceptAndWalk(EElementVisitor visitor) {
    accept(visitor);
    acceptAndWalkAll(visitor,getEditableFields());
    acceptAndWalkAll(visitor,getEditableConstructors());
    acceptAndWalkAll(visitor,getEditableMethods());
    acceptAndWalkAll(visitor,getEditableAnnotations());
  }

}
