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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.Arrays;

/**
 * A binding loader which delegates to a list of other loaders when
 * loading binding types.
 */
public class CompositeBindingLoader implements BindingLoader {

  // ========================================================================
  // Constants

  //FIXME this is inefficient - if we really want to optimize this, let's
  //just have a separate EmptyBindingLoader class
  private static final CompositeBindingLoader EMPTY_LOADER =
          new CompositeBindingLoader(Collections.EMPTY_LIST);

  // ========================================================================
  // Variables

  private final Collection loaderPath;

  // ========================================================================
  // Factory methods


  public static BindingLoader forPath(BindingLoader[] path) {
    if (path == null) throw new IllegalArgumentException("null path");
    return forPath(Arrays.asList(path));
  }

  public static BindingLoader forPath(Collection path) {
    if (path == null) throw new IllegalArgumentException("null path");
    IdentityHashMap seen = new IdentityHashMap();
    List flattened = new ArrayList(path.size());
    for (Iterator i = path.iterator(); i.hasNext();) {
      addToPath(flattened, seen, (BindingLoader) i.next());
    }
    if (flattened.size() == 0) return EMPTY_LOADER;
    if (flattened.size() == 1) return (BindingLoader) flattened.get(0);
    return new CompositeBindingLoader(flattened);
  }


  // ========================================================================
  // BindingLoader implementation

  public BindingType getBindingType(BindingTypeName btName) {
    if (btName == null) throw new IllegalArgumentException("null btName");
    BindingType result = null;
    for (Iterator i = loaderPath.iterator(); i.hasNext();) {
      result = ((BindingLoader) i.next()).getBindingType(btName);
      if (result != null)
        return result;
    }
    return null;
  }

  public BindingTypeName lookupPojoFor(XmlTypeName xName) {
    if (xName == null) throw new IllegalArgumentException("null xName");
    BindingTypeName result = null;
    for (Iterator i = loaderPath.iterator(); i.hasNext();) {
      result = ((BindingLoader) i.next()).lookupPojoFor(xName);
      if (result != null)
        return result;
    }
    return null;
  }

  public BindingTypeName lookupXmlObjectFor(XmlTypeName xName) {
    if (xName == null) throw new IllegalArgumentException("null xName");
    BindingTypeName result = null;
    for (Iterator i = loaderPath.iterator(); i.hasNext();) {
      result = ((BindingLoader) i.next()).lookupXmlObjectFor(xName);
      if (result != null)
        return result;
    }
    return null;
  }

  public BindingTypeName lookupTypeFor(JavaTypeName jName) {
    if (jName == null) throw new IllegalArgumentException("null jName");
    BindingTypeName result = null;
    for (Iterator i = loaderPath.iterator(); i.hasNext();) {
      result = ((BindingLoader) i.next()).lookupTypeFor(jName);
      if (result != null)
        return result;
    }
    return null;
  }

  public BindingTypeName lookupElementFor(JavaTypeName jName) {
    if (jName == null) throw new IllegalArgumentException("null jName");
    BindingTypeName result = null;
    for (Iterator i = loaderPath.iterator(); i.hasNext();) {
      result = ((BindingLoader) i.next()).lookupElementFor(jName);
      if (result != null)
        return result;
    }
    return null;
  }

  // ========================================================================
  // Private methods

  private static void addToPath(List path,
                                IdentityHashMap seen,
                                BindingLoader loader) {
    if (seen.containsKey(loader)) return;

    if (loader instanceof CompositeBindingLoader) {
      for (Iterator j = ((CompositeBindingLoader) loader).loaderPath.iterator(); j.hasNext();) {
        addToPath(path, seen, (BindingLoader) j.next());
      }
    } else {
      path.add(loader);
    }
  }

  private CompositeBindingLoader(List path) {
    if (path == null) throw new IllegalArgumentException("null path");
    loaderPath = Collections.unmodifiableList(path);
  }


  // ========================================================================
  // Dead code

  /*
  private static final String STANDARD_PATH =
    "org/apache/xmlbeans/binding-config.xml";

  public static BindingLoader forClassLoader(ClassLoader loader) {
    Enumeration i;
    try {
      i = loader.getResources(STANDARD_PATH);
    } catch (IOException e) {
      throw (IllegalStateException) (new IllegalStateException().initCause(e));
    }
    URL resource = null;
    List files = new ArrayList();
    try {
      while (i.hasMoreElements()) {
        resource = (URL) i.nextElement();
        files.add(BindingFile.forDoc(BindingConfigDocument.Factory.parse(resource)));
      }
    } catch (Exception e) {
      throw (IllegalStateException) (new IllegalStateException("Problem resolving " + resource).initCause(e));
    }
    return forPath(files);
  }


  public static BindingLoader forClasspath(File[] jarsOrDirs) {
    List files = new ArrayList();
    try {
      for (int i = 0; i < jarsOrDirs.length; i++) {
        if (!jarsOrDirs[i].exists())
          continue; // skip parts of the claspath which do not exist
        FileResourceLoader rl = new FileResourceLoader(jarsOrDirs[i]);
        InputStream resource = rl.getResourceAsStream(STANDARD_PATH);
        files.add(BindingFile.forDoc(BindingConfigDocument.Factory.parse(resource)));
      }
    } catch (Exception e) {
      throw (IllegalStateException) (new IllegalStateException("Problem resolving files").initCause(e));
    }
    return forPath(files);
  }
  */

}

