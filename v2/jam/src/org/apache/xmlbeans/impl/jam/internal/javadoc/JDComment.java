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

import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

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
    mText = normalize(text);
  }

  // ========================================================================
  // Comment implementation

  public String getText() {
    return mText;
  }

  public JSourcePosition getSourcePosition() {
    return mPosition;
  }

  // ========================================================================
  // Private methods

  //this is really just so we can all pass the same comment tests
  private static final String normalize(String text) {
    StringWriter out = new StringWriter();
    BufferedReader in = new BufferedReader(new StringReader(text));
    String line;
    try {
      boolean addBreak = false;
      while((line = in.readLine()) != null) {
        if (addBreak) out.write('\n');
        addBreak = true;
        out.write(line.trim());
      }
    } catch(IOException howOdd) {
      howOdd.printStackTrace();
      return text;
    }
    return out.toString();

  }

}
