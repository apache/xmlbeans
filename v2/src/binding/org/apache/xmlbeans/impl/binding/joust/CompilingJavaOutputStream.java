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
package org.apache.xmlbeans.impl.binding.joust;

import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;

/**
 * This a SourceJavaOutputStream which can compile it's results when
 * close() is called.  It always writes source files to disk.
 *
 * @author Patrick Calaham <pcal@bea.com>
 */
public class CompilingJavaOutputStream extends SourceJavaOutputStream
        implements WriterFactory
 {
  // ========================================================================
  // Constants

  private static final String PREFIX = "[CompilingJavaOutputStream] ";

  // ========================================================================
  // Variables

  private FileWriterFactory mWriterFactoryDelegate;
  private List mSourceFiles;
  private File mSourceDir = null;
  private File mCompileDir = null;
  private File[] mJavacClasspath = null;
  private boolean mKeepGenerated;
  private String mJavacPath = null;
  private boolean mDoCompile = true;

  // ========================================================================
  // Constructors

  /**
   * Construct a new CompilingJavaOutputStream which generates java sources
   * in the given directory.  In order to enable compilation of those sources,
   * you must call enableCompilation.
   *
   * @param srcDir Directory in which sources get generated.
   */
  public CompilingJavaOutputStream(File srcDir) {
    this();
    setSourceDir(srcDir);
  }

  /**
   * Constructs a new CompilingJavaOutputStream.  Note that if you use
   * this default constructor, you *must* call setSourceDir at some point
   * before the stream is used; failure to do so will produce an
   * IllegalStateException.
   */
  public CompilingJavaOutputStream() {
    super();
    setWriterFactory(this);
    mSourceFiles = new ArrayList();
  }

  // ========================================================================
  // Public methods

  //REVIEW the naming this directory 'source' seems a little confusing
  /**
   * Sets the source directory to which files will be written.  This can
   * safely be changed mistream if desired.
   */
  public void setSourceDir(File srcDir) {
    if (srcDir == null) throw new IllegalArgumentException("null srcDir");
    mWriterFactoryDelegate = new FileWriterFactory(mSourceDir = srcDir);
  }

  /**
   * Enables compilation of the generated source files into the given
   * directory.  If this method is never called, no compilation will occur.
   */
  public void setCompilationDir(File destDir) {
    mCompileDir = destDir;
  }

  /**
   * Sets the location of javac to be invoked.  Default compiler is used
   * if this is not set.  Ignored if compilationDir is never set.
   */
  public void setJavac(String javacPath) {
    mJavacPath = javacPath;
  }

  /**
   * Sets the classpath to use for compilation.  System classpath is used
   * by default.  This is ignored if compilationDir is never set.
   */
  public void setJavacClasspath(File[] classpath) {
    mJavacClasspath = classpath;
  }

  /**
   * Sets whether generated sources should be kept after compilation.
   * Default is true.  This is ignored if compilationDir is never set.
   */
  public void setKeepGenerated(boolean b) {
    mKeepGenerated = b;
  }

  /**
   * Sets whether javac should be run on the generated sources.  Default
   * is true.
   */
  public void setDoCompile(boolean b) {
    mDoCompile = b;
  }

  // ========================================================================
  // WriterFactory implementation

  /**
   * Delegate to FileWriterFactory, but ask it for Files instead of Writers
   * so that we can keep track of what we need to compile later.
   */
  public Writer createWriter(String packageName, String className)
          throws IOException {
    if (mWriterFactoryDelegate == null) {
      throw new IllegalStateException("delegate never set called on the "+
                                      "CompilingJavaOutputStream");
    }
    File out = mWriterFactoryDelegate.createFile(packageName,className);
    mSourceFiles.add(out);
    return new FileWriter(out);
  }

  // ========================================================================
  // JavaOutputStream implementation

  public void close() throws IOException {
    super.close();
    mLogger.logVerbose(PREFIX+" closing");
    if (mDoCompile && mCompileDir != null) {
      mLogger.logVerbose(PREFIX+"compileDir = "+mCompileDir);
      Iterator i = mSourceFiles.iterator();
      while(i.hasNext()) {
        mLogger.logVerbose(PREFIX+i.next().toString());
      }
      boolean verbose = mLogger.isVerbose();
      boolean result = CodeGenUtil.externalCompile
              (mSourceFiles,mCompileDir,mJavacClasspath,
               verbose,mJavacPath,null,null,!verbose,verbose);
      mLogger.logVerbose(PREFIX+" compilation result: "+result);
      if (!result) {
        throw new IOException("Compilation of sources failed, " +
                              "check log for details.");
      }
      if (!mKeepGenerated) {
        mLogger.logVerbose(PREFIX+" deleting "+mSourceDir);
        mSourceDir.delete();
      }
    }
  }
}