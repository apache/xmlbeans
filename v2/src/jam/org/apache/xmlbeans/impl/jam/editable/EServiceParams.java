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

package org.apache.xmlbeans.impl.jam.editable;

import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.JClassLoader;

import java.io.File;
import java.io.PrintWriter;

/**
 * Structure which encapsulates a set of parameters used to create a new
 * EService.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EServiceParams {

  /**
   * Sets a loader for external annotations to be used in the service.
   *
   * @param ann an implementation of JAnnotationLoader
   * @throws IllegalArgumentException if the argument is null
   */
  public void setAnnotationLoader(JAnnotationLoader ann);

  /**
   * Sets a PrintWriter to which the EService implementation should log
   * errors and debugging information.  If this is never set, all such output
   * will be suppressed.
   *
   * @param out a PrintWriter to write to
   * @throws IllegalArgumentException if the argument is null
   */
  public void setLogger(PrintWriter out);

  /**
   * Sets whether the EService should send verbose output to the logger.
   * Has no effect if setLogger() is never called.
   *
   * @param v whether or not boolean output is enabled.
   */
  public void setVerbose(boolean v);

  /**
   * Sets the parent JClassLoader of the service's JClassLoader.
   *
   * @param loader the parent loaer
   * @throws IllegalArgumentException if the argument is null
   */
  public void setParentClassLoader(JClassLoader loader);

}