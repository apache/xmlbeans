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


import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.editable.EAnnotation;
import org.apache.xmlbeans.impl.jam.editable.EAnnotationMember;
import org.apache.xmlbeans.impl.jam.editable.impl.EAnnotationImpl;
import org.apache.xmlbeans.impl.jam.internal.*;

/**
 * java.lang.ClassLoader-backed implementation of JClassLoader.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class RClassLoader implements JClassLoader {

  // ========================================================================
  // Constants

  private static final boolean REALLY_VERBOSE = false;

  // ========================================================================
  // Variables

  private ClassLoader mLoader;
  private Map mFd2Class = new HashMap(), mName2Package = new HashMap();
  private JClassLoader mParentLoader;

  // ========================================================================
  // Constructors

  public RClassLoader(ClassLoader c) {
    this(c,null);
  }

  public RClassLoader(ClassLoader c, JClassLoader parent) {
    if (c == null) throw new IllegalArgumentException("null classloader");
    mLoader = c;
    mParentLoader = parent; //this can be null
  }

  // ========================================================================
  // JClassLoader implementation

  public JClassLoader getParent() { return mParentLoader; }

  /**
   *
   */
  public JAnnotationLoader getAnnotationLoader() { return null; }//FIXME

  /**
   * Returns a reflect representation of the named class.
   */
  public JClass loadClass(String fd) {
    if (fd == null) throw new IllegalArgumentException("null fd");
    //
    //FIXME we should do some more work here to make sure that
    fd = fd.trim();
    // check cache first
    JClass out = (JClass)mFd2Class.get(fd);
    if (out != null) return out;
    // is it an array?
    if (fd.startsWith("[")) {
      mFd2Class.put(fd,out=ArrayJClass.createClassFor(fd,this));
      return out;
    }
    // see if it's a primitive.  REVIEW i think this check should only
    // happen if we're the system RClassLoader.
    out = PrimitiveJClass.getPrimitiveClassForName(fd);
    if (out != null) {
      mFd2Class.put(fd,out);
      return out;
    }
    // see if it's void.  REVIEW i think this check should only happen
    // if we're the system RClassLoader.
    if (VoidJClass.isVoid(fd)) {
      mFd2Class.put(fd,out = VoidJClass.getInstance());
      return out;
    }
    // still no dice.  ok, try to load it
    try {
      mFd2Class.put(fd,out=new RClass(mLoader.loadClass(fd),this));
      return out;
    } catch(ClassNotFoundException ignore) {}
    // doh.  ok, hand off to our parent.  if we don't have one, we're
    // done - it's unresolved.
    if (mParentLoader == null) {
      mFd2Class.put(fd,out = new UnresolvedJClass(fd));
      return out;
    } else {
      return mParentLoader.loadClass(fd);
    }
  }

  public JPackage getPackage(String named) {
    if (named == null) throw new IllegalArgumentException("null name");
    named = named.trim();
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      mName2Package.put(named,out = new JPackageImpl(named));
    }
    return out;
  }

  // ========================================================================
  // Package methods

  // this is just a choke point, in case for some reason this logic
  // ever needs to be modified.
  /*package*/ static JClass getClassFor(Class clazz, JClassLoader loader) {
    return loader.loadClass(clazz.getName());
  }


  // ========================================================================
  // Private methods

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

  // ========================================================================
  // New 175 stuff

  //FIXME this needs to support annotation inheritance
  /**
   * <p>Utility method for creating representations of 175 annotations
   * from 1.5 java.lang.reflect constructs.  This method accesses the
   * annotations via reflection so that the code will still compile
   * and run under 1.4.</p>
   */
  /*package*/ EAnnotation[] get175AnnotationsOn(Object reflectionThing,
                                                JClassLoader loader) {
    Method annGetter;
    try {
      annGetter = reflectionThing.getClass().getMethod("getDeclaredAnnotations",
                                             null);
      Object[] anns = (Object[])annGetter.invoke(reflectionThing,null);
      EAnnotation[] out = new EAnnotation[anns.length];
      for(int i=0; i<anns.length; i++) {
        out[i]  = new EAnnotationImpl(simpleName(anns[i].getClass()),
                                      loader);
        populateAnnotation(out[i],anns[i]);
      }
      return out;
    } catch(NoSuchMethodException nsme) {
      if (REALLY_VERBOSE) nsme.printStackTrace();
    } catch(IllegalAccessException iae) {
      iae.printStackTrace(); // this is not expected
    } catch(InvocationTargetException ite) {
      ite.printStackTrace();
    }
    return new EAnnotation[0];
  }

  private static String simpleName(Class clazz) {
    String out = clazz.getName();
    int dot = out.lastIndexOf('.');
    if (dot != -1) out = out.substring(dot+1);
    return out;
  }

  /**
   * @param dest Annotation object to be populated
   * @param src java.lang.annotation.Annotation instance containing the
   * annotation data we want to wrap in EAnnotation.
   */
  private void populateAnnotation(EAnnotation dest, Object src) {
    dest.setAnnotationObject(src);
    populateAnnotation(dest,src,src.getClass());
  }

  /**
   * Introspects the src object for annotation member methods, invokes them
   * and creates corresponding EAnnotationMembers in the given dest object.
   */
  private void populateAnnotation(EAnnotation dest, Object src, Class srcClass) {
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
      populateAnnotation(dest,src,srcClass);
    }
  }
}