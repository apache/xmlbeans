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
import org.apache.xmlbeans.impl.jam.annogen.provider.ValueSetter;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class DefaultValueSetter implements ValueSetter {

  // ========================================================================
  // Variables

  private Object mTargetAnno;

  // ========================================================================
  // Constructors

  public DefaultValueSetter(Object targetAnno) {
    mTargetAnno = targetAnno;
  }

  // ========================================================================
  // TargetAnnotation implementation

  public void setValue(String valueName, Object value)
  {
    Class[] sig = new Class[] {value.getClass()};
    Method setter = null;
    try {
      setter = mTargetAnno.getClass().
            getMethod(Annogen.SETTER_PREFIX + valueName, sig);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();  //FIXME log this
      return;
    }
    try {
      setter.invoke(mTargetAnno, new Object[]{value});
    } catch (IllegalAccessException e) {
      e.printStackTrace();  //FIXME log this
    } catch (InvocationTargetException e) {
      e.printStackTrace();  //FIXME log this
    }
  }
}
