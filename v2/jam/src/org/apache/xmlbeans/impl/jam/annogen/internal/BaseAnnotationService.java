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

import org.apache.xmlbeans.impl.jam.annogen.tools.Annogen;
import org.apache.xmlbeans.impl.jam.annogen.AnnotationService;
import org.apache.xmlbeans.impl.jam.annogen.provider.ElementId;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnotationPopulator;
import org.apache.xmlbeans.impl.jam.annogen.provider.ValueSetter;
import org.apache.xmlbeans.impl.jam.JAnnotatedElement;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class BaseAnnotationService implements AnnotationService {

  // ========================================================================
  // Variables

  private AnnotationPopulator mPopulator;
  private Map mCache = new HashMap();
  private ReflectElementIdFactory mReflectIdFactory =
    ReflectElementIdFactory.getInstance();

  // ========================================================================
  // Constructors

  public BaseAnnotationService(AnnotationPopulator pop) {
    if (pop == null) throw new IllegalArgumentException("null pop");
    mPopulator = pop;
  }

  // ========================================================================
  // AnnoService implementation

  public Object getAnnotation(Class annotationType, Package pakkage) {
    return getAnnotation(annotationType,mReflectIdFactory.create(pakkage));
  }

  public Object getAnnotation(Class annotationType, Class clazz) {
    return getAnnotation(annotationType,mReflectIdFactory.create(clazz));
  }

  public Object getAnnotation(Class annotationType, Constructor ctor) {
    return getAnnotation(annotationType,mReflectIdFactory.create(ctor));
  }

  public Object getAnnotation(Class annotationType, Constructor ctor,
                              int pnum) {
    return getAnnotation(annotationType,mReflectIdFactory.create(ctor,pnum));
  }

  public Object getAnnotation(Class annotationType, Field field) {
    return getAnnotation(annotationType,mReflectIdFactory.create(field));
  }

  public Object getAnnotation(Class annotationType, Method method) {
    return getAnnotation(annotationType,mReflectIdFactory.create(method));
  }

  public Object getAnnotation(Class annotationType, Method method,
                              int pnum) {
    return getAnnotation(annotationType,mReflectIdFactory.create(method,pnum));
  }

  public Object getAnnotation(Class annotationType, JAnnotatedElement je) {
    return getAnnotation(annotationType,new JElementId(je));
  }

  public Object getAnnotation(Class annoType, ElementId id) {
    if (!mPopulator.hasAnnotation(id,annoType)) return null;
    Object out = getCachedAnnotationFor(id,annoType);
    if (out != null) return out;
    try {
      out = createAnnotationFor(annoType);
    } catch(Exception e) {
      e.printStackTrace(); //FIXME
      return null;
    }
    if (out == null) return null;
    ValueSetter vs = new DefaultValueSetter(out);
    mPopulator.populateAnnotation(id,annoType,vs);
    return out;
  }

  // ========================================================================
  // Protected methods

  // somebody could conceivably want to change this behavior.  we can either
  // let them extend and override this method, or might be worth exposing
  // an 'AnnotationInstanceFactory' interface for them to implement.

  protected Object createAnnotationFor(Class annoType)
    throws ClassNotFoundException, InstantiationException,
           IllegalAccessException
  {
    String implName = Annogen.getImplClassFor(annoType);
    Class annoImpl = annoType.getClassLoader().loadClass(implName);
    return annoImpl.newInstance();
  }


  // ========================================================================
  // Private methods

  private Object getCachedAnnotationFor(ElementId id, Class annoType) {
    return mCache.get(getCacheKey(id,annoType));
  }

  private String getCacheKey(ElementId id, Class annoType) {
    return id.toString() + '@' + annoType.getName();
  }


}
