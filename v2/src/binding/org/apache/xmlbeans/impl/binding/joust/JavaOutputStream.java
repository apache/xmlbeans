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
 * <p>A JavaOutputStream is a service which provides sequential, read-only
 * java code-generation service. This is not a general-purpose java code
 * generator, but rather is tailored to produce java constructs required
 * for XMLbeans.</p>
 *
 * <p>By using this interface, the schema-to-java binding logic is isolated from
 * the details of java code generation.  This in turn allows for pluggability
 * of the generation logic - for example, one code generator might generate
 * java source files, while another might directly generate byte codes in
 * memory.</p>
 *
 * <b>A note about 'Type Names'</b>
 *
 * <p>A number of method signatures in this interface contain a String parameter
 * which is described as a 'Type Name.'  This is expected to be any
 * type declaration as you would normally see in java source code, e.g.
 * <code>int</code>, <code>String[][]</code>, <code>java.util.List</code>,
 * or <code>MyImportedClass</code>.  More specifically, it must be a valid
 * <code>TypeName</code> as described in
 * <a href='http://java.sun.com/docs/books/jls/second_edition/html/names.doc.html#73064'>
 * section 6.5.5 </a> of the Java Language Specification.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 *
 */
public interface JavaOutputStream {

  /**
   * Instructs the stream to begin writing a class with the given attributes.
   *
   * @param modifiers A java.lang.reflect.Modifier value describing the
   *        modifiers which apply to the new class.
   * @param packageName Fully-qualified name of the package which should
   *        contain the new class.
   * @param simpleName Unqualified name of the new class.
   * @param extendsClassName Name the class which the new class extends, or
   *        null if it should not extend anything.  The class name must be
   *        fully-qualified.
   * @param implementsInterfaceNames Array of interface names, one
   *        for each interface implemented by the new class, or null if
   *        the class does not implement anything.  Each class name must be
   *        fully-qualified.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         beginning a new class.
   * @throws IllegalArgumentException if modifers is not valid for a class,
   *         if packagename or classname is null or malformed, or if
   *         any class name parameter is malformed.
   */
  public void startClass(int modifiers,
                         String packageName,
                         String simpleName,
                         String extendsClassName,
                         String[] implementsInterfaceNames)
          throws IOException;

  /**
   * Instructs the stream to begin writing a new interface.
   *
   * @param packageName Fully-qualified name of the package which should
   *        contain the new interface.
   * @param simpleName Unqualified name of the new interface.
   * @param extendsInterfaceNames Array of interface names, one
   *        for each interface extendded by the new interface, or null if
   *        the interface does not extend anything.  Each class name must be
   *        fully-qualified.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         beginning a new interface.
   * @throws IllegalArgumentException if classname is null or if any classname
   *         parameters is malformed.
   */
  public void startInterface(String packageName,
                             String simpleName,
                             String[] extendsInterfaceNames)
          throws IOException;

  /**
   * Instructs the stream to write out a field (member variable) declaration
   * for the current class.
   *
   * @param modifiers A java.lang.reflect.Modifier value describing the
   *        modifiers which apply to the new field.
   * @param typeName The Type Name (see above) for the new field.
   * @param fieldName The name of the new field.
   * @param defaultValue An Expression describing the default value for the
   *                     new field, or null if none should be declared.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         a field declaration (e.g. if startClass has not been called).
   * @throws IllegalArgumentException if any type name is null or malformed or
   *         fieldName is null or not a valid java identifier, or if modifiers
   *         cannot be applied to a field.
   *
   * @return A handle to the field that is created.
   */
  public Variable writeField(int modifiers,
                             String typeName,
                             String fieldName,
                             Expression defaultValue) throws IOException;

  /**
   * Instructs the stream to write out a constructor for the current class.
   *
   * @param modifiers A java.lang.reflect.Modifier value describing the
   *        modifiers which apply to the new constructor.
   * @param paramTypeNames An array of Type Names (see above) for each of the
   *        constructor's parameters, or null if this is to be the default
   *        constructor.
   * @param paramNames An array of parameter names for each of the
   *        constructor's parameters, or null if this is to be the default
   *        constructor.
   * @param exceptionClassNames An array of class names, one
   *        for each exception type to be thrown by the new constructor, or
   *        null if the constructor does not throw anything.  Each name need
   *        not be qualified.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         a constructor declaration.
   * @throws IllegalArgumentException if paramTypeNames and paramNames are
   *         not of the same length (or both null), if any type name or
   *         exception class name is null or malformed, if the modifiers
   *         cannot be applied to a constructor.
   *
   * @return An array of Variables which provide handles to the parameters
   *         of the generated constructor.  Returns an empty array if the
   *         constructor does not take any parameters.
   */
  public Variable[] startConstructor(int modifiers,
                                     String[] paramTypeNames,
                                     String[] paramNames,
                                     String[] exceptionClassNames)
          throws IOException;

  /**
   * Instructs the stream to write out a new method for the current class.
   *
   * @param modifiers A java.lang.reflect.Modifier value describing the
   *        modifiers which apply to the new method.
   * @param methodName A name for the new method.
   * @param returnTypeName A Type Name (see above) for the method's return
   *        value, or "<code>void</vode>" if the method is void.
   * @param paramTypeNames An array of Type Names (see above) for each of the
   *        method's parameters, or null if the method takes no parameters.
   * @param paramNames An array of parameter names for each of the
   *        method's parameters, or null if the method takes no parameters.
   * @param exceptionClassNames An array of class names, one
   *        for each exception type to be thrown by the method, or
   *        null if the methoddoes not throw anything.  Each class name must
   *        be fully-qualified.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         a new method declaration.
   * @throws IllegalArgumentException if paramTypeNames and paramNames are
   *         not of the same length (or both null), if any type name or
   *         exception class name is null or malformed, if the modifiers
   *         cannot be applied to a method, or if methodName is null or
   *         malformed.
   *
   * @return An array of Variables which provide handles to the parameters
   *         of the generated method.  Returns an empty array if the method
   *         does not take any parameters.
   */
  public Variable[] startMethod(int modifiers,
                                String methodName,
                                String returnTypeName,
                                String[] paramTypeNames,
                                String[] paramNames,
                                String[] exceptionClassNames)
          throws IOException;

  /**
   * Writes out a source-code comment in the current class.  The comment
   * will usually be interpreted as applying to whatever is written next
   * in the stream, i.e. to write comments about a given class, you should
   * first call writeComment and then call writeClass.  The precise
   * formatting of the comments will be implementation-dependendent, and
   * some implementations may ignore comments altogether.
   *
   * @param comment The text of the comment.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         a field declaration (e.g. if startClass has not been called).
   *         writeComment should be a valid operation at all other times.
   */
  public void writeComment(String comment) throws IOException;

  /**
   * Writes out a return statement for the current method that returns
   * the given expression.
   *
   * @param  expression A handle to the expression to be returned.
   *
   * @throws IllegalArgumentException if expression is null.
   * @throws IllegalStateException if the current stream state does not allow
   *         a return declaration (e.g. if startMethod has not been called or
   *         the current method is void).
   */
  public void writeReturnStatement(Expression expression) throws IOException;

  /**
   * Writes out a return statement for the current method that returns
   * the given expression.
   *
   * @param  left A handle to the variable that goes on the left side
   *         of the equals sign.
   * @param  right A handle to the expression which goes on the right side
   *         of the equals sign.
   *
   * @throws IllegalArgumentException if either parameter is null.
   * @throws IllegalStateException if the current stream state does not allow
   *         an assignment declaration (e.g. if startMethod or
   *         startConstructor has not been called).
   */
  public void writeAssignmentStatement(Variable left, Expression right)
          throws IOException;

  /**
   * Instructs the stream to finish writing the current method or constructor.
   * Every call to startMethod or startConstructor must be balanced by a call
   * to endClassOrConstructor.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         the end of a class or interface.
   */
  public void endMethodOrConstructor() throws IOException;

  /**
   * Instructs the stream to finish writing the current class or interface.
   * Every call to startClass or startInterface must be balanced by a call to
   * endClassOrInterface.
   *
   * @throws IllegalStateException if the current stream state does not allow
   *         the end of a class or interface.
   */
  public void endClassOrInterface() throws IOException;

  /**
   * Returns the ExpressionFactory that should be to create instances of
   * Expression to be used in conjunction with this JavaOutputStream.
   *
   * @return An ExpressionFactory.  Must never return null.
   */
  public ExpressionFactory getExpressionFactory();

  /**
   * Closes the JavaOutputStream.  This should be called exactly once and
   * only when you are completely finished with the stream.
   */
  public void close() throws IOException;
}