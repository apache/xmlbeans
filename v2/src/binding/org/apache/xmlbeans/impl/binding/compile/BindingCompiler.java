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
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.logger.BindingLogger;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JFactory;
import org.apache.xmlbeans.*;

import java.io.File;
import java.io.IOException;

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
public abstract class BindingCompiler extends BindingLogger
        implements TypeMatcherContext {

  // ========================================================================
  // Variables

  private Tylar mBaseTylar = null;
  private boolean mIsCompilationStarted = false;

  // ========================================================================
  // Constructors

  public BindingCompiler() {}

  // ========================================================================
  // Abstract/Overrideable methods

  /**
   * Implemented by extending class, does the real binding work using the
   * given TylarWriter.  Note that this method doesn't not allow an exception
   * to be thrown; the extending class should call logError() with any
   * unexpected exceptions -  BindingCompiler takes care of the rest.
   */
  protected abstract void internalBind(TylarWriter writer);

  /**
   * Creates the ExplodedTylarImpl/Writer to be used when one of the bindAs...
   * methods is invoked.  This can be overridden by subclasses that need
   * to do more than simply create an ExplodedTylarImpl (such as attach
   * a JavaOutputStream).
   */
  protected ExplodedTylarImpl createDefaultExplodedTylarImpl(File destDir)
          throws IOException
  {
    return ExplodedTylarImpl.create(destDir,null);
  }

  // ========================================================================
  // Public methods

  /**
   * Public method for beginning the binding compilation process with
   * an arbitrary TylarWriter.  Delegates to the subclass to do the real work.
   */
  public final void bind(TylarWriter writer) {
    mIsCompilationStarted = true;
    internalBind(writer);
  }

  /**
   * Performs the binding and returns an exploded tylar in the specified
   * directory.  Returns null if any severe errors were encountered.
   */
  public ExplodedTylar bindAsExplodedTylar(File tylarDestDir)  {
    ExplodedTylarImpl tylar;
    try {
      tylar = createDefaultExplodedTylarImpl(tylarDestDir);
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
    bind(tylar); //ExplodedTylarImpl is also a TylarWriter
    try {
      // close it up.  this may cause the generated code to be compiled.
      tylar.close();
    } catch(IOException ioe) {
      logError(ioe);
    }
    return !super.isAnyErrorsFound() || super.isIgnoreErrors() ? tylar : null;
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
   * Sets the base type libraries that should be checked for resolving
   * bindings before creating bindings in the new lib being compiled.
   *
   * This is an optional setting; if the lib is not provided, only the
   * default (builtin) loader will be used.
   */
  public void setBaseLibrary(Tylar lib) {
    if (lib == null) throw new IllegalArgumentException("null lib");
    mBaseTylar = lib;
  }


  // ========================================================================
  // BindingLogger overrides

  /**
   * Sets whether this compiler should return a result and keep artificats
   * produced even when compilation encounters one or more severe errors.
   * This is false bydefault, and generally should be made true only
   * for debugging.
   */
  public void setIgnoreSevereErrors(boolean really) {
    assertCompilationStarted(false);
    super.setIgnoreErrors(really);
  }

  /**
   * Enables verbose output to our MessageSink.
   */
  public void setVerbose(boolean b) {
    assertCompilationStarted(false);
    super.setVerbose(b);
  }

  // ========================================================================
  // TypeMatcherContext implementation

    public BindingLogger getLogger() { return this; }

  /**
   * Subclasses should call this method to retrieve the BindingLoader
   * to use as a basis for the binding process.  Normally, this will
   * simply be the builtin loader.  However, if the user has
   * setBaseLibrary, the returned loader will also include those
   * bindings as well.  Note that this method must not be called until
   * binding has actually begun.
   *
   * @throws IllegalStateException if this method is called before
   * the abstract bind() method is called.
   */
  public BindingLoader getBaseBindingLoader() {
    assertCompilationStarted(true);
    if (mBaseTylar == null) {
      return BuiltinBindingLoader.getInstance();
    } else {
      return mBaseTylar.getBindingLoader();
    }
  }

  /**
   * Returns a SchemaTypeLoader to be used as a basis for the binding process.
   * Normally, this will simply be the builtin loader.  However, if the user
   * has setBaseLibrary, the returned loader will also include the schema
   * types in those libraries.  Note that this method must not be called until
   * binding has actually begun.
   *
   * @throws IllegalStateException if this method is called before
   * the abstract bind() method is called.
   */
  public SchemaTypeSystem getBaseSchemaTypeSystem()
  {
    assertCompilationStarted(true);
    if (mBaseTylar == null) {
      return XmlBeans.getBuiltinTypeSystem();
    } else {
      return mBaseTylar.getSchemaTypeSystem();
    }
  }

  /**
   * Returns a JClassLoader to be used as a basis for the binding process.
   * Normally, this will simply be the loader backed by the system
   * classloader.  However, if the user has setBaseLibrary, the returned
   * loader will also load java classes that may be stored those libraries.
   * Note that this method must not be called until binding has actually
   * begun.
   *
   * @throws IllegalStateException if this method is called before
   * the abstract bind() method is called.
   */
  public JClassLoader getBaseJavaTypeLoader()
  {
    assertCompilationStarted(true);
    if (mBaseTylar == null) {
      return JFactory.getInstance().
                createClassLoader(ClassLoader.getSystemClassLoader(),null,null);
    } else {
      return mBaseTylar.getJClassLoader();
    }
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

  // ========================================================================
  // Private methods

  private static File createTempDir() throws IOException
  {
    //FIXME this is not great
    String prefix = "BindingCompiler-"+System.currentTimeMillis();
    File directory = null;
    File f = File.createTempFile(prefix, null);
    directory = f.getParentFile();
    f.delete();
    File out = new File(directory, prefix);
    if (!out.mkdirs()) throw new IOException("Uknown problem creating temp file");
    return out;
  }
}
