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
package org.apache.xmlbeans.impl.jam.internal.reflect;

import org.apache.xmlbeans.impl.jam.mutable.*;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.internal.JamServiceContextImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ReflectClassBuilder extends JamClassBuilder {

  // ========================================================================
  // Constants

  private static final String JAVA15_EXTRACTOR =
    "org.apache.xmlbeans.impl.jam.internal.java15.Reflect15AnnotationExtractor";

  // ========================================================================
  // Public static utilities

  public static JamClassBuilder getSystemClassBuilder(JamServiceContext ctx) {
    return new ReflectClassBuilder(ClassLoader.getSystemClassLoader(),ctx);
  }

  // ========================================================================
  // Variables

  private ClassLoader mLoader;
  private ReflectAnnotationExtractor mExtractor = null;
  private JamLogger mLogger = null;

  // ========================================================================
  // Constructors

  public ReflectClassBuilder(ClassLoader rcl, JamServiceContext ctx) {
    if (rcl == null) throw new IllegalArgumentException("null rcl");
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    mLogger = ctx;
    mLoader = rcl;
    try {
      mExtractor = (ReflectAnnotationExtractor)
        Class.forName(JAVA15_EXTRACTOR).newInstance();
    } catch (ClassNotFoundException e) {
      mLogger.error(e);
    } catch (IllegalAccessException e) {
      mLogger.verbose(e);
    } catch (InstantiationException e) {
      mLogger.verbose(e);
    }
  }

  // ========================================================================
  // JamClassBuilder implementation

  public MClass build(String packageName, String className)
  {
    Class rclass;
    try {
      rclass = mLoader.loadClass(packageName+"."+className);
    } catch(ClassNotFoundException cnfe) {
//      getLogger().debug(cnfe);
      return null;
    }
    MClass out = createClassToBuild(packageName, className, null);
    populate(out,rclass);
    return out;
  }

  // ========================================================================
  // Private methods

  private void populate(MClass dest, Class src) {
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
    if (mExtractor != null) mExtractor.extractAnnotations(dest,src);
  }

  private void populate(MField dest, Field src) {
    dest.setSimpleName(src.getName());
    dest.setType(src.getType().getName());
    dest.setModifiers(src.getModifiers());
    if (mExtractor != null) mExtractor.extractAnnotations(dest,src);
  }

  private void populate(MConstructor dest, Constructor src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    for(int i=0; i<paramTypes.length; i++) {
      MParameter p = addParameter(dest, i, paramTypes[i]);
      if (mExtractor != null) mExtractor.extractAnnotations(p,src,i);
    }
    if (mExtractor != null) mExtractor.extractAnnotations(dest,src);
  }

  private void populate(MMethod dest, Method src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    dest.setReturnType(src.getReturnType().getName());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    for(int i=0; i<paramTypes.length; i++) {
      MParameter p = addParameter(dest, i, paramTypes[i]);
      if (mExtractor != null) mExtractor.extractAnnotations(p,src,i);
    }
    if (mExtractor != null) mExtractor.extractAnnotations(dest,src);
  }

  private void addThrows(MInvokable dest, Class[] exceptionTypes) {
    for(int i=0; i<exceptionTypes.length; i++) {
      dest.addException(exceptionTypes[i].getName());
    }
  }

  private MParameter addParameter(MInvokable dest,
                                  int paramNum,
                                  Class paramType)
  {
    MParameter p = dest.addNewParameter();
    p.setSimpleName("param"+paramNum);
    p.setType(paramType.getName());
    return p;
  }




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
