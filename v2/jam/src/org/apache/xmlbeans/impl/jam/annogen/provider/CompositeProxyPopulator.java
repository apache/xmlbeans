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
package org.apache.xmlbeans.impl.jam.annogen.provider;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class CompositeProxyPopulator implements ProxyPopulator {

  // ========================================================================
  // Variables

  private ProxyPopulator[] mPops;

  // ========================================================================
  // Constructors

  public CompositeProxyPopulator(ProxyPopulator[] pops) {
    if (pops == null) throw new IllegalArgumentException("null pops");
    mPops = pops;
  }

  // ========================================================================
  // Annoatation populator implementation

  public boolean hasAnnotation(ElementId id, Class annoType) {
    for(int i=0; i<mPops.length; i++) {
      if (mPops[i].hasAnnotation(id,annoType)) return true;
    }
    return false;
  }

  public void populateProxy(ElementId id,
                            Class annoType,
                            AnnotationProxy proxy) {
    for(int i=0; i<mPops.length; i++) mPops[i].populateProxy(id,annoType,proxy);
  }
}
