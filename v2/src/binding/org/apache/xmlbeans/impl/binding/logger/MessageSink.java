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

package org.apache.xmlbeans.impl.binding.logger;



/**
 * Implemented by helper objects which can receive log messages from a
 * BindingCompiler and do something useful with them.  Note that
 * BindingCompilers should generally try to proceed even when binding
 * errors are encounted so as to help the user identify as many errors as
 * possible in a single pass.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface MessageSink {

  /**
   * Logs a message that was produced while performing binding
   * on the given java construct.
   *
   * @param msg  message to be logged
   */
  public void log(Message msg);
}