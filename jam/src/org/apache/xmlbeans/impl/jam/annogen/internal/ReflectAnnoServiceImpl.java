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
package org.apache.xmlbeans.impl.jam.annogen.internal;

import org.apache.xmlbeans.impl.jam.annogen.ReflectAnnoService;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoType;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoProxy;
import org.apache.xmlbeans.impl.jam.annogen.provider.ElementId;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoProxySet;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoTypeRegistry;
import org.apache.xmlbeans.impl.jam.internal.reflect.Reflect15Delegate;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ReflectAnnoServiceImpl
    extends AnnoServiceBase implements ReflectAnnoService
 {

  // ========================================================================
  // Variables

  private ElementIdPool mIdPool = ElementIdPool.getInstance();

  private Reflect15Delegate mDelegate = null;

  private JamLogger mLogger;
  private AnnoTypeRegistry mRegistry;

  // ========================================================================
  // Constructors

  public ReflectAnnoServiceImpl(AnnoServiceParamsImpl asp) {
    super(asp);
    mLogger = asp.getLogger();
    mDelegate = Reflect15Delegate.Factory.create(mLogger);
    mRegistry = asp.getAnnoTypeRegistry();
  }

  // ========================================================================
  // AnnoServiceBase implementation of abstract methods

  protected void getIndigenousAnnotations(ElementId id, AnnoProxySet out) {
    if (mDelegate == null) return;
    Object[] raw = getRawAnnotations(id);
    for(int i=0; i<raw.length; i++) {
      Class declClass = mDelegate.getAnnotationClassFor(raw[i]);
      AnnoType at;
      try {
        at = mRegistry.getAnnoTypeForDeclaredType(declClass);
      } catch(ClassNotFoundException cnfe) {
        mLogger.error(cnfe);
        continue;
      }
      AnnoProxy proxy = out.findOrCreateProxyFor(declClass);
      copyValues(raw[i],proxy,at);
    }
  }
  // ========================================================================
  // ReflectAnnoService implementation

  public Object getAnnotation(Class requestedAnnoClass, Package pakkage) {
    return super.getAnnotation(requestedAnnoClass, mIdPool.getIdFor(pakkage));
  }


  public Object getAnnotation(Class annoClass, Class clazz) {
    return super.getAnnotation(annoClass, mIdPool.getIdFor(clazz));
  }

  public Object getAnnotation(Class requestedAnnoClass, Constructor ctor) {
    return super.getAnnotation(requestedAnnoClass, mIdPool.getIdFor(ctor));
  }

  public Object getAnnotation(Class requestedAnnoClass,
                              Constructor ctor,
                              int pnum) {
    return super.getAnnotation(requestedAnnoClass, mIdPool.getIdFor(ctor,pnum));
  }

  public Object getAnnotation(Class requestedAnnoClass, Field field) {
    return super.getAnnotation(requestedAnnoClass, mIdPool.getIdFor(field));
  }

  public Object getAnnotation(Class requestedAnnoClass, Method method) {
    return super.getAnnotation(requestedAnnoClass, mIdPool.getIdFor(method));
  }

  public Object getAnnotation(Class requestedAnnoClass,
                              Method method,
                              int pnum) {
    return super.getAnnotation(requestedAnnoClass, mIdPool.getIdFor(method,pnum));
  }

  public Object[] getAnnotations(Package pakkage) {
    return super.getAnnotations(mIdPool.getIdFor(pakkage));
  }

  public Object[] getAnnotations(Class clazz) {
    return super.getAnnotations(mIdPool.getIdFor(clazz));
  }

  public Object[] getAnnotations(Field field) {
    return super.getAnnotations(mIdPool.getIdFor(field));
  }

  public Object[] getAnnotations(Constructor ctor) {
    return super.getAnnotations(mIdPool.getIdFor(ctor));
  }

  public Object[] getAnnotations(Method field) {
    return super.getAnnotations(mIdPool.getIdFor(field));
  }

  public Object[] getAnnotations(Constructor ctor, int paramNum) {
    return super.getAnnotations(mIdPool.getIdFor(ctor,paramNum));
  }

  public Object[] getAnnotations(Method method, int paramNum) {
    return super.getAnnotations(mIdPool.getIdFor(method,paramNum));
  }

  // ========================================================================
  // Private methods

  private Object[] getRawAnnotations(ElementId id) {
    if (!(id instanceof ReflectElementId)) throw new IllegalStateException();
    Object ae = ((ReflectElementId)id).getAnnotatedElement();
    //REVIEW this switch is hokey but expedient, should probably do
    // something with double indirection
    switch(id.getType()) {
      case ElementId.PACKAGE_TYPE:
        return mDelegate.getAnnotations((Package)ae);
      case ElementId.CLASS_TYPE:
        return mDelegate.getAnnotations((Class)ae);
      case ElementId.METHOD_TYPE:
        return mDelegate.getAnnotations((Method)ae);
      case ElementId.CONSTRUCTOR_TYPE:
        return mDelegate.getAnnotations((Constructor)ae);
      case ElementId.FIELD_TYPE:
        return mDelegate.getAnnotations((Field)ae);
      case ElementId.PARAMETER_TYPE:
        if (ae instanceof Method) {
          return mDelegate.getAnnotations((Method)ae,id.getParameterNumber());
        } else {
          return mDelegate.getAnnotations((Constructor)ae,id.getParameterNumber());
        }
      default:
        throw new IllegalStateException("id.getType() == "+id.getType());
    }
  }

  private void copyValues(Object src, AnnoProxy dest, AnnoType destType) {
    boolean isVerbose = false;
    if (src == null) throw new IllegalArgumentException();
    Class annType = destType.getDeclaredClass();
    if (isVerbose) mLogger.verbose("type is "+annType.getName());
    //FIXME this is a bit clumsy right now - I think we need to be a little
    // more surgical in identifying the annotation member methods
    Method[] methods = annType.getMethods();
    for(int i=0; i<methods.length; i++) {
      if (isVerbose) mLogger.verbose("examining "+methods[i].toString());
      int mods = methods[i].getModifiers();
      if (Modifier.isStatic(mods)) continue;
      if (!Modifier.isPublic(mods)) continue;
      if (methods[i].getParameterTypes().length > 0) continue;
      {
        // try to limit it to real annotation methods.
        // FIXME seems like this could be better
        Class c = methods[i].getDeclaringClass();
        if (Object.class.equals(c)) continue;
      }
      if (isVerbose) mLogger.verbose("invoking "+methods[i].getName()+"()");
      Object value;
      try {
        value = methods[i].invoke(src, (Object[]) null);
      } catch (IllegalAccessException e) {
        mLogger.error(e);
        continue;
      } catch (InvocationTargetException e) {
        mLogger.error(e);
        continue;
      }
      if (isVerbose) mLogger.verbose("value is "+value);
      Class valClass = value.getClass();
      try {
      if (isSimpleType(valClass)) {
        dest.setValue(methods[i].getName(),value);
      } else if (valClass.isArray()) {
        if (isSimpleType(valClass.getComponentType())) {

        } else {

        }
        throw new IllegalArgumentException("array annotation properties NYI");

      } else {
        throw new IllegalArgumentException("complex annotation properties NYI");
      }
      } catch(Exception fixme) {
        mLogger.error(fixme);
      }
    }
  }

  private boolean isSimpleType(Class c) {
    return c.isPrimitive() || c.getName().equals("java.lang.String");
  }

}
