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

import java.io.IOException;
import java.net.URI;
import java.util.jar.JarInputStream;

import org.apache.xmlbeans.XmlException;

/**
 * Abstraction which can load a tylar from some location specified in a
 * URI.
 *
 * @author Patrick Calaham <pcal@bea.com>
 */
public interface TylarLoader {

  /**
   * Loads a single tylar from the given URI.
   *
   * @param uri pointing to where the tylar is stored.
   * @return
   * @throws IOException if an i/o error occurs while processing
   * @throws XmlException if an error occurs parsing the contents of the tylar.
   */
  public Tylar load(URI uri) throws IOException, XmlException;

  /**
   * Returns a composition of the set of tylars at the given URIs.  Tylars
   * will be consulted in the order in which they appear in this array when
   * resolving bindings and types; first one wins.
   *
   * @param uris pointing to where the tylars are stored.
   * @return
   * @throws IOException if an i/o error occurs while processing
   * @throws XmlException if an error occurs parsing the contents of the
   * tylars.
   */
  public Tylar load(URI[] uris) throws IOException, XmlException;

  /**
   * Loads a single tylar from the given JarInputStream.
   *
   * @param jar input stream on the tylar jar.
   * @return
   * @throws IOException if an i/o error occurs while processing
   * @throws XmlException if an error occurs parsing the contents of the
   * tylars.
   */
  public Tylar load(JarInputStream jar) throws IOException, XmlException;




}
