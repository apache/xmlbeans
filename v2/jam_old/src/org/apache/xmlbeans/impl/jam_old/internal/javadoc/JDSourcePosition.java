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

package org.apache.xmlbeans.impl.jam_old.internal.javadoc;


import com.sun.javadoc.SourcePosition;
import java.net.URI;
import org.apache.xmlbeans.impl.jam_old.JSourcePosition;

/**
 * Javadoc-backed implementation of JSourcePosition.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDSourcePosition implements JSourcePosition
{
  // ========================================================================
  // Variables

  private SourcePosition mPosition;

  // ========================================================================
  // Constructors
  
  public JDSourcePosition(SourcePosition p) {
    if (p == null) throw new IllegalArgumentException("null position");
    mPosition = p;
  }

  // ========================================================================
  // JSourcePosition implementation

  public int getColumn() { return mPosition.column(); } 

  public int getLine() { return mPosition.line(); } 

  public URI getSourceURI() { return mPosition.file().toURI(); } 
}
