package org.apache.xmlbeans.impl.binding.tylar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import org.apache.xmlbeans.XmlException;

/**
 * Singleton Factory for loading Tylars from a URI.  Currently, only
 * directory and jar tylars are supported.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class TylarFactory {

  // ========================================================================
  // Singleton

  public static final TylarFactory getInstance() {
    return INSTANCE;
  }

  private static final TylarFactory INSTANCE = new TylarFactory();

  private TylarFactory() {
  }

  // ========================================================================
  // Public methods

  public Tylar load(URI uri) throws IOException, XmlException {
    //FIXME we eventually need to be able to deal with other schemes
    File file = new File(uri);
    if (!file.exists()) throw new FileNotFoundException(uri.toString());
    if (file.isDirectory()) {
      return ExplodedTylarImpl.load(file);
    } else {
      return JarredTylar.load(file);
    }
  }

}
