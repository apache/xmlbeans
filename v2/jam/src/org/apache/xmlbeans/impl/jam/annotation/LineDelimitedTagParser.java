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
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

import java.util.Properties;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.StringWriter;

/**
 * <p>Attempts to parse tag contents as a series of line-delimited name-value
 * pairs.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class LineDelimitedTagParser extends TagParser {

  // ========================================================================
  // Constants

  private static final String VALUE_QUOTE = "\"";
  private static final String LINE_DELIMS = "\n\f\r";

  // ========================================================================
  // TagParser implementation

  public void parse(MAnnotatedElement target,
                      String tagName,
                      String tagText)
  {
    if (tagText == null) throw new IllegalArgumentException("null tagText");
    if (tagName == null) throw new IllegalArgumentException("null tagName");
    if (target == null) throw new IllegalArgumentException("null target");
    MAnnotation ann = createAnnotation(target,tagName);
    tagText = tagText.trim();
    if (tagText.length() == 0) return;
    ann.setSimpleValue(JAnnotation.SINGLE_VALUE_NAME,tagText,getStringType());
    StringTokenizer st = new StringTokenizer(tagText, LINE_DELIMS);
    while (st.hasMoreTokens()) {
      String pair = st.nextToken();
      int eq = pair.indexOf('=');
      if (eq <= 0) continue; // if abstent or is first character
      String name = pair.substring(0, eq).trim();
      if (eq < pair.length() - 1) {
        String value = pair.substring(eq + 1).trim();
        if (value.startsWith(VALUE_QUOTE)) {
          value = parseQuotedValue(value.substring(1),st);
        }
        setValue(ann,name,value);
      }
    }
  }

  // ========================================================================
  // Protected methods

  protected MAnnotation createAnnotation(MAnnotatedElement target,
                                         String tagName) {
    return target.findOrCreateAnnotation(tagName);
  }

  protected void setValue(MAnnotation ann,
                          String memberName,
                          String value) {
    ann.setSimpleValue(memberName,value,getStringType());
  }

  // ========================================================================
  // Private methods

  private String parseQuotedValue(String line, StringTokenizer st) {
    StringWriter out = new StringWriter();
    while(true) {
      int endQuote = line.indexOf(VALUE_QUOTE);
      if (endQuote == -1) {
        out.write(line);
        if (!st.hasMoreTokens()) return out.toString();
        out.write('\n');
        line = st.nextToken().trim();
        continue;
      } else {
        out.write(line.substring(0,endQuote).trim());
        return out.toString();
      }
    }
  }

}
