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
package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.mutable.MClass;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A ClassBuilder that doesn't do any lazy building - it is just a cache
 * of classes that are used when asked to build one.  This is used by
 * JamXmlReader, which does all of it's reading and building in a single
 * pass.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class CachedClassBuilder extends JamClassBuilder {

  // ========================================================================
  // Variables

  private Map mQcname2jclass = null;
  private List mClassNames = new ArrayList();

  // ========================================================================
  // Constructors

  public CachedClassBuilder() {}

  // ========================================================================
  // JamClassBuilder implementation

  public MClass build(String packageName, String className) {
    if (mQcname2jclass == null) return null;
    if (packageName.trim().length() > 0) {
      className = packageName+'.'+className;
    }
    return (MClass)mQcname2jclass.get(className);
  }

  public void populate(MClass c) { }

  // ========================================================================
  // Public methods

  public MClass createClassToBuild(String packageName,
                                   String className,
                                   String[] importSpecs) {
    String qualifiedName;
    if (packageName.trim().length() > 0) {
      qualifiedName = packageName+'.'+className;
    } else {
      qualifiedName = className;
    }
    MClass out;
    if (mQcname2jclass != null) {
      out = (MClass)mQcname2jclass.get(qualifiedName);
      if (out != null) return out;
    } else {
      mQcname2jclass = new HashMap();
    }
    out = super.createClassToBuild(packageName,className, importSpecs);
    mQcname2jclass.put(qualifiedName,out);
    mClassNames.add(qualifiedName);
    return out;
  }

  public String[] getClassNames() {
    String[] out = new String[mClassNames.size()];
    mClassNames.toArray(out);
    return out;
  }
}
