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

package org.apache.xmlbeans.impl.jam.internal;


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Type;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JConstructor;
import org.apache.xmlbeans.impl.jam.JFactory;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.impl.jam.JSourcePosition;

/**
 * Base class for types that are 'built in' to the VM.  Just provides
 * boring default implementations for lots of stuff.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BuiltinJClass implements JClass {

  // ========================================================================
  // JElement implementation

  public JElement getParent() { return null; }

  public JElement[] getChildren() { return null; }

  public JAnnotation[] getAnnotations() {
    return BaseJElement.NO_ANNOTATION;
  }

  public JAnnotation[] getAnnotations(String named) {
    return BaseJElement.NO_ANNOTATION;
  }

  public JAnnotation getAnnotation(String named) { return null; }

  public JComment[] getComments() { return BaseJElement.NO_COMMENT; }

  // ========================================================================
  // JMember implementation

  public int getModifiers() { return Object.class.getModifiers(); }

  public boolean isPackagePrivate() { return false; }

  public boolean isProtected() { return false; }

  public boolean isPublic() { return true; }

  public boolean isPrivate() { return false; }

  public JSourcePosition getSourcePosition() { return null; }

  public JClass getContainingClass() { return null; }

  // ========================================================================
  // JClass implementation

  public JClassLoader getClassLoader() {
    return JFactory.getInstance().getSystemClassLoader();
  }

  public JClass forName(String fd) {
    return getClassLoader().loadClass(fd);
  }

  public boolean isArray() { return false; }

  public JClass getArrayComponentType() { return null; }

  public int getArrayDimensions() { return 0; }

  public JClass getSuperclass() { return null; }

  public JClass[] getInterfaces() { return BaseJElement.NO_CLASS; }

  public JField[] getFields() { return BaseJElement.NO_FIELD; }

  public JField[] getDeclaredFields() { return BaseJElement.NO_FIELD; }

  public JConstructor[] getConstructors() { return BaseJElement.NO_CONSTRUCTOR;}

  public JMethod[] getMethods() { return BaseJElement.NO_METHOD; }

  public JMethod[] getDeclaredMethods() { return BaseJElement.NO_METHOD; }

  public JPackage getContainingPackage() { return null; }

  public boolean isAbstract() { return false; }

  public boolean isInterface() { return false; }

  public boolean isPrimitive() { return false; }

  public Class getPrimitiveClass() { return null; }

  public boolean isObject() { return false; }

  public boolean isVoid() { return false; }

  public boolean isFinal() { return false; }

  public boolean isStatic() { return false; }

  public JClass[] getClasses() { return BaseJElement.NO_CLASS; }

  public JProperty[] getProperties() { return BaseJElement.NO_PROPERTY; }

  public JPackage[] getImportedPackages() { return BaseJElement.NO_PACKAGE; }

  public JClass[] getImportedClasses() { return BaseJElement.NO_CLASS; }

  public boolean isUnresolved() { return false; }

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
}
