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
package org.apache.xmlbeans.impl.jam.annogen.internal;

import org.apache.xmlbeans.impl.jam.annogen.provider.*;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class AnnoServiceBase {

  // ========================================================================
  // Variables

  private ProviderContext mContext;
  private AnnoTypeRegistry mRegistry;
  private AnnoModifier mModifier;
  private JamLogger mLogger;


  // ========================================================================
  // Constructors

  public AnnoServiceBase(AnnoServiceParamsImpl asp) {
    if (asp == null) throw new IllegalArgumentException("null asp");
    AnnoModifier[] pps = asp.getPopulators();
    if (pps.length == 1) {
      mModifier = pps[0];
    } else {
      mModifier = new CompositeAnnoModifier(pps);
    }
    mModifier.init(asp);
    mRegistry = asp.getAnnoTypeRegistry();
    mLogger = asp.getLogger();
    mContext = (ProviderContext)asp;
  }

  // ========================================================================
  // Abstract methods

  protected abstract void getIndigenousAnnotations(ElementId id, AnnoProxySet out);

  // ========================================================================
  // Protected methods

  //FIXME add some caching here, please
  protected AnnoProxy[] getAnnotations(ElementId id) {
    AnnoProxySet apsi = new AnnoProxySetImpl(mContext);
    getIndigenousAnnotations(id,apsi);
    mModifier.modifyAnnos(id,apsi);
    return apsi.getAnnoProxies();
  }

  //FIXME this is quick and dirty, make more efficient please
  protected AnnoProxy getAnnotation(Class what, ElementId where) {
    AnnoType whatType;
    try {
      whatType = mRegistry.getAnnoTypeForRequestedClass(what);
    } catch(ClassNotFoundException cnfe) {
      mLogger.error(cnfe);
      return null;
    }
    Class proxyClass = whatType.getProxyClass();
    AnnoProxy[] annos = getAnnotations(where);
    for(int i=0; i<annos.length; i++) {
      if (proxyClass.isAssignableFrom(annos[i].getClass())) {
        return annos[i];
      }
    }
    return null;
  }

}


