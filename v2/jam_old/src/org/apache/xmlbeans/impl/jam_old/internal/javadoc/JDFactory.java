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

package org.apache.xmlbeans.impl.jam_old.internal.javadoc;

import org.apache.xmlbeans.impl.jam_old.JAnnotation;
import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JClassLoader;
import org.apache.xmlbeans.impl.jam_old.JComment;
import org.apache.xmlbeans.impl.jam_old.JConstructor;
import org.apache.xmlbeans.impl.jam_old.JElement;
import org.apache.xmlbeans.impl.jam_old.JField;
import org.apache.xmlbeans.impl.jam_old.JMethod;
import org.apache.xmlbeans.impl.jam_old.JPackage;
import org.apache.xmlbeans.impl.jam_old.JParameter;
import org.apache.xmlbeans.impl.jam_old.JSourcePosition;
import org.apache.xmlbeans.impl.jam_old.internal.JPackageImpl;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;

/**
 * A repository of static factory methods for all the classes created
 * by JavaDoc JAM.  Mostly here to allow implementation of JAM
 * on top of Javadoc 1.5 using JSR 175 style annotations.
 * 
 * Extend this class, override whatever factory methods you need
 * and call JDFactory.setInstance() to your own factory before
 * starting JAM.
 *
 * @author Cedric Beust <cedric@bea.com>
 */
public class JDFactory {

  /////
  // Singleton
  //
  private static JDFactory m_instance = new JDFactory();
  
  public static JDFactory getInstance() {
    return m_instance;
  }
  
  public static void setInstance(JDFactory instance) {
    m_instance = instance;
  }
  
  //
  // Singleton
  /////
  
  
  /////
  // JAnnotation
  //
  
  public JAnnotation createAnnotation(JElement parent, String name, String value, JSourcePosition sp) {
    return new JDAnnotation(parent, name, value, sp);
  }
  
  /////
  // JDClassLoaderFactory
  //
  public JDClassLoaderFactory createClassLoaderFactory() {
    return new JDClassLoaderFactory();
  }

  /////
  // JDClassLoader
  //
  public JDClassLoader createClassLoader(RootDoc root, JClassLoader parentLoader) {
    return new JDClassLoader(root, parentLoader);
  }

  /////
  // JComment
  //
  public JComment createComment(String string) {
    return new JDComment(string);
  }

  /////
  // JField
  //
  public static JField createField(FieldDoc x, JClassLoader loader) {
    return new JDField(x, loader);
  }

  /////
  // JField
  //
  public JClass createClass(ClassDoc doc, JDClassLoader loader) {
    return new JDClass(doc, loader);
  }

  //REVIEW not really clear why need two here
  public JClass createClass(ClassDoc doc, JClassLoader loader) {
    return new JDClass(doc, loader);
  }

  /////
  // JConstructor
  //
  public JConstructor createConstructor(ConstructorDoc x, JClassLoader loader) {
    return new JDConstructor(x, loader);
  }

  /////
  // JMethod
  //
  public JMethod createMethod(MethodDoc x, JClassLoader loader) {
    return new JDMethod(x, loader);
  }

  /////
  // JPackage
  //
  public JPackage createPackage(String named) {
    return new JPackageImpl(named);
  }

  /////
  // JSourcePosition
  //
  public JSourcePosition createSourcePosition(SourcePosition sp) {
    return new JDSourcePosition(sp);
  }

  /////
  // JParameter
  //
  public JParameter createParameter(Parameter parameter, JDExecutableMember member, JClassLoader loader) {
    return new JDParameter(parameter, member, loader);
  }

}
