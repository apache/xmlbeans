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
package org.apache.xmlbeans.impl.jam.annogen;

import org.apache.xmlbeans.impl.jam.annogen.internal.AnnoServiceParamsImpl;
import org.apache.xmlbeans.impl.jam.annogen.internal.AnnoServiceRootImpl;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class AnnoServiceFactory {

  // ========================================================================
  // Singleton

  /**
   * Return the default factory singleton for this VM.
   */
  public static AnnoServiceFactory getInstance() { return DEFAULT; }

  // ========================================================================
  // Constants

  private static final AnnoServiceFactory DEFAULT = new AnnoServiceFactory();


  private static final String REFLECTING_POPULATOR =
    "org.apache.xmlbeans.impl.jam.annogen.internal.java15.Reflect175ProxyPopulator";

  private static final String JAVADOC_POPULATOR =
    "org.apache.xmlbeans.impl.jam.annogen.internal.java15.Javadoc175ProxyPopulator";

  // ========================================================================
  // Constructors

  protected AnnoServiceFactory() {}

  // ========================================================================
  // Public methods

  /**
   * Create a new AnnoServiceParams.  The params can be populated and
   * then given to the createServiceRoot method to create a new AnnoService.
   */
  public AnnoServiceParams createServiceParams() {
    return new AnnoServiceParamsImpl();
  }

  /**
   * <p>Create a new AnnoService using the given parameters.</p>
   */
  public AnnoServiceRoot createServiceRoot(AnnoServiceParams params) {
    if (!(params instanceof AnnoServiceParamsImpl)) {
      throw new IllegalArgumentException("not valid service params");
    }
    return new AnnoServiceRootImpl((AnnoServiceParamsImpl)params);
  }

  /**
   * <p>Creates a default AnnoService which is only returns standard 175
   * annotations (i.e. you get the same thing you get using the standard
   * annotation APIs in java.lang.reflect).</p>
   */
  public AnnoServiceRoot createDefaultService() {
    return new AnnoServiceRootImpl(new AnnoServiceParamsImpl());
  }

  // ========================================================================
  // Private methods

  /*
  public ProxyPopulator getReflectingPopulator() {
    try {
      // class for name this because it's 1.5-specific.  if it fails, we
      // don't want to use the extractor
      Class.forName("java.lang.annotation.Annotation");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      //issue14RuntimeWarning(e);
      return null;
    }
    // ok, if we could load that, let's new up the extractor delegate
    try {
      return (ProxyPopulator)
        Class.forName(REFLECTING_POPULATOR).newInstance();
      // if this fails for any reason, things are in a bad state
    } catch (ClassNotFoundException e) {
//      issue14BuildWarning(e);
    } catch (IllegalAccessException e) {
//      issue14BuildWarning(e);
    } catch (InstantiationException e) {
//      issue14BuildWarning(e);
    }
    return null;
  }

  public ProxyPopulator getJavadocPopulator() {
    try {
      // class for name this because it's 1.5-specific.  if it fails, we
      // don't want to use the extractor
      Class.forName("java.lang.annotation.Annotation");
    } catch (ClassNotFoundException e) {
      //issue14RuntimeWarning(e);
      e.printStackTrace();
      return null;
    }
    // ok, if we could load that, let's new up the extractor delegate
    try {
      return (ProxyPopulator)
        Class.forName(JAVADOC_POPULATOR).newInstance();
      // if this fails for any reason, things are in a bad state
    } catch (ClassNotFoundException e) {
//      issue14BuildWarning(e);
    } catch (IllegalAccessException e) {
//      issue14BuildWarning(e);
    } catch (InstantiationException e) {
//      issue14BuildWarning(e);
    }
    return null;
  }
  */

}
