package org.apache.xmlbeans.impl.binding.tylar;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.io.IOException;

/**
 * Interface which is used at compile time for building up a tylar.
 */
public interface TylarWriter {

  /**
   * Writes the given BindingFile into the tylar.
   *
   * @param bf The binding file
   * @throws IOException
   */
  public void writeBindingFile(BindingFile bf) throws IOException;

  /**
   * Writes the given schema into the tylar.
   *
   * @param xsd  The schema
   * @param filepath Path relative to the 'schemas' directory of the tylar
   * @throws IOException
   */
  public void writeSchema(SchemaDocument xsd, String filepath) throws IOException;

  /**
   * Returns the JavaOutputStream which should be used for creating new java
   * code to be stored in this tylar.
   */
  public JavaOutputStream getJavaOutputStream();

}
