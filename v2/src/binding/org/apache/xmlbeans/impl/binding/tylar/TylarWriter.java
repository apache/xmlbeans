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

package org.apache.xmlbeans.impl.binding.tylar;

import java.io.IOException;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Interface which is used at compile time for building a tylar.
 *
 * @author Patrick Calahan <pcal@bea.com>
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
   * code to be stored in this tylar.  Note that the caller should never
   * close this stream directly; it will be closed by TylarWriter.close();
   * This method may return null, indicating the writer does not support
   * java code generation.
   */
  public JavaOutputStream getJavaOutputStream();

  /**
   * Should be exactly once called when the tylar is complete.  This signals
   * the implementation that any outstanding files should be flushed to disk,
   * for example.  It also signals that the JavaOutputStream associated with
   * this tylar should be closed (which in some cases may trigger compilation
   * of java sources).
   */
  public void close() throws IOException;

}
