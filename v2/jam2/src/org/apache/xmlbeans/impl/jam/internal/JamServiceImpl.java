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

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JamClassIterator;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.internal.elements.ClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JamServiceImpl implements JamService {

  // ========================================================================
  // Variables

  private ElementContext mContext;
  private String[] mClassNames;


  // ========================================================================
  // Constructors

  public JamServiceImpl(ElementContext ctx, String[] classes) {
    if (ctx == null) throw new IllegalArgumentException("null jcl");
    if (classes == null) throw new IllegalArgumentException("null classes");
    mContext = ctx;
    mClassNames = classes;
  }

  // ========================================================================
  // JamService implementation

  public JamClassLoader getClassLoader() {
    return mContext.getClassLoader();
  }

  public String[] getClassNames() {
    return mClassNames;
  }

  public JamClassIterator getClasses() {
    return new JamClassIterator(getClassLoader(),getClassNames());
  }

  public JClass[] getAllClasses() {
    JClass[] out = new JClass[mClassNames.length];
    for(int i=0; i<out.length; i++) {
      out[i] = getClassLoader().loadClass(mClassNames[i]);
    }
    return out;
  }

  // ========================================================================
  // Public methods - somexday we need to clean up the way this is exposed.
  // at the moment, we just cast down to this elements class to get at
  // these methods

  public EClass addNewClass(String packageName,
                            String className,
                            String[] importSpecs)
  {
    ClassImpl out = new ClassImpl(packageName,className,mContext,importSpecs);
    ((JamClassLoaderImpl)getClassLoader()).addToCache(out);//FIXME yuck, please sort this out
    return out;
  }

  public EClass addNewClass(JClass copyme) {
    throw new IllegalStateException("NYI");
  }



}
