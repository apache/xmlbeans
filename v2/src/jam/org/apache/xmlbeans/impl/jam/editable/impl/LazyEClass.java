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

import org.apache.xmlbeans.impl.jam.provider.EClassBuilder;
import org.apache.xmlbeans.impl.jam.*;

/**
 * Implementation of JClass which can be instantiated for a given class name
 * without having to interact with any source or classfile artifacts.  Only
 * when the user requests something substantive about the java type will
 * this impl request a given EClassBuilder to populate it, which in turn
 * might cause a source file to be parsed.  This allows the caller to deal
 * with JClass objects without having to parse anything for classes which
 * they aren't interested in.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class LazyEClass extends EClassImpl {

  // ========================================================================
  // Variables

  private EClassBuilder mBuilder = null;
  private boolean mIsIntialized = false;
  private boolean mIsPopulated = true;

  // ========================================================================
  // Constructors

  public LazyEClass(String packageName,
                    String className,
                    JClassLoader loader,
                    EClassBuilder builder) {
    super(packageName,className,loader);
    mBuilder = builder;
  }

  // ========================================================================
  // JElement implementation

  public JSourcePosition getSourcePosition() {
    checkInitialized();
    return super.getSourcePosition();
  }

  // ========================================================================
  // JMember implementation

  public JClass getContainingClass() {
    checkInitialized();
    return super.getContainingClass();
  }

  // ========================================================================
  // JClass implementation

  public boolean isUnresolved() {
    checkInitialized();
    return !mIsPopulated;//yuck
  }

  public JClass getSuperclass() {
    checkInitialized();
    return super.getSuperclass();
  }

  public JClass[] getInterfaces() {
    checkInitialized();
    return super.getInterfaces();
  }

  public JField[] getFields() {
    checkInitialized();
    return super.getFields();
  }

  public JField[] getDeclaredFields() {
    checkInitialized();
    return super.getDeclaredFields();
  }

  public JMethod[] getMethods() {
    checkInitialized();
    return super.getMethods();
  }

  public JMethod[] getDeclaredMethods() {
    checkInitialized();
    return super.getDeclaredMethods();
  }

  public JConstructor[] getConstructors() {
    checkInitialized();
    return super.getConstructors();
  }

  public JProperty[] getProperties() {
    checkInitialized();
    return super.getProperties();
  }

  public boolean isInterface() {
    checkInitialized();
    return super.isInterface();
  }

  public int getModifiers() {
    checkInitialized();
    return super.getModifiers();
  }

  public boolean isAssignableFrom(JClass clazz) {
    checkInitialized();
    return super.isAssignableFrom(clazz);
  }

  public JClass[] getClasses() {
    checkInitialized();
    return super.getClasses();
  }

  public JPackage[] getImportedPackages() {
    checkInitialized();
    return super.getImportedPackages();
  }

  public JClass[] getImportedClasses() {
    checkInitialized();
    return super.getImportedClasses();
  }

  // ========================================================================
  // Private methods

  private void checkInitialized() {
    if (mIsIntialized) return;
    mIsPopulated = mBuilder.populateClass(this);
    mIsIntialized = true;
  }
}