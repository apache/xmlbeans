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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JServiceFactory;
import org.apache.xmlbeans.impl.jam.JService;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JServiceParams;
import org.apache.xmlbeans.impl.jam.internal.PrimitiveJClass;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlRuntimeException;

import java.io.StringWriter;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 * Represents a Java class name, and provides some utility methods
 * for parsing out bits of the name.
 *
 * Examples:
 *
 * "int[]"
 * "java.lang.String"
 * "com.myco.MyClass$MyInnerClass$InnerInnerClass[][]"
 *
 * The classname is preceded by an "x=" if it is an XmlObject
 * subclass, i.e., if it's an XmlBeans-style class, for example:
 *
 * "x=org.apache.xmlbeans.XmlInt"
 */
public final class JavaTypeName {

  // ========================================================================
  // Constants

  private static String XMLOBJECT_CLASSNAME = XmlObject.class.getName();
  private static String[] PRIMITIVE_TYPES =
  {"int", "boolean", "float", "long", "double", "short", "char"};
  private static String[] BOXED_TYPES =
  {"java.lang.Integer", "java.lang.Boolean", "java.lang.Float",
   "java.lang.Long", "java.lang.Double", "java.lang.Short",
   "java.lang.Character"};

  // ========================================================================
  // Variables

  private final String className;
  private final String arrayString;
  private final boolean isXmlObject;

  // ========================================================================
  // Factories

  /**
   * Returns a JavaTypeName object for a fully-qualified class name.
   * The class-name should be dot-separated for packages and
   * dollar-separated for inner classes.
   *
   * The names "int", "byte" etc are considered special.
   * Arrays are dealt with as [][][] at the end.
   *
   * This is a static function to permit pooling in the future.
   */
  public static JavaTypeName forString(String className) {
    return new JavaTypeName(className);
  }

    /**
     * Builds a JavaTypeName for a a java type name
     * (in the format returned by Class.getName())
     */
    public static JavaTypeName forClassName(String type)
    {
        final JServiceFactory jserv_factory = JServiceFactory.getInstance();
        final JServiceParams params = jserv_factory.createServiceParams();
        final JService service;
        try {
            service = jserv_factory.createService(params);
        }
        catch (IOException e) {
            throw new XmlRuntimeException(e);
        }
        final JClassLoader jcl = service.getClassLoader();
        final JClass jc = jcl.loadClass(type);
        return forJClass(jc);
    }


  /**
   * Builds a JavaTypeName for the array containing items with
   * the given JavaTypeName.
   */
  public static JavaTypeName forArray(JavaTypeName itemType, int depth) {
    // efficiency later
    String arrayBrackets = "";
    while (depth-- > 0)
      arrayBrackets += "[]";
    return forString(itemType.toString() + arrayBrackets);
  }


  /**
   * Builds a JavaTypeName for the boxed type corresponding to the
   * given JavaTypeName.
   */
  public static JavaTypeName forBoxed(JavaTypeName type) {
    // We could use a map here and initialize it on first use
    for (int i = 0; i < PRIMITIVE_TYPES.length; i++)
      if (PRIMITIVE_TYPES[i].equals(type.toString()))
        return forString(BOXED_TYPES[i]);
    return null;
  }


  /**
   * Builds a JavaTypeName for the given JClass
   */
  public static JavaTypeName forJClass(JClass jClass) {
    if (jClass.isArray()) {
      return forArray(forJClass(jClass.getArrayComponentType()), jClass.getArrayDimensions());
    }

    JClass[] interfaces = jClass.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if (interfaces[i].getQualifiedName().equals(XMLOBJECT_CLASSNAME))
        return forString("x=" + jClass.getQualifiedName());
    }

    return forString(jClass.getQualifiedName());
  }

  // ========================================================================
  // Constructors

  /**
   * Do not use this constructor; use forClassName instead.
   */
  private JavaTypeName(String className) {
    if (className == null)
      throw new IllegalArgumentException();

    if (className.startsWith("x=")) {
      this.isXmlObject = true;
      className = className.substring(2);
    } else {
      this.isXmlObject = false;
    }

    int arrayDepth = 0;
    for (int i = className.length() - 2; i >= 0; i -= 2) {
      if (className.charAt(i) != '[' || className.charAt(i + 1) != ']')
        break;
      arrayDepth += 1;
    }
    this.className = className.substring(0, className.length() - 2 * arrayDepth);
    this.arrayString = className.substring(className.length() - 2 * arrayDepth);
  }

  // ========================================================================
  // Public methods

  /**
   * True for classnames that are XmlObjects.
   */
  public boolean isXmlObject() {
    return isXmlObject;
  }

  /**
   * Returns the array depth, 0 for non-arrays, 1 for [], 2 for [][], etc.
   */
  public int getArrayDepth() {
    return arrayString.length() / 2;
  }

  /**
   * Returns the array item type (peeling off "n" array indexes)
   */
  public JavaTypeName getArrayItemType(int depth) {
    if (arrayString.length() < depth * 2)
      return null;
    return forString(className + arrayString.substring(0, arrayString.length() - 2 * depth));
  }

  /**
   * Returns the dot-separated package name.
   */
  public String getPackage() {
    int index = className.lastIndexOf('.');
    if (index <= 0)
      return "";
    return className.substring(0, index);
  }

  /**
   * True if this is an inner class name.
   */
  public boolean isInnerClass() {
    return (className.lastIndexOf('$') >= 0);
  }

  /**
   * Returns the JavaTypeName of the containing class, or null if this
   * is not an inner class name.
   */
  public JavaTypeName getContainingClass() {
    int index = className.lastIndexOf('$');
    if (index < 0)
      return null;
    return JavaTypeName.forString(className.substring(0, index));
  }

  /**
   * Returns the short class name (i.e., no dots or dollars).
   */
  public String getShortClassName() {
    int index = className.lastIndexOf('$');
    int index2 = className.lastIndexOf('.');
    if (index2 > index)
      index = index2;
    if (index < 0)
      return className;
    return className.substring(index + 1);
  }

  /**
   * Loads the class represented by this JavaTypeName in the given
   * ClassLoader.  This is really horrible - the impedance mismatch
   * in the naming here is very really painful.  Need to do something better.
   */
  public Class loadClassIn(ClassLoader loader) throws ClassNotFoundException {
    int d = getArrayDepth();
    if (d == 0) {
      String s = toString();
      Class out = PrimitiveJClass.getPrimitiveClass(s);
      if (out != null) return out;
      return loader.loadClass(s);
    } else {
      Class clazz = PrimitiveJClass.getPrimitiveClass(className);
      if (clazz == null) clazz = loader.loadClass(className);
      int[] dimensions = new int[d];
      return Array.newInstance(clazz, dimensions).getClass();

      /* THIS IS COMMENTED OUT BECAUSE IT IS BROKEN ON THE CURRENT 1.5 BETA
         SEE SUN BUG 4983838.  IT'S NOT CLEAR WHETHER OR NOT THEY'RE GOING
         TO FIX IT.  MAYBE THE WORKAROUND IS BETTER ANYWAY, THOUGH :(
      StringWriter buff = new StringWriter();
      for(int i=0; i<d; i++) buff.write("[");
      String s = toString();
      s = s.substring(0,s.indexOf("["));
      String fd = PrimitiveJClass.getFieldDescriptor(s);
      if (fd != null) {
      buff.write(fd);
      } else {
      buff.write("L");
      buff.write(s);
      buff.write(";");
      }
      return loader.loadClass(buff.toString());
      */
    }
  }

  // ========================================================================
  // Object implementation

  /**
   * Returns the fully-qualified class name.
   */
  public String toString() {
    if (isXmlObject)
      return "x=" + className + arrayString;
    return className + arrayString;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JavaTypeName)) return false;

    final JavaTypeName javaName = (JavaTypeName) o;

    if (isXmlObject != javaName.isXmlObject) return false;
    if (!className.equals(javaName.className)) return false;
    if (!arrayString.equals(javaName.arrayString)) return false;

    return true;
  }

  public int hashCode() {
    return className.hashCode() + arrayString.length() + (isXmlObject ? 1 : 0);
  }

}
