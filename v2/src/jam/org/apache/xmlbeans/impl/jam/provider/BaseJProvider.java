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
import org.apache.xmlbeans.impl.jam.JService;
import org.apache.xmlbeans.impl.jam.JServiceParams;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.internal.RootJClassLoader;
import org.apache.xmlbeans.impl.jam.internal.JServiceImpl;
import org.apache.xmlbeans.impl.jam.internal.JServiceParamsImpl;
import org.apache.xmlbeans.impl.jam.internal.reflect.RClassLoader;

import java.io.IOException;

/**
 * <p>Base class which simplifies implementing the JProvider interface.
 * By extending this class, a provider needs only to implement the
 * JClassBuilder interface and override the createJClassBuilder method
 * to return instances of that implementation.</p>
 *
 * <p>If you are writing your own JAM provider, it is highly recommended
 * that you extend BaseJProvider and implement JClassBuilder, as this
 * gives you a lot of boilerplate work for free - primitives, arrays,
 * caching of JClasses.  All you have to do is fill out the JClass
 * interface in your JClassBuilder.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BaseJProvider implements JProvider
{
  // ========================================================================
  // Constructors

  protected BaseJProvider() {}

  // ========================================================================
  // JProvider implementation

  public JService createService(JServiceParams jsps) throws IOException {
    //assert that they aren't implementing JServiceParams themselves
    if (!(jsps instanceof JServiceParamsImpl)) {
      throw new IllegalArgumentException
              ("JServiceParams must be instantiated by JServiceFactory.");
    }
    //create and return the service
    return new JServiceImpl(createClassLoader((JServiceParamsImpl)jsps),
                            getSpecifiedClasses((JServiceParamsImpl)jsps));
  }

  public String getDescription() { return this.getClass().getName(); }

  // ========================================================================
  // Abstract methods

  /**
   * Subclasses only need to override this; we'll take care of the rest.
   */
  protected abstract JClassLoader createClassLoader(JServiceParamsImpl params,
                                                    JClassLoader parent)
          throws IOException;

  // ========================================================================
  // Protected methods - override at your own risk

  /**
   * Returns the set of classes to be included in a JService to be
   * created by the given params.  You should not override this
   * unless you really know what you're doing.
   */
  protected String[] getSpecifiedClasses(JServiceParamsImpl params)
          throws IOException
  {
    return params.getAllClassnames();
  }

  /**
   * Returns the JClassLoader to be used in the JService to be created by
   * the given params.  You should not override this unless you really know
   * what you're doing.
   */
  protected JClassLoader createClassLoader(JServiceParamsImpl params)
          throws IOException
  {
    //build up the clasloader chain
    JClassLoader parent = new RootJClassLoader(params.getAnnotationLoader());
    if (params.isUseSystemClasspath()) {
      parent = new RClassLoader(ClassLoader.getSystemClassLoader(),parent);
    }
    return createClassLoader(params,parent);
  }

}