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

import org.apache.xmlbeans.impl.jam.annogen.provider.ElementId;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.ExecutableMemberDoc;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ElementIdPool { //REVIEW rename to ElementIdPool

  // ========================================================================
  // Singleton

  public static ElementIdPool getInstance() { return INSTANCE; }

  private static final ElementIdPool INSTANCE
    = new ElementIdPool();

  private ElementIdPool() {}

  // ========================================================================
  // Public methods

  //FIXME do some caching here, please

  public ElementId getIdFor(ProgramElementDoc ped) {
    return new JavadocElementId(ped);
  }

  public ElementId getIdFor(ExecutableMemberDoc ped, int paramNum) {
    return new JavadocElementId(ped,paramNum);
  }


  public ElementId getIdFor(Package pakkage) {
    return new ReflectElementId(pakkage);
  }

  public ElementId getIdFor(Class clazz) {
    return new ReflectElementId(clazz);
  }

  public ElementId getIdFor(Field field) {
    return new ReflectElementId(field);
  }

  public ElementId getIdFor(Method method) {
    return new ReflectElementId(method);
  }

  public ElementId getIdFor(Constructor constructor) {
    return new ReflectElementId(constructor);
  }

  public ElementId getIdFor(Method method, int parameter) {
    return null;
  }

  public ElementId getIdFor(Constructor ctor, int parameter) {
    return null;
  }

}
