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
package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.editable.*;

import java.io.*;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultCommentProcessor
        implements EClassInitializer, EElementVisitor {

  // ========================================================================
  // Singleton

  public static EClassInitializer getInstance() { return INSTANCE; }

  private static EClassInitializer INSTANCE = new DefaultCommentProcessor();

  private DefaultCommentProcessor() {}

  // ========================================================================
  // EClassInitializer implementation

  public void initialize(EClass clazz) {
    clazz.acceptAndWalk(this);
  }

  // ========================================================================
  // EElementVisitor implementation - nothing to see here

  public void visit(EClass clazz)             { visit((EElement)clazz); }
  public void visit(EConstructor ctor)        { visit((EElement)ctor); }
  public void visit(EField field)             { visit((EElement)field); }
  public void visit(EMethod method)           { visit((EElement)method); }
  public void visit(EParameter param)         { visit((EElement)param); }
  public void visit(EAnnotation ann)          { visit((EElement)ann); }
  public void visit(EAnnotationMember member) { visit((EElement)member); }

  // ========================================================================
  // Protected methods

  protected void visit(EElement element) {
    EComment[] comments = element.getEditableComments();
    if (comments == null || comments.length == 0) return;
    for(int i=0; i<comments.length; i++) {
      String text = comments[i].getText().trim();
      if (!text.startsWith("/*")) {
        element.removeComment(comments[i]);
      } else {
        StringWriter out = new StringWriter();
        BufferedReader in = new BufferedReader(new StringReader(text));
        String line;
        boolean addBreak = false;
        try {
          while((line = in.readLine()) != null) {
            line = line.trim();
            if (line.equals("*/")) continue;
            int offset = line.indexOf('*');
            do {
              offset++;
            }  while(offset < line.length() && line.charAt(offset) == '*');
            if (addBreak) out.write('\n');
            if (offset >= line.length()) continue;
            out.write(line.substring(offset+1).trim());
            addBreak = true;
          }
          comments[i].setText(out.toString());
        } catch(IOException veryUnexpected) {
          veryUnexpected.printStackTrace();
        }
      }
    }
  }
}
