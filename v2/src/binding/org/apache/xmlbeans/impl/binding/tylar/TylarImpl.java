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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * Simple implementation of Tylar.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class TylarImpl extends BaseTylarImpl implements Tylar, TylarWriter {

  // ========================================================================
  // Variables

  private URI mSourceURI;
  private BindingFile mBindingFile = null;
  private Collection mSchemas = null;

  // ========================================================================
  // Constructors

  public TylarImpl() {}

  /**
   * @param sourceUri source uri or null
   * @param bf the binding file
   * @param schemas the schemas
   */
  public TylarImpl(URI sourceUri,
                   BindingFile bf,
                   Collection schemas)
  {
    mSourceURI = sourceUri;
    mBindingFile = bf;
    mSchemas = schemas;
  }

  // ========================================================================
  // Tylar implementation

  public BindingFile[] getBindingFiles() {
    return new BindingFile[] {mBindingFile};
  }

  public SchemaDocument[] getSchemas() {
    if (mSchemas == null) return new SchemaDocument[0];
    SchemaDocument[] out = new SchemaDocument[mSchemas.size()];
    mSchemas.toArray(out);
    return out;
  }

  public URI getLocation() {
    return mSourceURI;
  }

  public ClassLoader createClassLoader(ClassLoader parent) {
    try {
      return new URLClassLoader(new URL[] {mSourceURI.toURL()},parent);
    } catch(MalformedURLException mue){
      throw new RuntimeException(mue); //FIXME this is bad
    }
  }

  // ========================================================================
  // TylarWriter implementation
  //
  //  This is useful in the case where we want to build up an in-memory tylar
  //  that does not need to be persisted.

  public void writeBindingFile(BindingFile bf) {
    mBindingFile = bf;
  }

  public void writeSchema(SchemaDocument xsd, String path) {
    if (mSchemas == null) mSchemas = new ArrayList();
    mSchemas.add(xsd);
  }

  public JavaOutputStream getJavaOutputStream() {
    throw new UnsupportedOperationException
            ("In-memory tylar does not support java code generation.");
  }

  public void close() {}
}