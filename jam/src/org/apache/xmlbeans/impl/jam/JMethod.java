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

package org.apache.xmlbeans.impl.jam;

/**
 * Represents a method of a java class.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JMethod extends JInvokable {

  /**
   * Returns a JClass object representing the type of this methods
   * return value.  Note that void methods will return a JClass
   * representing void.</p>
   */
  public JClass getReturnType();

  /**
   * Return true if this method is declared final.
   */
  public boolean isFinal();

  /**
   * Return true if this method is static.
   */
  public boolean isStatic();

  /**
   * Return true if this member is final.  Note that constructors are
   * fields are never abstract.
   */
  public boolean isAbstract();

  /**
   * Returns true if this method is declared native.
   */
  public boolean isNative();

  /**
   * Returns true if this method is declared synchronized.
   */
  public boolean isSynchronized();

  /**
   * <p>Returns a qualied name for this method as specified by
   * <code>java.lang.reflect.Method.toString()</code>:</p>
   *
   * <p><i>
   * Returns a string describing this Method. The string is formatted as
   * the method access modifiers, if any, followed by the method return
   * type, followed by a space, followed by the class declaring the method,
   * followed by a period, followed by the method name, followed by a
   * parenthesized, comma-separated list of the method's formal parameter
   * types. If the method throws checked exceptions, the parameter list is
   * followed by a space, followed by the word throws followed by a
   * comma-separated list of the thrown exception types. For example:</i></p>
   *
   * <p><i>public boolean java.lang.Object.equals(java.lang.Object)</i></p>
   *
   * <p><i>The access modifiers are placed in canonical order as specified by
   * "The Java Language Specification". This is public, protected or private
   * first, and then other modifiers in the following order: abstract,
   * static, final, synchronized native.</i></p>
   */
  public String getQualifiedName();


  /**
   * Returns the name of this class in the format described in section
   * 4.3.3 of the VM spec, 'Class File Format: Method Descriptors.'
   * This is the nasty format of the name returned by
   * java.lang.reflect.Method.getName().  For details, see
   * http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html
   */
  //  public String getMethodDescriptor();   dunno if this is useful
}
