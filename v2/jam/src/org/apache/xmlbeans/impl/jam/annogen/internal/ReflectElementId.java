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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;



/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ReflectElementId implements ElementId {

  // ========================================================================
  // Variables

  private Object /*AnnotationElement*/ mElement;
  private int mParameterNumber;

  // ========================================================================
  // Constructor

  public ReflectElementId(Package pakkage) {
    if (pakkage == null) throw new IllegalArgumentException("null pakkage");
    mElement = pakkage;
  }

  public ReflectElementId(Class clazz) {
    if (clazz == null) throw new IllegalArgumentException("null clazz");
    mElement = clazz;
  }

  public ReflectElementId(Field field) {
    if (field == null) throw new IllegalArgumentException("null field");
    mElement = field;
  }

  public ReflectElementId(Method method) {
    if (method == null) throw new IllegalArgumentException("null method");
    mElement = method;
  }

  public ReflectElementId(Constructor ctor) {
    if (ctor == null) throw new IllegalArgumentException("null ctor");
    mElement = ctor;
  }

  public ReflectElementId(Method method, int paramNum) {
    if (method == null) throw new IllegalArgumentException("null method");
    mElement = method;
    mParameterNumber = paramNum;
  }

  public ReflectElementId(Constructor ctor, int paramNum) {
    if (ctor == null) throw new IllegalArgumentException("null ctor");
    mElement = ctor;
    mParameterNumber = paramNum;
  }

  // ========================================================================
  // ElementId implementation

  public String getContainingClass() {
    if (mElement instanceof Member) { //field, method, constructor
      return ((Member)mElement).getDeclaringClass().getName();
    } else if (mElement instanceof Class) {
      Class decl = ((Class)mElement).getDeclaringClass();
      return (decl == null) ? null : decl.getName(); 
    } else if (mElement instanceof Package) {
      return null;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String getContainingPackage() {
    if (mElement instanceof Member) { //field, method, constructor
      return ((Member)mElement).getDeclaringClass().getPackage().getName();
    } else if (mElement instanceof Class) {
      return ((Class)mElement).getPackage().getName();
    } else if (mElement instanceof Package) {
      return null;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String getName() {
    if (mElement instanceof Member) { //field, method, constructor
      return ((Method)mElement).getName();
    } else if (mElement instanceof Class) {
      return ((Class)mElement).getName();
    } else if (mElement instanceof Package) {
      return ((Package)mElement).getName();
    } else {
      throw new IllegalArgumentException();
    }
  }

  public int getType() {
    if (mParameterNumber > -1) {
      return PARAMETER_TYPE;
    } else if (mElement instanceof Field) {
      return FIELD_TYPE;
    } else if (mElement instanceof Method) {
      return METHOD_TYPE;
    } else if (mElement instanceof Constructor) {
      return CONSTRUCTOR_TYPE;
    } else if (mElement instanceof Class) {
      return CLASS_TYPE;
    } else if (mElement instanceof Package) {
      return PACKAGE_TYPE;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String[] getSignature() {
    if (mElement instanceof Method) {
      return getTypeNames(((Method)mElement).getParameterTypes());
    } else if (mElement instanceof Constructor) {
        return getTypeNames(((Constructor)mElement).getParameterTypes());
    } else {
      return null;
    }
  }

  public int getParameterNumber() { return mParameterNumber; }

  // ========================================================================
  // For internal use only

  public /*AnnoatedElement*/ Object getAnnotatedElement() { return mElement; }

  // ========================================================================
  // Private

  private String[] getTypeNames(Class[] classes) {
    String[] out = new String[classes.length];
    for(int i=0; i<out.length; i++) out[i] = classes[i].getName();
    return out;
  }



}
