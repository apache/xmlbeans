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
package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.provider.EClassBuilder;
import org.apache.xmlbeans.impl.jam.editable.*;
import org.apache.xmlbeans.impl.jam.editable.impl.EClassImpl;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.internal.JClassLoaderImpl;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ReflectionClassBuilder implements EClassBuilder {

  // ========================================================================
  // Constants

  private static final boolean REALLY_VERBOSE = false;

  // ========================================================================
  // Variables

  private ClassLoader mLoader;

  /**
   * @deprecated just to wean us off the R* impls.
   */
  public static JClassLoader createRClassLoader(ClassLoader cl) {
    return new JClassLoaderImpl(new ReflectionClassBuilder(cl),null);
  }

  public static EClassBuilder getSystemClassBuilder() {
    return new ReflectionClassBuilder(ClassLoader.getSystemClassLoader());
  }

  // ========================================================================
  // Constructors

  public ReflectionClassBuilder(ClassLoader rcl) {
    if (rcl == null) throw new IllegalArgumentException("null rcl");
    mLoader = rcl;
  }

  // ========================================================================
  // EClassBuilder implementation

  public EClass build(String packageName,
                      String className,
                      JClassLoader loader) {
    try {
      Class rclass = mLoader.loadClass(packageName+"."+className);
      EClass out = new EClassImpl(packageName, className, loader, null);
      populate(out,rclass);
      return out;
    } catch(ClassNotFoundException cnfe) {
      if (REALLY_VERBOSE) cnfe.printStackTrace();
      return null;
    }
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
  }

  private void populate(EConstructor dest, Constructor src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    Class[] exceptions = src.getExceptionTypes();
    Class[] params = src.getParameterTypes();
    populate(dest,exceptions,params);
  }

  private void populate(EMethod dest, Method src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    dest.setReturnType(src.getReturnType().getName());
    Class[] exceptions = src.getExceptionTypes();
    Class[] params = src.getParameterTypes();
    populate(dest,exceptions,params);
  }

  private void populate(EInvokable dest, Class[] exceptions, Class[] params) {
    for(int i=0; i<exceptions.length; i++) {
      dest.addException(exceptions[i].getName());
    }
    for(int i=0; i<params.length; i++) {
      EParameter p = dest.addNewParameter();
      p.setSimpleName("param"+i);
      p.setType(params[i].getName());
    }
  }

  private void add175Annotations(EElement dest, Object src) {
    /*Annotation[]*/Object[] anns = get175AnnotationsOn(src);
    if (anns == null || anns.length == 0) return;
    for(int i=0; i<anns.length; i++) {
      populate(dest.addNewAnnotation(),anns[i]);
    }
  }

  private void populate(EAnnotation dest, Object ann) {
    dest.setSimpleName(ann.getClass().getName()); //REVIEW
    dest.setAnnotationObject(ann);
    dest.setDefinition(ann.getClass().getName());
    populateAnnotationMembers(dest, ann, ann.getClass());
  }

  /**
   * Introspects the src object for annotation member methods, invokes them
   * and creates corresponding EAnnotationMembers in the given dest object.
   */
  private void populateAnnotationMembers(EAnnotation dest,
                                         Object src,
                                         Class srcClass)
  {
    Method[] methods = srcClass.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) {
      if (methods[i].getParameterTypes().length > 0) continue;
      EAnnotationMember member = dest.addNewMember();
      member.setSimpleName(methods[i].getName());
      try {
        member.setValue(methods[i].invoke(src,null));
      } catch(IllegalAccessException iae) {
        iae.printStackTrace(); // this is not expected
      } catch(InvocationTargetException ite) {
        ite.printStackTrace();
      }
    }
    //REVIEW will it be a superclass or an interface?  this might be broken
    srcClass = srcClass.getSuperclass();
    if (srcClass != null &&
            !srcClass.getName().equals("java.lang.annotation.Annotation") &&
            !srcClass.getName().equals("java.lang.Object")) {
      populateAnnotationMembers(dest,src,srcClass);
    }
  }

  //FIXME this needs to support annotation inheritance
  /**
   * <p>Utility method for returning the java.lang.annoatation.Annotations
   * associated with a given reflection object.  This method accesses the
   * annotations via reflection so that the code will still compile
   * and run under 1.4.</p>
   */
  private Object[] get175AnnotationsOn(Object reflectionThing) {
    Method annGetter;
    try {
      annGetter = reflectionThing.getClass().getMethod("getDeclaredAnnotations",
                                             null);
      return (Object[])annGetter.invoke(reflectionThing,null);
    } catch(NoSuchMethodException nsme) {
      if (REALLY_VERBOSE) nsme.printStackTrace();
    } catch(IllegalAccessException iae) {
      iae.printStackTrace(); // this is not expected
    } catch(InvocationTargetException ite) {
      ite.printStackTrace();
    }
    return null;
  }

  private static String simpleName(Class clazz) {
    String out = clazz.getName();
    int dot = out.lastIndexOf('.');
    if (dot != -1) out = out.substring(dot+1);
    return out;
  }


//salvaged from RClassLoader, may be useful for parser
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
}
