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

package org.apache.xmlbeans.impl.jam.internal.javadoc;


import com.sun.javadoc.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.JClassHelper;
import org.apache.xmlbeans.impl.jam.internal.JPropertyImpl;

/**
 * javadoc-backed implementation of JClass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDClass extends JDMember implements JClass {

  // ========================================================================
  // Variables

  private ClassDoc mClass;
  private JElement[] mChildren = null;
  private JMethod[] mMethods = null;
  private JField[] mFields = null;
  private JClassHelper mHelper;
  private Map mWrapperMap = new HashMap();

  // ========================================================================
  // Constructors

  /**
   * Constructs a JDClass for the given ClassDoc in the given context.
   */
  public JDClass(ClassDoc c, JClassLoader loader) {
    super(c,loader);
    mClass = c;
    mHelper = new JClassHelper(this);
    // assert that it isn't an array type.
    String dim = c.dimension();
    if (dim != null && dim.length() > 0) {
      throw new IllegalStateException("Internal Error: cannot use JDClass "+
				      "to represent array types.");
    }
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() { return mHelper.getParent(); }

  public JElement[] getChildren() { return mHelper.getChildren(); }

  public String getSimpleName() {
    String out = getQualifiedName();
    int lastDot = out.lastIndexOf('.');
    return (lastDot == -1) ? out : out.substring(lastDot+1);
  }    

  public String getQualifiedName() { 
    return mClass.qualifiedName();
  }

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
      return JDClassLoader.getClassFor(mClass.superclass(),mLoader);
    }
  }

  public JClass[] getInterfaces() {
    return getClasses(mClass.interfaces(),mLoader);
  }

  public JField[] getFields() { return mHelper.getAllFields(); }

  public JField[] getDeclaredFields() {
    return getFields(mClass.fields());
  }

  public JConstructor[] getConstructors() {
    return getConstructors(mClass.constructors());
  }

  public JMethod[] getMethods() { return mHelper.getAllMethods(); }

  public JMethod[] getDeclaredMethods() {
    return getMethods(mClass.methods());
  }

  public JProperty[] getProperties() {
    return JPropertyImpl.getProperties(this);
  }

  public JPackage getContainingPackage() {
    return mLoader.getPackage(mClass.containingPackage().name());
  }

  public boolean isInterface() { return mClass.isInterface(); }

  public boolean isAbstract() { return mClass.isAbstract(); }

  public JClass[] getClasses() {
    return getClasses(mClass.innerClasses(),mLoader);
  }

  public boolean isAssignableFrom(JClass c) {
    return mHelper.isAssignableFrom(c);
  }



  public String getFieldDescriptor() {
    // technically, we shuld be able to safely just return
    // mClass.qualifiedName(), since this is never an array type
    return JDClassLoader.getFieldDescriptorFor(mClass);
  }

  // these can never apply because we have builtin classes to handle
  // these cases

  public boolean isPrimitive() { return false; }

  public Class getPrimitiveClass() {
    return null;
  }

  public boolean isVoid() { return false; }

  public boolean isObject() { return false; }

  public boolean isArray() { return false; }

  public JClass getArrayComponentType() { return null; }

  public int getArrayDimensions() { return 0; }

  public JPackage[] getImportedPackages() { 
    PackageDoc[] docs = mClass.importedPackages();
    if (docs == null) return NO_PACKAGE;
    JPackage[] out = new JPackage[docs.length];
    for(int i=0; i<out.length; i++) {
      out[i] = mLoader.getPackage(docs[i].name());
    }
    return out;
  }

  public JClass[] getImportedClasses() { 
    return getClasses(mClass.importedClasses(),mLoader);
  }

  public boolean isUnresolved() { return false; }

  // ========================================================================
  // Object implementation

  public boolean equals(Object o) {
    if (o instanceof JClass) {
      return ((JClass)o).getFieldDescriptor().equals(getFieldDescriptor());
    }
    return false;
  }

  public int hashCode() { return getFieldDescriptor().hashCode(); }

  // ========================================================================
  // Private methods

  private JField[] getFields(FieldDoc[] fieldDocs) {
    if (fieldDocs.length == 0) return NO_FIELD;
    JField[] out = new JField[fieldDocs.length];
    for(int i=0; i<fieldDocs.length; i++) {
      out[i] = getField(fieldDocs[i]);
    }
    return out;
  }

  private JField getField(FieldDoc x) {
    JField out = (JField)mWrapperMap.get(x);
    if (out == null) {
      out = JDFactory.getInstance().createField(x, mLoader);
      mWrapperMap.put(x,out);
    }
    return out;
  }


  private JMethod getMethod(MethodDoc x) {
    JMethod out = (JMethod)mWrapperMap.get(x);
    if (out == null) {
      out = JDFactory.getInstance().createMethod(x, mLoader);
      mWrapperMap.put(x,out);
    }
    return out;
  }

  private JMethod[] getMethods(MethodDoc[] methodDocs) {
    if (methodDocs.length == 0) return NO_METHOD;
    JMethod[] out = new JMethod[methodDocs.length];
    for(int i=0; i<methodDocs.length; i++) {
      out[i] = getMethod(methodDocs[i]);
    }
    return out;
  }

  private JConstructor getConstructor(ConstructorDoc x) {
    JConstructor out = (JConstructor)mWrapperMap.get(x);
    if (out == null) {
      out = JDFactory.getInstance().createConstructor(x,mLoader);
      mWrapperMap.put(x,out);
    }
    return out;
  }

  private JConstructor[] getConstructors(ConstructorDoc[] docs) {
    if (docs.length == 0) return NO_CONSTRUCTOR;
    JConstructor[] out = new JConstructor[docs.length];
    for(int i=0; i<docs.length; i++) {
      out[i] = getConstructor(docs[i]);
    }
    return out;
  }

  // ========================================================================
  // Package utility methods

  //  /*package*/ static

  /*package*/ static JClass[] getClasses(ClassDoc[] docs, 
					 JClassLoader loader) {
    if (docs == null) return NO_CLASS;
    JClass[] out = new JClass[docs.length];
    for(int i=0; i<out.length; i++) {
      out[i] = JDClassLoader.getClassFor(docs[i],loader); 
      //REVIEW qtn ok?
    }
    return out;
  }

}
