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

import java.util.HashMap;
import java.util.Map;
import org.apache.xmlbeans.impl.jam.JClass;

/**
 * JClass impl for primitive type singletons.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class PrimitiveJClass extends BuiltinJClass {

  // ========================================================================
  // Constants

  public static final Object[][] PRIMITIVES = {
    // Name       FD     Class
    {"int", "I", int.class},
    {"long", "J", long.class},
    {"boolean", "Z", boolean.class},
    {"short", "S", short.class},
    {"byte", "B", byte.class},
    {"char", "C", char.class},
    {"float", "F", float.class},
    {"double", "D", double.class},
  };

  private static final Map NAME_TO_CLASS, FD_TO_CLASS;

  static {
    NAME_TO_CLASS = new HashMap();
    FD_TO_CLASS = new HashMap();
    for (int i = 0; i < PRIMITIVES.length; i++) {
      PrimitiveJClass c = new PrimitiveJClass
              ((String) PRIMITIVES[i][0],
                      (String) PRIMITIVES[i][1],
                      (Class) PRIMITIVES[i][2]);
      NAME_TO_CLASS.put(c.getQualifiedName(), c);
      FD_TO_CLASS.put(c.getFieldDescriptor(), c);
    }
  };

  // ========================================================================
  // Factory methods

  /**
   * Returns a JClass representing the named primitive type.  The name
   * parameter can be a simple type name (e.g. 'int') or a field
   * descriptor (e.g. 'I').  Returns null if the parameter does not
   * name a primitive type.
   */
  public static JClass getPrimitiveClassForName(String named) {
    JClass out = (JClass) NAME_TO_CLASS.get(named);
    if (out != null) return out;
    return (JClass) FD_TO_CLASS.get(named);
  }

  /**
   * Returns a JClass representing the given primitive Class.  Returns
   * null if the parameter is not a primitive class.
   */
  public static JClass getPrimitiveClass(Class clazz) {
    return getPrimitiveClassForName(clazz.getName());
  }

  // ========================================================================
  // Static utilities

  /**
   * Returns true if the named type is a primitive.  The parameter can
   * be a simple type name (e.g. 'int') or a field descriptor
   * (e.g. 'I').
   */
  public static boolean isPrimitive(String name) {
    return (NAME_TO_CLASS.get(name) != null ||
            FD_TO_CLASS.get(name) != null);
  }


  // ========================================================================
  // Variables

  private String mName;
  private String mFD;
  private Class mClass;

  // ========================================================================
  // Constructors

  private PrimitiveJClass(String name, String fd, Class type) {
    mClass = type;
    mName = name;
    mFD = fd;
  }

  // ========================================================================
  // JElement implementation

  public String getSimpleName() {
    return mName;
  }

  public String getQualifiedName() {
    return mName;
  }

  // ========================================================================
  // JClass implementation

  public String getFieldDescriptor() {
    return mFD;
  }

  public boolean isAssignableFrom(JClass c) {
    return c.equals(this);
  }

  public boolean isPrimitive() {
    return true;
  }
}
