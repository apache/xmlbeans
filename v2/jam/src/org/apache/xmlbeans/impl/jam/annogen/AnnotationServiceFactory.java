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

import org.apache.xmlbeans.impl.jam.annogen.internal.AnnotationServiceParamsImpl;
import org.apache.xmlbeans.impl.jam.annogen.internal.BaseAnnotationService;
import org.apache.xmlbeans.impl.jam.annogen.internal.CompositeProxyPopulator;
import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyPopulator;
import org.apache.xmlbeans.impl.jam.annogen.internal.CompositeProxyPopulator;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class AnnotationServiceFactory {

  // ========================================================================
  // Singleton

  /**
   * Return the default factory singleton for this VM.
   */
  public static AnnotationServiceFactory getInstance() { return DEFAULT; }

  // ========================================================================
  // Constants

  private static final AnnotationServiceFactory DEFAULT = new AnnotationServiceFactory();


  private static final String REFLECTING_POPULATOR =
    "org.apache.xmlbeans.impl.jam.annogen.internal.reflect175";

  // ========================================================================
  // Variables

  private ProxyPopulator mReflectingPopulator = null;

  // ========================================================================
  // Constructors

  protected AnnotationServiceFactory() {}

  // ========================================================================
  // Public methods

  /**
   * Create a new AnnoServiceParams.  The params can be populated and
   * then given to the createService method to create a new AnnoService.
   */
  public AnnotationServiceParams createServiceParams() {
    return new AnnotationServiceParamsImpl();

  }

  /**
   * <p>Create a new AnnoService using the given parameters.</p>
   */
  public AnnotationService createService(AnnotationServiceParams params) {
    if (!(params instanceof AnnotationServiceParamsImpl)) {
      throw new IllegalArgumentException("not valid service params");
    }
    return new BaseAnnotationService((AnnotationServiceParamsImpl)params);
  }


  /**
   * <p>Creates a default AnnoService which is only returns standard 175
   * annotations (i.e. you get the same thing you get using the standard
   * annotation APIs in java.lang.reflect).</p>
   */
  public AnnotationService createDefaultService() {
    return new BaseAnnotationService(new AnnotationServiceParamsImpl());
  }


  // ========================================================================
  // Private methods


  public ProxyPopulator getReflectingPopulator() {
    if (mReflectingPopulator != null) return mReflectingPopulator;

    try {
      // class for name this because it's 1.5-specific.  if it fails, we
      // don't want to use the extractor
      Class.forName("java.lang.annotation.Annotation");
    } catch (ClassNotFoundException e) {
      //issue14RuntimeWarning(e);
      return null;
    }
    // ok, if we could load that, let's new up the extractor delegate
    try {
      mReflectingPopulator = (ProxyPopulator)
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
}
