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
import java.io.IOException;

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * @deprecated phasing this out in favor of RuntimeTylar and BuildtimeTylar.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class TylarImpl extends BaseTylarImpl implements Tylar, TylarWriter {

  // ========================================================================
  // Variables

  private URL[] mLocations;
  private BindingFile mBindingFile = null;
  private Collection mSchemas = null;
  private SchemaTypeSystem mSts = null;

  // ========================================================================
  // Constructors

  public TylarImpl() {}


  /**
   * @param locations source uri or null
   * @param bf the binding file
   * @param schemas the schemas
   * @param sts schema type system (or null)
   */
  public TylarImpl(URL[] locations,
                   BindingFile bf,
                   Collection schemas,
                   SchemaTypeSystem sts)
  {
    mLocations = locations;
    mBindingFile = bf;
    mSchemas = schemas;
    mSts = sts;
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

  public SchemaTypeLoader getSchemaTypeLoader() throws IOException, XmlException {
    if (mSts == null) {
      mSts = getDefaultSchemaTypeSystem();
    }
    return mSts;
  }

  public URL[] getLocations() {
    return mLocations;
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

  public void writeSchemaTypeSystem(SchemaTypeSystem sts) {
    mSts = sts;
  }

  public JavaOutputStream getJavaOutputStream() {
    throw new UnsupportedOperationException
            ("In-memory tylar does not support java code generation.");
  }

  public void close() {}

  // ========================================================================
  // Deprecated methods
  
  /**
   * @deprecated
   */
  public TylarImpl(URL[] sourceUrls,
                   BindingFile bf,
                   Collection schemas)
  {
    this(sourceUrls,bf,schemas,null);
  }

}