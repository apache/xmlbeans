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

import java.net.URI;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Abstract representation of a type library archive.  This is the interface
 * which is used by the binding runtime for retrieving information about a
 * tylar.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface Tylar {

  // ========================================================================
  // Public methods

  /**
   * Returns a short textual description of this tylar.  This is primarily
   * useful for logging and debugging.
   */
  public String getDescription();

  /**
   * Returns a URI describing the location of the physical store from
   * which this Tylar was loaded.  This is useful for logging purposes.
   */
  public URI getLocation();

  /**
   * Returns the binding files contained in this Tylar.
   */
  public BindingFile[] getBindingFiles();

  /**
   * Returns the schema documents contained in this Tylar.
   */
  public SchemaDocument[] getSchemas();

  /**
   * Returns a BindingLoader for the bindings in this tylar.  This is really
   * just a convenience method; it simply returns a composite of the binding
   * files returned by getBindingFiles() plus the BuiltinBindingLoader.
   */
  public BindingLoader getBindingLoader();

  /**
   * Returns a BindingLoader for the bindings in this tylar.  This is really
   * just a convenience method; it simply returns a the schema type system
   * that results from compiling all of the schemas returned by getSchemas()
   * plus the BuiltinSchemaTypeSystem.
   */
  public SchemaTypeLoader getSchemaTypeLoader();


  /**
   * Returns a JClassLoader which can be used to load descriptions of the
   * java types contained in this tylar.
   */
  public JamClassLoader getJamClassLoader();

  /**
   * Returns a new ClassLoader that can load any class files contained in
   * this tylar.  Returns null if this tylar contains no class resources.
   *
   * REVIEW are we sure this method is needed?
   *
   * @param parent The parent for new classloader.
   */
  public ClassLoader createClassLoader(ClassLoader parent);
}
