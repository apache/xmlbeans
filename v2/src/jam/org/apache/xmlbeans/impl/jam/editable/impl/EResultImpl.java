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

package org.apache.xmlbeans.impl.jam.editable.impl;

import org.apache.xmlbeans.impl.jam.editable.EResult;
import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EResultImpl implements EResult, JClassLoader {

  // ========================================================================
  // Variables

  private Map mClasses = new HashMap();
  private JClassLoader mBaseClassLoader;
  private JAnnotationLoader mAnnotationLoader = null;

  // ========================================================================
  // Constructors

  public EResultImpl(EResultParamsImpl params) {
    mBaseClassLoader = params.getParentClassLoader();
    mAnnotationLoader = params.getAnnotationLoader();
  }

  // ========================================================================
  // EResult implementation

  public EClass addNewClass(String packageName, String className) {
    EClassImpl out = new EClassImpl(packageName,className,this);
    mClasses.put(out.getQualifiedName(),out);
    return out;
  }

  public EClass addNewClass(JClass copyme) {
    throw new IllegalStateException("NYI");
  }

  public JClassLoader getClassLoader() {
    return this;
  }

  // ========================================================================
  // JResult implementation

  public String[] getClassNames() {
    String[] out = new String[mClasses.values().size()];
    mClasses.keySet().toArray(out);
    return out;
  }

  public JClassIterator getClasses() {
    return new JClassIterator(this,getClassNames());
  }

  public JClass[] getAllClasses() {
    JClass[] out = new JClass[mClasses.values().size()];
    mClasses.values().toArray(out);
    return out;
  }

  // ========================================================================
  // JClassLoader implementation

  public JClass loadClass(String classname) {
    JClass out = (JClass)mClasses.get(classname);
    if (out != null) return out;
    return mBaseClassLoader.loadClass(classname);
  }

  public JPackage getPackage(String qualifiedPackageName) {
    return mBaseClassLoader.getPackage(qualifiedPackageName);
  }

  public JAnnotationLoader getAnnotationLoader() {
    return mAnnotationLoader;
  }

  public JClassLoader getParent() {
    return mBaseClassLoader;
  }
}
