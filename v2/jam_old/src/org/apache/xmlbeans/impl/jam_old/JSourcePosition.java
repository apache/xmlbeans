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

package org.apache.xmlbeans.impl.jam_old;

import java.net.URI;

/**
 * Describes a specific point in a source file.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JSourcePosition {

  /**
   * Returns the text column number for this source position, or -1 if
   * it is not known.
   */
  public int getColumn();

  /**
   * Returns the text line number for this source position, or -1 if
   * it is not known.
   */
  public int getLine();

  /**
   * Returns the URI of the source file, or null if it is not known.
   */
  public URI getSourceURI();
}
