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
package org.apache.xmlbeans.impl.jam.annotation;

import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JAnnotation;

import java.util.Properties;
import java.util.Enumeration;

import com.sun.javadoc.Tag;

/**
 * This provides ejbgen-style tag parsing: tag contents
 * are treated as whitespace-separated name=value pairs, where values
 * can be double-quoted.
 * 
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class WhitespaceDelimitedTagParser extends JavadocTagParser {

  public void parse(MAnnotatedElement target,  Tag tag) {
    MAnnotation[] anns = createAnnotations(target,tag);
    String tagText = tag.text();
    if (tagText == null) return;
    tagText = tagText.trim();
    if (tagText.length() == 0) return;
    Properties props = new Properties();
    parseAssignments(props,tagText); //FIXME no need to create Properties here
    if (props.size() > 0) {
      Enumeration names = props.propertyNames();
      while(names.hasMoreElements()) {
        String name = (String)names.nextElement();
        setValue(anns,name,props.getProperty(name));
      }
    } else {
      //add the single member text if and only if there are no name-value
      //pairs.  this is how ejbgen likes it but i'm not sure it's the right
      //thing - might be nicer to have the info always available
       setSingleValueText(anns,tag);
    }
  }

  // ========================================================================
  // Private methods

  //REVIEW the comment parsing logic here should be factored and made pluggable

  /**
   * Parse a line that contains assignments, taking into account
   * - newlines (ignore them)
   * - double quotes (the value is everything in-between)
   * - // (everything after is ignored)
   * - multiple assignments on the same line
   *
   * @param out This variable will contain a list of properties
   * representing the line once parsed.
   * @param line The line to be parsed
   *
   * This method contributed by Cedric Beust
   */
  private void parseAssignments(Properties out, String line) {
    getLogger().verbose("PARSING LINE " + line,this);
    String originalLine = line;
    line = removeComments(line);
    while (null != line && -1 != line.indexOf("=")) {
      int keyStart = -1;
      int keyEnd = -1;
      int ind = 0;
      // Skip stuff before the key
      char c = line.charAt(ind);
      while (isBlank(c)) {
        ind++;
        c = line.charAt(ind);
      }
      keyStart = ind;
      while (isLegal(line.charAt(ind))) ind++;
      keyEnd = ind;
      String key = line.substring(keyStart, keyEnd);
      ind = line.indexOf("=");
      if (ind == -1) {
        return; //FIXME let's be a little conservative, just for now
        //throw new IllegalStateException("'=' expected: "+line);
      }
      ind++;
      // Skip stuff after the equal sign
      try {
        c = line.charAt(ind);
      }
      catch(StringIndexOutOfBoundsException ex){
        ex.printStackTrace();
      }
      while (isBlank(c)) {
        ind++;
        c = line.charAt(ind);
      }

      String value;
      int valueStart = -1;
      int valueEnd = -1;
      if (c == '"') {
        valueStart = ++ind;
        while ('"' != line.charAt(ind)) {
          ind++;
          if (ind >= line.length()) {
            getLogger().verbose("missing double quotes on line "+line,this);
          }
        }
        valueEnd = ind;
      }
      else {
        valueStart = ind++;
        while (ind < line.length() && isLegal(line.charAt(ind))) ind++;
        valueEnd = ind;
      }
      value = line.substring(valueStart, valueEnd);
      if (ind < line.length()) {
        line = line.substring(ind + 1);
      }
      else {
        line = null;
      }
      getLogger().verbose("SETTING KEY:"+key+" VALUE:"+value,this);
      out.setProperty(key, value);
    }
  }

  /**
   * Remove all the texts between "//" and '\n'
   *
   * This method contributed by Cedric Beust
   */
  private String removeComments(String value) {
    String result = "";
    int size = value.length();
    String current = value;

    int currentIndex = 0;

    int beginning = current.indexOf("//");

    //
    // Ignore // if it's between double quotes
    //
    int doubleQuotesIndex = current.indexOf("\"");
    if (-1 != doubleQuotesIndex && doubleQuotesIndex < beginning) {
      // do nothing
      result = value;
    }
    else {
      while (currentIndex < size && beginning != -1) {
        beginning = value.indexOf("//", currentIndex);
        if (-1 != beginning) {
          if (beginning > 0 && value.charAt(beginning-1) == ':') {
            //this is a quick fix for problem of unquoted url values.  for
            //now, just say it's not a comment if preceded by ':'.  should
            //review this later
            currentIndex = beginning+2;
            continue;
          }
          int end = value.indexOf('\n', beginning);
          if (-1 == end) end = size;
          // We have identified a portion to remove, copy the one we want to
          // keep
          result = result + value.substring(currentIndex, beginning).trim() + "\n";
          current = value.substring(end);
          currentIndex = end;
        }
      }
      result += current;
    }

    return result.trim();
  }

  private boolean isBlank(char c) {
    return c == ' ' || c == '\t' || c == '\n';
  }

  private boolean isLegal(char c) {
    return (! isBlank(c)) && c != '=';
//     return Character.isJavaIdentifierStart(c) || c == '-' || Character.isDigit(c) || c == '.';
  }

}
