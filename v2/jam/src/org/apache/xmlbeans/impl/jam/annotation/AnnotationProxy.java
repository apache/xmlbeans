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

import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;

import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Iterator;
import java.util.Enumeration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.io.StringWriter;

/**
 * <p>Provides a proxied view of some annotation artifact.  JAM calls the
 * public methods on this class to initialize the proxy with annotation
 * values; those methods should not be called by user code.</p>
 *
 * <p>This class provides default implementations of
 * initFromAnnotationInstance() and initFromJavadocTag() which will often be
 * sufficient.  However, extending classes are free to override them if
 * they need to specialize how tags and annotations are mapped into the
 * proxy's member values.  A typical example might be overriding
 * <code>initFromJavadocTag()</code> in provide a specialized parsing of
 * the tags's name-value pairs.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class AnnotationProxy {

  // ========================================================================
  // Constants

  /**
   * <p>Name of the member of annotations which have only a single member.
   * As specified in JSR175, that name is "value", but you should use
   * this constant to prevent typos.</p>
   */
  public static final String SINGLE_MEMBER_NAME = "value";


  /**
   * <p>The delimiters to use by default when parsing out name=value pairs
   * from a javadoc tag.</p>
   */
  private static final String DEFAULT_NVPAIR_DELIMS = "\n\r";

  private static final String VALUE_QUOTE = "\"";


  // ========================================================================
  // Variables

  private JamServiceContext mContext;

  //FIXME need to expose a knob for setting this
  private String mNvPairDelims = DEFAULT_NVPAIR_DELIMS;

  // ========================================================================
  // Initialization methods - called by JAM, don'

  /**
   * <p>Called by JAM to initialize the proxy.  Do not try to call this
   * yourself.</p>
   */
  public void init(JamServiceContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null logger");
    mContext = ctx;
  }

  // ========================================================================
  // Public abstract methods

  /**
   * <p>Called by JAM to initialize a named member on this annotation proxy.
   * </p>
   */
  public abstract void setValue(String name, Object value);

  //docme
  public abstract JAnnotationValue[] getValues();


  //docme
  public JAnnotationValue getValue(String named) {
    if (named == null) throw new IllegalArgumentException("null name");
    //FIXME this impl is very gross
    named = named.trim();
    JAnnotationValue[] values = getValues();
    for(int i=0; i<values.length; i++) {
      
      if (named.equals(values[i].getName())) return values[i];
    }
    return null;
  }

  // ========================================================================
  // Public methods

  /**
   * <p>Called by JAM to initialize this proxy's properties using a
   * JSR175 annotation instnce.  The value is guaranteed to be an instance
   * of the 1.5-specific <code>java.lang.annotation.Annotation</code>
   * marker interface.  (It's typed as <code>Object</code> in order to
   * preserve pre-1.5 compatibility).</p>
   *
   * <p>The implementation of this method introspects the given object
   * for JSR175 annotation member methods, invokes them, and then calls
   * <code>setMemberValue</code> using the method's name and invocation
   * result as the name and value.</p>
   *
   * <p>Extending classes are free to override this method if different
   * behavior is required.</p>
   */
  public void initFromAnnotationInstance(Object jsr175annotationObject) {
    if (jsr175annotationObject == null) throw new IllegalArgumentException();
    Class annType = jsr175annotationObject.getClass();
    //FIXME this is a bit clumsy right now - I think we need to be a little
    // more surgical in identifying the annotation member methods
    Method[] methods = annType.getMethods();
    for(int i=0; i<methods.length; i++) {
      int mods = methods[i].getModifiers();
      if (Modifier.isStatic(mods)) continue;
      if (!Modifier.isPublic(mods)) continue;
      if (methods[i].getParameterTypes().length > 0) continue;
      {
        // try to limit it to real annotation methods.  
        // FIXME seems like this could be better
        Class c = methods[i].getDeclaringClass();
        String name = c.getName();
        if (name.equals("java.lang.Object") ||
          name.equals("java.lang.annotation.Annotation")) {
          continue;
        }
      }
      try {
        setValue(methods[i].getName(),
                 methods[i].invoke(jsr175annotationObject,null));
      } catch (IllegalAccessException e) {
        //getLogger().warning(e);
      } catch (InvocationTargetException e) {
        //getLogger().warning(e);
      }
    }
  }


  //REVIEW i'm not sure this is sufficient.  they might need access to
  //the whole tag (including the name) when doing this.  they may
  //want to tell us what the tag name is.  a bit of a chicken-and-egg
  //problem.  i guess if the need to do that, they just have to write
  //their own CommentInitializer.

  /**
   * <p>Called by JAM to initialize this proxy's properties using a
   * javadoc tag.  The parameter will contain the raw contents of the tag,
   * excluding the name declaration (i.e. everything after the '@mytag').</p>
   *
   * <p>The implementation of this method parses the tagContents
   * for 'name = value' pairs delimited by line breaks.  If one or more such
   * pairs is found, <code>setMemberValue</code> is called for each.  If no
   * such pairs are found, <code>setMemberValue()</code> is called using
   * SINGLE_MEMBER_NAME as the name and the tag contents as the value.</p>
   *
   * <p>Extending classes are free to override this method if different
   * behavior is required.</p>
   */

  public void initFromJavadocTag(String tagline) {
    if (tagline == null) throw new IllegalArgumentException("null tagline");
    tagline = tagline.trim();
    if (tagline.length() == 0) return;
    Properties props = new Properties();
    parseAssignments(props,tagline);
    if (props.size() == 0) {
      setValue(SINGLE_MEMBER_NAME,tagline);
    } else {
      Enumeration names = props.propertyNames();
      while(names.hasMoreElements()) {
        String name = (String)names.nextElement();
        setValue(name,props.getProperty(name));
      }
    }
  }

  // ========================================================================
  // Protected methods

  /**
   * <p>Returns an instance of JamLogger that this AnnotationProxy should use
   * for logging debug and error messages.</p>
   */
  protected JamLogger getLogger() { return mContext; }


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
    String result = new String();
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

  /*
  OLD TAG PARSING CODE

  public void initFromJavadocTag(String tagline) {
    if (tagline == null) throw new IllegalArgumentException("null tagline");
    StringTokenizer st = new StringTokenizer(tagline, mNvPairDelims);
    while (st.hasMoreTokens()) {
      String pair = st.nextToken();
      int eq = pair.indexOf('=');
      if (eq <= 0) continue; // if not there or is first character
      String name = pair.substring(0, eq).trim();
      if (eq < pair.length() - 1) {
        String value = pair.substring(eq + 1).trim();
        if (value.startsWith(VALUE_QUOTE)) {
          value = parseQuotedValue(value.substring(1),st);
        }
        setValue(name,value);
      }
    }
  }

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
  */
}