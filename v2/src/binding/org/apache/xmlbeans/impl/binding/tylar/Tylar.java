package org.apache.xmlbeans.impl.binding.tylar;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.XmlException;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.net.URI;
import java.io.File;
import java.io.IOException;

/**
 * Abstract representation of a type library archive.  This is the interface
 * which is used by the binding runtime for retrieving information about a
 * tylar.
 */
public interface Tylar {

  // ========================================================================
  // Public methods

  /**
   * Returns the binding file for this Tylar.
   */
  public BindingFile getBindingFile() throws IOException, XmlException;

  /**
   * Returns the schemas contained in this Tylar.
   */
  public SchemaDocument[] getSchemas() throws IOException, XmlException;

  /**
   * Returns a URI describing the location of the physical store from
   * which this Tylar was built.  This is useful for logging purposes.
   */
  public URI getLocation();

}
