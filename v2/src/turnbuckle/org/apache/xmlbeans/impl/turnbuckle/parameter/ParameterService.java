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
package org.apache.xmlbeans.impl.turnbuckle.parameter;

import org.apache.tools.ant.taskdefs.Property;

/**
 * <p>Provides the tool with access to user-specified parameters.  Note
 * that many typical parameters, such as 'verbose' or as set of input java
 * file set, are implicitly handled by the tool context in the services
 * it provides.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface ParameterService {

  /**
   * <p>Returns a properties object containing the flags and options
   * specified by the user.</p>
   */
  public Property getParameters();
}
