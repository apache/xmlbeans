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

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.visitor.ElementVisitor;
import org.apache.xmlbeans.impl.jam.internal.elements.*;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamClassLoaderImpl implements JamClassLoader {


  // ========================================================================
  // Variables

  private Map mName2Package = new HashMap();
  private Map mFd2ClassCache = null;
  private JamClassBuilder mBuilder;
  private ElementVisitor mInitializer = null;
  private ElementContext mContext;

  // ========================================================================
  // Constructor

  public JamClassLoaderImpl(ElementContext context,
                            JamClassBuilder builder,
                            ElementVisitor initializerOrNull) {
    if (builder == null) throw new IllegalArgumentException("null builder");
    if (context == null) throw new IllegalArgumentException("null builder");
    mBuilder = builder;
    mInitializer = initializerOrNull; //ok to be null
    mContext = context;
    initCache();
  }

  // ========================================================================
  // JamClassLoader implementation

  public final JClass loadClass(String fd)
  {
    fd = fd.trim();//REVIEW is this paranoid?
    EClass out = (EClass)mFd2ClassCache.get(fd);
    if (out != null) return out;
    if (fd.startsWith("[")) {
      return ArrayClassImpl.createClassForFD(fd,this);
    }
    // parse out the package and class names - this is kinda broken
    int dot = fd.lastIndexOf('.');
    String pkg;
    String name;
    if (dot == -1) {
      pkg = "";
      name = fd;
    } else {
      pkg  = fd.substring(0,dot);
      name = fd.substring(dot+1);
    }
    out = mBuilder.build(pkg,name);
    if (out == null) {
      out = new ClassImpl(pkg,name,mContext,null);
      ((ClassImpl)out).setIsUnresolved(true);
      mContext.debug("[JamClassLoaderImpl] unresolved class '"+
        pkg+" "+name+"'!!");
    }
    if (mInitializer != null) out.acceptAndWalk(mInitializer);
    mFd2ClassCache.put(fd,out);
    return out;
  }

  public JPackage getPackage(String named) {
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      out = new PackageImpl(mContext,named);
      mName2Package.put(named,out);
    }
    return out;
  }

  // ========================================================================
  // Private methods

  /**
   * <p>Stuff the primitives and void into the cache.</p>
   */
  private void initCache() {
    mFd2ClassCache = new HashMap();
    PrimitiveClassImpl.mapNameToPrimitive(mContext,mFd2ClassCache);
    mFd2ClassCache.put("void",new VoidClassImpl(mContext));
  }

  // ========================================================================
  // Public methods?

  /**
   * Returns an unmodifiable collection containing the JClasses which
   * have been resolved by this JamClassLoader.
   */
  public Collection getResolvedClasses() {
    return Collections.unmodifiableCollection(mFd2ClassCache.values());
  }

  public void addToCache(JClass c) {
    //FIXME hack for editable classes for now
    mFd2ClassCache.put(c.getQualifiedName(),c);
  }

}