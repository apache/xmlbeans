package org.apache.xmlbeans.impl.binding.tylar;

import java.io.File;

/**
 * These constants describe the physical structure of the tylar archive.
 * The values are subject to change at any time and should not be used
 * outside of this package.
 */
public interface TylarConstants {

  // ========================================================================
  // Constants

  public static final char SEP = File.separatorChar;

  public static final String SRC_ROOT = "src";

  public static final String METAINF = "META-INF";

  public static final String BINDING_FILE = METAINF +SEP+ "binding-file.xml";

  public static final String SCHEMA_DIR = METAINF +SEP+ "schemas";
}
