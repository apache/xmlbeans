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
package org.apache.xmlbeans.impl.jam_old.provider;

import org.apache.xmlbeans.impl.jam_old.JServiceFactory;
import org.apache.xmlbeans.impl.jam_old.JServiceParams;
import org.apache.xmlbeans.impl.jam_old.JService;
import org.apache.xmlbeans.impl.jam_old.JClassLoader;
import org.apache.xmlbeans.impl.jam_old.internal.RootJClassLoader;
import org.apache.xmlbeans.impl.jam_old.internal.javadoc.JDClassLoaderFactory;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>Default implementation of JServiceFactory.  It currently uses javadoc
 * for inspecting sources and reflection for inspecting classes.</p>
 *
 * <p>Note that this class can be used as a base class for custom factories.
 * </p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultJServiceFactory extends JServiceFactory {

  // ========================================================================
  // Constructors

  public DefaultJServiceFactory() {}

  // ========================================================================
  // JServiceFactory implementation

  public JServiceParams createServiceParams() {
    return new JServiceParamsImpl();
  }

  public JService createService(JServiceParams jsps) throws IOException {
    //assert that they aren't implementing JServiceParams themselves or
    //getting them from somewhere else
    if (!(jsps instanceof JServiceParamsImpl)) {
      throw new IllegalArgumentException
              ("JServiceParams must be instantiated by this JServiceFactory.");
    }
    //create and return the service
    return new JServiceImpl(createClassLoader((JServiceParamsImpl)jsps),
                           getSpecifiedClasses((JServiceParamsImpl)jsps));
  }

  // ========================================================================
  // Protected methods - override at your own risk

  /**
   * <p>Returns the set of classes to be included in a JService to be
   * created by the given params.  You should not override this
   * unless you really know what you're doing.</p>
   */
  protected String[] getSpecifiedClasses(JServiceParamsImpl params)
          throws IOException
  {
    return params.getAllClassnames();
  }

  /**
   * <p>Creates the main classloader to be used given the input params.
   * This is usually a composite of the source classloader and a
   * classfile classloader.  Subclasses may override to change the behavior.
   * </p>
   */
  protected JClassLoader createClassLoader(JServiceParamsImpl params)
          throws IOException
  {
    // Build up the clasloader chain.  Note that each loader we create is
    // used as a parent of the next.
    //
    // The root classloader deals with Object, void, primitives, arrays...
    JClassLoader loader = new RootJClassLoader();
    // Usually they will also want the system classloader in there, but this
    // is optional
    if (params.isUseSystemClasspath()) {
      loader = ReflectionClassBuilder.createRClassLoader
              (ClassLoader.getSystemClassLoader());
    }
    // Now create a loader for any classfile loading they specified
    JClassLoader classfileLoader = createClassfileLoader(params,loader);
    if (classfileLoader != null) loader = classfileLoader;
    // Finally check for sources
    JClassLoader sourceLoader = createSourceLoader(params,loader);
    if (sourceLoader != null) loader = sourceLoader;
    //
    return loader;
  }


  /**
   * <p>Creates the sourcefile classloader to be used given the input params.
   * Returns null if no source files are specified in the params.  Subclasses
   * may override to change the way in which java sources are loaded.</p>
   */
  protected JClassLoader createSourceLoader(JServiceParamsImpl params,
                                            JClassLoader parent)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    File[] files = params.getSourceFiles();
    if (files == null || files.length == 0) return null;
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

  /**
   * <p>Creates the sourcefile classloader to be used given the input params.
   * If no class files or classloaders are specified in the params, this
   * source files are specified in the params, this just returns null.
   * Subclasses may override to change the way in which java classes
   * are loaded.</p>
   */
  protected JClassLoader createClassfileLoader(JStoreParams jp,
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
      ClassLoader cl = new URLClassLoader(urls);
      return ReflectionClassBuilder.createRClassLoader(cl);
    //  return new RClassLoader(,parent);
    }
  }
}
