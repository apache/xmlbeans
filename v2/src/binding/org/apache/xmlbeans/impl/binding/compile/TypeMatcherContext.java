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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.binding.logger.BindingLogger;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.SchemaTypeSystem;

/**
 * Provides context/initialzation information for a TypeMatcher instance.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface TypeMatcherContext {

  /**
   * @return The logger to which the type matcher should send log messages.
   */
  public BindingLogger getLogger();

  /**
   * Returns the BindingLoader as a basis for the binding process.  Normally,
   * this will simply be the builtin loader.
   */
  public BindingLoader getBaseBindingLoader();

  /**
   * Returns a SchemaTypeLoader to be used as a basis for the binding process.
   * Normally, this will simply be the builtin loader.
   */
  public SchemaTypeSystem getBaseSchemaTypeSystem();

  /**
   * Returns a JClassLoader to be used as a basis for the binding process.
   * Normally, this will simply be the loader backed by the system
   * classloader.
   */
  public JamClassLoader getBaseJavaTypeLoader();
}