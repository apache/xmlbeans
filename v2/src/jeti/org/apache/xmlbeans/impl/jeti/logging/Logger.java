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
package org.apache.xmlbeans.impl.jeti.logging;

import org.apache.xmlbeans.impl.jam_old.JElement;

import java.util.logging.Level;

/**
 * <p>Client interface logging service.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface Logger {

  /**
   * Outputs a generic logging message.
   */
  public void log(Level level,
                  String msgId,
                  Object[] msgArgs);

  /**
   * Outputs a generic logging message.
   */
  public void log(Level level,
                  String msgId,
                  Object[] msgArgs,
                  Throwable error);

  /**
   * Outputs a diagnostic message that was encountered while processing
   * a specific java construct.
   *
   * @param level
   * @param javaDiagnosticContext
   * @param msgId
   * @param msgArgs
   */
  public void addDiagnostic(Level level,
                            JElement javaDiagnosticContext,
                            String msgId,
                            Object[] msgArgs);

  /**
   * Outputs a diagnostic message that was encountered while processing
   * a specific java construct.
   *
   *
   * @param level
   * @param javaDiagnosticContext
   * @param msgId
   * @param msgArgs
   */
  public void addDiagnostic(Level level,
                            JElement javaDiagnosticContext,
                            String msgId,
                            Object[] msgArgs,
                            Throwable error);

}
