/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.jam.internal.javadoc;

import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JFileSet;
import org.apache.xmlbeans.impl.jam.internal.JFileSetImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class does its best to encapsulate and make threadsafe the
 * nastiness that is the javadoc 'invocation' API.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class JDClassLoaderFactory extends Doclet {

  // ========================================================================
  // Variables

  private static RootDoc mCurrentRoot = null;

  // ========================================================================
  // Singleton

  private static final JDClassLoaderFactory INSTANCE =
          new JDClassLoaderFactory();

  private JDClassLoaderFactory() {
  }

  public static final JDClassLoaderFactory getInstance() {
    return INSTANCE;
  }

  // ========================================================================
  // Public methods

  /**
   * Runs javadoc on the given source directory and returns a JDRoot
   * which wraps the javadoc view of the source files there.
   */
  public synchronized JDClassLoader create(JFileSet fileset,
                                           JClassLoader parentLoader,
                                           JAnnotationLoader annLoader,
                                           PrintWriter out)
          throws IOException, FileNotFoundException {
    List argList = new ArrayList();
    argList.add("-private");

    String cp = ((JFileSetImpl)fileset).getClasspath();
    if (cp != null) {
      argList.add("-docletpath");
      argList.add(cp);
      argList.add("-classpath");
      argList.add(cp);
    }
    File[] files = fileset.getFiles();
    if (files.length == 0) {
      throw new FileNotFoundException("No input files found.");
    }
    for (int i = 0; i < files.length; i++) {
      argList.add(files[i].toString());
      if (out != null) out.println(files[i].toString());
    }
    String[] args = new String[argList.size()];
    argList.toArray(args);
    // create a buffer to capture the crap javadoc spits out.  we'll
    // just ignore it unless something goes wrong
    StringWriter spew = new StringWriter();
    PrintWriter spewWriter = new PrintWriter(spew);
    int result =
            com.sun.tools.javadoc.Main.execute("JAM",
                    spewWriter,
                    spewWriter,
                    spewWriter,
                    this.getClass().getName(),
                    args);
    if (result != 0 || mCurrentRoot == null) {
      spewWriter.flush();
      throw new RuntimeException("Unknown javadoc problem: result=" + result +
              ", mCurrentRoot=" + mCurrentRoot + ":\n" +
              spew.toString());
    }
    JDClassLoader loader = new JDClassLoader(mCurrentRoot, parentLoader);
    mCurrentRoot = null;
    return loader;
  }

  // ========================================================================
  // Doclet 'implementation'

  public static boolean start(RootDoc root) {
    if (mCurrentRoot != null) {  // should not be possible
      throw new IllegalStateException();
    }
    mCurrentRoot = root;
    return true;
  }
}
