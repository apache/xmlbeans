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

/**
 * Exception thrown to indicate the likely manifestation of a thorny
 * classloading problem with javadoc.  See the EXPLANATION constant for
 * details.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocClassloadingException extends RuntimeException {

  // ========================================================================
  // Constants

  private static final String SOME_CLASS_IN_TOOLS_JAR =
    "com.sun.javadoc.Doclet";

  private static final String STANDARD_EXPLANATION =
    "An error has occurred while invoking javadoc to inspect your source\n"+
    "files.  This may be due to the fact that $JAVA_HOME/lib/tools.jar does\n"+
    "not seem to be in your system classloader.  One common case in which \n"+
    "this happens is when using the 'ant' tool, which uses a special\n"+
    "context classloader to load classes from tools.jar.\n"+
    "\n"+
    "This situation elicits what is believed to a javadoc bug in the initial\n"+
    "release of JDK 1.5.  Javadoc attempts to use its own context classloader\n"+
    "tools.jar but ignores one that may have already been set, which leads\n"+
    "to some classes being loaded into two different classloaders.  The\n"+
    "telltale sign of this problem is a javadoc error message saying that\n"+
    "'languageVersion() must return LanguageVersion - you might see this\n"+
    "message in your process' output.\n"+
    "\n"+
    "This will hopefully be fixed in a later release of JDK 1.5; if a new\n"+
    "version of 1.5 has become available, you might be able to solve this\n"+
    "by simply upgrading to the latest JDK.\n"+
    "\n"+
    "Alternatively, you can work around it by simply including \n"+
    "$JAVA_HOME/lib/tools.jar in the java -classpath\n"+
    "parameter.  If you are running ant, you will need to modify the standard\n"+
    "ant script to include tools.jar in the -classpath.\n";



  // ========================================================================
  // Static utilities

  /**
   * Returns true if tools.jar is not in the system classloader.  If true, it
   * is very likely that any javadoc failures are due to the problem described
   * above.
   */
  public static boolean isClassloadingProblemPresent() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    try {
      cl.loadClass(SOME_CLASS_IN_TOOLS_JAR);
      return false;
    } catch(ClassNotFoundException cnfe) {
      return true;
    }
  }

  // ========================================================================
  // Constructors

  public JavadocClassloadingException() {
    super((ALTERNATE_EXPLANATION != null) ? ALTERNATE_EXPLANATION :
            STANDARD_EXPLANATION);
  }

  // ========================================================================
  // hack

  private static String ALTERNATE_EXPLANATION = null;

  /**
   * Quick and dirty way for user to alter the explanation for this problem
   * with more specific information or solutions.
   */
  public static void setExplanation(String msg) {
    ALTERNATE_EXPLANATION = msg;
  }

}
