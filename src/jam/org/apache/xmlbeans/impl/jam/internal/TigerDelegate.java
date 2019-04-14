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

import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;

/**
 * <p>
 * Base for classes which expose 1.5 (aka 'tiger')-specific functionality.
 * </p>
 *
 * This class should be moved into a common directory between annogen
 * and jam.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class TigerDelegate {

  // ========================================================================
  // Constants

  private static final String SOME_TIGER_SPECIFIC_JAVADOC_CLASS =
    "com.sun.javadoc.AnnotationDesc";

  private static final String SOME_TIGER_SPECIFIC_REFLECT_CLASS =
    "java.lang.annotation.Annotation";

  // ========================================================================
  // Variables

  protected JamLogger mLogger  = null;

  /**
   * @deprecated
   */
  protected ElementContext mContext = null;

  private static boolean m14RuntimeWarningDone   = false;
  private static boolean m14BuildWarningDone = false;

  // ========================================================================
  // Public methods

  /**
   * @deprecated
   */
  public void init(ElementContext ctx) {
    mContext = ctx;
    init(ctx.getLogger());
  }

  public void init(JamLogger log) { mLogger = log; }

  // ========================================================================
  // Protected methods

  protected TigerDelegate() {}

  protected JamLogger getLogger() { return mLogger; }

  /**
   * Displays a warning indicating that the current build of JAM was
   * done under 1.4 (or earlier), which precludes the use of 1.5-specific
   * features.
   */
  protected static void issue14BuildWarning(Throwable t, JamLogger log) {
    if (!m14BuildWarningDone) {
      log.warning("This build of JAM was not made with JDK 1.5." +
                  "Even though you are now running under JDK 1.5, "+
                  "JSR175-style annotations will not be available");
      if (log.isVerbose(TigerDelegate.class)) log.verbose(t);
      m14BuildWarningDone = true;
    }
  }

  /**
   * Displays a warning indicating that JAM is running under 1.4 (or earlier),
   * which precludes the use of 1.5-specific features.
   */
  protected static void issue14RuntimeWarning(Throwable t, JamLogger log) {
    if (!m14RuntimeWarningDone) {
      log.warning("You are running under a pre-1.5 JDK.  JSR175-style "+
                  "source annotations will not be available");
      if (log.isVerbose(TigerDelegate.class)) log.verbose(t);
      m14RuntimeWarningDone = true;
    }
  }

  protected static boolean isTigerJavadocAvailable(JamLogger logger) {
    try {
      // class for name this because it's 1.5 specific.  if it fails, we
      // don't want to use the extractor
      Class.forName(SOME_TIGER_SPECIFIC_JAVADOC_CLASS);
      return true;
    } catch (ClassNotFoundException e) {
      issue14RuntimeWarning(e,logger);
      return false;
    }
  }

  protected static boolean isTigerReflectionAvailable(JamLogger logger) {
    try {
      // class for name this because it's 1.5 specific.  if it fails, we
      // don't want to use the extractor
      Class.forName(SOME_TIGER_SPECIFIC_REFLECT_CLASS);
      return true;
    } catch (ClassNotFoundException e) {
      issue14RuntimeWarning(e,logger);
      return false;
    }
  }
}