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
package org.apache.xmlbeans.impl.jam.internal.java15;

import org.apache.xmlbeans.impl.jam.internal.reflect.ReflectAnnotationExtractor;
import org.apache.xmlbeans.impl.jam.mutable.MMember;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Reflect15AnnotationExtractor implements ReflectAnnotationExtractor {

  public void extractAnnotations(MMember dest, Method src) {
    Annotation[] anns = src.getDeclaredAnnotations();
    if (anns == null) return;
    for(int i=0; i<anns.length; i++) {
      dest.addAnnotationForInstance(anns[i].annotationType(),anns[i]);
    }
  }

  public void extractAnnotations(MConstructor dest, Constructor src) {
    Annotation[] anns = src.getDeclaredAnnotations();
    if (anns == null) return;
    for(int i=0; i<anns.length; i++) {
      dest.addAnnotationForInstance(anns[i].annotationType(),anns[i]);
    }
  }

  public void extractAnnotations(MField dest, Field src) {
    Annotation[] anns = src.getDeclaredAnnotations();
    if (anns == null) return;
    for(int i=0; i<anns.length; i++) {
      dest.addAnnotationForInstance(anns[i].annotationType(),anns[i]);
    }
  }

  public void extractAnnotations(MClass dest, Class src) {
    Annotation[] anns = src.getDeclaredAnnotations();
    if (anns == null) return;
    for(int i=0; i<anns.length; i++) {
      dest.addAnnotationForInstance(anns[i].annotationType(),anns[i]);
    }
  }

  public void extractAnnotations(MParameter dest, Method src, int paramNum) {
    Annotation[][] anns = src.getParameterAnnotations();
    if (anns == null) return;
    for(int i=0; i<anns[paramNum].length; i++) {
      dest.addAnnotationForInstance(anns[paramNum][i].annotationType(),
                                    anns[paramNum][i]);
    }
  }

  public void extractAnnotations(MParameter dest, Constructor src,
                                 int paramNum) {
    Annotation[][] anns;
    try {
      anns = src.getParameterAnnotations();
    } catch(NullPointerException wtf) {
      //FIXME workaround, sun code throws an NPE here
//      System.err.println("[Reflect15AnnotationExtractor] Ignoring apprent bug in reflection");
      //wtf.printStackTrace();
      return;
    }
    if (anns == null) return;
    for(int i=0; i<anns[paramNum].length; i++) {
      dest.addAnnotationForInstance(anns[paramNum][i].annotationType(),
                                    anns[paramNum][i]);
    }
  }
}
