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

package org.apache.xmlbeans.impl.jam.mutable;

import org.apache.xmlbeans.impl.jam.JSourcePosition;

import java.net.URI;

/**
 * <p>Mutable version of JSourcePosition.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface MSourcePosition extends JSourcePosition {

  /**
   * Sets the text column number for this source position.  Set to -1 if
   * it is not known.
   */
  public void setColumn(int col);

  /**
   * Sets the text line number for this source position.  Set to -1 if
   * it is not known.
   */
  public void setLine(int line);

  /**
   * Sets the URI of the source file.  Set to null if it is not known.
   */
  public void setSourceURI(URI uri);

}