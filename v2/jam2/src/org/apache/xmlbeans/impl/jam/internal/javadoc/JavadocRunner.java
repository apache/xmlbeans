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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * <p>This class does its best to encapsulate and make threadsafe the
 * nastiness that is the javadoc 'invocation' API.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocRunner extends Doclet {

  // ========================================================================
  // Constructor

  /* package */ JavadocRunner() {}

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
                                       String[] javadocArgs)
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
}