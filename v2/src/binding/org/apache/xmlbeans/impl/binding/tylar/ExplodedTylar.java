package org.apache.xmlbeans.impl.binding.tylar;

import java.io.File;
import java.io.IOException;

/**
 * An extension of Tylar which is known to exist as an open directory
 * structure.  This is useful for consumers who may need additional control
 * over the generated artifacts, e.g. to manually perform compilation of
 * generated java source files.
 */
public interface ExplodedTylar extends Tylar {

  // ========================================================================
  // Public methods - these services are the 'value add' we provide over
  // just a generic Tylar.

  /**
   * Returns the directory on disk in which the tylar is stored.  Never
   * returns null.
   */
  public File getRootDir();

  /**
   * Returns the directory in which generated source files are stored in
   * the tylar.
   */
  public File getSourceDir();

  /**
   * Returns the directory in which generated class files are stored in
   * the tylar.  (Note that this typically is the same as the root dir).
   */
  public File getClassDir();

  /**
   * Returns the directory in which generated schema files are stored in
   * the tylar.
   */
  public File getSchemaDir();


  /**
   * Jars up the exploded tylar directory into the given file and returns
   * a handle to the JarredTylar.  The main advantage of using this method
   * over jarring it yourself is that this may save you the cost or reparsing
   * the binding file and the schemas, in the event that you want to
   * immediately hand the tylar to the runtime.
   *
   * @param jarfile Destination file for the new jar
   * @return A handle to the newly-created tylar
   * @throws java.io.IOException if the specified jarfile already exists or if an
   * error occurs while writing the file.
   */
  public TylarConstants toJar(File jarfile) throws IOException;
}