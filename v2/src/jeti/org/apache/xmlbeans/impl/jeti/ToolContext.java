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
package org.apache.xmlbeans.impl.jeti;

import org.apache.xmlbeans.impl.jam.JServiceFactory;
import org.apache.xmlbeans.impl.jeti.parameter.ParameterService;
import org.apache.xmlbeans.impl.jeti.output.OutputService;
import org.apache.xmlbeans.impl.jeti.logging.LoggingService;


// user->ide/ant/commandline->xbeans

/**
 * <p>Encapsulates a set of services to be used by in a single invocation
 * of a tool.  This abstraction isolates the tool code from any particular
 * means of, for example, retrieving user parameters or logging diagnostic
 * information.  This isolation allows the same tools code to run in
 * a variety of environments - e.g. in ant script or within an IDE.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface ToolContext {

  /**
   * <p>Returns a service which can be used to log diagnostic messages
   * that the tool encounters.</p>
   */
  public LoggingService getLoggingService();

  /**
   * <p>Returns a service which should be used for producing output artifacts
   * such java code or xml files.</p>
   */
  public OutputService getOutputService();

  /**
   * <p>Returns an entry point into the JAM service, which provides a model
   * of java type information.</p>
   */
  public JServiceFactory getJavaTypeService();

  /**
   * <p>Returns a service which exposes user-specified parameters for
   * the tool invocation.</p>
   */
  public ParameterService getParameterService();
}
