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

package org.apache.xmlbeans.impl.jam.internal.javadoc;

import com.sun.javadoc.RootDoc;

import java.lang.reflect.Method;

/**
 * <p>Used by JavadocRunner to ensure that we always stash the results
 * of a javadoc run in a place where we can get them.  This is particularly
 * painful because one implements a javdoc doclet by providing a static
 * callback method.  In the case of multiple classloaders, extra care must be
 * taken to ensure that we can stash away the RootDoc result received in
 * the callback and retrieve it later.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JavadocResults {

  // ========================================================================
  // Singleton

  private static final JavadocResults INSTANCE = new JavadocResults();

  // ========================================================================
  // Variables

  private ThreadLocal mRootsPerThread = new ThreadLocal();

  // ========================================================================
  // Public methods

  public static void prepare() {
    Thread.currentThread().
            setContextClassLoader(JavadocResults.class.getClassLoader());
  }

  public static void setRoot(RootDoc root) {
    try {
      Object holder = getHolder();
      Method setter = holder.getClass().getMethod("_setRoot",
                                                  new Class[] { RootDoc.class });
      setter.invoke(holder,new Object[] {root});
    } catch(Exception e) {
      e.printStackTrace();
      throw new IllegalStateException();//FIXME??
    }
  }

  public static RootDoc getRoot() {
    try {
      Object holder = getHolder();
      Method getter = holder.getClass().getMethod("_getRoot",new Class[0]);
      return (RootDoc)getter.invoke(holder,null);
    } catch(Exception e) {
      e.printStackTrace();
      throw new IllegalStateException();//FIXME??
    }
  }

  // ========================================================================
  // DO NOT CALL THESE METHODS

  public void _setRoot(RootDoc root) { mRootsPerThread.set(root); }

  public RootDoc _getRoot() { return (RootDoc)mRootsPerThread.get(); }

  public static JavadocResults getInstance() { return INSTANCE; }

  private static Object getHolder() throws Exception {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class clazz = classLoader.loadClass(JavadocResults.class.getName());
    Method method = clazz.getMethod("getInstance",new Class[0]);
    return method.invoke(null,new Object[0]);
  }

}
