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
package org.apache.xmlbeans.test.jam;

import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyPopulator;
import org.apache.xmlbeans.impl.jam.annogen.provider.ElementId;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyContext;
import org.apache.xmlbeans.test.jam.cases.jsr175.impl.RFEAnnotationImpl;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class TestProxyPopulator implements ProxyPopulator {

  public boolean hasAnnotation(ElementId id, Class annoType) {
    return true;
  }

  public void populateProxy(ElementId id,
                            Class annotationType,
                            AnnotationProxy targetProxy) {
    if (targetProxy instanceof RFEAnnotationImpl) {
      RFEAnnotationImpl rfe = (RFEAnnotationImpl)targetProxy;
      rfe.set_id(rfe.id()+1);
    }
  }

  public void init(ProxyContext pc) {
  }
}