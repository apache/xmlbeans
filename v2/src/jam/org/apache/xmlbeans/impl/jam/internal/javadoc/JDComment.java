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

package org.apache.xmlbeans.impl.jam.internal.javadoc;


import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JSourcePosition;

/**
 * Javadoc-backed implementation of Comment.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDComment implements JComment
{
  // ========================================================================
  // Variables

  private JSourcePosition mPosition = null;
  private String mText = null;

  // ========================================================================
  // Constructors
  
  public JDComment(String text) {
    mText = text;
  }

  // ========================================================================
  // Comment implementation

  public String getText() {
    return mText;
  }

  public JSourcePosition getSourcePosition() {
    return mPosition;
  }
}
