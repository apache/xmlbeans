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

import org.apache.xmlbeans.impl.jam.mutable.MMember;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.sun.javadoc.ClassDoc;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface Reflect15Delegate {


  // ========================================================================
  // Factory

  public static class Factory {

    private static final String JAVA15_DELEGATE =
      "org.apache.xmlbeans.impl.jam.internal.java15.Reflect15DelegateImpl";

    public static Reflect15Delegate create(JamLogger logger) {
    try {
      // class for name this because it's 1.5 specific.  if it fails, we
      // don't want to use the extractor
      Class.forName("java.lang.annotation.Annotation");
    } catch (ClassNotFoundException e) {
      //issue14RuntimeWarning(e);  FIXME
      return null;
    }
      // ok, if we could load that, let's new up the extractor delegate
      try {
        Reflect15Delegate out = (Reflect15Delegate)
            Class.forName(JAVA15_DELEGATE).newInstance();
        out.init(logger);
      } catch (ClassNotFoundException e) {
        //issue14BuildWarning(e);
      } catch (IllegalAccessException e) {
        //issue14BuildWarning(e);
      } catch (InstantiationException e) {
        //issue14BuildWarning(e);
      }
      return null;
    }
  }

  // ========================================================================
  // Public methods

  public void populateAnnotationTypeIfNecessary(Class cd,
                                                MClass clazz,
                                                ReflectClassBuilder builder);

  public Class getAnnotationClassFor(/*Annotation*/Object annotation);

  public void init(JamLogger logger);

  public boolean isEnum(Class clazz);

  public Constructor getEnclosingConstructor(Class clazz);

  public Method getEnclosingMethod(Class clazz);

  public /*Annotation[]*/ Object[] getAnnotations(Package on);

  public /*Annotation[]*/ Object[] getAnnotations(Class on);

  public /*Annotation[]*/ Object[] getAnnotations(Method on);

  public /*Annotation[]*/ Object[] getAnnotations(Field on);

  public /*Annotation[]*/ Object[] getAnnotations(Constructor on);

  public /*Annotation[]*/ Object[] getAnnotations(Method on, int parmNum);

  public /*Annotation[]*/ Object[] getAnnotations(Constructor on, int parmNum);



  // ========================================================================
  // Stuff to deprecate

  /**
   * @deprecated
   */
  public void init(ElementContext ctx);


  public void extractAnnotations(MMember dest, Method src);

  public void extractAnnotations(MConstructor dest, Constructor src);

  public void extractAnnotations(MField dest, Field src);

  public void extractAnnotations(MClass dest, Class src);

  public void extractAnnotations(MParameter dest, Method src, int paramNum);

  public void extractAnnotations(MParameter dest, Constructor src, int paramNum);

}
