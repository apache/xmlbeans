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
package org.apache.xmlbeans.impl.binding.joust;

import java.io.IOException;

/**
 * <p>Performs some boilerplate input validation and state checking on stream
 * operations and then delegates to another JavaOutputStream.  This class
 * simply allows validation logic to be reused by multiple implementations of
 * JavaOutputStream.</p>
 *
 *
 * State diagram:
 *
 * <pre>
 *   +<-----endClass<-----+ +<-------------------------------+
 *   |                    | |                                |
 * BEGIN-->startClass--->CLASS-------->writeImport---------->+
 *                         |                                 |
 *                         +------->writeMemberVariable----->+
 *                         |                                 |
 *                         +--->startMethod--->METHOD--->endMethod
 *                               (or ctor)      | ^      (or ctor)
 *                                              V |
 *                                       write...Statement
 * </pre>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ValidatingJavaOutputStream implements JavaOutputStream {

  // FIXME This class isn't implemented yet - it's just a proxy that isn't
  // FIXME doing any validation

  // ========================================================================
  // Constants

  // ========================================================================
  // Variables

  private JavaOutputStream mDest;

  // ========================================================================
  // Constructors

  public ValidatingJavaOutputStream(JavaOutputStream destination) {
    if (destination == null) throw new IllegalArgumentException();
    mDest = destination;
  }

  // ========================================================================
  // JavaOutputStream implementation

  public void startClass(int modifiers,
                         String packageName,
                         String simpleName,
                         String extendsClassName,
                         String[] implementsInterfaceNames)
          throws IOException {
    mDest.startClass(modifiers, packageName, simpleName,
                     extendsClassName, implementsInterfaceNames);
  }

  public void startInterface(String packageName,
                             String simpleName,
                             String[] extendsInterfaceNames)
          throws IOException {
    mDest.startInterface(packageName, simpleName, extendsInterfaceNames);
  }

  public Variable writeField(int modifiers,
                             String typeName,
                             String fieldName,
                             Expression defaultValue) throws IOException {
    return mDest.writeField(modifiers, typeName, fieldName, defaultValue);
  }

  public Variable[] startConstructor(int modifiers,
                                     String[] paramTypeNames,
                                     String[] paramNames,
                                     String[] exceptionClassNames)
          throws IOException {
    return mDest.startConstructor(modifiers, paramTypeNames,
                                  paramNames, exceptionClassNames);
  }

  public Variable[] startMethod(int modifiers,
                                String methodName,
                                String returnTypeName,
                                String[] paramTypeNames,
                                String[] paramNames,
                                String[] exceptionClassNames)
          throws IOException {
    return mDest.startMethod(modifiers, methodName, returnTypeName,
                             paramTypeNames, paramNames, exceptionClassNames);
  }

  public void writeComment(String comment) throws IOException {
    mDest.writeComment(comment);
  }

  public void writeReturnStatement(Expression expression) throws IOException {
    mDest.writeReturnStatement(expression);
  }

  public void writeAssignmentStatement(Variable left, Expression right)
          throws IOException {
    mDest.writeAssignmentStatement(left, right);
  }

  public void endMethodOrConstructor() throws IOException {
    mDest.endMethodOrConstructor();
  }

  public void endClassOrInterface() throws IOException {
    mDest.endClassOrInterface();
  }

  public ExpressionFactory getExpressionFactory() {
    return mDest.getExpressionFactory();
  }

  public void close() throws IOException {
    mDest.close();
  }
}