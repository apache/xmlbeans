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

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.CompositeBindingLoader;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.net.URI;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>An implementation of the Tylar interface which is designed to be used
 * at runtime (i.e. by the Un/Marshallers).  This implementation is as
 * possible about loading resources from a given classloader.  Note that it
 * is strictly read-only; a RuntimeTylar subclass should not try to also
 * implement TylarWriter.</p>
 *
 * <p>RuntimeTylar is optimized for the case where getSchemas() and
 * getBindingFiles() never get called, which is typical of the runtime
 * scenario.  Implementing these methods requires being able to scan the
 * contents of a given directory in the resource path, something not provided
 * by ClassLoader; here, that ability is delegated to subclasses who must
 * implement the abstract method listResourcesInPath().</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class RuntimeTylar implements Tylar, TylarConstants {

  // ========================================================================
  // Variables

  private ClassLoader mClassLoader = null;
  private String mDescription = null;
  private URL[] mLocations = null;
  private BindingFile[] mBindingFiles = null;
  private SchemaTypeLoader mSchemaTypeLoader = null;
  private BindingLoader mBindingLoader = null;
  private JamClassLoader mJamClassLoader = null;

  // ========================================================================
  // Constructors

  /*package*/ RuntimeTylar(ClassLoader loader) {
    if (loader == null) throw new IllegalArgumentException("null loader");
    mClassLoader = loader;
    mLocations = new URL[0];
    mDescription = "loaded from "+loader.toString();
  }


  /*package*/ RuntimeTylar(ClassLoader loader, URL[] locs)
  {
    if (loader == null) throw new IllegalArgumentException("null loader");
    if (locs == null) throw new IllegalArgumentException("null locs");
    mClassLoader = loader;
    mLocations = locs;
    mDescription = "loaded from "+locs;
  }

  // ========================================================================
  // Tylar implementation

  public String getDescription() { return mDescription; }

  public URL[] getLocations() { return mLocations; }

  public BindingLoader getBindingLoader() /*throws IOException, XmlException*/ {
try {
    if (mBindingLoader == null) {
      BindingFile[] bfs = getBindingFiles();
      BindingLoader[] loaders = new BindingLoader[bfs.length+1];
      System.arraycopy(bfs,0,loaders,0,bfs.length);
      loaders[loaders.length-1] = BuiltinBindingLoader.getBuiltinBindingLoader(false);
      mBindingLoader = CompositeBindingLoader.forPath(loaders);
    }
    return mBindingLoader;
} catch(Exception e) { throw new RuntimeException(e); }
  }

  public BindingFile[] getBindingFiles() /*throws IOException, XmlException*/ {
try {
    if (mBindingFiles == null) {
      List list = new ArrayList();
      Enumeration urls = mClassLoader.getResources(BINDING_FILE);
      while(urls.hasMoreElements()) {
        URL next = (URL)urls.nextElement();
        InputStream in = null;
        try {
          in = next.openStream();
          list.add(BindingFile.forDoc(BindingConfigDocument.Factory.parse(in)));
        } catch(IOException ioe) {
          throw ioe;
        } finally {
          if (in != null) in.close();
        }
      }
      mBindingFiles = new BindingFile[list.size()];
      list.toArray(mBindingFiles);
    }
    return mBindingFiles;
} catch(Exception e) { throw new RuntimeException(e); } //FIXME
  }

  public SchemaDocument[] getSchemas() {
    // if we ever need to implement this, we should probably do it by
    // adding an index file to the schema dir that we can read to see what
    // schema files are out there.  don't think this should ever be needed,
    // though.
    throw new UnsupportedOperationException
      ("SchemaDocuments cannot be retrieved at runtime.");
  }

  public SchemaTypeLoader getSchemaTypeLoader() {
    if (mSchemaTypeLoader == null) {
      mSchemaTypeLoader= SchemaTypeLoaderImpl.build
        (new SchemaTypeLoader[] { BuiltinSchemaTypeSystem.get() } ,
         null,
         mClassLoader);
    }
    return mSchemaTypeLoader;
  }

  public JamClassLoader getJamClassLoader() {
    if (mJamClassLoader == null) {
     mJamClassLoader =
       JamServiceFactory.getInstance().createJamClassLoader(mClassLoader);
    }
    return mJamClassLoader;
  }
}