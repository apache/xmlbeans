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
package org.apache.xmlbeans.impl.jam.annogen.internal.reflect175;

import org.apache.xmlbeans.impl.jam.annogen.provider.AnnotationPopulator;
import org.apache.xmlbeans.impl.jam.annogen.provider.ElementId;
import org.apache.xmlbeans.impl.jam.annogen.provider.ValueSetter;
import org.apache.xmlbeans.impl.jam.annogen.internal.ReflectElementId;
import org.apache.xmlbeans.impl.jam.annogen.internal.ReflectElementId;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.annotation.Annotation;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Reflect175AnnotationPopulator extends AnnotationPopulator {

  // ========================================================================
  // Variables

  private ClassLoader mClassLoader = ClassLoader.getSystemClassLoader();

  // ========================================================================
  // AnnotationPopulator implementation

  public boolean hasAnnotation(ElementId id, Class annoType) {
    if (!(id instanceof ReflectElementId)) {
      throw new IllegalArgumentException("This case is NYI");
    }
    AnnotatedElement element =
      (AnnotatedElement)((ReflectElementId)id).getAnnotatedElement();
    return (element.getAnnotation(annoType) != null);
  }

  public void populateAnnotation(ElementId id,
                                 Class annoType,
                                 ValueSetter target)
  {
    if (!(id instanceof ReflectElementId)) {
      throw new IllegalArgumentException("This case is NYI");
    }
    AnnotatedElement element =
      (AnnotatedElement)((ReflectElementId)id).getAnnotatedElement();
    Annotation ann = element.getAnnotation(annoType);
    if (ann == null) return; // nothing there, nothing to do
    Method[] methods = annoType.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) {
      Class returnType = methods[i].getReturnType();
      if (returnType.isArray()) throw new IllegalStateException("array members are NYI");//FIXME
      if (returnType == null || returnType == void.class) continue;
      if (methods[i].getParameterTypes().length > 0) continue;
      try {
        Object value = methods[i].invoke(ann,null);
        target.setValue(methods[i].getName(),value);
      } catch(IllegalAccessException iae) {
        iae.printStackTrace();//FIXME log this
      } catch(InvocationTargetException ite) {
        ite.printStackTrace();//FIXME log this
      }
    }
  }
}
