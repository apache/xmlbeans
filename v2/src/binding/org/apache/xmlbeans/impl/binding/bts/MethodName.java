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

import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xml.xmlbeans.bindingConfig.JavaMethodName;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Abstraction which uniquely identifies a particular method on some class.
 * This requires both a method name and some information about the method
 * signature - this class just provides a structured representation of
 * that information, as well a service (getMethodOn) for reflecting
 * on a given class to find the java.lang.Method named by the MethodName.
 *
 * The basic motivation for adding this class is to remove guesswork from the
 * runtime about how to map a 'getterName' string plus some notion of
 * property type to an actual java.lang.Method.  With MethodName, the
 * binding file is able to make it completely unambiguous.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class MethodName {

  // ========================================================================
  // Variables

  private String mMethodName;
  private JavaTypeName[] mParamTypes;

  // ========================================================================
  // Factory methods

  /**
   * Creates a MethodName which names the given JMethod.
   */
  public static MethodName create(JMethod m) {
    JParameter[] params = m.getParameters();
    if (params == null || params.length == 0) {
      return new MethodName(m.getSimpleName());
    } else {
      JavaTypeName[] types = new JavaTypeName[params.length];
      for (int i = 0; i < types.length; i++) {
        types[i] = JavaTypeName.forJClass(params[i].getType());
      }
      return new MethodName(m.getSimpleName(), types);
    }
  }

  /**
   * Creates a MethodName for a method with the given name and no parameters
   * (e.g., a getter).
   */
  public static MethodName create(String methodName) {
    return new MethodName(methodName);
  }

  /**
   * Creates a MethodName for a method with the given name and a single
   * parameter of the given type (e.g., a setter).
   */
  public static MethodName create(String methodName, JavaTypeName paramType) {
    return create(methodName, new JavaTypeName[]{paramType});
  }

  /**
   * Creates a MethodName for a method with the given name and a set
   * of parameters of the given types.
   */
  public static MethodName create(String methodName, JavaTypeName[] paramTypes) {
    if (paramTypes == null || paramTypes.length == 0) {
      return new MethodName(methodName);
    } else {
      return new MethodName(methodName, paramTypes);
    }
  }

  /**
   * Creates a MethodName from the given XmlObject.
   */
  /*package*/
  static MethodName create(JavaMethodName jmn) {
    if (jmn == null) return null;
    
    return create(jmn.getMethodName(),
                  namesForStrings(jmn.getParamTypeArray()));
  }

  private static JavaTypeName[] namesForStrings(String[] names) {
    JavaTypeName[] out = new JavaTypeName[names.length];
    for (int i = 0; i < out.length; i++) out[i] = JavaTypeName.forString(names[i]);
    return out;
  }


  // ========================================================================
  // Constructors

  private MethodName(String methodName, JavaTypeName[] types) {
    mMethodName = methodName;
    mParamTypes = types;
  }

  private MethodName(String methodName) {
    mMethodName = methodName;
    mParamTypes = new JavaTypeName[0];
  }

  // ========================================================================
  // Public methods

  public String getSimpleName() {
    return mMethodName;
  }

  /**
   * Returns the java.lang.Method which is named by this MethodName
   * on the given class.
   *
   * @param containingClass Class to be searched for the method.
   * @return
   * @throws ClassNotFoundException if one of the paramType classes specified
   * for this MethodName cannot be loaded from the given class' classloader.
   * @throws NoSuchMethodException If the named method does not exist on
   * this class.
   * @throws IllegalArgumentException if containingClass is null.
   */
  public Method getMethodOn(Class containingClass)
          throws ClassNotFoundException, NoSuchMethodException {
    if (containingClass == null) {
      throw new IllegalArgumentException("null class");
    }
    Class[] types = null;
    if (mParamTypes != null && mParamTypes.length > 0) {
      types = new Class[mParamTypes.length];
      for (int i = 0; i < types.length; i++) {
        types[i] = mParamTypes[i].loadClassIn(containingClass.getClassLoader());
      }
    }
    return containingClass.getMethod(mMethodName, types);
  }

  // ========================================================================
  // Object implementation

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MethodName)) return false;
    final MethodName methodName = (MethodName) o;
    if (mMethodName != null ? !mMethodName.equals(methodName.mMethodName) :
            methodName.mMethodName != null)
      return false;
    if (!Arrays.equals(mParamTypes, methodName.mParamTypes)) return false;

    return true;
  }

  public int hashCode() {
    return (mMethodName != null ? mMethodName.hashCode() : 0);
  }


  // ========================================================================
  // Package methods

  /**
   * Populates the given xmlobject with our contents.
   */
  /*package*/
  void write(JavaMethodName name) {
    name.setMethodName(mMethodName);
    if (mParamTypes != null && mParamTypes.length > 0) {
      String[] types = new String[mParamTypes.length];
      for (int i = 0; i < types.length; i++) types[i] = mParamTypes[i].toString();
      name.setParamTypeArray(types);
    }
  }

  /* should make this a test
   public static void main(String[] args) throws Exception {
     test("java.lang.String",String.class);
     test("java.lang.String[]",String[].class);
     test("int",int.class);
     test("int[]",int[].class);
     test("double[][][]",double[][][].class);
   }

   public static void test(String jtn, Class c) throws Exception {
     JavaTypeName name = JavaTypeName.forString(jtn);
     if (!name.loadClassIn(ClassLoader.getSystemClassLoader()).equals(c)) {
       System.out.println(jtn+" failed "+c.getName());
     } else {
       System.out.println(jtn+" passed "+c.getName());
     }
   }
   */
}