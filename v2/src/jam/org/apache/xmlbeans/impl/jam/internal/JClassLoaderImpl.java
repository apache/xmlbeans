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

import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.editable.impl.EClassImpl;
import org.apache.xmlbeans.impl.jam.editable.EClass;
import org.apache.xmlbeans.impl.jam.provider.EClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.EClassInitializer;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JClassLoaderImpl implements JClassLoader {


  // ========================================================================
  // Variables

  private boolean mVerbose = true;
  private Map mName2Package = new HashMap();
  private Map mFd2ClassCache = new HashMap();
  private JAnnotationLoader mAnnotationLoader = null;//FIXME
  private EClassBuilder mBuilder;
  private EClassInitializer mInitializer = null;

  private static final JClassLoader ROOT = new RootJClassLoader();

  // ========================================================================
  // Constructor

  public JClassLoaderImpl(EClassBuilder builder,
                          EClassInitializer initializer) {
    if (builder == null) throw new IllegalArgumentException("null builder");
    mBuilder = builder;
    mInitializer = initializer;
  }

  // ========================================================================
  // JClassLoader implementation

  public final JClass loadClass(String fd)
  {
    fd = fd.trim();//REVIEW is this paranoid?
    JClass out = (JClass)mFd2ClassCache.get(fd);
    if (out != null) return out;
    if (fd.startsWith("[")) {
      return ArrayJClass.createClassFor(fd,this);
    } else {
      if (fd.equals("java.lang.Object")) return ObjectJClass.getInstance();
      if (fd.equals("void")) return VoidJClass.getInstance();
    }
    // parse out the package and class names - kinda broken
    int dot = fd.lastIndexOf('.');
    String pkg;
    String name;
    if (dot == -1) {
      //System.out.println("==== "+fd);
      pkg = "";
      name = fd;
    } else {
      pkg  = fd.substring(0,dot);
      name = fd.substring(dot+1);
    }
    out = mBuilder.build(pkg,name,this);
    if (out == null) {
      out = ROOT.loadClass(fd);
    }
    if (out == null) {
      out = new EClassImpl(pkg,name,this);
      ((EClassImpl)out).setIsUnresolved(true);
      if (mVerbose) System.out.println("[JClassLoaderImpl] unresolve class '"+
                                       pkg+" "+name+"'!!");
    }
    if (out instanceof EClassImpl) {
      if (mInitializer != null) mInitializer.initialize((EClassImpl)out);
    }
    mFd2ClassCache.put(fd,out);
    return out;
  }

  public JAnnotationLoader getAnnotationLoader() {
    return mAnnotationLoader;
  }

  //FIXME
  public JPackage getPackage(String named) {
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      out = JDFactory.getInstance().createPackage(named);
      mName2Package.put(named,out);
    }
    return out;
  }

  // ========================================================================
  // Public methods

  /**
   * Returns an unmodifiable collection containing the JClasses which
   * have been resolved by this JClassLoader.
   */
  public Collection getResolvedClasses() {
    return Collections.unmodifiableCollection(mFd2ClassCache.values());
  }

}