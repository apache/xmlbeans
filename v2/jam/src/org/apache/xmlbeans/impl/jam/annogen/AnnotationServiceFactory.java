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


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class AnnotationServiceFactory {

  // ========================================================================
  // Constants

  private static final AnnotationServiceFactory DEFAULT = new AnnotationServiceFactory();

  // ========================================================================
  // Singleton

  /**
   * Return the default factory singleton for this VM.
   */
  public static AnnotationServiceFactory getInstance() { return DEFAULT; }

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
  public AnnotationService createService(AnnotationService params) {
    return null;
  }


  /**
   * <p>Creates a default AnnoService which is only returns standard 175
   * annotations (i.e. you get the same thing you get using the standard
   * annotation APIs in java.lang.reflect).</p>
   */
  public AnnotationService createDefaultService() {
    return null;
  }
}
