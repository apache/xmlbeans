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

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface Reflect15Delegate {

  public void init(ElementContext ctx);

  public void extractAnnotations(MMember dest, Method src);

  public void extractAnnotations(MConstructor dest, Constructor src);

  public void extractAnnotations(MField dest, Field src);

  public void extractAnnotations(MClass dest, Class src);

  public void extractAnnotations(MParameter dest, Method src, int paramNum);

  public void extractAnnotations(MParameter dest, Constructor src, int paramNum);

  public boolean isEnum(Class clazz);

  public Constructor getEnclosingConstructor(Class clazz);

  public Method getEnclosingMethod(Class clazz);
  

}
