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
import org.apache.xmlbeans.impl.jam.provider.JamClassPopulator;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.javadoc.ClassDoc;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ReflectClassBuilder extends JamClassBuilder implements JamClassPopulator {

  // ========================================================================
  // Constants

  private static final String JAVA15_EXTRACTOR =
    "org.apache.xmlbeans.impl.jam.internal.java15.Reflect15DelegateImpl";

  // ========================================================================
  // Public static utilities

  public static JamClassBuilder getSystemClassBuilder(JamServiceContext ctx) {
    return new ReflectClassBuilder(ClassLoader.getSystemClassLoader(),ctx);
  }

  // ========================================================================
  // Variables

  private ClassLoader mLoader;
  private Reflect15Delegate mDelegate = null;
  private JamLogger mLogger = null;

  // ========================================================================
  // Constructors

  public ReflectClassBuilder(ClassLoader rcl, JamServiceContext ctx) {
    if (rcl == null) throw new IllegalArgumentException("null rcl");
    if (ctx == null) throw new IllegalArgumentException("null ctx");
    mLogger = ctx;
    mLoader = rcl;
    try {
      // class for name this because it's 1.5 specific.  if it fails, we
      // don't want to use the extractor
      Class.forName("java.lang.annotation.Annotation");
    } catch (ClassNotFoundException e) {
      if (mLogger.isVerbose(this)) {
        mLogger.warning("You are running under a pre-1.5 JDK.  JSR175-style "+
                        "class annotations will not be understood");
        mLogger.verbose(e);
      }
      return;
    }
    // ok, if we could load that, let's new up the extractor delegate
    try {
      mDelegate = (Reflect15Delegate)
        Class.forName(JAVA15_EXTRACTOR).newInstance();
      mDelegate.init((ElementContext)ctx);
      // if this fails for any reason, things are in a bad state
    } catch (ClassNotFoundException e) {
      mLogger.error("Internal error, failed to instantiate "+
                    JAVA15_EXTRACTOR);
      mLogger.error(e);
    } catch (IllegalAccessException e) {
      mLogger.error("Internal error, failed to instantiate "+
                    JAVA15_EXTRACTOR);
      mLogger.error(e);
    } catch (InstantiationException e) {
      mLogger.error("Internal error, failed to instantiate "+
                    JAVA15_EXTRACTOR);
      mLogger.error(e);
    }
  }
  // ========================================================================
  // JamClassBuilder implementation

  public MClass build(String packageName, String className)
  {
    if (getLogger().isVerbose(this)) {
      getLogger().verbose("trying to build '"+packageName+"' '"+className+"'");
    }
    Class rclass;
    try {
      String loadme = (packageName.trim().length() > 0) ?
        (packageName + '.'  + className) :
        className;
      rclass = mLoader.loadClass(loadme);
    } catch(ClassNotFoundException cnfe) {
      getLogger().verbose(cnfe,this);
      return null;
    }
    MClass out = createClassToBuild(packageName, className, null, this);
    out.setArtifact(rclass);
    return out;
  }

  // ========================================================================
  // JamClassPopulator implementation

  public void populate(MClass dest) {
    Class src = (Class)dest.getArtifact();
    dest.setModifiers(src.getModifiers());
    dest.setIsInterface(src.isInterface());
    if (mDelegate != null) dest.setIsEnumType(mDelegate.isEnum(src));
    // set the superclass
    Class s = src.getSuperclass();
    if (s != null) dest.setSuperclass(s.getName());
    // set the interfaces
    Class[] ints = src.getInterfaces();
    for(int i=0; i<ints.length; i++) dest.addInterface(ints[i].getName());
    // add the fields
    Field[] fields = null;
    try {
      fields = src.getFields();
    } catch(Exception ignore) {
      //FIXME there seems to be some JDK bugs here, workaround for now 180996
    }
    if (fields != null) {
      for(int i=0; i<fields.length; i++) populate(dest.addNewField(),fields[i]);
    }
    // add the methods
    Method[] methods = src.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) populate(dest.addNewMethod(),methods[i]);
    // add the constructors
    Constructor[] ctors = src.getDeclaredConstructors();
    for(int i=0; i<ctors.length; i++) populate(dest.addNewConstructor(),ctors[i]);
    // add the annotations
    if (mDelegate != null) mDelegate.extractAnnotations(dest,src);

    // add any inner classes
    Class[] inners = src.getDeclaredClasses();
    if (inners != null) {
      for(int i=0; i<inners.length; i++) {
        String simpleName = inners[i].getName();
        int lastDot = simpleName.lastIndexOf('.');
        simpleName = simpleName.substring(lastDot+1);
        MClass inner = dest.addNewInnerClass(simpleName);
        inner.setArtifact(inners[i]);
        populate(inner);
      }
    }
  }

  private void populate(MField dest, Field src) {
    dest.setSimpleName(src.getName());
    dest.setType(src.getType().getName());
    dest.setModifiers(src.getModifiers());
    if (mDelegate != null) mDelegate.extractAnnotations(dest,src);
  }

  private void populate(MConstructor dest, Constructor src) {
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    for(int i=0; i<paramTypes.length; i++) {
      MParameter p = addParameter(dest, i, paramTypes[i]);
      if (mDelegate != null) mDelegate.extractAnnotations(p,src,i);
    }
    if (mDelegate != null) mDelegate.extractAnnotations(dest,src);
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
      if (mDelegate != null) mDelegate.extractAnnotations(p,src,i);
    }
    if (mDelegate != null) mDelegate.extractAnnotations(dest,src);
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
