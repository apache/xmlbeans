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

import org.apache.xmlbeans.impl.jam.annogen.provider.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.annogen.tools.Annogen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public /*abstract*/ class AnnotationProxyBase implements AnnotationProxy {

  // ========================================================================
  // AnnotationProxy implementation

  /**
   * Just try to set the value via reflection.
   */ 
  public void setValue(String valueName, Object value)
    throws IllegalAccessException, InvocationTargetException
  {
    Class[] sig = new Class[] {value.getClass()};
    Method setter = null;
    try {
      setter = this.getClass().
            getMethod(Annogen.SETTER_PREFIX + valueName, sig);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();  //FIXME log this
      return;
    }
    setter.invoke(this, new Object[]{value});
  }
}
