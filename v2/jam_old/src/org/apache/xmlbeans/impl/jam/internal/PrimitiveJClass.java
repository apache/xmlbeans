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

  private static final Map NAME_TO_JCLASS, FD_TO_JCLASS,
  NAME_TO_FD, NAME_TO_CLASS;

  static {
    NAME_TO_JCLASS = new HashMap();
    FD_TO_JCLASS = new HashMap();
    NAME_TO_FD = new HashMap();
    NAME_TO_CLASS = new HashMap();
    for (int i = 0; i < PRIMITIVES.length; i++) {
      PrimitiveJClass c = new PrimitiveJClass
              ((String) PRIMITIVES[i][0],
                      (String) PRIMITIVES[i][1],
                      (Class) PRIMITIVES[i][2]);
      NAME_TO_JCLASS.put(c.getQualifiedName(), c);
      FD_TO_JCLASS.put(c.getFieldDescriptor(), c);
      NAME_TO_FD.put(PRIMITIVES[i][0],PRIMITIVES[i][1]);
      NAME_TO_CLASS.put(PRIMITIVES[i][0],PRIMITIVES[i][2]);
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
    JClass out = (JClass) NAME_TO_JCLASS.get(named);
    if (out != null) return out;
    return (JClass) FD_TO_JCLASS.get(named);
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
    return (NAME_TO_JCLASS.get(name) != null ||
            FD_TO_JCLASS.get(name) != null);
  }

  /**
   * Returns the field descriptor for the given name, e.g. 'int' returns
   * 'I'.
   */
  public static final String getFieldDescriptor(String classname) {
    return (String)NAME_TO_FD.get(classname);
  }

  /**
   * Returns the primitve class for the given name, e.g. 'int' returns
   * int.class.  It's really stupid that there isn't a way to deal
   * with this built in to java.
   */
  public static final Class getPrimitiveClass(String classname) {
    return (Class)NAME_TO_CLASS.get(classname);
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

  public Class getPrimitiveClass() {
    return mClass;
  }
}
