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

import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JConstructor;
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JSourcePosition;
import org.apache.xmlbeans.impl.jam.internal.JPackageImpl;

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
