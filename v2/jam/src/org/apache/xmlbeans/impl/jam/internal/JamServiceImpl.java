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
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
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
  // Hackish methods

  // this is a back door for jamxmlutils, which can't know the class names
  // until after it's parsed the xml file (which can't be done without
  // a jamservice - catch 22).
  public void setClassNames(String[] names) {
    mClassNames = names;
  }


}
