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
import java.io.PrintWriter;
import java.io.Writer;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * <p>Implementation of JavaOutputStream which outputs Java source code.</p>
 *
 * <p>Note that this class has no direct knowledge of where that source code
 * goes; that functionality is factored out into the WriterFactory interface,
 * which returns a new PrintWriter for a given package and class name on
 * demand.  Typically, the implementation will be FileWriterFactory, which
 * simply creates files under a source root directory, but this factoring
 * allows for other arrangements to be supported.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class SourceJavaOutputStream
        implements JavaOutputStream, ExpressionFactory {

  // ========================================================================
  // Constants

  private static final String COMMENT_LINE_DELIMITERS = "\n\r\f";
  private static final String INDENT_STRING = "  ";

  private static final char[] hexLow = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
  };

  private static final char[] hexHigh = {
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A',
    'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B',
    'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C',
    'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D',
    'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
    'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F',
  };


  // ========================================================================
  // Variables

  private PrintWriter mOut = null;
  private int mIndentLevel = 0;
  private String mPackageName = null;
  private String mClassOrInterfaceName = null;
  private WriterFactory mWriterFactory;
  private StringWriter mCommentBuffer = null;
  private PrintWriter mCommentPrinter = null;
  protected boolean mVerbose = false;

  // ========================================================================
  // Constructors

  public SourceJavaOutputStream(WriterFactory factory) {
    setWriterFactory(factory);
  }

  protected SourceJavaOutputStream() {}

  protected void setWriterFactory(WriterFactory factory) {
    if (factory == null) throw new IllegalArgumentException();
    mWriterFactory = factory;
  }

  // ========================================================================
  // Public methods

  public void setVerbose(boolean b) { mVerbose = b; }

  // ========================================================================
  // JavaOutputStream implementation

  public void startFile(String packageName,
                        String classOrInterfaceName) throws IOException {
    if (packageName == null) {
      throw new IllegalArgumentException("null package");
    }
    if (classOrInterfaceName == null) {
      throw new IllegalArgumentException("null classname");
    }
    if (mOut != null) {
      throw new IllegalStateException("Start new file without calling "+
                                      "endFile on existing file");
    }
    if (mIndentLevel != 0) throw new IllegalStateException(); //sanity check
    mOut = new PrintWriter(mWriterFactory.createWriter(packageName,
                                                       classOrInterfaceName));
    mPackageName = makeI18nSafe(packageName);
    mClassOrInterfaceName = makeI18nSafe(classOrInterfaceName);
  }

  public void startClass(int modifiers,
                         String extendsClassName,
                         String[] interfaceNames)
          throws IOException {
    checkStateForWrite();
    printCommentsIfNeeded();
    if (mVerbose) verbose("startClass "+mPackageName+"."+mClassOrInterfaceName);
    extendsClassName = makeI18nSafe(extendsClassName);
    mOut.println("package " + mPackageName + ";");
    mOut.println();
    // We need to write up the actual class declaration and save it until
    // after the imports have been written
    //FIXME we should format this code more nicely
    mOut.print(Modifier.toString(modifiers));
    mOut.print(" class ");
    mOut.print(mClassOrInterfaceName);
    if (extendsClassName != null) {
      mOut.print(" extends ");
      mOut.print(extendsClassName);
    }
    if (interfaceNames != null && interfaceNames.length > 0) {
      mOut.print(" implements ");
      for (int i = 0; i < interfaceNames.length; i++) {
        mOut.print(makeI18nSafe(interfaceNames[i]));
        if (i < interfaceNames.length - 1) mOut.print(", ");
      }
    }
    mOut.println(" {");
    mOut.println();
    increaseIndent();
  }

  public void startInterface(String[] extendsInterfaceNames)
          throws IOException {
    if (mVerbose) verbose("startInterface "+mPackageName+"."+mClassOrInterfaceName);
    checkStateForWrite();
    printCommentsIfNeeded();
    mPackageName = makeI18nSafe(mPackageName);
    mOut.println("package " + mPackageName + ";");
    // We need to write up the actual class declaration and save it until
    // after the imports have been written
    //FIXME we should format this code more nicely
    mOut.print("public interface ");
    mOut.print(mClassOrInterfaceName);
    if (extendsInterfaceNames != null && extendsInterfaceNames.length > 0) {
      mOut.print(" extends ");
      for (int i = 0; i < extendsInterfaceNames.length; i++) {
        mOut.print(makeI18nSafe(extendsInterfaceNames[i]));
        if (i < extendsInterfaceNames.length - 1) mOut.print(", ");
      }
    }
    mOut.println("{");
    mOut.println();
    increaseIndent();
  }

  public Variable writeField(int modifiers,
                             String typeName,
                             String fieldName,
                             Expression defaultValue) throws IOException {
    if (mVerbose) verbose("writeField "+typeName+" "+fieldName);
    checkStateForWrite();
    printCommentsIfNeeded();
    printIndents();
    typeName = makeI18nSafe(typeName);
    fieldName = makeI18nSafe(fieldName);
    mOut.print(Modifier.toString(modifiers));
    mOut.print(" ");
    mOut.print(typeName);
    mOut.print(" ");
    mOut.print(fieldName);
    if (defaultValue != null) {
      mOut.print(" = ");
      mOut.print(((String) defaultValue.getMemento()));
    }
    mOut.println(';');
    mOut.println();
    return newVar("this." + fieldName);
  }

  public Variable[] startConstructor(int modifiers,
                                     String[] paramTypeNames,
                                     String[] paramNames,
                                     String[] exceptionClassNames)
          throws IOException {
    return startMethod(modifiers, null, mClassOrInterfaceName,
                       paramTypeNames, paramNames, exceptionClassNames);
  }

  public Variable[] startMethod(int modifiers,
                                String returnTypeName,
                                String methodName,
                                String[] paramTypeNames,
                                String[] paramNames,
                                String[] exceptionClassNames)
          throws IOException {
    if (mVerbose) verbose("startMethod "+methodName);
    checkStateForWrite();
    printCommentsIfNeeded();
    methodName = makeI18nSafe(methodName);
    returnTypeName = makeI18nSafe(returnTypeName);
    printIndents();
    mOut.print(Modifier.toString(modifiers));
    mOut.print(" ");
    if (returnTypeName != null) {
      mOut.print(returnTypeName);
      mOut.print(" ");
    }
    mOut.print(methodName);
    // print the parameter list
    Variable[] ret;
    if (paramTypeNames == null || paramTypeNames.length == 0) {
      mOut.print("()");
      ret = new Variable[0];
    } else {
      ret = new Variable[paramTypeNames.length];
      for (int i = 0; i < ret.length; i++) {
        mOut.print((i == 0) ? "(" : ", ");
        ret[i] = newVar(paramNames[i]);
        mOut.print(makeI18nSafe(paramTypeNames[i]));
        mOut.print(' ');
        mOut.print(makeI18nSafe(paramNames[i]));
      }
      mOut.print(")");
    }
    // print the throws clause
    if (exceptionClassNames != null && exceptionClassNames.length > 0) {
      for (int i = 0; i < exceptionClassNames.length; i++) {
        mOut.print((i == 0) ? " throws " : ", ");
        mOut.print(makeI18nSafe(exceptionClassNames[i]));
      }
    }
    mOut.println(" {");
    increaseIndent();
    return ret;
  }

  public void writeComment(String comment) throws IOException {
    if (mVerbose) verbose("comment");
    getCommentPrinter().println(comment);
  }

  public void writeAnnotation(Annotation ann) throws IOException {
    //FIXME haven't really thought much about how to write annotations
    //as javadoc - this is more just proof-of-concept at this point.
    //FIXME Eventually, will also need a switch for writing out jsr175
    PrintWriter out = getCommentPrinter();
    Iterator i = ((AnnotationImpl)ann).getPropertyNames();
    while(i.hasNext()) {
      String n = i.next().toString();
      out.print('@');
      out.print(((AnnotationImpl)ann).getType());
      out.print('.');
      out.print(n);
      out.print(" = ");
      out.print(((AnnotationImpl)ann).getValueDeclaration(n));
      out.println();
    }
  }

  public void writeReturnStatement(Expression expression) throws IOException {
    if (mVerbose) verbose("return");
    checkStateForWrite();
    printCommentsIfNeeded();
    printIndents();
    mOut.print("return ");
    mOut.print(((String) expression.getMemento()));
    mOut.println(";");
  }

  public void writeAssignmentStatement(Variable left, Expression right)
          throws IOException {
    if (mVerbose) verbose("assignment");
    checkStateForWrite();
    printCommentsIfNeeded();
    printIndents();
    mOut.print(((String) left.getMemento()));
    mOut.print(" = ");
    mOut.print(((String) right.getMemento()));
    mOut.println(";");
  }

  public void endMethodOrConstructor() throws IOException {
    if (mVerbose) verbose("endMethodOrConstructor");
    checkStateForWrite();
    printCommentsIfNeeded();
    reduceIndent();
    printIndents();
    mOut.println('}');
    mOut.println();
  }

  public void endClassOrInterface() throws IOException {
    if (mVerbose) verbose("endClassOrInterface");
    checkStateForWrite();
    printCommentsIfNeeded();
    reduceIndent();
    printIndents();
    mOut.println('}');
  }

  public void endFile() throws IOException {
    checkStateForWrite();
    printCommentsIfNeeded();
    if (mVerbose) verbose("endFile");
    closeOut();
  }

  public ExpressionFactory getExpressionFactory() {
    return this;
  }

  public Annotation createAnnotation(String type) {
    return new AnnotationImpl(type);
  }

  public void close() throws IOException {
    if (mVerbose) verbose("close");
    closeOut();//just to be safe
  }

  // ========================================================================
  // ExpressionFactory implementation

  private static final Expression TRUE = newExp("true");
  private static final Expression FALSE = newExp("true");
  private static final Expression NULL = newExp("null");

  public Expression createBoolean(boolean value) {
    return value ? TRUE : FALSE;
  }

  public Expression createString(String value) {
    return newExp("\"" + makeI18nSafe(value) + "\"");
  }

  public Expression createInt(int value) {
    return newExp(String.valueOf(value));
  }

  public Expression createNull() {
    return NULL;
  }

  // ========================================================================
  // Private methods

  private PrintWriter getCommentPrinter() {
    if (mCommentPrinter == null) {
      mCommentBuffer = new StringWriter();
      mCommentPrinter = new PrintWriter(mCommentBuffer);
    }
    return mCommentPrinter;
  }

  private void printCommentsIfNeeded() {
    if (mCommentBuffer == null) return;
    checkStateForWrite();
    String comment = mCommentBuffer.toString();
    printIndents();
    mOut.println("/**");
    StringTokenizer st = new StringTokenizer(makeI18nSafe(comment),
                                             COMMENT_LINE_DELIMITERS);
    while (st.hasMoreTokens()) {
      printIndents();
      mOut.print(" * ");
      mOut.println(st.nextToken());
    }
    printIndents();
    mOut.println(" */");
    mCommentBuffer = null;
    mCommentPrinter = null;
  }

  private void checkStateForWrite() {
    if (mOut == null) {
      throw new IllegalStateException("Attempt to generate code when no "+
                                      "file open.  This is indicates that "+
                                      "there is some broken logic in the " +
                                      "calling class");
    }
  }


  private void printIndents() {
    for (int i = 0; i < mIndentLevel; i++) mOut.print(INDENT_STRING);
  }

  private void increaseIndent() {
    mIndentLevel++;
  }

  private void reduceIndent() {
    mIndentLevel--;
    if (mIndentLevel < 0) {
      throw new IllegalStateException("Indent level reduced below zero. "+
                                      "This is indicates that "+
                                      "there is some broken logic in the " +
                                      "calling class");
    }
  }

  private void closeOut() {
    if (mOut != null) {
      mOut.flush();
      mOut.close();
      mOut = null;
    }
  }

  private static Expression newExp(final String s) {
    final String memento = makeI18nSafe(s);
    return new Expression() {
      public Object getMemento() {
        return memento;
      }
    };
  }

  private static Variable newVar(String s) {
    final String memento = makeI18nSafe(s);
    return new Variable() {
      public Object getMemento() {
        return memento;
      }
    };
  }

  private static String makeI18nSafe(String s) {
    if (s == null) return null;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) > 127)
        return buildI18nSafe(s);
    }
    return s;
  }

  private static String buildI18nSafe(String s) {
    StringBuffer mI18nSafeBuffer = new StringBuffer();
    int i = 0;
    int j = 0;
    for (; ;) {
      for (; i < s.length(); i++) {
        if (s.charAt(i) > 127)
          break;
      }
      if (j < i)
        mI18nSafeBuffer.append(s.substring(j, i));
      for (; i < s.length(); i++) {
        int ch = s.charAt(i);
        if (ch <= 127)
          break;
        int highByte = ch >>> 8;
        int lowByte = ch & 0xFF;
        mI18nSafeBuffer.append("\\u");
        mI18nSafeBuffer.append(hexHigh[highByte]);
        mI18nSafeBuffer.append(hexLow[highByte]);
        mI18nSafeBuffer.append(hexHigh[lowByte]);
        mI18nSafeBuffer.append(hexLow[lowByte]);
      }
      j = i;
    }
  }

  private void verbose(String msg) {
    if (mVerbose) System.out.println(msg);
  }

  // ========================================================================
  // main() - quick test

  public static void main(String[] args) throws IOException {
    SourceJavaOutputStream sjos = new SourceJavaOutputStream
             (new WriterFactory() {
               private PrintWriter OUT = new PrintWriter(System.out);

               public Writer createWriter(String x, String y) {
                 return OUT;
               }
             });
     JavaOutputStream joust = new ValidatingJavaOutputStream(sjos);
    ExpressionFactory exp = joust.getExpressionFactory();
    joust.startFile("foo.bar.baz","MyClass");
    Annotation author = joust.createAnnotation("author");
    author.setValue("name","Patrick Calahan");
    joust.writeComment("Test class");
    joust.writeAnnotation(author);


    joust.startClass(Modifier.PUBLIC | Modifier.FINAL,"MyBaseClass", null);
    String[] paramTypes = {"int", "List"};
    String[] paramNames = {"count", "fooList"};
    String[] exceptions = {"IOException"};
    Annotation deprecated = joust.createAnnotation("deprecated");
    deprecated.setValue("value",true);
    Variable counter =
            joust.writeField(Modifier.PRIVATE, "int", "counter", exp.createInt(99));
    joust.writeComment("This is the constructor comment");
    joust.writeComment("And here is another.\n\n  ok?");
    joust.writeAnnotation(deprecated);
    Variable[] params = joust.startConstructor
            (Modifier.PUBLIC, paramTypes, paramNames, exceptions);
    joust.writeAssignmentStatement(counter, params[0]);
    joust.endMethodOrConstructor();
    joust.endClassOrInterface();
    joust.endFile();
    joust.close();
  }

}