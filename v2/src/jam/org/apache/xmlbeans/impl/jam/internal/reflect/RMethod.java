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


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apache.xmlbeans.impl.jam.*;

/**
 * Reflection-backed implementation of JClass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
/*package*/ final class RMethod extends RMember implements JMethod 
{

  // ========================================================================
  // Variables

  private Method mMethod;

  // ========================================================================
  // Constructors
  
  public RMethod(Method m, JClassLoader loader) {
    super(m,loader);
    mMethod = m;
  }

  // ========================================================================
  // JElement implementation

  public JElement[] getChildren() { return getParameters(); }

  // ========================================================================
  // JMethod implementation

  public JClass getReturnType() {
    return RClassLoader.getClassFor(mMethod.getReturnType(),mLoader);
  }

  public JParameter[] getParameters() {
    return RParameter.createParameters
      (mMethod.getParameterTypes(),this,mLoader);
  }

  public JClass[] getExceptionTypes() {
    return RClass.getClasses(mMethod.getExceptionTypes(),mLoader);
  }

  public boolean isSynchronized() { 
    return Modifier.isSynchronized(mMethod.getModifiers());
  }

  public boolean isNative() { 
    return Modifier.isNative(mMethod.getModifiers());
  }

}
