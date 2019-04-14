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

import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.mutable.MInvokable;
import org.apache.xmlbeans.impl.jam.mutable.MMethod;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamClassPopulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ReflectClassBuilder extends JamClassBuilder implements JamClassPopulator {

  // ========================================================================
  // Variables

  private ClassLoader mLoader;
  private ReflectTigerDelegate mTigerDelegate = null;

  // ========================================================================
  // Constructors

  public ReflectClassBuilder(ClassLoader rcl) {
    if (rcl == null) throw new IllegalArgumentException("null rcl");
    mLoader = rcl;
  }

  // ========================================================================
  // JamClassBuilder implementation

  public void init(ElementContext ctx) {
    super.init(ctx);
    initDelegate(ctx);
  }

  public MClass build(String packageName, String className) {
    assertInitialized();
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
    assertInitialized();
    Class src = (Class)dest.getArtifact();
    dest.setModifiers(src.getModifiers());
    dest.setIsInterface(src.isInterface());
    if (mTigerDelegate != null) dest.setIsEnumType(mTigerDelegate.isEnum(src));
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

    if (mTigerDelegate != null) mTigerDelegate.populateAnnotationTypeIfNecessary(src,dest,this);

    // add the constructors
    Constructor[] ctors = src.getDeclaredConstructors();
    for(int i=0; i<ctors.length; i++) populate(dest.addNewConstructor(),ctors[i]);
    // add the annotations
    if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(dest,src);


    // add any inner classes
    Class[] inners = src.getDeclaredClasses();
    if (inners != null) {
      for(int i=0; i<inners.length; i++) {
        if (mTigerDelegate != null) {
          // skip anonymous classes
          if ((mTigerDelegate.getEnclosingConstructor(inners[i]) != null) ||
            (mTigerDelegate.getEnclosingMethod(inners[i]) != null)) continue;
        }
        String simpleName = inners[i].getName();
        int lastDollar = simpleName.lastIndexOf('$');
        simpleName = simpleName.substring(lastDollar+1);
        { //skip member anons
          char first = simpleName.charAt(0);
          if ( ('0' <= first) && (first <= '9')) {
            continue;
          }
        }
        MClass inner = dest.addNewInnerClass(simpleName);
        inner.setArtifact(inners[i]);
        populate(inner);
      }
    }
  }


  // ========================================================================
  // Private methods

  private void initDelegate(ElementContext ctx) {
    // ok, if we could load that, let's new up the extractor delegate
    mTigerDelegate = ReflectTigerDelegate.create(ctx);
  }

  private void populate(MField dest, Field src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.getName());
    dest.setType(src.getType().getName());
    dest.setModifiers(src.getModifiers());
    if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(dest,src);
  }

  private void populate(MConstructor dest, Constructor src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    for(int i=0; i<paramTypes.length; i++) {
      MParameter p = addParameter(dest, i, paramTypes[i]);
      if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(p,src,i);
    }
    if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(dest,src);
  }

  private void populate(MMethod dest, Method src) {
    dest.setArtifact(src);
    dest.setSimpleName(src.getName());
    dest.setModifiers(src.getModifiers());
    dest.setReturnType(src.getReturnType().getName());
    Class[] exceptions = src.getExceptionTypes();
    addThrows(dest,exceptions);
    Class[] paramTypes = src.getParameterTypes();
    for(int i=0; i<paramTypes.length; i++) {
      MParameter p = addParameter(dest, i, paramTypes[i]);
      if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(p,src,i);
    }
    if (mTigerDelegate != null) mTigerDelegate.extractAnnotations(dest,src);
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
}
