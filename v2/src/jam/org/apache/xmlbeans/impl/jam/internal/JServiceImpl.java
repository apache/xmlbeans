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

import org.apache.xmlbeans.impl.jam.JService;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JClassIterator;
import org.apache.xmlbeans.impl.jam.JClass;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JServiceImpl implements JService {

  // ========================================================================
  // Variables

  private JClassLoader mClassLoader;
  private String[] mClassNames;

  // ========================================================================
  // Constructors

  public JServiceImpl(JClassLoader jcl, String[] classes) {
    if (jcl == null) throw new IllegalArgumentException("null jcl");
    if (classes == null) throw new IllegalArgumentException("null classes");
    mClassLoader = jcl;
    mClassNames = classes;
  }

  // ========================================================================
  // JService implementation

  public JClassLoader getClassLoader() {
    return mClassLoader;
  }

  public String[] getClassNames() {
    return mClassNames;
  }

  public JClassIterator getClasses() {
    return new JClassIterator(getClassLoader(),getClassNames());
  }

  public JClass[] getAllClasses() {
    JClass[] out = new JClass[mClassNames.length];
    for(int i=0; i<out.length; i++) {
      out[i] = getClassLoader().loadClass(mClassNames[i]);
    }
    return out;
  }

}
