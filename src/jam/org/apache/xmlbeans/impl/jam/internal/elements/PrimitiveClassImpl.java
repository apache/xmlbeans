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

package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.JClass;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>JClass implementation for primitive types.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public final class PrimitiveClassImpl extends BuiltinClassImpl {

  // ========================================================================
  // Constants

  private static final Object[][] PRIMITIVES = {
    // Name       FD   Class
    {"int",       "I", int.class},
    {"long",      "J", long.class},
    {"boolean",   "Z", boolean.class},
    {"short",     "S", short.class},
    {"byte",      "B", byte.class},
    {"char",      "C", char.class},
    {"float",     "F", float.class},
    {"double",    "D", double.class},
  };

  private static final Map NAME_TO_FD, NAME_TO_CLASS;

  static {
    NAME_TO_FD = new HashMap();
    NAME_TO_CLASS = new HashMap();
    for (int i = 0; i < PRIMITIVES.length; i++) {
      NAME_TO_FD.put(PRIMITIVES[i][0],PRIMITIVES[i][1]);
      NAME_TO_CLASS.put(PRIMITIVES[i][0],PRIMITIVES[i][2]);
    }
  };

  // ========================================================================
  // Factory methods

  public static void mapNameToPrimitive(ElementContext ctx, Map out) {
    for(int i=0; i<PrimitiveClassImpl.PRIMITIVES.length; i++) {
      JClass c = new PrimitiveClassImpl(ctx,(String)PRIMITIVES[i][0]);
      out.put(PrimitiveClassImpl.PRIMITIVES[i][0],c);
      out.put(PrimitiveClassImpl.PRIMITIVES[i][1],c);
      // REVIEW we map both the name and the fd to the class.  does that
      // seem ok?
    }
  }

  /**
   * Returns the field descriptor for an named primitive, e.g. 'I' for
   * 'int', or null if the parameter does not name a primitive.
   */
  public static String getPrimitiveClassForName(String named) {
    return (String)NAME_TO_FD.get(named);
  }

  /**
   * Returns a JClass representing the named primitive type.  The name
   * parameter can be a simple type name (e.g. 'int') or a field
   * descriptor (e.g. 'I').  Returns null if the parameter does not
   * name a primitive type.
   */
/*  public static JClass getPrimitiveClassForName(String named) {
    JClass out = (JClass) NAME_TO_JCLASS.get(named);
    if (out != null) return out;
    return (JClass) FD_TO_JCLASS.get(named);
  }*/

  /**
   * Returns a JClass representing the given primitive Class.  Returns
   * null if the parameter is not a primitive class.
   */
  /*public static JClass getPrimitiveClass(Class clazz) {
    return getPrimitiveClassForName(clazz.getName());
  }*/

  // ========================================================================
  // Public static utilities

  /**
   * Returns true if the named type is a primitive.  The parameter can
   * be a simple type name (e.g. 'int') or a field descriptor
   * (e.g. 'I').
   */
  public static boolean isPrimitive(String name) {
    return (NAME_TO_FD.get(name) != null);
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
  // Constructors

  private PrimitiveClassImpl(ElementContext ctx, String name) {
    super(ctx);
    if (name == null) throw new IllegalArgumentException("null name");
    if (!NAME_TO_FD.containsKey(name)) {
      throw new IllegalArgumentException("Unknown primitive class '"+
        name+"'");
    }
    reallySetSimpleName(name);
  }

  // ========================================================================
  // JElement implementation

  public String getQualifiedName() { return getSimpleName(); }

  // ========================================================================
  // JClass implementation

  public String getFieldDescriptor() {
    return (String)NAME_TO_FD.get(getSimpleName());
  }

  public boolean isAssignableFrom(JClass c) {
    return c.isPrimitiveType() && c.getSimpleName().equals(getSimpleName());
  }

  public boolean isPrimitiveType() { return true; }

  public Class getPrimitiveClass() {
    return (Class)NAME_TO_CLASS.get(getSimpleName());
  }
}
