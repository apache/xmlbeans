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

  public void startFile(String packageName,
                        String interfaceOrClassName)
          throws IOException {
    mDest.startFile(packageName,interfaceOrClassName);
  }

  public void startClass(int modifiers,
                         String extendsClassName,
                         String[] implementsInterfaceNames)
          throws IOException {
    mDest.startClass(modifiers, extendsClassName, implementsInterfaceNames);
  }

  public void startInterface(String[] extendsInterfaceNames)
          throws IOException {
    mDest.startInterface(extendsInterfaceNames);
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

  public void writeAnnotation(Annotation ann) throws IOException {
    mDest.writeAnnotation(ann);
  }

  public Annotation createAnnotation(String type) {
    return mDest.createAnnotation(type);
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

  public void endFile() throws IOException {
    mDest.endFile();
  }

  public ExpressionFactory getExpressionFactory() {
    return mDest.getExpressionFactory();
  }

  public void close() throws IOException {
    mDest.close();
  }
}