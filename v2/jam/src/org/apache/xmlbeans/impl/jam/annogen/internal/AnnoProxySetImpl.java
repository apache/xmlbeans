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

import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoTypeSet;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoTypeSet;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoType;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoProxySet;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoProxy;
import org.apache.xmlbeans.impl.jam.annogen.provider.AnnoTypeRegistry;
import org.apache.xmlbeans.impl.jam.annogen.provider.ProviderContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class AnnoProxySetImpl implements AnnoProxySet {

  private Map mAt2Ap = new HashMap();
  private AnnoTypeRegistry mTypeRegistry;
  private JamLogger mLogger;

  // ========================================================================
  // Constructor

  public AnnoProxySetImpl(ProviderContext ctx) {
    mTypeRegistry = ctx.getAnnoTypeRegistry();
    mLogger = ctx.getLogger();
  }

  // ========================================================================
  // Public methods

  public boolean containsProxyFor(Class requestedClass) {
    AnnoType annoType = getAnnoType(requestedClass);
    if (annoType == null) return false;
      return mAt2Ap.containsKey(annoType);
  }

  public AnnoProxy findOrCreateProxyFor(Class requestedClass) {
    AnnoType annoType = getAnnoType(requestedClass);
    if (annoType == null) return null;
    AnnoProxy ap = (AnnoProxy)mAt2Ap.get(annoType);
    if (ap != null) return ap;
    return createProxyInstance(annoType);
  }

  public AnnoProxy removeProxyFor(Class requestedClass) {
    AnnoType annoType = getAnnoType(requestedClass);
    if (annoType == null) return null;
    return (AnnoProxy)mAt2Ap.remove(annoType);
  }

  public AnnoProxy[] getAnnoProxies() {
    AnnoProxy[] out = new AnnoProxy[mAt2Ap.values().size()];
    mAt2Ap.values().toArray(out);
    return out;
  }

  // ========================================================================
  // Private methods

  private AnnoType getAnnoType(Class requestedClass) {
    try {
      return mTypeRegistry.getAnnoTypeForRequestedClass(requestedClass);
    } catch(ClassNotFoundException cnfe) {
      mLogger.error(cnfe);
      return null;
    }
  }

  private AnnoProxy createProxyInstance(AnnoType at)
  {
    try {
      return (AnnoProxy)at.getProxyClass().newInstance();
    } catch (InstantiationException e) {
      mLogger.error(e);
    } catch (IllegalAccessException e) {
      mLogger.error(e);
    }
    return null;
  }

}
