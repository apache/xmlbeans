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

import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.LanguageVersion;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.impl.jam.provider.JamLogger;


/**
 * <p>This class does its best to encapsulate and make threadsafe the
 * nastiness that is the javadoc 'invocation' API.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocRunner extends Doclet {

  // ========================================================================
  // Constants
  
  private static final String JAVADOC_RUNNER_150 =
    "org.apache.xmlbeans.impl.jam.internal.java15.JavadocRunner_150";
  
  // ========================================================================
  // Factory methods
  
  public static JavadocRunner newInstance() {
    //REVIEW we should probably normalize the handling of 1.5-specific class
    //instantation and error handling
    try {
      Class onefive = Class.forName(JAVADOC_RUNNER_150);
      return (JavadocRunner)onefive.newInstance();
    } catch(ClassNotFoundException cnfe) {
    } catch (IllegalAccessException e) {
    } catch (InstantiationException e) {
    }
    return new JavadocRunner();
  }
  
  // ========================================================================
  // Constructor

  public JavadocRunner() {}

  // ========================================================================
  // Public methods

  /**
   * Runs javadoc on the given source directory and returns a JDRoot
   * which wraps the javadoc view of the source files there.
   */
  /*package*/ synchronized RootDoc run(File[] files,
                                       PrintWriter out,
                                       String sourcePath,
                                       String classPath,
                                       String[] javadocArgs,
                                       JamLogger logger)
          throws IOException, FileNotFoundException
  {
    if (files == null || files.length == 0) {
      throw new FileNotFoundException("No input files found.");
    }
    List argList = new ArrayList();
    if (javadocArgs != null) {
      argList.addAll(Arrays.asList(javadocArgs));
    }
    argList.add("-private");
    if (sourcePath != null) {
      argList.add("-sourcepath");
      argList.add(sourcePath);
    }
    if (classPath != null) {
      argList.add("-classpath");
      argList.add(classPath);
      argList.add("-docletpath");
      argList.add(classPath);
    }
    for(int i=0; i<files.length; i++) {
      argList.add(files[i].toString());
      if (out != null) out.println(files[i].toString());
    }
    String[] args = new String[argList.size()];
    argList.toArray(args);
    // create a buffer to capture the crap javadoc spits out.  we'll
    // just ignore it unless something goes wrong
    PrintWriter spewWriter;
    StringWriter spew = null;
    if (out == null) {
      spewWriter = new PrintWriter(spew = new StringWriter());
    } else {
      spewWriter = out;
    }
    ClassLoader originalCCL = Thread.currentThread().getContextClassLoader();
    try {
      JavadocResults.prepare();
      if (logger.isVerbose(this)) {
        logger.verbose("Invoking javadoc.  Command line equivalent is: ");
        StringWriter sw = new StringWriter();
        sw.write("javadoc ");
        for(int i=0; i<args.length; i++) {
          sw.write("'");
          sw.write(args[i]);
          sw.write("' ");
        }
        logger.verbose("  "+sw.toString());
      }
      int result = com.sun.tools.javadoc.Main.execute("JAM",
                                                      spewWriter,
                                                      spewWriter,
                                                      spewWriter,
                                                      this.getClass().getName(),
                                                      args);
      RootDoc root = JavadocResults.getRoot();
      if (result != 0 || root == null) {
        spewWriter.flush();
        throw new RuntimeException("Unknown javadoc problem: result="+result+
                                   ", root="+root+":\n"+
                                   ((spew == null) ? "" : spew.toString()));
      }
      return root;
    } catch(RuntimeException e) {
      throw e;
    } finally {
      //clean up the mess
      Thread.currentThread().setContextClassLoader(originalCCL);
    }
  }

  // ========================================================================
  // Doclet 'implementation'

  public static boolean start(RootDoc root) {
    JavadocResults.setRoot(root);
    return true;
  }

  /* need to tuck this away in 1.5-safeville
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }
  */
}