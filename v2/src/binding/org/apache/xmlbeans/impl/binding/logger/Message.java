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

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.jam_old.JElement;

import java.util.logging.Level;

/**
 * Encapsulates a message that was generated while running a BindingCompiler.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface Message {

  /**
   * @return The severity level of the message.  This must never return null.
   */
  public Level getLevel();

  /**
   * @return The text of the message.  This must never return null.
   */
  public String getMessage();

  /**
   * @return The exception which caused the message, or null.
   */
  public Throwable getException();

  /**
   * @return The JElement representing the java construct to which the message
   * applies, or null.
   */
  public JElement getJavaContext();

  /**
   * @return The SchemaType representing the xsd type to which the message
   * applies, or null.
   */
  public SchemaType getSchemaTypeContext();

  /**
   * @return The SchemaProperty representing the xsd property to which the
   * message applies, or null.
   */
  public SchemaProperty getSchemaPropertyContext();
}