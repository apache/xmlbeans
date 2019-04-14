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

import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;

/**
 * <p>Provides a proxied view of some annotation artifact.  JAM calls the
 * public methods on this class to initialize the proxy with annotation
 * values; those methods should not be called by user code.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
/**
 * @deprecated do not use, moving into internal
 */
public abstract class AnnotationProxy {

  // ========================================================================
  // Constants

  /**
   * <p>Name of the member of annotations which have only a single member.
   * As specified in JSR175, that name is "value", but you should use
   * this constant to prevent typos.</p>
   */
  public static final String SINGLE_MEMBER_NAME = "value";


  /**
   * <p>The delimiters to use by default when parsing out name=value pairs
   * from a javadoc tag.</p>
   */
  private static final String DEFAULT_NVPAIR_DELIMS = "\n\r";

  // ========================================================================
  // Variables

  protected JamServiceContext mContext;

  // ========================================================================
  // Initialization methods - called by JAM, don't implement

  /**
   * <p>Called by JAM to initialize the proxy.  Do not try to call this
   * yourself.</p>
   */
  public void init(JamServiceContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null logger");
    mContext = ctx;
  }

  // ========================================================================
  // Public abstract methods

  /**
   * <p>Called by JAM to initialize a named member on this annotation proxy.
   * </p>
   */
  public abstract void setValue(String name, Object value, JClass type);

  //docme
  public abstract JAnnotationValue[] getValues();


  //docme
  public JAnnotationValue getValue(String named) {
    if (named == null) throw new IllegalArgumentException("null name");
    //FIXME this impl is very gross
    named = named.trim();
    JAnnotationValue[] values = getValues();
    for(int i=0; i<values.length; i++) {
      
      if (named.equals(values[i].getName())) return values[i];
    }
    return null;
  }

  // ========================================================================
  // Protected methods

  /**
   * <p>Returns an instance of JamLogger that this AnnotationProxy should use
   * for logging debug and error messages.</p>
   */
  protected JamLogger getLogger() { return mContext.getLogger(); }

}