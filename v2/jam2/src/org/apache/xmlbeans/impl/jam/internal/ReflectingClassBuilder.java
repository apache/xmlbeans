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

import org.apache.xmlbeans.impl.jam.editable.*;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ReflectingClassBuilder extends JamClassBuilder {

  // ========================================================================
  // Public static utilities

  public static JamClassBuilder getSystemClassBuilder() {
    return new ReflectingClassBuilder(ClassLoader.getSystemClassLoader());
  }

  // ========================================================================
  // Variables

  private ClassLoader mLoader;

  // ========================================================================
  // Constructors

  public ReflectingClassBuilder(ClassLoader rcl) {
    if (rcl == null) throw new IllegalArgumentException("null rcl");
    mLoader = rcl;
  }

  // ========================================================================
  // JamClassBuilder implementation

  public EClass build(String packageName, String className)
  {
    Class rclass;
    try {
      rclass = mLoader.loadClass(packageName+"."+className);
    } catch(ClassNotFoundException cnfe) {
      getLogger().debug(cnfe);
      return null;
    }
    EClass out = createClass(packageName, className, null);
    populate(out,rclass);
    return out;
  }

  // ========================================================================
  // Private methods

  private void populate(EClass dest, Class src) {
    dest.setModifiers(src.getModifiers());
    dest.setIsInterface(src.isInterface());
    // set the superclass
    Class s = src.getSuperclass();
    if (s != null) dest.setSuperclass(s.getName());
    // set the interfaces
    Class[] ints = src.getInterfaces();
    for(int i=0; i<ints.length; i++) dest.addInterface(ints[i].getName());
    // add the fields
    Field[] fields = src.getFields();
    for(int i=0; i<fields.length; i++) populate(dest.addNewField(),fields[i]);
    // add the methods
    Method[] methods = src.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) populate(dest.addNewMethod(),methods[i]);
    // add the constructors
    Constructor[] ctors = src.getDeclaredConstructors();
    for(int i=0; i<ctors.length; i++) populate(dest.addNewConstructor(),ctors[i]);
    // add the annotations
    add175Annotations(dest, src);
  }

  private void populate(EField dest, Field src) {
    dest.setSimpleName(src.getName());
    dest.setType(src.getType().getName());
    dest.setModifiers(src.getModifiers());
    add175Annotations(dest, src);
  }

  private void populate(EConstructor dest, Constructor src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    /*Annotation[]*/ Object[][] paramAnns = get175ParameterAnnotationsOn(src);
    for(int i=0; i<paramTypes.length; i++) {
      addParameter(dest, i, paramTypes[i],
        (paramAnns == null) ? null : paramAnns[i]);
    }
    add175Annotations(dest, src);
  }

  private void populate(EMethod dest, Method src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    dest.setReturnType(src.getReturnType().getName());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    /*Annotation[]*/ Object[][] paramAnns = get175ParameterAnnotationsOn(src);
    for(int i=0; i<paramTypes.length; i++) {
      addParameter(dest, i, paramTypes[i],
        (paramAnns == null) ? null : paramAnns[i]);
    }
    add175Annotations(dest, src);
  }

  private void addThrows(EInvokable dest, Class[] exceptionTypes) {
    for(int i=0; i<exceptionTypes.length; i++) {
      dest.addException(exceptionTypes[i].getName());
    }
  }

  private void addParameter(EInvokable dest,
                            int paramNum,
                            Class paramType,
                            /*Annotation*/ Object[] param175Anns) {
    EParameter p = dest.addNewParameter();
    p.setSimpleName("param"+paramNum);
    p.setType(paramType.getName());
    if (param175Anns != null) { //add the 175 annotations to the parameter
      for(int i=0; i<param175Anns.length; i++) {
        dest.addAnnotationForInstance(param175Anns[i]);
      }
    }
  }

  private void add175Annotations(EAnnotatedElement dest, Object src) {
    /*Annotation[]*/ Object[] anns = get175AnnotationsOn(src);
    if (anns == null || anns.length == 0) return;
    for(int i=0; i<anns.length; i++) {
      dest.addAnnotationForInstance(src);
    }
  }



  /**
   * <p>Utility method for returning the java.lang.annoatation.Annotations
   * associated with a given reflection object.  This method accesses the
   * Annotations via reflection so that JAM will still compile and load
   * under 1.4.</p>
   */
  private /*Annotation[]*/ Object[] get175AnnotationsOn(Object thing) {
    Method annGetter;
    try {
      annGetter = thing.getClass().
        getMethod("getDeclaredAnnotations", null);
      return (Object[])annGetter.invoke(thing,null);
    } catch(NoSuchMethodException nsme) {
      getLogger().debug(nsme);
    } catch(IllegalAccessException iae) {
      getLogger().debug(iae);
    } catch(InvocationTargetException ite) {
      getLogger().debug(ite);
    }
    return null;
  }

  /**
   * <p>Utility method for returning the annotations associated with
   * the parameters of a java.lang.Constructor or java.lang.Method.
   * This method accesses the Annotations via reflection so that JAM will
   * still compile and load under 1.4.</p>
   *
   * <p>I really don't understand why reflection has not introduced
   * an abstraction for method parameters.  This has always been a really
   * gross part of the reflection API and it's gotten even grosser with
   * JSR175.  JAM to the rescue!</p>
   *
   */
  private /*Annotation[][]*/ Object[][] get175ParameterAnnotationsOn(Object thing)
  {
    Method annGetter;
    try {
      annGetter = thing.getClass().
        getMethod("getParameterAnnotations", null);
      return (Object[][])annGetter.invoke(thing,null);
    } catch(NoSuchMethodException nsme) {
      getLogger().debug(nsme);
    } catch(IllegalAccessException iae) {
      getLogger().debug(iae);
    } catch(InvocationTargetException ite) {
      getLogger().debug(ite);
    }
    return null;
  }


  /*
  private static String simpleName(Class clazz) {
    String out = clazz.getName();
    int dot = out.lastIndexOf('.');
    if (dot != -1) out = out.substring(dot+1);
    return out;
  }
  */


//salvaged from RClassLoader, may be useful for parser
  /*
  private void validateClassName(String className)
          throws IllegalArgumentException
  {
    if (!Character.isJavaIdentifierStart(className.charAt(0))) {
      throw new IllegalArgumentException
              ("Invalid first character in class name: "+className);
    }
    for(int i=1; i<className.length(); i++) {
      char c = className.charAt(i);
      if (c == '.') {
        if (className.charAt(i-1) == '.') {
          throw new IllegalArgumentException
                  ("'..' not allowed in class name: "+className);
        }
        if (i == className.length()-1) {
          throw new IllegalArgumentException
                  ("'.' not allowed at end of class name: "+className);
        }
      } else {
        if (!Character.isJavaIdentifierPart(c)) {
          throw new IllegalArgumentException
                  ("Illegal character '"+c+"' in class name: "+className);
        }
      }
    }
  }
  */
}
