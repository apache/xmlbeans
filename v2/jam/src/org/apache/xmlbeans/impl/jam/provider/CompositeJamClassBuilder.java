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
package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

/**
 * <p>Composite implementation of JamClassBuilder.  When building,
 * the first one in the list to not return null wins.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class CompositeJamClassBuilder extends JamClassBuilder {

  // ========================================================================
  // Variables

  private JamClassBuilder[] mBuilders;
  private JamClassBuilder mLastBuiltAt = null;

  // ========================================================================
  // Constructors

  public CompositeJamClassBuilder(JamClassBuilder[] builders) {
    if (builders == null) throw new IllegalArgumentException("null builders");
    mBuilders = builders;
  }

  // ========================================================================
  // JamClassBuilder implementation

  public void init(ElementContext ctx) {
    for(int i=0; i<mBuilders.length; i++) mBuilders[i].init(ctx);
  }

  public MClass build(String pkg, String cname) {
    MClass out = null;
    for(int i=0; i<mBuilders.length; i++) {
      out = mBuilders[i].build(pkg,cname);
      if (out != null) {
        mLastBuiltAt = mBuilders[i];
        return out;
      }
    }
    return null;
  }

  public void populate(MClass c) {
    mLastBuiltAt.populate(c);
    mLastBuiltAt = null;
  }


}
