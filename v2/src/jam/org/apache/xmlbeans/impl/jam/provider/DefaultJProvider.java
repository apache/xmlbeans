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

package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.JProvider;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.internal.JServiceParamsImpl;
import org.apache.xmlbeans.impl.jam.internal.reflect.RClassLoader;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassLoaderFactory;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Singleton which is the DefaultJProvider to be used in the current VM.
 * This is the Provider to which the ServiceFactory delegates.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultJProvider extends BaseJProvider {

  // ========================================================================
  // Constants

  private static final JProvider INSTANCE = new DefaultJProvider();

  // ========================================================================
  // Singleton

  public static JProvider getInstance() { return INSTANCE; }

  // ========================================================================
  // BaseJProvider implementation

  public JClassLoader createClassLoader(JServiceParamsImpl params,
                                        JClassLoader parent)
    throws IOException
  {
    JClassLoader classfileLoader = createClassfileLoader(params,parent);
    if (classfileLoader != null) parent = classfileLoader;
    JClassLoader sourceLoader = createSourceLoader(params,parent);
    if (sourceLoader != null) {
      return sourceLoader;
    } else {
      return parent;
    }
  }

  // ========================================================================
  // Public methods

  public JClassLoader createSourceLoader(JServiceParamsImpl params,
                                         JClassLoader parent)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    File[] files = params.getSourceFiles();
    if (files == null || files.length == 0) return parent;
    String sourcePath = (params.getInputSourcepath() == null) ? null :
            params.getInputSourcepath().toString();
    String classPath = (params.getInputClasspath() == null) ? null :
            params.getInputClasspath().toString();
    return JDClassLoaderFactory.getInstance().
            create(files,
                   parent,
                   params.getAnnotationLoader(),
                   params.getOut(),
                   sourcePath,
                   classPath,
                   null);//FIXME get javadoc args from param props
  }

  public JClassLoader createClassfileLoader(JStoreParams jp,
                                            JClassLoader parent)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    JPath cp = jp.getInputClasspath();
    if (cp == null) {
      return null;
    } else {
      URL[] urls = cp.toUrlPath();
      return new RClassLoader(new URLClassLoader(urls),parent);
    }
  }
}