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

import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BaseJClassLoader implements JClassLoader {
  // ========================================================================
  // Variables

  private Map mName2Package = new HashMap();
  private Map mFd2Class = new HashMap();
  private JAnnotationLoader mAnnotationLoader = null;//FIXME
  private JClassLoader mParentLoader;

  // ========================================================================
  // Constructor

  public BaseJClassLoader(JClassLoader parent) {
    mParentLoader = parent;
  }

  // ========================================================================
  // Abstract methods

  protected abstract JClass createClass(String qcname);

  // ========================================================================
  // JClassLoader implementation

  public final JClassLoader getParent() { return mParentLoader; }

  public final JClass loadClass(String fd)
  {
    fd = fd.trim();//REVIEW is this paranoid?
    JClass out = (JClass)mFd2Class.get(fd);
    if (out != null) return out;
    if (fd.startsWith("[")) {
      return ArrayJClass.createClassFor(fd,this);
    } else {
      if (fd.equals("java.lang.Object")) return mParentLoader.loadClass(fd);
    }
    out = createClass(fd);
    if (out != null) {
      mFd2Class.put(fd,out);
      return out;
    } else {
      return mParentLoader.loadClass(fd);
    }
  }

  public JAnnotationLoader getAnnotationLoader() {
    return mAnnotationLoader;
  }

  //FIXME
  public JPackage getPackage(String named) {
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      out = JDFactory.getInstance().createPackage(named);
      mName2Package.put(named,out);
    }
    return out;
  }

  // ========================================================================
  // Public methods

  /**
   * Returns an unmodifiable collection containing the JClasses which
   * have been resolved by this JClassLoader.
   */
  public Collection getResolvedClasses() {
    return Collections.unmodifiableCollection(mFd2Class.values());
  }
}