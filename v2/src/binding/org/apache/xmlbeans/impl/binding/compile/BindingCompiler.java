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
package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.binding.tylar.*;
import org.apache.xmlbeans.impl.binding.joust.CompilingJavaOutputStream;
import org.apache.xmlbeans.impl.jam.JElement;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Abstract base class for classes which produce a tylar based on
 * java and/or schema inputs.  It deals with the details of creating
 * the various kinds of tylars and allows the extending class to focus
 * only on binding work; extending classes are responsible only for filling
 * out a TylarWriter in the abstract bind() method.  This class also
 * provides convenient logging functionality.
 *
 * Note that the term 'compiler' here is arguably being abused, since
 * a BindingCompiler is stateful; in addition to the compilation logic, it
 * also includes a set of input artifacts (or at least pointers to them).
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BindingCompiler {

  // ========================================================================
  // Constants

  private static final BindingLogger DEFAULT_LOG = new SimpleBindingLogger();

  // ========================================================================
  // Variables

  private BindingLogger mLogger = DEFAULT_LOG;
  private boolean mAnyErrorsFound = false;
  private boolean mIgnoreErrors = false;
  private boolean mVerbose = false;
  private boolean mDoCompile = true;

  // this is the joust we use to build up the tylar that is passed to
  // the subclass' bind() methods in all cases.  However, BindingCompiler
  // makes no assumption that the subclass will actually make use of any
  // of the codegen facilities - they're just there if you want them.
  private CompilingJavaOutputStream mJoust;

  // ========================================================================
  // Constructors

  public BindingCompiler() {
    mJoust = new CompilingJavaOutputStream();
  }

  // ========================================================================
  // Abstract/Overrideable methods

  /**
   * Implemented by extending class, does the real binding work using the
   * given TylarWriter.  Note that this method doesn't not allow an exception
   * to be thrown; the extending class should call logError() with any
   * unexpected exceptions -  BindingCompiler takes care of the rest.
   */
  protected abstract void bind(TylarWriter writer);

  // ========================================================================
  // Public methods

  /**
   * Performs the binding and returns an exploded tylar in the specified
   * directory.  Returns null if any severe errors were encountered.
   */
  public ExplodedTylar bindAsExplodedTylar(File tylarDestDir)
  {
    mJoust.setSourceDir(new File(tylarDestDir,TylarConstants.SRC_ROOT));
    if (mDoCompile) {
      // signal the compile outputstream to compile classes
      mJoust.setCompilationDir(tylarDestDir);
    }
    ExplodedTylarImpl tylar;
    try {
      tylar = ExplodedTylarImpl.create(tylarDestDir,mJoust);
    } catch(IOException ioe) {
      logError(ioe);
      return null;
    }
    if (!tylarDestDir.exists()) {
      if (!tylarDestDir.mkdirs()) {
        logError("failed to create "+tylarDestDir);
        return null;
      }
    }
    bind((TylarWriter)tylar);
    try {
      // close it up.  this may cause the generated code to be compiled.
      System.out.println("COMPILE!!!!!!!!!!!!!!!!!!");
      if (mDoCompile) logVerbose("Compiling java sources...");
      tylar.close();
    } catch(IOException ioe) {
      logError(ioe);
    }
    return !mAnyErrorsFound || mIgnoreErrors ? tylar : null;
  }

  /**
   * Performs the binding and returns a tylar in the specified jar file.
   * Note that this is done by simply creating an exploded tylar in a
   * temporary directory and then jarring up the result.  Returns null if any
   * severe errors were encountered.
   */
  public Tylar bindAsJarredTylar(File tylarJar) {
    File tempDir = null;
    try {
      tempDir = createTempDir();
      tempDir.deleteOnExit();//REVIEW maybe we should delete it ourselves?
      ExplodedTylar et = bindAsExplodedTylar(tempDir);
      return et.toJar(tylarJar);
    } catch(IOException ioe) {
      logError(ioe);
      return null;
    }
  }

  /**
   * Sets the BindingLogger which will receive log messages from work
   * done by this BindingCompiler.
   */
  public void setLogger(BindingLogger logger) {
    if (logger == null) throw new IllegalArgumentException("null logger");
    mLogger = logger;
  }

  /**
   * Sets whether this compiler should return a result and keep artificats
   * produced even when compilation encounters one or more severe errors.
   * This is false bydefault, and generally should be made true only
   * for debugging.
   */
  public void setIgnoreSeverErrors(boolean really) {
    mIgnoreErrors = true;
  }

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  Note that not all
   * BindingCompilers generate any source code at all, so setting this may
   * have no effect.
   */
  public void setCompileJava(boolean b) {
    mDoCompile = b;
  }

  /**
   * Sets the location of javac to be invoked.  Default compiler is used
   * if this is not set.  Ignored if doCompile is set to false.  Also note
   * that not all BindingCompilers generate any source code at all, so
   * setting this may have no effect.
   */
  public void setJavac(String javacPath) {
    mJoust.setJavac(javacPath);
  }

  /**
   * Sets the classpath to use for compilation of generated sources.
   * The System classpath is used by default.  This is ignored if doCompile is
   * false.  Also note that not all BindingCompilers generate any source
   * code at all, so setting this may have no effect.
   */
  public void setJavacClasspath(File[] classpath) {
    mJoust.setJavacClasspath(classpath);
  }

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  This will have no effect if
   * doCompile is set to false.  Also note that not all BindingCompilers
   * generate any source code at all, so setting this may have no effect.
   */
  public void setKeepGeneratedJava(boolean b) {
    mJoust.setKeepGenerated(b);
  }

  /**
   * Enables verbose output to our BindingLogger.
   */
  public void setVerbose(boolean b) {
    mJoust.setVerbose(b);
    mVerbose = b;
  }

  // ========================================================================
  // Protected logging methods

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.  The binding process should attempt
   * to continue even after such errors are encountered so as to identify
   * as many errors as possible in a single pass.
   *
   * @return true if processing should attempt to continue.
   */
  protected boolean logError(JElement context, Throwable error) {
    mAnyErrorsFound = true;
    mLogger.log(Level.SEVERE,null,error,context);
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.  The binding process should attempt
   * to continue even after such errors are encountered so as to identify
   * as many errors as possible in a single pass.
   *
   * @return true if processing should attempt to continue.
   *
   */
  protected boolean logError(JElement context, String msg) {
    mAnyErrorsFound = true;
    mLogger.log(Level.SEVERE,msg,null,context);
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred while performing binding
   * on the given java construct.  The binding process should attempt
   * to continue even after such errors are encountered so as to identify
   * as many errors as possible in a single pass.
   *
   * @return true if processing should attempt to continue.
   */
  protected boolean logError(String msg) {
    mAnyErrorsFound = true;
    mLogger.log(Level.SEVERE,msg,null);
    return mIgnoreErrors;
  }

  /**
   * Logs a message that fatal error that occurred.
   *
   * @return true if processing should attempt to continue.
   */
  protected boolean logError(Throwable t) {
    mAnyErrorsFound = true;
    mLogger.log(Level.SEVERE,null,t);
    return mIgnoreErrors;
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  protected void logVerbose(JElement context, String msg) {
    if (mVerbose) mLogger.log(Level.FINEST,msg,null,context);
  }

  /**
   * Logs an informative message that should be printed only in 'verbose'
   * mode.
   */
  protected void logVerbose(String msg) {
    if (mVerbose) mLogger.log(Level.FINEST,msg,null);
  }

  // ========================================================================
  // Private methods

  private static File createTempDir() throws IOException
  {
    //FIXME this is not great
    String prefix = "java2schema-"+System.currentTimeMillis();
    File directory = null;
    File f = File.createTempFile(prefix, null);
    directory = f.getParentFile();
    f.delete();
    File out = new File(directory, prefix);
    if (!out.mkdirs()) throw new IOException("Uknown problem creating temp file");
    return out;
  }
}
