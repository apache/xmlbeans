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

package org.apache.xmlbeans;


import java.io.IOException;
import java.net.URI;
import java.util.jar.JarInputStream;

/**
 * BindingContextFactory is used to create BindingContext objects
 * from a binding configuration file.
 */
public abstract class BindingContextFactory
{
  /**
   * Create a BindingContext that only knows about builtin types
   *
   * @return a BindingContext object for builtin types
   */
  public abstract BindingContext createBindingContext();

  /**
   * Creates a BindingContext from a set of tylars located at the given URI.
   * The order in which tylars appear in the array determines their precedence
   * for loading types.
   *
   * @param tylarUris An array of URIs which identify the tylars to be used
   * in the BindingContext.
   * @return The BindingContext
   * @throws IOException if a problem occurs while opening or parsing the
   * contents of the tylars.
   */
  public abstract BindingContext createBindingContext(URI[] tylarUris)
          throws IOException, XmlException;

  /**
   * Creates a BindingContext from a tylar located at the given URI.
   * The order in which tylars appear in the array determines their precedence
   * for loading types.
   *
   * @param tylarUri A URIs to the tylar to be used in the BindingContext.
   * @return The BindingContext
   * @throws IOException if a problem occurs while opening or parsing the
   * contents of the tylars.
   */
  public abstract BindingContext createBindingContext(URI tylarUri)
          throws IOException, XmlException;

  /**
   * Creates a BindingContext given an input stream on a type library.
   *
   * @param jar Input stream on the type library jar.
   * @return
   * @throws IOException If an error occurs reading the stream.
   * @throws XmlException If an error occurs parsing the contents of the
   * type library.
   */
  public abstract BindingContext createBindingContext(JarInputStream jar)
          throws IOException, XmlException;



  protected final static String DEFAULT_IMPL =
          "org.apache.xmlbeans.impl.marshal.BindingContextFactoryImpl";

  public static BindingContextFactory newInstance()
  {
    try {
      Class default_impl = Class.forName(DEFAULT_IMPL);
      final BindingContextFactory factory =
              (BindingContextFactory)default_impl.newInstance();
      return factory;
    }
    catch (ClassNotFoundException e) {
      throw new XmlRuntimeException(e);
    }
    catch (InstantiationException e) {
      throw new XmlRuntimeException(e);
    }
    catch (IllegalAccessException e) {
      throw new XmlRuntimeException(e);
    }
  }

}
