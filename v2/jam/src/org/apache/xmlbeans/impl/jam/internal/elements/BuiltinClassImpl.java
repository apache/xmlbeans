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

/**
 * <p>Base class for types that are 'built in' to the VM.  This includes
 * void, primitives (but not their wrappers like Integer), and array types.
 * Note that java.lang.Object is not considered a Builtin.</p>
 *
 * <p>Note that while builtin classes cannot be modified, they can
 * be annotated and commented.</p>
 *
 * I think this is going to include generics as well.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class BuiltinClassImpl extends AnnotatedElementImpl
  implements MClass
{

  // ========================================================================
  // Constructors

  protected BuiltinClassImpl(ElementContext ctx) {
    super(ctx);
  }

  // ========================================================================
  // JElement implementation

  public void accept(MVisitor visitor) { visitor.visit(this); }

  public void accept(JVisitor visitor) { visitor.visit(this); }

  public String getQualifiedName() { return mSimpleName; }

  public String getFieldDescriptor() { return mSimpleName; }

  // ========================================================================
  // JMember implementation

  public int getModifiers() { return Object.class.getModifiers(); }
  public boolean isPublic() { return true; }
  public boolean isPackagePrivate() { return false; }
  public boolean isProtected() { return false; }
  public boolean isPrivate() { return false; }
  public JSourcePosition getSourcePosition() { return null; }
  public JClass getContainingClass() { return null; }

  // ========================================================================
  // JClass implementation

  public JClass forName(String fd) {
    return getClassLoader().loadClass(fd);
  }

  public JClass getArrayComponentType() { return null; }
  public int getArrayDimensions() { return 0; }

  public JClass getSuperclass() { return null; }
  public JClass[] getInterfaces() { return NO_CLASS; }
  public JField[] getFields() { return NO_FIELD; }
  public JField[] getDeclaredFields() { return NO_FIELD; }
  public JConstructor[] getConstructors() { return NO_CONSTRUCTOR;}
  public JMethod[] getMethods() { return NO_METHOD; }
  public JMethod[] getDeclaredMethods() { return NO_METHOD; }
  public JPackage getContainingPackage() { return null; }
  public boolean isInterface() { return false; }

  public boolean isArrayType() { return false; }
  public boolean isAnnotationType() { return false; }
  public boolean isPrimitiveType() { return false; }
  public boolean isBuiltinType() { return true; }
  public boolean isUnresolvedType() { return false; }
  public boolean isObjectType() { return false; }
  public boolean isVoidType() { return false; }
  public boolean isEnumType() { return false; }
  public Class getPrimitiveClass() { return null; }
  public boolean isAbstract() { return false; }
  public boolean isFinal() { return false; }
  public boolean isStatic() { return false; }
  public JClass[] getClasses() { return NO_CLASS; }
  public JProperty[] getProperties() { return NO_PROPERTY; }
  public JProperty[] getDeclaredProperties() { return NO_PROPERTY; }
  public JPackage[] getImportedPackages() { return NO_PACKAGE; }
  public JClass[] getImportedClasses() { return NO_CLASS; }

  // ========================================================================
  // MClass implementation

  public MField[] getMutableFields() { return NO_FIELD; }
  public MConstructor[] getMutableConstructors() { return NO_CONSTRUCTOR; }
  public MMethod[] getMutableMethods() { return NO_METHOD; }

  // can't do any of this stuff

  public void setSimpleName(String s) { nocando(); }

  public void setIsAnnotationType(boolean b) { nocando(); }
  public void setIsInterface(boolean b) { nocando(); }
  public void setIsUnresolvedType(boolean b) { nocando(); }
  public void setIsEnumType(boolean b) { nocando(); }
  public void setSuperclass(String qualifiedClassName) { nocando(); }
  public void setSuperclassUnqualified(String unqualifiedClassName) { nocando(); }
  public void setSuperclass(JClass clazz) { nocando(); }
  public void addInterface(String className) { nocando(); }
  public void addInterfaceUnqualified(String unqualifiedClassName) { nocando(); }
  public void addInterface(JClass interf) { nocando(); }
  public void removeInterface(String className) { nocando(); }
  public void removeInterface(JClass interf) { nocando(); }
  public MConstructor addNewConstructor() { nocando(); return null; }
  public void removeConstructor(MConstructor constr) { nocando(); }
  public MField addNewField() { nocando(); return null; }
  public void removeField(MField field) { nocando(); }
  public MMethod addNewMethod() { nocando(); return null; }
  public void removeMethod(MMethod method) { nocando(); }
  public void setModifiers(int modifiers) { nocando(); }

  public JProperty addNewProperty(String name, JMethod m, JMethod x) {
    nocando();
    return null;
  }
  public void removeProperty(JProperty prop) { nocando(); }

  public JProperty addNewDeclaredProperty(String name, JMethod m, JMethod x) {
    nocando();
    return null;
  }
  public void removeDeclaredProperty(JProperty prop) { nocando(); }

  // ========================================================================
  // Object implementation

  public boolean equals(Object o) {
    if (o instanceof JClass) {
      return ((JClass)o).getFieldDescriptor().equals(getFieldDescriptor());
    } else {
      return false;
    }
  }

  public int hashCode() { return getFieldDescriptor().hashCode(); }

  // ========================================================================
  // Protected methods

  protected void reallySetSimpleName(String name) {
    super.setSimpleName(name);
  }

  // ========================================================================
  // Private methods

  private void nocando() {
    throw new UnsupportedOperationException("Cannot alter builtin types");
  }

}
