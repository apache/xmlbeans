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

package org.apache.xmlbeans.impl.jam_old.internal.javadoc;


import com.sun.javadoc.MethodDoc;
import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JClassLoader;
import org.apache.xmlbeans.impl.jam_old.JMethod;

/**
 * javadoc-backed implementation of JClass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDMethod extends JDExecutableMember 
	      implements JMethod 
{

  // ========================================================================
  // Variables

  private MethodDoc mMethod;

  // ========================================================================
  // Constructors
  
  public JDMethod(MethodDoc f, JClassLoader loader) {
    super(f,loader);
    mMethod = f;
  }

  // ========================================================================
  // JMethod implementation

  public JClass getReturnType() {
    return JDClassLoader.getClassFor(mMethod.returnType(),mLoader);
  }

  public boolean isSynchronized() { return mMethod.isSynchronized(); }

  public boolean isNative() { return mMethod.isNative(); }

  public boolean isAbstract() {  return mMethod.isAbstract(); }
}
