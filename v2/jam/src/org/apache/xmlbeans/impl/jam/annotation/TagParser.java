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
package org.apache.xmlbeans.impl.jam.annotation;

import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class TagParser {

  // ========================================================================
  // Variables

  private JamServiceContext mContext = null;

  // ========================================================================
  // Public methods

  /**
   * <p>Called by JAM to initialize the proxy.  Do not try to call this
   * yourself.</p>
   */
  public void init(JamServiceContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null logger");
    if (mContext != null) throw new IllegalStateException
      ("TagParser.init() called twice");
    mContext = ctx;
  }

  // ========================================================================
  // Abstract methods

  public abstract void parse(MAnnotatedElement target,
                               String tagName,
                               String tagText);

  // ========================================================================
  // Protected methods

  protected JamLogger getLogger() { return mContext.getLogger(); }

  protected JClass getStringType() {
    return ((ElementContext)mContext).getClassLoader().
      loadClass("java.lang.String");
  }
}
