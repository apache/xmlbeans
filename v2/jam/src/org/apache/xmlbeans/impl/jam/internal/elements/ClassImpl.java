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

package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.mutable.*;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.JClassRefContext;
import org.apache.xmlbeans.impl.jam.internal.classrefs.QualifiedJClassRef;
import org.apache.xmlbeans.impl.jam.internal.classrefs.UnqualifiedJClassRef;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Implementation of JClass and MClass.</p>
 *
 * @author Patrick Calahan <jdclasspcal@bea.com>
 */
public class ClassImpl extends MemberImpl implements MClass,
  JClassRef, JClassRefContext
{
  // ========================================================================
  // Constants

  public static final int NEW = 0;
  public static final int BUILDING = 1;
  public static final int POPULATING = 2;
  public static final int INITIALIZING = 3;
  public static final int LOADED = 4;
  //public static final int UNRESOLVED = 4;

  // ========================================================================
  // Variables

  private int mState = NEW;

  private boolean mIsAnnotationType = false;
  private boolean mIsInterface = false;

  private String mPackageName = null;

  private JClassRef mSuperClassRef = null; // classrefs to the class we extend
  private ArrayList mInterfaceRefs = null; // refs to interfaces we elements.

  private ArrayList mFields = null;
  private ArrayList mMethods = null;
  private ArrayList mConstructors = null;
  private ArrayList mProperties = null;

  private String[] mImports = null;

  // FIXME implement this - we should only create one UnqualifiedJClassRef
  // for each unqualified name so as to avoid resolving them over and over.
  //private Map mName2Uqref = null;

  // ========================================================================
  // Constructors

  public ClassImpl(String packageName,
                   String simpleName,
                   ElementContext ctx,
                   String[] importSpecs) {
    super(ctx);
    super.setSimpleName(simpleName);
    mPackageName = packageName.trim();
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
    return getMutableFields();
  }

  public JMethod[] getMethods() {
    List list = new ArrayList();
    addMethodsRecursively(this, list);
    JMethod[] out = new JMethod[list.size()];
    list.toArray(out);
    return out;
  }

  public JProperty[] getProperties() {
    if (mProperties == null) return new JProperty[0];
    JProperty[] out = new JProperty[mProperties.size()];
    mProperties.toArray(out);
    return out;
  }

  public JMethod[] getDeclaredMethods() { return getMutableMethods(); }

  public JConstructor[] getConstructors() { return getMutableConstructors(); }

  public boolean isInterface() { return mIsInterface; }

  public boolean isAnnotationType() { return mIsAnnotationType; }

  public boolean isFinal() { return Modifier.isFinal(getModifiers()); }

  public boolean isStatic() { return Modifier.isStatic(getModifiers()); }

  public boolean isAbstract() { return Modifier.isAbstract(getModifiers()); }

  public boolean isAssignableFrom(JClass arg) {
    if (isPrimitiveType() || arg.isPrimitiveType()) {
      return getQualifiedName().equals(arg.getQualifiedName());
    }
    return isAssignableFromRecursively(arg);
  }

  public JClass[] getClasses() {
    return new JClass[0];//FIXME
  }

  public String getFieldDescriptor() {
    return getQualifiedName();
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

  public void accept(MVisitor visitor) { visitor.visit(this); }

  public void accept(JVisitor visitor) { visitor.visit(this); }

  public void setSimpleName(String name) {
    throw new UnsupportedOperationException("Class names cannot be changed");
  }

  public Class getPrimitiveClass()  { return null; }
  public boolean isPrimitiveType()  { return false; }
  public boolean isBuiltinType()    { return false; }
  public boolean isVoidType()       { return false; }
  public boolean isUnresolvedType() { return false; }
  public boolean isObjectType() {
    return getQualifiedName().equals("java.lang.Object");
  }

  public boolean isArrayType() { return false; }
  public JClass getArrayComponentType() { return null; }
  public int getArrayDimensions() { return 0; }

  // ========================================================================
  // MClass implementation

  public void setSuperclass(String qualifiedClassName) {
    if (qualifiedClassName == null) {
      mSuperClassRef = null;
    } else {
      if (qualifiedClassName.equals(getQualifiedName())) {
        throw new IllegalArgumentException
          ("A class cannot be it's own superclass: '"+qualifiedClassName+"'");
      }
      mSuperClassRef = QualifiedJClassRef.create(qualifiedClassName, this);
    }
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

  public void addInterface(String qcName) {
    if (mInterfaceRefs == null) mInterfaceRefs = new ArrayList();
    if (qcName.equals(getQualifiedName())) {
      throw new IllegalArgumentException
        ("A class cannot implement itself: '"+qcName+"'");
    }
    mInterfaceRefs.add(QualifiedJClassRef.create(qcName,this));
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

  public MConstructor addNewConstructor() {
    if (mConstructors == null) mConstructors = new ArrayList();
    MConstructor out = new ConstructorImpl(this);
    mConstructors.add(out);
    return out;
  }

  public void removeConstructor(MConstructor constr) {
    if (mConstructors == null) return;
    mConstructors.remove(constr);
  }

  public MConstructor[] getMutableConstructors() {
    if (mConstructors == null || mConstructors.size() == 0) {
      return new MConstructor[0];
    }
    MConstructor[] out = new MConstructor[mConstructors.size()];
    mConstructors.toArray(out);
    return out;
  }

  public MField addNewField() {
    if (mFields == null) mFields = new ArrayList();
    MField out = new FieldImpl(defaultName(mFields.size()),
                                this,"java.lang.Object");
    mFields.add(out);
    return out;
  }

  public void removeField(MField field) {
    if (mFields == null) return;
    mFields.remove(field);
  }

  public MField[] getMutableFields() {
    if (mFields == null || mFields.size() == 0) {
      return new MField[0];
    }
    MField[] out = new MField[mFields.size()];
    mFields.toArray(out);
    return out;
  }

  public MMethod addNewMethod() {
    if (mMethods == null) mMethods = new ArrayList();
    MMethod out = new MethodImpl(defaultName(mMethods.size()),this);
    mMethods.add(out);
    return out;
  }

  public void removeMethod(MMethod method) {
    if (mMethods == null) return;
    mMethods.remove(method);
  }

  public MMethod[] getMutableMethods() {
    if (mMethods == null || mMethods.size() == 0) {
      return new MMethod[0];
    }
    MMethod[] out = new MMethod[mMethods.size()];
    mMethods.toArray(out);
    return out;
  }

  public JProperty addNewProperty(String name, MMethod getter, MMethod setter) {
    if (mProperties == null) mProperties = new ArrayList();
    JProperty out = new PropertyImpl(name,getter,setter,
                                     getter.getReturnType().getFieldDescriptor());
    mProperties.add(out);
    return out;
  }

  public void removeProperty(JProperty p) {
    if (mProperties != null) mProperties.remove(p);
  }

  public void setIsInterface(boolean b) { mIsInterface = b; }

  public void setIsAnnotationType(boolean b) { mIsAnnotationType = b; }

  public String getQualifiedName() {
    return ((mPackageName.length() > 0) ? (mPackageName + '.') : "") +
      mSimpleName;
  }

  // ========================================================================
  // JClassRef implementation (to accommodate direct references)

  public JClass getRefClass() { return this; }

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
  // Public methods for internal use only

  public void setState(int state) { mState = state; }





  // ========================================================================
  // Public static utility methods

  /**
   * Throws an IllegalArgument exception if the given string is not a valid
   * class name.  Useful for parameter checking in several places.
   */
  public static void validateClassName(String className)
          throws IllegalArgumentException
  {
    if (className == null) {
      throw new IllegalArgumentException("null class name specified");
    }
    if (!Character.isJavaIdentifierStart(className.charAt(0))) {
      throw new IllegalArgumentException
              ("Invalid first character in class name: "+className);
    }
    for(int i=1; i<className.length(); i++) {
      char c = className.charAt(i);
      if (c == '.') {
        if (className.charAt(i-1) == '.') {
          throw new IllegalArgumentException
                  ("'..' not allowed in class name: "+className);
        }
        if (i == className.length()-1) {
          throw new IllegalArgumentException
                  ("'.' not allowed at end of class name: "+className);
        }
      } else {
        if (!Character.isJavaIdentifierPart(c)) {
          throw new IllegalArgumentException
                  ("Illegal character '"+c+"' in class name: "+className);
        }
      }
    }
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

  private void addFieldsRecursively(JClass clazz, Collection out) {
    JField[] fields = clazz.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) out.add(fields[i]);
    JClass[] ints = clazz.getInterfaces();
    for (int i = 0; i < ints.length; i++) {
      addFieldsRecursively(ints[i], out);
    }
    clazz = clazz.getSuperclass();
    if (clazz != null) addFieldsRecursively(clazz, out);
  }

  private void addMethodsRecursively(JClass clazz, Collection out) {
    JMethod[] methods = clazz.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) out.add(methods[i]);
    JClass[] ints = clazz.getInterfaces();
    for (int i = 0; i < ints.length; i++) {
      addMethodsRecursively(ints[i], out);
    }
    clazz = clazz.getSuperclass();
    if (clazz != null) addMethodsRecursively(clazz, out);
  }

}
