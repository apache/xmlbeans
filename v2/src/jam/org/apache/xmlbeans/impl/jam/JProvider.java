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

package org.apache.xmlbeans.impl.jam;

import org.apache.xmlbeans.impl.jam.JService;
import org.apache.xmlbeans.impl.jam.JServiceParams;

import java.io.IOException;

/**
 * Interface through which custom JAM implementations may be exposed.
 * Typical users should not be concerned with this interface - use
 * JServiceFactory instead.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JProvider {

  // ========================================================================
  // Public methods

  /**
   * Creates a new JAM service based on the given parameters.
   *
   * @param params Parameters for the new service.
   * @return a new service
   * @throws IOException if an IO error occurrs while creating the service.
   * @throws IllegalArgumentException is params is null or not an instance
   * returned by JServiceFactory.createServiceParams().
   */
  public JService createService(JServiceParams params) throws IOException;

  /**
   * Returns a brief description of this JAM provider.
   */
  public String getDescription();

  //may want to add something like this to let them interrogate the
  //provider about it's capabilities
  //
  //public JProviderFeatures getFeatures();

}
