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

package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.provider.JClassBuilder;

import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class StandardJClassLoader implements JClassLoader {

  // ========================================================================
  // Variables

  private JClassBuilder mService;
  private JAnnotationLoader mAnnotationLoader = null;
  private JClassLoader mParent;
  private Map mName2Package = new WeakHashMap();
  private Map mFd2Class = new WeakHashMap();

  // ========================================================================
  // Constructors

  public StandardJClassLoader(JClassBuilder service,
                              JClassLoader parent,
                              JAnnotationLoader annloader)
  {
    if (service == null) throw new IllegalArgumentException("null service");
    if (parent == null) throw new IllegalArgumentException("null parent");
    mService = service;
    mAnnotationLoader = annloader; //this one can be null
    mParent = parent;
  }

  // ========================================================================
  // JClassLoader implementation

  public JClass loadClass(String fieldDescriptor) {
    //FIXME this is busted for arrays
    JClass out = (JClass)mFd2Class.get(fieldDescriptor);
    if (out != null) return out;
    //FIXME check here to be sure it's a valid name
    out = mService.buildJClass(fieldDescriptor,this);
    if (out == null) {
      out = mParent.loadClass(fieldDescriptor);
      if (out == null) throw new IllegalStateException();
    }
    mFd2Class.put(fieldDescriptor,out);
    return out;
  }

  public JPackage getPackage(String named) {
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      out = new JPackageImpl(named);
      mName2Package.put(named,out);
    }
    return out;
  }

  public JAnnotationLoader getAnnotationLoader() { return mAnnotationLoader; }

  public JClassLoader getParent() { return mParent; }
}