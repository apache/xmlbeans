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
package org.apache.xmlbeans.impl.jam.annogen.provider;

/**
 * <p>Gives providers an opportunity to say something extra about the
 * annotations which apply to a given element.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface AnnoModifier {

  // ========================================================================
  // Public methods

  /**
   * <p>Provides the Populator with a context object to access various services
   * such as logging.  For a given instance, this method is guranteed to be
   * called exactly once and before any other methods in this interface.</p>
   */
  public void init(ProviderContext pc);

  /**
   * <p>Called to give the Populator a chance to modify the annotations which
   * apply to a given element.</p>
   *
   * @param id  Element to which the annotations apply.
   * @param currentAnnos Currently applicable annotations.
   */

  public void modifyAnnos(ElementId id, AnnoProxySet currentAnnos);
}
