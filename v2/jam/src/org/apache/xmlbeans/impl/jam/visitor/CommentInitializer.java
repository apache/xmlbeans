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
package org.apache.xmlbeans.impl.jam.visitor;

import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.editable.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class CommentInitializer extends ElementVisitor {

  // ========================================================================
  // Constructors - but maybe it should be a singleton?

  public CommentInitializer() {}

  // ========================================================================
  // Constants

  private static final int TAGNAME_COL = 0;
  private static final int CLASSNAME_COL = 1;

  // ========================================================================
  // Variables

  private Map mTag2Annclass = null; //maps jd tag names to annotation classes

  // ========================================================================
  // Public methods

  public void setJavadocTagMappings(String[][] mappings) {
    if (mappings == null) throw new IllegalArgumentException("null mappings");
    mTag2Annclass = new HashMap();
    for(int i=0; i<mappings.length; i++) {
      mTag2Annclass.put(mappings[i][TAGNAME_COL],mappings[i][CLASSNAME_COL]);
    }
  }

  // ========================================================================
  // EElementVisitor implementation - nothing to see here

  public void visit(EClass clazz)       { visit((EAnnotatedElement)clazz); }
  public void visit(EConstructor ctor)  { visit((EAnnotatedElement)ctor); }
  public void visit(EField field)       { visit((EAnnotatedElement)field); }
  public void visit(EMethod method)     { visit((EAnnotatedElement)method); }
  public void visit(EParameter param)   { visit((EAnnotatedElement)param); }

  public void visit(EAnnotation ann)          {}
  public void visit(EComment param)           {}

  // ========================================================================
  // Protected methods

  protected void visit(EAnnotatedElement element) {
    EComment comment = element.getEditableComment();
    if (comment != null) {
      String[] commentsAndTags = getCommentsAndTags(comment);
      if (commentsAndTags == null || commentsAndTags.length == 0) return;
      processComment(element,commentsAndTags[0]);
      for(int i=1; i<commentsAndTags.length; i++) {
        processJavadocTag(element,commentsAndTags[i]);
      }
    }
  }

  /**
   * Returns an array of strings containing the javadoc comments and raw
   * javadoc tag contents from the given elements.  Note that comment tokens
   * (leading '/*', '*', and '*[slash]' are stripped from the comments,
   * and they are trimmed up a bit.  The tag sections contain the javadoc
   * tag, including the leading '@'.  Array index 0 contains the comments,
   * and each subsequent index holds the javadoc tag contents, if any.
   *
   */
  protected String[] getCommentsAndTags(JComment comment) {
    String text = comment.getText().trim();
    if (!text.startsWith("/*")) {
      return new String[] {comment.getText()}; //not sure what to do with this
    }
    // looks like we have real work to do.  first set up a reader
    BufferedReader in = new BufferedReader(new StringReader(text));
    // now create a list to store the string we will return
    List commentsAndTags = new ArrayList();
    // get the comment string
    StringWriter commentText = new StringWriter();
    String nextLine = eatDocChunk(in,commentText);
    commentsAndTags.add(commentText.toString());
    // now process the tags, if any
    while(nextLine != null) {
      StringWriter tagText = new StringWriter();
      tagText.write(nextLine);
      tagText.write('\n');
      nextLine = eatDocChunk(in,tagText);
      commentsAndTags.add(tagText.toString());
    }
    String[] out = new String[commentsAndTags.size()];
    commentsAndTags.toArray(out);
    return out;
  }

  protected void processComment(EAnnotatedElement commentedElement,
                                String trimmedComment) {
    commentedElement.getEditableComment().setText(trimmedComment);
  }


  //FIXME this method nned help
  protected void processJavadocTag(EAnnotatedElement element, String tagtext) {
    tagtext = tagtext.trim();
    if (!tagtext.startsWith("@")) {
      throw new IllegalArgumentException("invalid tagtext '"+tagtext+"'");
    }
    tagtext = tagtext.substring(1).trim();
    int space = tagtext.indexOf(' ');
    String tagname = tagtext.substring(0,space);
    tagtext = tagtext.substring(space).trim();
    element.addAnnotationForTag(tagname,tagtext);
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