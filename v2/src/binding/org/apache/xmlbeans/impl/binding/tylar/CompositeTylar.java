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
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.io.IOException;

/**
 * @deprecated I think we'd like to eliminate this class if possible.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class CompositeTylar extends BaseTylarImpl {

  // ========================================================================
  // Variables

  private Tylar[] mTylars; //the tylars we are composing

  // ========================================================================
  // Constructors

  /**
   * Constructs a composition of the tylars in the given collection.  Bindings
   * and types will be sought in the tylars in the order in which they are
   * presented in this collection (as returned by Collection.iterator()).
   *
   * @throws IllegalArgumentException if any object in the collection is not
   * a Tylar or the collection is null.
   */
  public CompositeTylar(Collection tylars) {
    if (tylars == null) throw new IllegalArgumentException("null tylars");
    mTylars = new Tylar[tylars.size()];
    int n = 0;
    for(Iterator i = tylars.iterator(); i.hasNext(); n++) {
      Object next = i.next();
      if (next instanceof Tylar) {
        mTylars[n] = (Tylar)next;
      } else {
        throw new IllegalArgumentException("Collection contains a "+
                next.getClass()+" which does not implement Tylar");
      }
    }
  }

  /**
   * Constructs a composition of the given Tylars.  Bindings and types
   * will be sought in the tylars in the order in which they are presented in
   * this array.
   */
  public CompositeTylar(Tylar[] tylars) {
    if (tylars == null) throw new IllegalArgumentException("null tylars");
    mTylars = tylars;
  }

  // ========================================================================
  // Tylar implementation

  public String getDescription() {
    return "CompositeTylar containing "+mTylars.length+" tylars";
  }

  public BindingFile[] getBindingFiles() /*throws IOException, XmlException*/ {
    //REVIEW consider caching
    Collection all = new ArrayList();
    for(int i=0; i<mTylars.length; i++) {
      all.addAll(Arrays.asList(mTylars[i].getBindingFiles()));
    }
    BindingFile[] out = new BindingFile[all.size()];
    all.toArray(out);
    return out;
  }

  public SchemaDocument[] getSchemas() /*throws IOException, XmlException*/ {
    //REVIEW consider caching
    Collection all = new ArrayList();
    for(int i=0; i<mTylars.length; i++) {
      all.addAll(Arrays.asList(mTylars[i].getSchemas()));
    }
    SchemaDocument[] out = new SchemaDocument[all.size()];
    all.toArray(out);
    return out;
  }

  public SchemaTypeLoader getSchemaTypeLoader() throws IOException, XmlException {
    if (mTylars.length == 0) return XmlBeans.getBuiltinTypeSystem();
    if (mTylars.length == 1) return mTylars[0].getSchemaTypeLoader();
    SchemaTypeLoader[] sts = new SchemaTypeLoader[mTylars.length];
    for(int i=0; i<mTylars.length; i++) sts[i] = mTylars[i].getSchemaTypeLoader();
    return XmlBeans.typeLoaderUnion(sts);


  }
}
