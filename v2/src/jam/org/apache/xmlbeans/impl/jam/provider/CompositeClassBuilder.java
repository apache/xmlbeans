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

import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class CompositeClassBuilder implements EClassBuilder {

  private EClassBuilder[] mBuilders;

  // ========================================================================
  // Constructors

  public CompositeClassBuilder(EClassBuilder[] builders) {
    mBuilders = builders;
  }

  // ========================================================================
  // EClassBuilder implementation

  public EClass build(String pkg, String cname, JClassLoader cl) {
    EClass out = null;
    for(int i=0; i<mBuilders.length; i++) {
      out = mBuilders[i].build(pkg,cname,cl);
      if (out != null) return out;
    }
    return null;
  }

}
