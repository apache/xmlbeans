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
    EComment comment = element.getEditableComment();
    if (comment == null) return;
    String text = comment.getText().trim();
    if (!text.startsWith("/*")) {
      element.removeComment();
      return;
    }
    BufferedReader in = new BufferedReader(new StringReader(text));
    StringWriter commentText = new StringWriter();
    String nextLine = eatDocChunk(in,commentText);
    processComment(element,commentText.toString());
    while(nextLine != null) {
      StringWriter tagText = new StringWriter();
      tagText.write(nextLine);
      tagText.write('\n');
      nextLine = eatDocChunk(in,tagText);
      processJavadocTag(element,tagText.toString());
    }
  }

  protected void processJavadocTag(EElement element, String tagline) {

  }

  protected void processComment(EElement commentedElement,
                                String trimmedComment) {
    commentedElement.getEditableComment().setText(trimmedComment);
  }

  // ========================================================================
  // Private methods

  /**
   * <p>Writes the trimmed contents of the comment text provided by the
   * BufferedReader in the given BufferedWriter, until either a javadoc
   * tag is encountered (line starting with '@') or the end of the comment
   * section is encountered.</p>
   *
   * @return The next line read off of the input buffer which starts
   * a new section (starting with '@'), or null if the end of the comment
   * was reached.
   */
  private String eatDocChunk(BufferedReader in, Writer out) {
    String line;
    boolean firstLineYet = false;
    int breaksToAdd = 0;
    try {
      while((line = in.readLine()) != null) {
        line = getTrimmedLine(line);
        if (line.startsWith("@")) {  // are we starting a javadoc tag?
          return line;
        } else {
          if (firstLineYet) breaksToAdd++;
          if (line.length() > 0) {
            firstLineYet = true;
            for(int i=0; i<breaksToAdd; i++) out.write('\n');
            breaksToAdd = 0;
            out.write(line);
          }
        }
      }
    } catch(IOException veryUnexpected) {
      veryUnexpected.printStackTrace();
    }
    return null;
  }

  private String getTrimmedLine(String rawLine) {
    rawLine = rawLine.trim();
    int offset = rawLine.indexOf('*');
    if (offset == -1) return rawLine;
    do {
      offset++;
    }  while(offset < rawLine.length() && rawLine.charAt(offset) == '*');
    if (offset >= rawLine.length()) return "";
    return rawLine.substring(offset+1).trim();
  }
}