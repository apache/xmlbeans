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

import java.net.URL;
import java.io.IOException;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
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
   * Returns the binding files contained in this Tylar.
   */
  public BindingFile[] getBindingFiles() /*throws IOException, XmlException*/;

  /**
   * Returns the schema documents contained in this Tylar.  Note that this
   * is an optional operation; Tylars used at runtime (i.e. created by
   * a TylarLoader) will typically throw UnsupportedOperationException
   * in the implementation of this method.
   */
  public SchemaDocument[] getSchemas() /*throws IOException, XmlException*/;

  /**
   * Returns a BindingLoader for the bindings in this tylar.  This is really
   * just a convenience method; it simply returns a composite of the binding
   * files returned by getBindingFiles() plus the BuiltinBindingLoader.
   */
  public BindingLoader getBindingLoader() /*throws IOException, XmlException*/;

  /**
   * Returns a BindingLoader for the bindings in this tylar.  This is really
   * just a convenience method; it simply returns a the schema type system
   * that results from compiling all of the schemas returned by getSchemas()
   * plus the BuiltinSchemaTypeSystem.
   */
  public SchemaTypeLoader getSchemaTypeLoader() throws IOException, XmlException;


  /**
   * Returns a JClassLoader which can be used to load descriptions of the
   * java types contained in this tylar.
   */
  public JamClassLoader getJamClassLoader();


  /**
   * Returns a short textual description of this tylar.  This is primarily
   * useful for logging and debugging.
   */
  public String getDescription();

  /**
   * Returns an array of URLs describing where the tylar resources are being
   * loaded from.  This is generally only useful for debugging purposes.
   * This method may return null.
   */
  public URL[] getLocations();

  /**
   * @deprecated
   */ 
  public URL getLocation();
}
