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
