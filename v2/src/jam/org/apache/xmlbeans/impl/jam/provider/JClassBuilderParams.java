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

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Structure containing information given to a BaseJProvider subclass in
 * order to instantiate a new JClassBuilder.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JClassBuilderParams {

  // ========================================================================
  // Public methods

  /**
   * @return The classpath to be searched when trying to build a JClass for
   * a java class which was not in the inputSources or inputClasses, or null.
   */
  public JPath getInputClasspath();

  /**
   * @return The sourcepath to be searched when trying to build a JClass for
   * a java class which was not in the inputSources or inputClasses, or null.
   */
  public JPath getInputSourcepath();

  /**
   * @return a PrintWriter to which logging and debugging information should
   * be written by the JClassBuilder, or null, indicating that such output
   * should be suppressed.
   */
  public PrintWriter getOut();

  /**
   * @return The classpath to be used in loading external classes on which
   * the service implementation depends, or null.  This is not generally
   * needed.
   */
  public JPath getToolClasspath();

  /**
   * @return a set of service implementation-specific properties specified
   * by the client, or null.
   */
  public Properties getProperties();
}