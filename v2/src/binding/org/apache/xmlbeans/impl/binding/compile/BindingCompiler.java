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
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.PathBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Collection;

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
 * Also note that BindingCompilers are not threadsafe.  It's not clear why
 * multiple threads would want to access one, anyway.  Just don't do it.
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
  private Tylar[] mBaseLibraries = null;
  private BindingLoader mBaseBindingLoader = null;
  private SchemaTypeLoader mBaseSchemaTypeLoader = null;
  private boolean mIsCompilationStarted = false;

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
  public abstract void bind(TylarWriter writer);

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
   * Sets the list type libraries that should be checked for resolving
   * bindings before creating bindings in the new tylar being compiled.
   * Bindings provided in these tylars will preempt the compilation of
   * equivalent bindings in the tylar generated by this compiler, so it is
   * the user's responsibility to ensure that these tylars are made available
   * at runtime.
   *
   * This is an optional setting; if the libraries are not provided, only the
   * default (builtin) loader will be used.
   */
  public void setBaseLibraries(Tylar[] list) {
    assertCompilationStarted(false);
    if (list == null) throw new IllegalArgumentException("null list");
    mBaseLibraries = list;
  }

  /**
   * Sets the BindingLogger which will receive log messages from work
   * done by this BindingCompiler.
   */
  public void setLogger(BindingLogger logger) {
    if (logger == null) throw new IllegalArgumentException("null logger");
    assertCompilationStarted(false);
    mLogger = logger;
  }

  /**
   * Sets whether this compiler should return a result and keep artificats
   * produced even when compilation encounters one or more severe errors.
   * This is false bydefault, and generally should be made true only
   * for debugging.
   */
  public void setIgnoreSevereErrors(boolean really) {
    assertCompilationStarted(false);
    mIgnoreErrors = true;
  }

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  Note that not all
   * BindingCompilers generate any source code at all, so setting this may
   * have no effect.
   */
  public void setCompileJava(boolean b) {
    assertCompilationStarted(false);
    mDoCompile = b;
  }

  /**
   * Sets the location of javac to be invoked.  Default compiler is used
   * if this is not set.  Ignored if doCompile is set to false.  Also note
   * that not all BindingCompilers generate any source code at all, so
   * setting this may have no effect.
   */
  public void setJavac(String javacPath) {
    assertCompilationStarted(false);
    mJoust.setJavac(javacPath);
  }

  /**
   * Sets the classpath to use for compilation of generated sources.
   * The System classpath is used by default.  This is ignored if doCompile is
   * false.  Also note that not all BindingCompilers generate any source
   * code at all, so setting this may have no effect.
   */
  public void setJavacClasspath(File[] classpath) {
    assertCompilationStarted(false);
    mJoust.setJavacClasspath(classpath);
  }

  /**
   * Sets whether this BindingCompiler should keep any generated java source
   * code it generates.  The default is true.  This will have no effect if
   * doCompile is set to false.  Also note that not all BindingCompilers
   * generate any source code at all, so setting this may have no effect.
   */
  public void setKeepGeneratedJava(boolean b) {
    assertCompilationStarted(false);
    mJoust.setKeepGenerated(b);
  }

  /**
   * Enables verbose output to our BindingLogger.
   */
  public void setVerbose(boolean b) {
    assertCompilationStarted(false);
    mJoust.setVerbose(b);
    mVerbose = b;
  }

  // ========================================================================
  // Protected methods

  /**
   * Subclasses should call this method to retrieve the BindingLoader
   * to use as a basis for the binding process.  Normally, this will
   * simply be the builtin loader.  However, if the user has
   * setExistingBindings, the returned loader will also include those
   * bindings as well.  Note that this method must not be called until
   * binding has actually begun.
   *
   * @throws IllegalStateException if this method is called before
   * the abstract bind() method is called.
   */
  protected BindingLoader getBaseBindingLoader() {
    assertCompilationStarted(true);
    if (mBaseBindingLoader == null) {
      if (mBaseLibraries == null) {
        mBaseBindingLoader = BuiltinBindingLoader.getInstance();
      } else {
        //new up a loader on the bindings provided in the base libraries
        //they gave us
        BindingLoader[] loaders = new BindingLoader[mBaseLibraries.length+1];
        for(int i=0; i<mBaseLibraries.length; i++) {
          loaders[i] = mBaseLibraries[i].getBindingFile();
        }
        loaders[loaders.length-1] = BuiltinBindingLoader.getInstance();
        mBaseBindingLoader = PathBindingLoader.forPath(loaders);
      }
    }
    return mBaseBindingLoader;
  }


  /**
   * Returns a SchemaTypeLoader to be used as a basis for the binding process.
   * Normally, this will simply be the builtin loader.  However, if the user
   * has setBaseLibraries, the returned loader will also include the schema
   * types in those libraries.  Note that this method must not be called until
   * binding has actually begun.
   *
   * @throws IllegalStateException if this method is called before
   * the abstract bind() method is called.
   */
  protected SchemaTypeLoader getBaseSchemaTypeLoader() throws XmlException
  {
    assertCompilationStarted(true);
    if (mBaseSchemaTypeLoader == null) {
      if (mBaseLibraries == null) {
        mBaseSchemaTypeLoader = XmlBeans.getBuiltinTypeSystem();
      } else {
        //create a schema type loader based on all of the schemas in all
        //of the base tylars
        Collection schemas = new ArrayList();
        for(int i=0; i<mBaseLibraries.length; i++) {
          SchemaDocument[] xsds = mBaseLibraries[i].getSchemas();
          for(int j=0; j<xsds.length; j++) schemas.add(xsds[j].getSchema());
        }
        XmlObject[] xxds = new XmlObject[schemas.size()];
        schemas.toArray(xxds);
        mBaseSchemaTypeLoader = XmlBeans.
                compileXsd(xxds,XmlBeans.getBuiltinTypeSystem(),null);
      }

    }
    return mBaseSchemaTypeLoader;
  }

  /**
   * Asserts that binding compilation has or has not yet begun.  Some
   * operations can only occur when the compiler is being initialized, and
   * some can only occur after initialization is complete (i.e. binding
   * after bind() has been called).
   *
   * @throws IllegalStateException if the assertion fails.
   */
  protected void assertCompilationStarted(boolean isStarted) {
    if (mIsCompilationStarted != isStarted) {
      throw new IllegalStateException
              ("This method cannot be invoked "+
               (mIsCompilationStarted ? "after" : "before")+
               "binding compilation has begun");
    }
  }

  /**
   * Should be the first thing called in every implmentation of
   * bind(TylarWriter).  Just tells us that the compiler's state has changed.
   */
  protected void notifyCompilationStarted() {
    mIsCompilationStarted = true;
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
