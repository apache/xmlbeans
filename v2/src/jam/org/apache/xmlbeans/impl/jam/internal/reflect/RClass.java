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

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.BaseJElement;
import org.apache.xmlbeans.impl.jam.internal.JClassHelper;
import org.apache.xmlbeans.impl.jam.internal.JPropertyImpl;
import org.apache.xmlbeans.impl.jam.internal.PrimitiveJClass;

/**
 * Javadoc-backed implementation of JClass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class RClass extends BaseJElement implements JClass {

  // ========================================================================
  // Variables

  private Map mWrapperMap = new HashMap();
  private Class mClass;
  private JClassHelper mHelper;//this class is dumb
  private JClassLoader mLoader;

  // ========================================================================
  // Constructors
  
  public RClass(Class c, JClassLoader loader) {
    mLoader = loader;
    mClass = c;
    mHelper = new JClassHelper(this);
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() {
    return mHelper.getParent();
  }

  public JElement[] getChildren() {
    return mHelper.getChildren();
  }

  public String getSimpleName() {
    String out = getQualifiedName();
    int lastDot = out.lastIndexOf('.');
    return (lastDot == -1) ? out : out.substring(lastDot+1);
  }

  public String getQualifiedName() {
    if (!isArray()) {
      return mClass.getName();
    } else {
      StringWriter out = new StringWriter();
      out.write(getArrayComponentType().getQualifiedName());
      int dim = getArrayDimensions();
      for(int i=0; i<dim; i++) out.write("[]");
      return out.toString();
    }
  }

  // ========================================================================
  // BaseJElement implementation

  /**
   * We can't implement this until JSR175 is here.
   */
  protected void getLocalAnnotations(Collection out) {}

  /**
   * We can't ever implement this.
   */
  protected void getLocalComments(Collection out) {}

  // ========================================================================
  // JClass implementation

  public JClassLoader getClassLoader() { return mLoader; }

  public JClass forName(String fd) {
    return mLoader.loadClass(fd);
  }
  
  public JClass getSuperclass() {
    if (isObject() || isInterface() || isPrimitive()) {
      return null;
    } else {
      return RClassLoader.getClassFor(mClass.getSuperclass(),mLoader);
    }
  }

  public JClass[] getInterfaces() {
    return getClasses(mClass.getInterfaces());
  }

  public JField[] getFields() {
    return mHelper.getAllFields();
  }

  public JField[] getDeclaredFields() {
    return getFields(mClass.getDeclaredFields());
  }

  public JConstructor[] getConstructors() {
    return getConstructors(mClass.getConstructors());
  }

  public JMethod[] getMethods() {
    return mHelper.getAllMethods();
  }

  public JMethod[] getDeclaredMethods() {
    return getMethods(mClass.getDeclaredMethods());
  }

  public JPackage getContainingPackage() {
    String pkgName;
    if (mClass.getPackage() != null) {
      pkgName = mClass.getPackage().getName();
    } else {
      // If the package isn't already defined in the classloader,
      // let's not go defining new packages - just figure it out the
      // hard way.
      pkgName = !isArray() ? getQualifiedName() :
        getArrayComponentType().getQualifiedName();
      int lastDot = pkgName.lastIndexOf(".");
      if (lastDot != -1 && lastDot < pkgName.length()-1) {
        pkgName = pkgName.substring(0,lastDot);
      }
    }
    return mLoader.getPackage(pkgName);
  }

  public boolean isInterface() {
    return mClass.isInterface();
  }

  public boolean isPrimitive() {
    return false;
    //it's always false now, right?
    //(PrimitiveJClass.getPrimitiveClass(mClass) != null);
  }

  public Class getPrimitiveClass() {
    return null;
  }

  public boolean isVoid() {
    return mClass.getName().equals("void");
  }

  public boolean isObject() { return mClass == Object.class; }

  public boolean isAbstract() { 
    return Modifier.isAbstract(mClass.getModifiers()); 
  }

  public boolean isArray() { return mClass.isArray(); }

  public JClass getArrayComponentType() {
    if (mClass.getComponentType() == null) {
      return null;
    } else {
      Class c = mClass.getComponentType();
      while(c.getComponentType() != null) c = c.getComponentType();
      return RClassLoader.getClassFor(c,mLoader);
    }
  }


  public int getArrayDimensions() {
    int out = 0;
    Class c = mClass;
    while((c = c.getComponentType()) != null) out++;
    return out;
  }

  public String getFieldDescriptor() {
    return mClass.getName();
  }

  public JClass[] getClasses() { return getClasses(mClass.getClasses()); }

  public boolean isAssignableFrom(JClass clazz) {
    return mHelper.isAssignableFrom(clazz);
  }

  public boolean isStatic() { 
    return Modifier.isStatic(mClass.getModifiers());
  }

  public boolean isFinal() { 
    return Modifier.isFinal(mClass.getModifiers());
  }

  public JProperty[] getProperties() {
    //REVIEW maybe we should retrieve the BeanInfo in order to get
    //properties
    return JPropertyImpl.getProperties(this);
  }

  public JPackage[] getImportedPackages() { return BaseJElement.NO_PACKAGE; }

  public JClass[] getImportedClasses() { return BaseJElement.NO_CLASS; }

  public boolean isUnresolved() { return false; }

  // ========================================================================
  // JMember implementation

  public JClass getContainingClass() {
    if (mClass.getDeclaringClass() == null) return null;
    return RClassLoader.getClassFor(mClass.getDeclaringClass(),mLoader);
  }

  public boolean isProtected() { 
    return Modifier.isProtected(mClass.getModifiers());
  }

  public boolean isPublic() { 
    return Modifier.isPublic(mClass.getModifiers());
  }

  public boolean isPrivate() { 
    return Modifier.isPrivate(mClass.getModifiers());
  }

  public boolean isPackagePrivate() {
    return !isPublic() && !isProtected() && !isPrivate();
  }

  public int getModifiers() { return mClass.getModifiers(); }

  public JSourcePosition getSourcePosition() { return null; }

  // ========================================================================
  // Object implementation

  public boolean equals(Object o) {
    if (o instanceof JClass) {
      return ((JClass)o).getFieldDescriptor().equals(getFieldDescriptor());
    }
    return false;
  }

  public int hashCode() {
    return getFieldDescriptor().hashCode();
  }

  // ========================================================================
  // Private methods

  private JField[] getFields(Field[] fields) {
    List list = new ArrayList();
    for(int i=0; i<fields.length; i++) {
      list.add(getField(fields[i]));
    }
    JField[] out = new JField[list.size()];
    list.toArray(out);
    return out;
  }

  private JMethod[] getMethods(Method[] methods) {
    List list = new ArrayList();
    for(int i=0; i<methods.length; i++) {
      list.add(getMethod(methods[i]));
    }
    JMethod[] out = new JMethod[list.size()];
    list.toArray(out);
    return out;
  }

  private JConstructor[] getConstructors(Constructor[] constructors) {
    List list = new ArrayList();
    for(int i=0; i<constructors.length; i++) {
      list.add(getConstructor(constructors[i]));
    }
    JConstructor[] out = new JConstructor[list.size()];
    list.toArray(out);
    return out;
  }

  private JField getField(Field f) {
    JField out = (JField)mWrapperMap.get(f);
    if (out == null) mWrapperMap.put(f,out = new RField(f,mLoader));
    return out;
  }

  private JMethod getMethod(Method f) {
    JMethod out = (JMethod)mWrapperMap.get(f);
    if (out == null) mWrapperMap.put(f,out = new RMethod(f,mLoader));
    return out;
  }

  private JConstructor getConstructor(Constructor f) {
    JConstructor out = (JConstructor)mWrapperMap.get(f);
    if (out == null) mWrapperMap.put(f,out = new RConstructor(f,mLoader));
    return out;
  }

  private JClass[] getClasses(Class[] c) { return getClasses(c,mLoader); }

  // ========================================================================
  // Package utility methods


  /*package*/ static JClass[] getClasses(Class[] c, JClassLoader loader) {
    if (c == null) return null;
    JClass[] out = new JClass[c.length];
    for(int i=0; i<out.length; i++) {
      out[i] = RClassLoader.getClassFor(c[i],loader);
    }
    return out;
  }
}
