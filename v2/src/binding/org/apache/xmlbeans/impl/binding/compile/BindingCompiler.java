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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.binding.tylar.*;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.CompositeBindingLoader;
import org.apache.xmlbeans.impl.binding.logger.BindingLogger;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.*;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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
  private BindingLoader mBuiltinBindingLoader = null;
  private List mSchemasToInclude = null;

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
   * <p>Public method for beginning the binding compilation process with
   * an arbitrary TylarWriter.  Delegates to the subclass to do the real work.
   * After delegating to internalBind(), this method will finish by writing
   * out any schemas which were specified for inclusion.</p>
   *
   * Note: the caller of this method is responsible for calling close() on
   * the TylarWriter!
   */
  public final void bind(TylarWriter writer) {
    mIsCompilationStarted = true;
    internalBind(writer);
    if (mSchemasToInclude != null) {
      for(int i=0; i<mSchemasToInclude.size(); i++) {
        SchemaToInclude sti = (SchemaToInclude)mSchemasToInclude.get(i);
        try {
          writer.writeSchema(sti.schema,sti.filepath);
        } catch(IOException ioe) {
          logError(ioe);
        }
      }
    }
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
      if (et == null) {
        logError("Fatal error encountered building tylar.");
        return null;
      } else {
        return et.toJar(tylarJar);
      }
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

  /**
   * Specifies a schema to be included in the output tylar.  This will
   * have no effect on the binding process.
   *
   * @param xsd
   * @param filepath
   */
  public void includeSchema(SchemaDocument xsd, String filepath) {
    if (xsd == null) throw new IllegalArgumentException("null xsd");
    if (filepath == null) throw new IllegalArgumentException("null filepath");
    if (mSchemasToInclude == null) mSchemasToInclude = new ArrayList();
    mSchemasToInclude.add(new SchemaToInclude(xsd,filepath));
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
    BindingLoader builtin =
      (mBuiltinBindingLoader != null) ? mBuiltinBindingLoader :
        BuiltinBindingLoader.getBuiltinBindingLoader(false);
    if (mBaseTylar == null) return builtin;
    BindingLoader[] loaders = null;
    try {
      loaders = new BindingLoader[]{ mBaseTylar.getBindingLoader(), builtin };
    } catch(Exception ioe) {
      logError(ioe);
    }
    return (loaders == null) ? builtin : CompositeBindingLoader.forPath(loaders);
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
  public SchemaTypeLoader getBaseSchemaTypeLoader()
  {
    assertCompilationStarted(true);
    if (mBaseTylar != null) {
    } else {
      try {
        return mBaseTylar.getSchemaTypeLoader();
      } catch(IOException ioe) { logError(ioe);
      } catch(XmlException xe) { logError(xe);
      }
    }
    return XmlBeans.getBuiltinTypeSystem();

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
  public JamClassLoader getBaseJavaTypeLoader()
  {
    assertCompilationStarted(true);
    if (mBaseTylar == null) {
      return JamServiceFactory.getInstance().createSystemJamClassLoader();
    } else {
      return mBaseTylar.getJamClassLoader();
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

  /**
   * <p>Sets the builtin binding loader to use. This method should
   * remain protected - user code should not be setting this directly,
   * though they may set it indirectly via, for example, a 'binding style'
   * switch.</p>
   * @param bl
   */
  protected void setBuiltinBindingLoader(BindingLoader bl) {
    mBuiltinBindingLoader = bl;
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


  private class SchemaToInclude {
    SchemaToInclude(SchemaDocument sd, String fp) {
      schema = sd;
      filepath = fp;

    }
    SchemaDocument schema;
    String filepath;
  }
}
